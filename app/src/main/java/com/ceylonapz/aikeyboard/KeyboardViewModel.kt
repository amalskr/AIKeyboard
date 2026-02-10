package com.ceylonapz.aikeyboard

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val emojiSuggester = EmojiSuggester()
    val emojiSuggestions = mutableStateOf<List<String>>(emptyList())
    private var checkJob: Job? = null
    private var statusJob: Job? = null

    private val checkingMessages = listOf(
        "🔍 Analyzing text...",
        "📝 Checking grammar...",
        "🧠 Processing sentences...",
        "📖 Reviewing structure...",
        "✍️ Inspecting spelling...",
        "🔤 Validating words...",
        "💬 Almost done..."
    )

    // ── Key handlers ───────────────────────────────────────
    fun onPeriodTyped(commitText: (String) -> Unit) {
        if (isChecking.value) return
        commitText(".")
        sentenceBuffer.append(".")
    }

    fun onQuestionMarkTyped(commitText: (String) -> Unit) {
        if (isChecking.value) return
        commitText("?")
        sentenceBuffer.append("?")
    }

    fun onExclamationTyped(commitText: (String) -> Unit) {
        if (isChecking.value) return
        commitText("!")
        sentenceBuffer.append("!")
    }

    fun onGrammarCheckTapped() {
        if (isChecking.value) {
            // Stop the ongoing check and re-enable typing
            checkJob?.cancel()
            statusJob?.cancel()
            isChecking.value = false
            statusMessage.value = ""
        } else {
            triggerGrammarCheck()
        }
    }

    fun onCharTyped(char: Char, commitText: (String) -> Unit) {
        if (isChecking.value) return
        val c = if (isShiftOn.value) char.uppercaseChar() else char.lowercaseChar()
        commitText(c.toString())
        sentenceBuffer.append(c)
        isShiftOn.value = false
    }

    fun onSpaceTyped(commitText: (String) -> Unit) {
        if (isChecking.value) return
        commitText(" ")
        sentenceBuffer.append(" ")
        updateEmojiSuggestions()
    }

    fun onDeleteTyped(deleteOne: () -> Unit) {
        if (isChecking.value) return
        deleteOne()
        if (sentenceBuffer.isNotEmpty()) {
            sentenceBuffer.deleteCharAt(sentenceBuffer.length - 1)
        }
        grammarResult.value = null
        statusMessage.value = ""
        emojiSuggestions.value = emptyList()
    }

    fun onShiftToggle() {
        isShiftOn.value = !isShiftOn.value
    }

    fun onEnter(sendEnter: () -> Unit) {
        if (isChecking.value) return
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
        statusJob?.cancel()
        isChecking.value = true
        Log.d(TAG, "🔵 Starting Gemini grammar check...")

        // Cycle through status messages while checking
        statusJob = viewModelScope.launch {
            var index = 0
            while (true) {
                statusMessage.value = checkingMessages[index % checkingMessages.size]
                index++
                delay(800)
            }
        }

        checkJob = viewModelScope.launch {
            try {
                val result = geminiClient.checkGrammar(text)
                Log.d(TAG, "🔵 Result: is_error=${result.is_error}, corrected='${result.correctedText}'")

                statusJob?.cancel()
                grammarResult.value = result
                isConnected.value = true

                statusMessage.value = if (result.is_error) {
                    "✏️ Grammar issue found"
                } else {
                    "✅ Looks good!"
                }

            } catch (e: Exception) {
                Log.e(TAG, "🔵 Check failed: ${e.message}", e)
                statusJob?.cancel()
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

    // ── Emoji suggestion for last word ─────────────────────
    private fun updateEmojiSuggestions() {
        val text = sentenceBuffer.toString().trimEnd()
        val lastWord = text.split(" ").lastOrNull() ?: ""
        Log.d(TAG, "🟡 Emoji check for word: '$lastWord'")
        emojiSuggestions.value = emojiSuggester.suggest(lastWord)
        Log.d(TAG, "🟡 Emoji suggestions: ${emojiSuggestions.value}")
    }

    fun onEmojiSelected(emoji: String, commitText: (String) -> Unit) {
        commitText("$emoji ")
        sentenceBuffer.append("$emoji ")
        emojiSuggestions.value = emptyList()
    }
}