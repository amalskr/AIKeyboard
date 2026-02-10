package com.ceylonapz.aikeyboard

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiClient() {

    private val model = GenerativeModel(
        modelName = "gemini-3-pro-preview",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    //GrammarResult
    suspend fun checkGrammar(sentence: String): String =
        withContext(Dispatchers.IO) {

            val prompt = """
                You are a grammar checker embedded in a mobile keyboard.
When given a sentence, analyze it and respond ONLY with a JSON object.
No markdown, no backticks, no extra text. Pure JSON only.

JSON format:
{
  "originalText": "User sentence",
  "correctedText": "Corrected sentence",
  "is_error": true
}

Rules:
- If the sentence is already correct, return has_errors: false and empty errors array
- Keep corrections minimal — only fix actual errors, don't rephrase
- Preserve the user's tone and intent
- Fix: grammar, spelling, punctuation, subject-verb agreement, tense consistency
- Do NOT change: casual tone, slang (if intentional), proper nouns, abbreviations
            """.trimIndent()

            val response = model.generateContent(prompt)

            response.text ?: "No information found."
        }
}