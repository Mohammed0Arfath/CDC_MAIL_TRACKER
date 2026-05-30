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
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") return@withContext null

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
            val jsonText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: return@withContext null
            
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
            null
        }
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
