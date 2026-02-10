package com.ceylonapz.aikeyboard

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class GrammarChecker(apiKey: String) {

    private val api = ClaudeApiService.create(apiKey)
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val SYSTEM_PROMPT = """
You are a grammar checker embedded in a mobile keyboard.
When given a sentence, analyze it and respond ONLY with a JSON object (no markdown, no backticks, no extra text).

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
- Be fast — this runs on every sentence in a keyboard
"""
    }

    suspend fun check(sentence: String): GrammarResult =
        withContext(Dispatchers.IO) {
            try {
                val request = ClaudeRequest(
                    model = "claude-sonnet-4-20250514",
                    maxTokens = 512,
                    system = SYSTEM_PROMPT,
                    messages = listOf(
                        Message(
                            role = "user",
                            content = "Check this sentence: $sentence"
                        )
                    )
                )

                val response = api.createMessage(request)

                // Handle API error
                response.error?.let {
                    return@withContext GrammarResult(
                        originalText = sentence,
                        correctedText = sentence,
                        hasErrors = false,
                        errors = emptyList()
                    )
                }

                // Extract text from response
                val rawText = response.content
                    .filter { it.type == "text" }
                    .joinToString("") { it.text }
                    .trim()

                // Clean markdown fences if Claude adds them
                val cleanJson = rawText
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                // Parse the grammar response
                val grammarResp = json.decodeFromString<GrammarResponse>(cleanJson)

                GrammarResult(
                    originalText = sentence,
                    correctedText = grammarResp.corrected,
                    hasErrors = grammarResp.has_errors,
                    errors = grammarResp.errors
                )
            } catch (e: Exception) {
                e.printStackTrace()
                // On any failure, return no errors (don't block typing)
                GrammarResult(
                    originalText = sentence,
                    correctedText = sentence,
                    hasErrors = false,
                    errors = emptyList()
                )
            }
        }
}