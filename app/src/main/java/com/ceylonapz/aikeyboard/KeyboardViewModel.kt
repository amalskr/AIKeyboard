package com.ceylonapz.aikeyboard

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class KeyboardViewModel : ViewModel() {

    companion object {
        private const val TAG = "AIKeyboard"
    }

    var vibrator: Vibrator? = null

    fun vibrateKey() {
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(30, 80))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(30)
            }
        }
    }

    private fun vibrateLong() {
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(100)
            }
        }
    }

    val sentenceBuffer = StringBuilder()
    val grammarResult = mutableStateOf<GrammarResult?>(null)
    val isChecking = mutableStateOf(false)
    val statusMessage = mutableStateOf("")
    val isShiftOn = mutableStateOf(false)
    val isConnected = mutableStateOf(true)
    val keyboardMode = mutableStateOf(KeyboardMode.QWERTY)

    private val geminiClient = GeminiClient()
    private val emojiSuggester = EmojiSuggester()
    private val wordSuggester = WordSuggester()
    val emojiSuggestions = mutableStateOf<List<String>>(emptyList())
    val wordSuggestions = mutableStateOf<List<String>>(emptyList())
    private var checkJob: Job? = null
    private var statusJob: Job? = null
    private var autoDismissJob: Job? = null
    private var lastCheckedText = ""

    private val checkingMessages = listOf(
        "🔍 Analyzing text...",
        "📝 Checking grammar...",
        "🧠 Processing sentences...",
        "📖 Reviewing structure...",
        "✍️ Inspecting spelling...",
        "🔤 Validating words...",
        "💬 Almost done..."
    )

    // ── Mode switching ────────────────────────────────────
    fun switchToQwerty() {
        vibrateKey()
        keyboardMode.value = KeyboardMode.QWERTY
    }

    fun switchToSymbols() {
        vibrateKey()
        keyboardMode.value = KeyboardMode.SYMBOLS_1
    }

    fun toggleSymbolPage() {
        vibrateKey()
        keyboardMode.value = when (keyboardMode.value) {
            KeyboardMode.SYMBOLS_1 -> KeyboardMode.SYMBOLS_2
            KeyboardMode.SYMBOLS_2 -> KeyboardMode.SYMBOLS_1
            else -> KeyboardMode.SYMBOLS_1
        }
    }

    fun switchToEmoji() {
        vibrateKey()
        keyboardMode.value = KeyboardMode.EMOJI
    }

    fun onLanguageSwitchTapped(showPicker: () -> Unit) {
        vibrateKey()
        showPicker()
    }

    fun onSymbolTyped(symbol: String, commitText: (String) -> Unit) {
        if (isChecking.value) return
        vibrateKey()
        commitText(symbol)
        sentenceBuffer.append(symbol)
    }

    // ── Key handlers ───────────────────────────────────────
    fun onPeriodTyped(commitText: (String) -> Unit) {
        if (isChecking.value) return
        vibrateKey()
        commitText(".")
        sentenceBuffer.append(".")
    }

    fun onQuestionMarkTyped(commitText: (String) -> Unit) {
        if (isChecking.value) return
        vibrateKey()
        commitText("?")
        sentenceBuffer.append("?")
    }

    fun onExclamationTyped(commitText: (String) -> Unit) {
        if (isChecking.value) return
        vibrateKey()
        commitText("!")
        sentenceBuffer.append("!")
    }

    fun onGrammarCheckTapped() {
        vibrateLong()
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
        vibrateKey()
        val c = if (isShiftOn.value) char.uppercaseChar() else char.lowercaseChar()
        commitText(c.toString())
        sentenceBuffer.append(c)
        isShiftOn.value = false
        updateWordSuggestions()
    }

    fun onSpaceTyped(commitText: (String) -> Unit) {
        if (isChecking.value) return
        vibrateKey()
        commitText(" ")
        sentenceBuffer.append(" ")
        wordSuggestions.value = emptyList()
        updateEmojiSuggestions()
    }

    fun onDeleteTyped(deleteOne: () -> Unit) {
        if (isChecking.value) return
        vibrateKey()
        deleteOne()
        if (sentenceBuffer.isNotEmpty()) {
            sentenceBuffer.deleteCharAt(sentenceBuffer.length - 1)
        }
        grammarResult.value = null
        statusMessage.value = ""
        emojiSuggestions.value = emptyList()
        updateWordSuggestions()
    }

    fun onShiftToggle() {
        vibrateKey()
        isShiftOn.value = !isShiftOn.value
    }

    fun onEnter(sendEnter: () -> Unit) {
        if (isChecking.value) return
        vibrateKey()
        sendEnter()
        sentenceBuffer.clear()
        lastCheckedText = ""
        grammarResult.value = null
        statusMessage.value = ""
    }

    // ── Grammar Check via Gemini ───────────────────────────
    private fun triggerGrammarCheck() {
        val fullText = sentenceBuffer.toString().trim()
        Log.d(TAG, "🔵 Grammar check requested. Buffer: '$fullText'")

        if (fullText.length < 4) {
            Log.d(TAG, "🔵 Too short (${fullText.length} chars), skipping")
            return
        }

        // Skip if text hasn't changed since last check
        if (fullText == lastCheckedText) {
            Log.d(TAG, "🔵 Text unchanged, skipping re-check")
            statusMessage.value = "\u2705 Already checked!"
            autoDismissStatus()
            return
        }

        // Only send new text if buffer starts with already-checked prefix
        val textToCheck = if (lastCheckedText.isNotEmpty() && fullText.startsWith(lastCheckedText)) {
            val newPart = fullText.substring(lastCheckedText.length).trim()
            Log.d(TAG, "🔵 Incremental check, new part: '$newPart'")
            if (newPart.length < 4) {
                Log.d(TAG, "🔵 New part too short (${newPart.length} chars), skipping")
                return
            }
            newPart
        } else {
            fullText
        }

        checkJob?.cancel()
        statusJob?.cancel()
        isChecking.value = true
        Log.d(TAG, "🔵 Sending to Gemini: '$textToCheck'")

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
                val result = geminiClient.checkGrammar(textToCheck)
                Log.d(TAG, "🔵 Result: is_error=${result.is_error}, corrected='${result.correctedText}'")

                statusJob?.cancel()
                grammarResult.value = result
                isConnected.value = true

                if (result.is_error) {
                    statusMessage.value = "\u270F\uFE0F Grammar issue found"
                } else {
                    // No errors — mark the full buffer as checked
                    lastCheckedText = fullText
                    statusMessage.value = "\u2705 Looks good!"
                    autoDismissStatus()
                }
                vibrateLong()

            } catch (e: Exception) {
                Log.e(TAG, "🔵 Check failed: ${e.message}", e)
                statusJob?.cancel()
                grammarResult.value = null
                statusMessage.value = "\u26A0\uFE0F Check failed"
                isConnected.value = false
                vibrateLong()
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
        Log.d(TAG, "\uD83D\uDFE2 Applying: '${result.originalText}' \u2192 '${result.correctedText}'")

        // Build the new full text: checked prefix + corrected new part
        val fullText = sentenceBuffer.toString().trim()
        val newFullText = if (lastCheckedText.isNotEmpty() && fullText.startsWith(lastCheckedText)) {
            lastCheckedText + result.correctedText
        } else {
            result.correctedText
        }

        deleteSurrounding(fullText.length)
        commitText(newFullText)
        sentenceBuffer.clear()
        sentenceBuffer.append(newFullText)
        lastCheckedText = newFullText
        grammarResult.value = null
        statusMessage.value = "\u2705 Corrected!"
        autoDismissStatus()
    }

    private fun autoDismissStatus(delayMs: Long = 2000L) {
        autoDismissJob?.cancel()
        autoDismissJob = viewModelScope.launch {
            delay(delayMs)
            statusMessage.value = ""
        }
    }

    fun dismissSuggestion() {
        // Mark current buffer as checked so it won't re-check on dismiss
        lastCheckedText = sentenceBuffer.toString().trim()
        grammarResult.value = null
        statusMessage.value = ""
    }

    // ── Word suggestions ──────────────────────────────────
    private fun updateWordSuggestions() {
        val text = sentenceBuffer.toString()
        val lastWord = text.split(" ").lastOrNull() ?: ""
        wordSuggestions.value = if (lastWord.length >= 2) {
            wordSuggester.suggest(lastWord)
        } else {
            emptyList()
        }
    }

    fun onWordSelected(word: String, commitText: (String) -> Unit) {
        vibrateKey()
        val text = sentenceBuffer.toString()
        val lastWord = text.split(" ").lastOrNull() ?: ""
        if (lastWord.isNotEmpty()) {
            // Complete the remaining part of the word + add space
            val remaining = word.substring(lastWord.length)
            commitText("$remaining ")
            sentenceBuffer.append("$remaining ")
        }
        wordSuggestions.value = emptyList()
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
        vibrateKey()
        commitText("$emoji ")
        sentenceBuffer.append("$emoji ")
        emojiSuggestions.value = emptyList()
    }
}