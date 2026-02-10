package com.ceylonapz.aikeyboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Color Theme ────────────────────────────────────────────
data class KeyboardColorScheme(
    val Bg: Color,
    val KeyBg: Color,
    val KeyPressed: Color,
    val KeyText: Color,
    val Accent: Color,
    val SuggestionBg: Color,
    val CorrectGreen: Color,
    val ErrorBg: Color,
    val StatusGray: Color,
    val DimText: Color,
    val EmojiRowBg: Color
)

private val DarkKeyboardColors = KeyboardColorScheme(
    Bg = Color(0xFF1A1A2E),
    KeyBg = Color(0xFF16213E),
    KeyPressed = Color(0xFF0F3460),
    KeyText = Color(0xFFE0E0E0),
    Accent = Color(0xFF7C5CFC),
    SuggestionBg = Color(0xFF1A2E1A),
    CorrectGreen = Color(0xFF66BB6A),
    ErrorBg = Color(0xFF2E1A1A),
    StatusGray = Color(0xFF888899),
    DimText = Color(0xFF555566),
    EmojiRowBg = Color(0xFF1E1E3A)
)

private val LightKeyboardColors = KeyboardColorScheme(
    Bg = Color(0xFFE8E8EE),
    KeyBg = Color(0xFFFFFFFF),
    KeyPressed = Color(0xFFD0D0DA),
    KeyText = Color(0xFF1A1A2E),
    Accent = Color(0xFF6B4CE6),
    SuggestionBg = Color(0xFFDFF5DF),
    CorrectGreen = Color(0xFF388E3C),
    ErrorBg = Color(0xFFFDE8E8),
    StatusGray = Color(0xFF666677),
    DimText = Color(0xFF999AAA),
    EmojiRowBg = Color(0xFFDDDDE8)
)

val LocalKeyboardColors = staticCompositionLocalOf { DarkKeyboardColors }

object KeyboardColors {
    val current: KeyboardColorScheme
        @Composable get() = LocalKeyboardColors.current
}

val NUMBER_ROW = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
val ROW1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
val ROW2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
val ROW3 = listOf("z", "x", "c", "v", "b", "n", "m")

// ── Main Compose Keyboard ──────────────────────────────────
@Composable
fun ComposeKeyboard(
    viewModel: KeyboardViewModel,
    onCommitText: (String) -> Unit,
    onDeleteOne: () -> Unit,
    onSendEnter: () -> Unit,
    onDeleteSurrounding: (Int) -> Unit
) {
    val colors = if (isSystemInDarkTheme()) DarkKeyboardColors else LightKeyboardColors

    CompositionLocalProvider(LocalKeyboardColors provides colors) {
        ComposeKeyboardContent(viewModel, onCommitText, onDeleteOne, onSendEnter, onDeleteSurrounding)
    }
}

