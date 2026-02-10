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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Color Theme ────────────────────────────────────────────
object KeyboardColors {
    val Bg = Color(0xFF1A1A2E)
    val KeyBg = Color(0xFF16213E)
    val KeyPressed = Color(0xFF0F3460)
    val KeyText = Color(0xFFE0E0E0)
    val Accent = Color(0xFF7C5CFC)
    val SuggestionBg = Color(0xFF1A2E1A)
    val CorrectGreen = Color(0xFF66BB6A)
    val ErrorBg = Color(0xFF2E1A1A)
    val StatusGray = Color(0xFF888899)
    val DimText = Color(0xFF555566)
}

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
    val result by viewModel.grammarResult
    val status by viewModel.statusMessage
    val isChecking by viewModel.isChecking
    val isShift by viewModel.isShiftOn
    val emojis by viewModel.emojiSuggestions

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KeyboardColors.Bg)
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
                        color = KeyboardColors.Accent
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = status,
                    color = if (status.contains("✅")) KeyboardColors.CorrectGreen
                    else KeyboardColors.StatusGray,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(2.dp))

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
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            SpecialKey(
                label = if (isShift) "⇧" else "⇪",
                weight = 1.4f,
                bgColor = if (isShift) KeyboardColors.Accent else KeyboardColors.KeyBg
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
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // Grammar check button
            SpecialKey(
                label = "✓ AI",
                weight = 1.3f,
                bgColor = KeyboardColors.Accent.copy(alpha = 0.4f)
            ) { viewModel.onGrammarCheckTapped() }

            // Comma
            KeyButton(
                label = ",",
                modifier = Modifier.weight(0.8f),
                fontSize = 20
            ) { onCommitText(","); viewModel.sentenceBuffer.append(",") }

            // Space bar
            Box(
                modifier = Modifier
                    .weight(3.5f)
                    .height(48.dp)
                    .padding(horizontal = 2.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(KeyboardColors.KeyBg)
                    .clickable { viewModel.onSpaceTyped(onCommitText) },
                contentAlignment = Alignment.Center
            ) {
                Text("space", color = KeyboardColors.DimText, fontSize = 13.sp)
            }

            // Period
            KeyButton(
                label = ".",
                modifier = Modifier.weight(0.8f),
                fontSize = 22
            ) { viewModel.onPeriodTyped(onCommitText) }

            // Question mark
            KeyButton(
                label = "?",
                modifier = Modifier.weight(0.8f),
                fontSize = 20
            ) { viewModel.onQuestionMarkTyped(onCommitText) }

            // Exclamation
            KeyButton(
                label = "!",
                modifier = Modifier.weight(0.8f),
                fontSize = 20
            ) { viewModel.onExclamationTyped(onCommitText) }

            // Enter
            SpecialKey(
                label = "↵",
                weight = 1.3f,
                bgColor = KeyboardColors.Accent
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(KeyboardColors.ErrorBg)
            .padding(10.dp)
    ) {
        // Original text label
        Text(
            text = "Original:",
            color = KeyboardColors.StatusGray,
            fontSize = 10.sp
        )
        Text(
            text = result.originalText,
            color = KeyboardColors.StatusGray,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(6.dp))

        // Corrected text preview
        Text(
            text = "Suggested:",
            color = KeyboardColors.StatusGray,
            fontSize = 10.sp
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(KeyboardColors.SuggestionBg)
                .padding(8.dp)
        ) {
            Text(
                text = result.correctedText,
                color = KeyboardColors.CorrectGreen,
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
                Text("Ignore", color = KeyboardColors.StatusGray, fontSize = 12.sp)
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KeyboardColors.Accent
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1E1E3A))
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
                    .background(KeyboardColors.Accent.copy(alpha = 0.15f))
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
            .padding(horizontal = sidePadding, vertical = 2.dp),
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
    bgColor: Color = KeyboardColors.KeyBg,
    fontSize: Int = 18,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .height(48.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isPressed) KeyboardColors.KeyPressed else bgColor)
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
            color = KeyboardColors.KeyText,
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
    bgColor: Color = KeyboardColors.KeyBg,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .weight(weight)
            .height(48.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = KeyboardColors.KeyText, fontSize = 16.sp)
    }
}