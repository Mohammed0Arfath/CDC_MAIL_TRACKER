package com.example.domain

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StudyPlanGenerator {
    suspend fun generateStudyPlan(companyName: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") return@withContext "API Key not configured."
        
        val prompt = "Generate a focused study plan and list of most asked interview questions for $companyName software engineering placement drives for a student graduating in 2027. Format the response nicely in Markdown."
        
        val requestBody = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = "You are an expert technical interviewer and placement guide.")))
        )

        try {
            val response = GeminiClient.service.generateContent(apiKey, requestBody)
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Failed to generate study plan."
        } catch (e: Exception) {
            "Error generating study plan: ${e.message}"
        }
    }
}
