package com.ceylonapz.aikeyboard

import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
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

    fun vibrateKey() = doVibrate(durationMs = 55)

    private fun vibrateLong() = doVibrate(durationMs = 150)

    private fun doVibrate(durationMs: Long) {
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                vibrateWithTouchUsage(v, effect)
            } else {
                v.vibrate(effect)
            }
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(durationMs)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun vibrateWithTouchUsage(v: Vibrator, effect: VibrationEffect) {
        v.vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH))
    }

    val sentenceBuffer = StringBuilder()
    val grammarResult = mutableStateOf<GrammarResult?>(null)
    val isChecking = mutableStateOf(false)
    val checkFailed = mutableStateOf(false)
    val isShiftOn = mutableStateOf(false)
    val isConnected = mutableStateOf(true)
    val keyboardMode = mutableStateOf(KeyboardMode.QWERTY)

    val replySuggestions = mutableStateOf<List<String>>(emptyList())
    val isGeneratingReplies = mutableStateOf(false)
    val replyFailed = mutableStateOf<String?>(null)

    private val geminiClient = GeminiClient()
    private val emojiSuggester = EmojiSuggester()
    private val wordSuggester = WordSuggester()
    val emojiSuggestions = mutableStateOf<List<String>>(emptyList())
    val wordSuggestions = mutableStateOf<List<String>>(emptyList())
    private var checkJob: Job? = null
    private var replyJob: Job? = null
    private var lastCheckedText = ""

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
            isChecking.value = false
        } else {
            triggerGrammarCheck()
        }
    }

    // ── Smart Reply ────────────────────────────────────────
    fun onSmartReplyTapped(readClipboard: () -> String?) {
        vibrateLong()

        if (isGeneratingReplies.value) {
            replyJob?.cancel()
            isGeneratingReplies.value = false
            return
        }

        val incoming = readClipboard()?.trim().orEmpty()
        if (incoming.isEmpty()) {
            replyFailed.value = "Copy a message first, then tap 💬"
            replySuggestions.value = emptyList()
            viewModelScope.launch {
                delay(2500)
                replyFailed.value = null
            }
            return
        }

        // A tap with replies already showing dismisses them
        if (replySuggestions.value.isNotEmpty()) {
            replySuggestions.value = emptyList()
            return
        }

        val intent = sentenceBuffer.toString().trim()

        replyJob?.cancel()
        replyFailed.value = null
        isGeneratingReplies.value = true
        Log.d(TAG, "Smart reply incoming='$incoming' intent='$intent'")

        replyJob = viewModelScope.launch {
            try {
                val replies = geminiClient.suggestReplies(incoming, intent)
                replySuggestions.value = replies
                if (replies.isEmpty()) {
                    replyFailed.value = "No suggestions returned"
                    viewModelScope.launch {
                        delay(2500)
                        replyFailed.value = null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Smart reply failed: ${e.message}", e)
                replySuggestions.value = emptyList()
                replyFailed.value = e.message ?: "Reply failed"
                viewModelScope.launch {
                    delay(2500)
                    replyFailed.value = null
                }
            } finally {
                isGeneratingReplies.value = false
                vibrateLong()
            }
        }
    }

    fun onReplySelected(
        reply: String,
        deleteSurrounding: (Int) -> Unit,
        commitText: (String) -> Unit
    ) {
        vibrateKey()
        // Replace the user's rough draft (if any) with the polished reply
        val draftLen = sentenceBuffer.length
        if (draftLen > 0) {
            deleteSurrounding(draftLen)
        }
        commitText(reply)
        sentenceBuffer.clear()
        sentenceBuffer.append(reply)
        replySuggestions.value = emptyList()
        lastCheckedText = reply
        grammarResult.value = null
        wordSuggestions.value = emptyList()
        emojiSuggestions.value = emptyList()
    }

    fun dismissReplies() {
        replySuggestions.value = emptyList()
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
    }

    // ── Grammar Check via Gemini ───────────────────────────
    private fun triggerGrammarCheck() {
        val fullText = sentenceBuffer.toString().trim()
        Log.d(TAG, "Grammar check requested. Buffer: '$fullText'")

        if (fullText.length < 4) {
            Log.d(TAG, "Too short (${fullText.length} chars), skipping")
            return
        }

        // Skip if text hasn't changed since last check
        if (fullText == lastCheckedText) {
            Log.d(TAG, "Text unchanged, skipping re-check")
            return
        }

        // Only send new text if buffer starts with already-checked prefix
        val textToCheck = if (lastCheckedText.isNotEmpty() && fullText.startsWith(lastCheckedText)) {
            val newPart = fullText.substring(lastCheckedText.length).trim()
            Log.d(TAG, "Incremental check, new part: '$newPart'")
            if (newPart.length < 4) {
                Log.d(TAG, "New part too short (${newPart.length} chars), skipping")
                return
            }
            newPart
        } else {
            fullText
        }

        checkJob?.cancel()
        isChecking.value = true
        checkFailed.value = false
        Log.d(TAG, "Sending to Gemini: '$textToCheck'")

        checkJob = viewModelScope.launch {
            try {
                val result = geminiClient.checkGrammar(textToCheck)
                Log.d(TAG, "Result: is_error=${result.is_error}, corrected='${result.correctedText}'")

                grammarResult.value = result
                isConnected.value = true

                if (!result.is_error) {
                    // No errors — mark the full buffer as checked
                    lastCheckedText = fullText
                }
                vibrateLong()

            } catch (e: Exception) {
                Log.e(TAG, "Check failed: ${e.message}", e)
                grammarResult.value = null
                isConnected.value = false
                checkFailed.value = true
                vibrateLong()
                // Auto-clear the failure flash after 2.5s
                viewModelScope.launch {
                    delay(2500)
                    checkFailed.value = false
                }
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
        Log.d(TAG, "Applying: '${result.originalText}' -> '${result.correctedText}'")

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
    }

    fun dismissSuggestion() {
        // Mark current buffer as checked so it won't re-check on dismiss
        lastCheckedText = sentenceBuffer.toString().trim()
        grammarResult.value = null
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
        Log.d(TAG, "Emoji check for word: '$lastWord'")
        emojiSuggestions.value = emojiSuggester.suggest(lastWord)
        Log.d(TAG, "Emoji suggestions: ${emojiSuggestions.value}")
    }

    fun onEmojiSelected(emoji: String, commitText: (String) -> Unit) {
        vibrateKey()
        commitText("$emoji ")
        sentenceBuffer.append("$emoji ")
        emojiSuggestions.value = emptyList()
    }
}
