package com.ceylonapz.aikeyboard

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class KeyboardViewModel : ViewModel() {

    companion object {
        private const val TAG = "AIKeyboard"
    }

    val sentenceBuffer = StringBuilder()
    val grammarResult = mutableStateOf<GrammarResult?>(null)
    val isChecking = mutableStateOf(false)
    val statusMessage = mutableStateOf("")
    val isShiftOn = mutableStateOf(false)
    val isConnected = mutableStateOf(true)

    private val geminiClient = GeminiClient()
    private var checkJob: Job? = null

    // ── Key handlers ───────────────────────────────────────
    fun onPeriodTyped(commitText: (String) -> Unit) {
        commitText(".")
        sentenceBuffer.append(".")
        triggerGrammarCheck()
    }

    fun onQuestionMarkTyped(commitText: (String) -> Unit) {
        commitText("?")
        sentenceBuffer.append("?")
        triggerGrammarCheck()
    }

    fun onExclamationTyped(commitText: (String) -> Unit) {
        commitText("!")
        sentenceBuffer.append("!")
        triggerGrammarCheck()
    }

    fun onCharTyped(char: Char, commitText: (String) -> Unit) {
        val c = if (isShiftOn.value) char.uppercaseChar() else char.lowercaseChar()
        commitText(c.toString())
        sentenceBuffer.append(c)
        isShiftOn.value = false
    }

    fun onSpaceTyped(commitText: (String) -> Unit) {
        commitText(" ")
        sentenceBuffer.append(" ")
    }

    fun onDeleteTyped(deleteOne: () -> Unit) {
        deleteOne()
        if (sentenceBuffer.isNotEmpty()) {
            sentenceBuffer.deleteCharAt(sentenceBuffer.length - 1)
        }
        grammarResult.value = null
        statusMessage.value = ""
    }

    fun onShiftToggle() {
        isShiftOn.value = !isShiftOn.value
    }

    fun onEnter(sendEnter: () -> Unit) {
        sendEnter()
        sentenceBuffer.clear()
        grammarResult.value = null
        statusMessage.value = ""
    }

    // ── Grammar Check via Gemini ───────────────────────────
    private fun triggerGrammarCheck() {
        val text = sentenceBuffer.toString().trim()
        Log.d(TAG, "🔵 Punctuation typed! Buffer: '$text'")

        if (text.length < 4) {
            Log.d(TAG, "🔵 Too short (${text.length} chars), skipping")
            return
        }

        checkJob?.cancel()
        isChecking.value = true
        statusMessage.value = "🔍 Checking grammar..."
        Log.d(TAG, "🔵 Starting Gemini grammar check...")

        checkJob = viewModelScope.launch {
            try {
                // Call Gemini — returns GrammarResult directly
                val result = geminiClient.checkGrammar(text)
                Log.d(TAG, "🔵 Result: is_error=${result.is_error}, corrected='${result.correctedText}'")

                grammarResult.value = result
                isConnected.value = true

                statusMessage.value = if (result.is_error) {
                    "✏️ Grammar issue found"
                } else {
                    "✅ Looks good!"
                }

            } catch (e: Exception) {
                Log.e(TAG, "🔵 Check failed: ${e.message}", e)
                grammarResult.value = null
                statusMessage.value = "⚠️ Check failed"
                isConnected.value = false
            } finally {
                isChecking.value = false
            }
        }
    }

    // ── Apply correction ───────────────────────────────────
    fun applySuggestion(
        deleteSurrounding: (Int) -> Unit,
        commitText: (String) -> Unit
    ) {
        val result = grammarResult.value ?: return
        Log.d(TAG, "🟢 Applying: '${result.originalText}' → '${result.correctedText}'")
        deleteSurrounding(result.originalText.length)
        commitText(result.correctedText)
        sentenceBuffer.clear()
        sentenceBuffer.append(result.correctedText)
        grammarResult.value = null
        statusMessage.value = "✅ Corrected!"
    }

    fun dismissSuggestion() {
        grammarResult.value = null
        statusMessage.value = ""
    }
}