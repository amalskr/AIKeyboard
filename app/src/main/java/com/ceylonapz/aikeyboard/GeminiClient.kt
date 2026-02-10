package com.ceylonapz.aikeyboard

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class GeminiClient {

    companion object {
        private const val TAG = "AIKeyboard"
    }

    private val model = GenerativeModel(
        modelName = "gemini-3-pro-preview",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun checkGrammar(sentence: String): GrammarResult =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "📝 Checking: \"$sentence\"")

            try {
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
- If the sentence is already correct, set is_error to false and correctedText same as originalText
- Keep corrections minimal — only fix actual errors, don't rephrase
- Preserve the user's tone and intent
- Fix: grammar, spelling, punctuation, subject-verb agreement, tense consistency
- Do NOT change: casual tone, slang (if intentional), proper nouns, abbreviations

Sentence: $sentence
""".trimIndent()

                val response = model.generateContent(prompt)
                val rawText = response.text ?: ""

                Log.d(TAG, "🌐 Raw response: $rawText")

                // Clean markdown fences if Gemini adds them
                val cleanJson = rawText
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                Log.d(TAG, "📄 Clean JSON: $cleanJson")

                val result = json.decodeFromString<GrammarResult>(cleanJson)

                Log.d(TAG, "✅ is_error=${result.is_error}, corrected='${result.correctedText}'")

                result

            } catch (e: Exception) {
                Log.e(TAG, "❌ GeminiClient error: ${e.message}", e)

                GrammarResult(
                    originalText = sentence,
                    correctedText = sentence,
                    is_error = false
                )
            }
        }
}