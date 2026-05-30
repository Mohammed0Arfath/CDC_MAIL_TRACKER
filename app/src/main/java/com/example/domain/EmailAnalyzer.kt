package com.example.domain

import com.example.BuildConfig
import com.example.data.AlertType
import com.example.data.EmailAlert
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmailAnalyzer {

    suspend fun analyzeEmail(rawEmailText: String): EmailAlert? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext analyzeEmailOffline(rawEmailText)
        }

        val prompt = rawEmailText
        
        val systemInstructionText = """
            You are a personal placement assistant for Mohammed Arfath R (Reg Number: 22MIS0479, NEOPAT ID: M5X3V7Y6), an Integrated M.Tech Software Engineering student (2027 batch) at VIT Vellore.
            You monitor CDC placement emails from vitianscdc2027@vitstudent.ac.in.
            
            Identify if the email is:
            1) REGISTRATION
            2) SHORTLIST (includes Online Test, Next Round, Interviews, Selection)
            3) EVENT (Tech Talks, PPTs)
            4) UNKNOWN (or irrelevant/ignore)
            
            ELIGIBILITY RULES:
            - Mohammed is eligible ONLY IF branches mention: Integrated M.Tech, M.Tech 5 Year Integrated, 5 yrs M.Tech (CSE/IT), M.Tech 5 year Integrated (Software Engineering/AI/CSE) or variations.
            - If not eligible, set isEligible to false and type to UNKNOWN.
            
            If REGISTRATION:
            Extract: Company Name, Category (Dream/Super Dream/Regular), Stipend, CTC, Job Location, Registration Deadline, Registration Link.
            
            If SHORTLIST / NEXT ROUND / SELECTION:
            Extract: Company Name. Scan for "Mohammed Arfath R", "22MIS0479", or "M5X3V7Y6". 
            If found, status="YOU ARE IN THE LIST", else status="Your name was not found".
            Extract all 22MIS0XXX batchmates found.
            Note: Ignore boilerplate like "shortlisting by the company for the selection process" which doesn't mean a shortlist was released.

            If EVENT:
            Extract Company Name, date, time, venue, registration link.
            
            Respond strictly in the provided JSON schema.
        """.trimIndent()
        
        // Let's use standard JSON mode matching our EmailAlert fields
        val requestBody = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText))),
            generationConfig = GenerationConfig(
                responseFormat = ResponseFormat(
                    text = ResponseFormatText(
                        mimeType = "application/json"
                    )
                ),
                temperature = 0.1f // low for extraction
            )
        )

        try {
            val response = GeminiClient.service.generateContent(apiKey, requestBody)
            val jsonText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: return@withContext analyzeEmailOffline(rawEmailText)
            
            // Format output JSON directly to EmailAlert data class via kotlinx serialization
            val jsonDecoder = Json { ignoreUnknownKeys = true }
            val parsedAlert = jsonDecoder.decodeFromString<EmailAlertDto>(jsonText)
            
            return@withContext EmailAlert(
                type = parsedAlert.type,
                companyName = parsedAlert.companyName ?: "Unknown",
                category = parsedAlert.category ?: "",
                status = parsedAlert.status ?: "",
                stipend = parsedAlert.stipend ?: "",
                ctc = parsedAlert.ctc ?: "",
                location = parsedAlert.location ?: "",
                deadline = parsedAlert.deadline ?: "",
                link = parsedAlert.link ?: "",
                eventDateTime = parsedAlert.eventDateTime ?: "",
                isEligible = parsedAlert.isEligible,
                rawEmailBody = rawEmailText,
                batchmates = parsedAlert.batchmates?.joinToString(", ") ?: ""
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext analyzeEmailOffline(rawEmailText)
        }
    }

    private fun analyzeEmailOffline(rawEmailText: String): EmailAlert {
        val lowerText = rawEmailText.lowercase()

        // 1. Determine Type
        val type = when {
            lowerText.contains("shortlist") || lowerText.contains("test link") || lowerText.contains("selected candidates") || lowerText.contains("selection list") || lowerText.contains("next round") || lowerText.contains("interview schedule") -> AlertType.SHORTLIST
            lowerText.contains("registration") || lowerText.contains("register here") || lowerText.contains("deadline") || lowerText.contains("super dream") || lowerText.contains("dream category") -> AlertType.REGISTRATION
            lowerText.contains("tech talk") || lowerText.contains("webinar") || lowerText.contains("pre placement talk") || lowerText.contains("ppt") || lowerText.contains("guest lecture") -> AlertType.EVENT
            else -> AlertType.UNKNOWN
        }

        // 2. Extract Company Name (look for "Dear Students", "Registration for", etc. or capitalise words)
        var companyName = "Unknown Company"
        val registrationRegex = Regex("(?i)registration\\s+for\\s+([A-Za-z0-9\\s]{3,30})")
        val recruitRegex = Regex("(?i)hiring\\s+([A-Za-z0-9\\s]{3,30})")
        val shortlistRegex = Regex("(?i)([A-Za-z0-9\\s]{3,30})\\s+shortlist")
        
        val regMatch = registrationRegex.find(rawEmailText)
        val recMatch = recruitRegex.find(rawEmailText)
        val shortMatch = shortlistRegex.find(rawEmailText)

        if (regMatch != null) {
            companyName = regMatch.groupValues[1].trim()
        } else if (recMatch != null) {
            companyName = recMatch.groupValues[1].trim()
        } else if (shortMatch != null) {
            companyName = shortMatch.groupValues[1].trim()
        } else {
            // Find first line or subject
            val lines = rawEmailText.split("\n")
            for (line in lines) {
                if (line.isNotBlank() && line.length < 50) {
                    companyName = line.trim()
                    break
                }
            }
        }

        // 3. Category
        val category = when {
            lowerText.contains("super dream") -> "Super Dream"
            lowerText.contains("dream") -> "Dream"
            lowerText.contains("regular") -> "Regular"
            else -> ""
        }

        // 4. Status (for shortlists)
        var status = ""
        var batchmatesString = ""
        if (type == AlertType.SHORTLIST) {
            val arfathFound = lowerText.contains("arfath") || lowerText.contains("22mis0479") || lowerText.contains("m5x3v7y6")
            status = if (arfathFound) "YOU ARE IN THE LIST" else "Your name was not found"

            // Look for other 22MIS0XXX batchmates
            val batchmateRegex = Regex("22MIS0\\d{3}", RegexOption.IGNORE_CASE)
            val matches = batchmateRegex.findAll(rawEmailText).map { it.value.uppercase() }.distinct().toList()
            batchmatesString = matches.joinToString(", ")
        }

        // 5. Eligibility (default to true if it mentions computer science, IT, integrated M.tech, software engineering)
        val isEligible = lowerText.contains("integrated m.tech") || 
                lowerText.contains("integrated mtech") || 
                lowerText.contains("software engineering") || 
                lowerText.contains("m.tech 5-year") ||
                lowerText.contains("m.tech (se)") ||
                lowerText.contains("5 year") ||
                !lowerText.contains("eligibility") // if no eligibility block, default to true

        // 6. Extract fields like stipends or link
        var stipend = ""
        if (lowerText.contains("stipend")) {
            val stipendRegex = Regex("(?i)stipend[:\\s]+([A-Za-z0-9,\\s/]+)")
            stipend = stipendRegex.find(rawEmailText)?.groupValues?.get(1)?.trim() ?: ""
        }

        var ctc = ""
        if (lowerText.contains("ctc")) {
            val ctcRegex = Regex("(?i)ctc[:\\s]+([A-Za-z0-9,\\s.LPA/]+)")
            ctc = ctcRegex.find(rawEmailText)?.groupValues?.get(1)?.trim() ?: ""
        }

        var link = ""
        val linkRegex = Regex("(?i)(https?://[\\w\\-\\.]+\\.[a-zA-Z]{2,5}(?:/[\\w\\-%&?\\=]*)?)")
        link = linkRegex.find(rawEmailText)?.value ?: ""

        var deadline = ""
        if (lowerText.contains("deadline") || lowerText.contains("last date")) {
            val deadlineRegex = Regex("(?i)deadline[:\\s]+([A-Za-z0-9\\s,\\-:]+)")
            deadline = deadlineRegex.find(rawEmailText)?.groupValues?.get(1)?.trim() ?: ""
        }

        return EmailAlert(
            type = type,
            companyName = companyName,
            category = category,
            status = status,
            stipend = stipend,
            ctc = ctc,
            location = "",
            deadline = deadline,
            link = link,
            eventDateTime = "",
            isEligible = isEligible,
            rawEmailBody = rawEmailText,
            batchmates = batchmatesString
        )
    }
}

// DTO for parsing
@kotlinx.serialization.Serializable
data class EmailAlertDto(
    val type: AlertType,
    val isEligible: Boolean,
    val companyName: String? = null,
    val category: String? = null,
    val status: String? = null,
    val stipend: String? = null,
    val ctc: String? = null,
    val location: String? = null,
    val deadline: String? = null,
    val link: String? = null,
    val eventDateTime: String? = null,
    val batchmates: List<String>? = null
)
