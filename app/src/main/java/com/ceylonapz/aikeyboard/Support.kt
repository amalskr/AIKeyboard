package com.ceylonapz.aikeyboard

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Claude API Request
@Serializable
data class ClaudeRequest(
    val model: String = "claude-sonnet-4-20250514",
    @SerialName("max_tokens") val maxTokens: Int = 512,
    val messages: List<Message>,
    val system: String? = null
)

@Serializable
data class Message(
    val role: String,
    val content: String
)

// ── Claude API Response
@Serializable
data class ClaudeResponse(
    val id: String = "",
    val content: List<ContentBlock> = emptyList(),
    val usage: Usage? = null,
    val error: ApiError? = null
)

@Serializable
data class ContentBlock(
    val type: String = "text",
    val text: String = ""
)

@Serializable
data class Usage(
    @SerialName("input_tokens") val inputTokens: Int = 0,
    @SerialName("output_tokens") val outputTokens: Int = 0
)

@Serializable
data class ApiError(
    val type: String = "",
    val message: String = ""
)

// ── Grammar check result parsed from Claude's JSON
@Serializable
data class GrammarResponse(
    val corrected: String,
    val has_errors: Boolean,
    val errors: List<GrammarErrorItem> = emptyList()
)

@Serializable
data class GrammarErrorItem(
    val type: String = "",       // e.g., "spelling", "grammar", "punctuation"
    val original: String = "",
    val fixed: String = "",
    val explanation: String = ""
)

// ── Local data class used by UI
data class GrammarResult(
    val originalText: String,
    val correctedText: String,
    val hasErrors: Boolean,
    val errors: List<GrammarErrorItem>
)