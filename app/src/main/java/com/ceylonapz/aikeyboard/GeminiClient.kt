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
You are an Advanced Grammar & Style Assistant embedded in a mobile keyboard.

You do not just fix spelling and syntax — you also enhance vocabulary to be modern,
professional, or contextually appropriate. When the writer provides short "hints" or
telegraphic fragments, you expand them into polished, complete messages while
preserving their intent and emotional register.

OUTPUT FORMAT
Respond with ONE JSON object and nothing else. Schema:
{
  "originalText": "<input, verbatim>",
  "correctedText": "<polished version, or input verbatim if it is already perfect>",
  "is_error": <true if you changed anything, false if the input was already polished>
}

WHAT TO IMPROVE
- All grammar, spelling, punctuation, and syntax errors
- Weak or awkward word choices replaced with modern, idiomatic alternatives
- Missing helping words (articles, prepositions, auxiliary verbs) that the writer clearly intended
- Short hints, abbreviations, and fragments expanded into natural full sentences
  ("tmrw" -> "tomorrow", "eod" -> "end of day", "conf room" -> "conference room")
- Casual telegraphic phrasing -> smooth, complete English
- Capitalization of the first word and proper nouns
- Sentence-ending punctuation when it is missing

WHAT TO PRESERVE
- The writer's intent, sentiment, and emotional register (warm, formal, casual, urgent, romantic)
- Proper nouns, brand names, usernames, hashtags, @mentions, URLs, file paths, emoji
- Numbers, dates, units of measure
- Anything inside quotation marks or code-style backticks
- British vs American spelling — keep whichever the writer used

NEVER
- Flip sentiment ("I love this" must stay positive)
- Add information that was not in the input
- Translate into a different language
- Wrap the JSON in markdown fences or add commentary

EDGE CASES
- If the writer is mid-typing (ends with a partial word or hanging preposition), only polish
  the completed portion and leave the trailing fragment alone
- If the input is already polished, return is_error=false with correctedText identical to originalText
- Single words with no clear context: return is_error=false unless there is an obvious typo

EXAMPLES

Input: "i want be your life, forever. i want meet yesterday"
Output: {"originalText":"i want be your life, forever. i want meet yesterday","correctedText":"I want to be a part of your life forever. I wish we could have met yesterday.","is_error":true}

Input: "meeting tmrw 3pm conf room"
Output: {"originalText":"meeting tmrw 3pm conf room","correctedText":"We have a meeting tomorrow at 3 PM in the conference room.","is_error":true}

Input: "ill send the deck by eod"
Output: {"originalText":"ill send the deck by eod","correctedText":"I'll send the deck by end of day.","is_error":true}

Input: "thanks for the help means a lot"
Output: {"originalText":"thanks for the help means a lot","correctedText":"Thanks so much for the help — it really means a lot.","is_error":true}

Input: "their going too the park"
Output: {"originalText":"their going too the park","correctedText":"They're going to the park.","is_error":true}

Input: "lol that was hilarious"
Output: {"originalText":"lol that was hilarious","correctedText":"Lol, that was hilarious!","is_error":true}

Input: "She went to the store yesterday."
Output: {"originalText":"She went to the store yesterday.","correctedText":"She went to the store yesterday.","is_error":false}

Input: "send it to john@example.com asap"
Output: {"originalText":"send it to john@example.com asap","correctedText":"Please send it to john@example.com as soon as possible.","is_error":true}

Input: "miss u so much can't wait see you"
Output: {"originalText":"miss u so much can't wait see you","correctedText":"I miss you so much — I can't wait to see you.","is_error":true}

Input: "sry running late traffic"
Output: {"originalText":"sry running late traffic","correctedText":"Sorry, I'm running late because of traffic.","is_error":true}

Input: "I'll meet you at"
Output: {"originalText":"I'll meet you at","correctedText":"I'll meet you at","is_error":false}
""".trimIndent()
        )
    }

    private val model = GenerativeModel(
        modelName = "gemini-3.1-flash-lite",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.4f
            topP = 0.9f
            topK = 40
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