@Composable
private fun ComposeKeyboardContent(
    viewModel: KeyboardViewModel,
    onCommitText: (String) -> Unit,
    onDeleteOne: () -> Unit,
    onSendEnter: () -> Unit,
    onDeleteSurrounding: (Int) -> Unit
) {
    val colors = KeyboardColors.current
    val result by viewModel.grammarResult
    val status by viewModel.statusMessage
    val isChecking by viewModel.isChecking
    val isShift by viewModel.isShiftOn
    val emojis by viewModel.emojiSuggestions

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.Bg)
            .padding(horizontal = 3.dp, vertical = 4.dp)
    ) {
        // ── Emoji Suggestion Row ────────────────────────────
        AnimatedVisibility(
            visible = emojis.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            EmojiSuggestionRow(
                emojis = emojis,
                onEmojiSelected = { emoji ->
                    viewModel.onEmojiSelected(emoji, onCommitText)
                }
            )
        }

        // ── Grammar Suggestion Bar ─────────────────────────
        AnimatedVisibility(
            visible = result != null && result!!.is_error,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            result?.let { gr ->
                GrammarSuggestionBar(
                    result = gr,
                    onAccept = {
                        viewModel.applySuggestion(onDeleteSurrounding, onCommitText)
                    },
                    onDismiss = { viewModel.dismissSuggestion() }
                )
            }
        }

        // ── Status Bar ─────────────────────────────────────
        AnimatedVisibility(
            visible = status.isNotEmpty() && (result == null || !result!!.is_error)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 2.dp,
                        color = colors.Accent
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = status,
                    color = if (status.contains("✅")) colors.CorrectGreen
                    else colors.StatusGray,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(2.dp))

        // ── Number Row: 1234567890 ────────────────────────────
        KeyRow(NUMBER_ROW, isShift = false) { char ->
            viewModel.onCharTyped(char, onCommitText)
        }

        // ── Row 1: QWERTYUIOP ──────────────────────────────
        KeyRow(ROW1, isShift) { viewModel.onCharTyped(it, onCommitText) }

        // ── Row 2: ASDFGHJKL ───────────────────────────────
        KeyRow(ROW2, isShift, sidePadding = 18.dp) {
            viewModel.onCharTyped(it, onCommitText)
        }

        // ── Row 3: Shift + ZXCVBNM + Backspace ────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            SpecialKey(
                label = if (isShift) "⇧" else "⇪",
                weight = 1.4f,
                bgColor = if (isShift) colors.Accent else colors.KeyBg
            ) { viewModel.onShiftToggle() }

            ROW3.forEach { key ->
                KeyButton(
                    label = if (isShift) key.uppercase() else key,
                    modifier = Modifier.weight(1f)
                ) { viewModel.onCharTyped(key[0], onCommitText) }
            }

            SpecialKey(
                label = "⌫",
                weight = 1.4f
            ) { viewModel.onDeleteTyped(onDeleteOne) }
        }

        // ── Row 4: Punctuation + Space + Enter ─────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // Grammar check / stop button
            SpecialKey(
                label = if (isChecking) "■" else "✓ AI",
                weight = 1.3f,
                bgColor = if (isChecking) Color(0xFFCC4444) else colors.Accent.copy(alpha = 0.4f)
            ) { viewModel.onGrammarCheckTapped() }

            // Exclamation
            KeyButton(
                label = "!",
                modifier = Modifier.weight(0.8f),
                fontSize = 20
            ) { viewModel.onExclamationTyped(onCommitText) }

            // Question mark
            KeyButton(
                label = "?",
                modifier = Modifier.weight(0.8f),
                fontSize = 20
            ) { viewModel.onQuestionMarkTyped(onCommitText) }

            // Space bar
            Box(
                modifier = Modifier
                    .weight(3.5f)
                    .height(52.dp)
                    .padding(horizontal = 3.dp, vertical = 3.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.KeyBg)
                    .clickable { viewModel.onSpaceTyped(onCommitText) },
                contentAlignment = Alignment.Center
            ) {
                Text("space", color = colors.DimText, fontSize = 13.sp)
            }

            // Period
            KeyButton(
                label = ".",
                modifier = Modifier.weight(0.8f),
                fontSize = 22
            ) { viewModel.onPeriodTyped(onCommitText) }

            // Comma
            KeyButton(
                label = ",",
                modifier = Modifier.weight(0.8f),
                fontSize = 20
            ) { if (!isChecking) { onCommitText(","); viewModel.sentenceBuffer.append(",") } }

            // Enter
            SpecialKey(
                label = "↵",
                weight = 1.3f,
                bgColor = colors.Accent
            ) { viewModel.onEnter(onSendEnter) }
        }
    }
}

// ── Grammar Suggestion Bar (matches new GrammarResult) ─────
@Composable
fun GrammarSuggestionBar(
    result: GrammarResult,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = KeyboardColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.ErrorBg)
            .padding(10.dp)
    ) {
        // Original text label
        Text(
            text = "Original:",
            color = colors.StatusGray,
            fontSize = 10.sp
        )
        Text(
            text = result.originalText,
            color = colors.StatusGray,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(6.dp))

        // Corrected text preview
        Text(
            text = "Suggested:",
            color = colors.StatusGray,
            fontSize = 10.sp
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(colors.SuggestionBg)
                .padding(8.dp)
        ) {
            Text(
                text = result.correctedText,
                color = colors.CorrectGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(8.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) {
                Text("Ignore", color = colors.StatusGray, fontSize = 12.sp)
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.Accent
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text("Apply Fix ✓", fontSize = 12.sp)
            }
        }
    }
}

// ── Emoji Suggestion Row ────────────────────────────────
@Composable
fun EmojiSuggestionRow(
    emojis: List<String>,
    onEmojiSelected: (String) -> Unit
) {
    val colors = KeyboardColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(colors.EmojiRowBg)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        emojis.forEach { emoji ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.Accent.copy(alpha = 0.15f))
                    .clickable { onEmojiSelected(emoji) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// ── Reusable Key Components ────────────────────────────────
@Composable
fun KeyRow(
    keys: List<String>,
    isShift: Boolean,
    sidePadding: Dp = 0.dp,
    onKey: (Char) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = sidePadding, vertical = 3.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        keys.forEach { key ->
            KeyButton(
                label = if (isShift) key.uppercase() else key,
                modifier = Modifier.weight(1f)
            ) { onKey(key[0]) }
        }
    }
}

@Composable
fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    bgColor: Color = KeyboardColors.current.KeyBg,
    fontSize: Int = 18,
    onClick: () -> Unit
) {
    val colors = KeyboardColors.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .height(52.dp)
            .padding(3.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isPressed) colors.KeyPressed else bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = colors.KeyText,
            fontSize = fontSize.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
fun RowScope.SpecialKey(
    label: String,
    weight: Float,
    bgColor: Color = KeyboardColors.current.KeyBg,
    onClick: () -> Unit
) {
    val colors = KeyboardColors.current
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .weight(weight)
            .height(52.dp)
            .padding(3.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = colors.KeyText, fontSize = 16.sp)
    }
}