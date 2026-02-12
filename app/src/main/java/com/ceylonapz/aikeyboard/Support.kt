package com.ceylonapz.aikeyboard

import kotlinx.serialization.Serializable

@Serializable
data class GrammarResult(
    val originalText: String,
    val correctedText: String,
    val is_error: Boolean,
)

enum class KeyboardMode {
    QWERTY,
    SYMBOLS_1,
    SYMBOLS_2,
    EMOJI
}
