package com.ceylonapz.aikeyboard

import kotlinx.serialization.Serializable

@Serializable
data class GrammarResult(
    val originalText: String,
    val correctedText: String,
    val is_error: Boolean,
)
