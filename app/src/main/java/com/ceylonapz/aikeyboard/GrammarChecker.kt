package com.ceylonapz.aikeyboard


import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class GrammarChecker(private val apiKey: String) {

    companion object {
        private const val TAG = "AIKeyboard"
        private const val SYSTEM_PROMPT = """
You are a grammar checker embedded in a mobile keyboard.
When given a sentence, analyze it and respond ONLY with a JSON object.
No markdown, no backticks, no extra text. Pure JSON only.

JSON format:
{
  "corrected": "The corrected sentence here",
  "has_errors": true,
  "errors": [
    {
      "type": "grammar|spelling|punctuation|style",
      "original": "the wrong part",
      "fixed": "the corrected part",
      "explanation": "Brief explanation"
    }
  ]
}

Rules:
- If the sentence is already correct, return has_errors: false and empty errors array
- Keep corrections minimal — only fix actual errors, don't rephrase
- Preserve the user's tone and intent
- Fix: grammar, spelling, punctuation, subject-verb agreement, tense consistency
- Do NOT change: casual tone, slang (if intentional), proper nouns, abbreviations
"""
    }

    private val api = GeminiApiService.create()
    private val json = Json { ignoreUnknownKeys = true }

    init {
        Log.d(TAG, "══════════════════════════════════════")
        Log.d(TAG, "GrammarChecker initialized (Gemini)")
        Log.d(TAG, "API Key present: ${apiKey.isNotBlank()}")
        Log.d(TAG, "API Key prefix: ${apiKey.take(10)}...")
        Log.d(TAG, "══════════════════════════════════════")
    }

    suspend fun check(sentence: String): GrammarResult =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "──────────────────────────────────")
            Log.d(TAG, "📝 Grammar check started")
            Log.d(TAG, "📝 Input: \"$sentence\"")

            try {
                val request = GeminiRequest(
                    systemInstruction = SystemInstruction(
                        parts = listOf(Part(text = SYSTEM_PROMPT))
                    ),
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(text = "Check this sentence: $sentence")
                            ),
                            role = "user"
                        )
                    ),
                    generationConfig = GenerationConfig(
                        temperature = 0.1f,
                        maxOutputTokens = 512,
                        responseMimeType = "application/json"
                    )
                )

                Log.d(TAG, "🌐 Calling Gemini API...")

                val response = api.generateContent(
                    apiKey = apiKey,
                    request = request
                )

                // Check for API error
                response.error?.let { err ->
                    Log.e(TAG, "❌ API Error: ${err.code} - ${err.message}")
                    return@withContext GrammarResult(
                        originalText = sentence,
                        correctedText = sentence,
                        hasErrors = false,
                        errors = emptyList()
                    )
                }

                Log.d(TAG, "✅ API Response received")
                Log.d(TAG, "✅ Candidates: ${response.candidates?.size ?: 0}")

                // Extract text from response
                val rawText = response.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text
                    ?.trim()
                    ?: ""

                Log.d(TAG, "📄 Raw response: $rawText")

                if (rawText.isBlank()) {
                    Log.e(TAG, "❌ Empty response from Gemini")
                    return@withContext GrammarResult(
                        originalText = sentence,
                        correctedText = sentence,
                        hasErrors = false,
                        errors = emptyList()
                    )
                }

                // Clean markdown fences if present
                val cleanJson = rawText
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                Log.d(TAG, "📄 Clean JSON: $cleanJson")

                // Parse
                val grammarResp = json.decodeFromString<GrammarResponse>(cleanJson)

                Log.d(TAG, "✅ Parsed successfully")
                Log.d(TAG, "✅ Has errors: ${grammarResp.has_errors}")
                Log.d(TAG, "✅ Corrected: \"${grammarResp.corrected}\"")
                Log.d(TAG, "✅ Error count: ${grammarResp.errors.size}")
                grammarResp.errors.forEach { err ->
                    Log.d(
                        TAG,
                        "  ⚠️ [${err.type}] \"${err.original}\" → \"${err.fixed}\" (${err.explanation})"
                    )
                }
                Log.d(TAG, "──────────────────────────────────")

                GrammarResult(
                    originalText = sentence,
                    correctedText = grammarResp.corrected,
                    hasErrors = grammarResp.has_errors,
                    errors = grammarResp.errors
                )

            } catch (e: Exception) {
                Log.e(TAG, "❌ EXCEPTION: ${e.javaClass.simpleName}")
                Log.e(TAG, "❌ Message: ${e.message}")
                Log.e(TAG, "❌ Stack trace:", e)
                Log.e(TAG, "──────────────────────────────────")

                GrammarResult(
                    originalText = sentence,
                    correctedText = sentence,
                    hasErrors = false,
                    errors = emptyList()
                )
            }
        }
}