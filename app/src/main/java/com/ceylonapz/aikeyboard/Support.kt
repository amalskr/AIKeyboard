package com.ceylonapz.aikeyboard

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// ── Gemini API Request ─────────────────────────────────────
@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    @SerialName("generationConfig") val generationConfig: GenerationConfig? = null,
    @SerialName("systemInstruction") val systemInstruction: SystemInstruction? = null
)

@Serializable
data class Content(
    val parts: List<Part>,
    val role: String = "user"
)

@Serializable
data class Part(
    val text: String
)

@Serializable
data class GenerationConfig(
    val temperature: Float = 0.1f,
    val maxOutputTokens: Int = 512,
    @SerialName("responseMimeType") val responseMimeType: String? = null
)

@Serializable
data class SystemInstruction(
    val parts: List<Part>
)

// ── Gemini API Response ────────────────────────────────────
@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val error: GeminiError? = null
)

@Serializable
data class Candidate(
    val content: CandidateContent? = null,
    val finishReason: String? = null
)

@Serializable
data class CandidateContent(
    val parts: List<Part>? = null,
    val role: String? = null
)

@Serializable
data class GeminiError(
    val code: Int = 0,
    val message: String = "",
    val status: String = ""
)

// ── Grammar check result parsed from Gemini's JSON ─────────
@Serializable
data class GrammarResponse(
    val corrected: String,
    val has_errors: Boolean,
    val errors: List<GrammarErrorItem> = emptyList()
)

@Serializable
data class GrammarErrorItem(
    val type: String = "",
    val original: String = "",
    val fixed: String = "",
    val explanation: String = ""
)

// ── Local data class used by UI ────────────────────────────
data class GrammarResult(
    val originalText: String,
    val correctedText: String,
    val hasErrors: Boolean,
    val errors: List<GrammarErrorItem>
)
