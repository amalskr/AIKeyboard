package com.ceylonapz.aikeyboard

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class GeminiClient {

    companion object {
        private const val TAG = "AIKeyboard"
    }

    private val systemInstruction: Content = content("system") {
        text(
            """
You are a precise grammar and spelling corrector embedded in a mobile keyboard.

OUTPUT FORMAT
Respond with ONE JSON object and nothing else. Schema:
{
  "originalText": "<input sentence, verbatim>",
  "correctedText": "<sentence after corrections, or input verbatim if nothing to fix>",
  "is_error": <true if you made any change, false if input was already correct>
}

WHAT TO FIX
- Spelling errors and obvious typos ("teh" → "the", "recieve" → "receive")
- Subject-verb agreement ("he go" → "he goes")
- Verb tense consistency within the sentence
- Article usage ("a apple" → "an apple")
- Pronoun case ("between you and I" → "between you and me")
- Plural/singular errors ("two cat" → "two cats")
- Missing capitalization of the first word and proper nouns
- Missing or incorrect punctuation, including sentence-ending punctuation
- Common confusables when context is unambiguous ("their" vs "there" vs "they're", "your" vs "you're", "its" vs "it's", "then" vs "than")
- Doubled words ("the the cat") and accidental extra spaces

WHAT NOT TO TOUCH
- Tone, style, voice, or word choice — never rephrase for elegance
- Slang, contractions, or casual register the user clearly intends ("gonna", "wanna", "lol")
- Proper nouns, brand names, usernames, hashtags, @mentions, URLs, file paths, code
- Numbers, dates, units of measure
- Emoji and non-ASCII characters — keep them exactly as written
- Sentence structure — do not reorder clauses or combine/split sentences
- Anything inside quotation marks or code-style backticks
- British vs American spelling — keep whichever the user used

EDGE CASES
- If input is a single word or fragment under 3 words AND has no obvious typo, return is_error=false
- If input ends mid-word or mid-clause (user still typing), only fix obvious typos in completed words; leave the trailing fragment alone
- If the input is already correct, you MUST return is_error=false with correctedText identical to originalText
- If multiple corrections are needed, apply them all in a single corrected sentence
- Preserve the exact leading/trailing whitespace of the input in correctedText

EXAMPLES

Input: "i has a apple"
Output: {"originalText":"i has a apple","correctedText":"I have an apple.","is_error":true}

Input: "She went to the store yesterday."
Output: {"originalText":"She went to the store yesterday.","correctedText":"She went to the store yesterday.","is_error":false}

Input: "their going too the park"
Output: {"originalText":"their going too the park","correctedText":"They're going to the park.","is_error":true}

Input: "lol that was hilarious"
Output: {"originalText":"lol that was hilarious","correctedText":"Lol, that was hilarious.","is_error":true}

Input: "gonna grab coffee brb"
Output: {"originalText":"gonna grab coffee brb","correctedText":"Gonna grab coffee, brb.","is_error":true}

Input: "I'll meet you at"
Output: {"originalText":"I'll meet you at","correctedText":"I'll meet you at","is_error":false}

Input: "the the dog ran fast"
Output: {"originalText":"the the dog ran fast","correctedText":"The dog ran fast.","is_error":true}

Input: "send it to john@example.com asap"
Output: {"originalText":"send it to john@example.com asap","correctedText":"Send it to john@example.com ASAP.","is_error":true}
""".trimIndent()
        )
    }

    private val model = GenerativeModel(
        modelName = "gemini-3-pro-preview",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.1f
            topP = 0.8f
            topK = 20
            responseMimeType = "application/json"
        },
        systemInstruction = systemInstruction
    )

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun checkGrammar(sentence: String): GrammarResult =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "📝 Checking: \"$sentence\"")

            try {
                val response = model.generateContent("Input: \"$sentence\"")
                val rawText = response.text.orEmpty()

                Log.d(TAG, "🌐 Raw response: $rawText")

                val cleanJson = extractJson(rawText)
                Log.d(TAG, "📄 Clean JSON: $cleanJson")

                val result = json.decodeFromString<GrammarResult>(cleanJson)

                // Sanity check: if model claims an error but corrected == original, flip the flag
                val finalResult = if (result.is_error &&
                    result.correctedText.trim() == result.originalText.trim()
                ) {
                    result.copy(is_error = false)
                } else if (!result.is_error &&
                    result.correctedText.trim() != result.originalText.trim()
                ) {
                    result.copy(is_error = true)
                } else {
                    result
                }

                Log.d(TAG, "✅ is_error=${finalResult.is_error}, corrected='${finalResult.correctedText}'")
                finalResult

            } catch (e: Exception) {
                Log.e(TAG, "❌ GeminiClient error: ${e.message}", e)
                GrammarResult(
                    originalText = sentence,
                    correctedText = sentence,
                    is_error = false
                )
            }
        }

    private fun extractJson(raw: String): String {
        val trimmed = raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        return if (start >= 0 && end > start) trimmed.substring(start, end + 1) else trimmed
    }
}
