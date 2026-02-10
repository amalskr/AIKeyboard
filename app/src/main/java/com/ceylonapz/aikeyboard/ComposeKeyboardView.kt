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
import androidx.compose.ui.text.style.TextDecoration
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
    val AccentLight = Color(0xFF9D84FF)
    val SuggestionBg = Color(0xFF1A2E1A)
    val SuggestionBorder = Color(0xFF4CAF50)
    val CorrectGreen = Color(0xFF66BB6A)
    val ErrorRed = Color(0xFFEF5350)
    val ErrorBg = Color(0xFF2E1A1A)
    val StatusGray = Color(0xFF888899)
    val DimText = Color(0xFF555566)
}

val ROW1 = listOf("q","w","e","r","t","y","u","i","o","p")
val ROW2 = listOf("a","s","d","f","g","h","j","k","l")
val ROW3 = listOf("z","x","c","v","b","n","m")

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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KeyboardColors.Bg)
            .padding(horizontal = 3.dp, vertical = 4.dp)
    ) {
        // ── Grammar Suggestion Bar ─────────────────────────
        AnimatedVisibility(
            visible = result != null && result!!.hasErrors,
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
            visible = status.isNotEmpty() && (result == null || !result!!.hasErrors)
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

        // ── Row 4: Numbers row / punctuation ───────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // Comma
            KeyButton(
                label = ",",
                modifier = Modifier.weight(1f),
                fontSize = 20
            ) { onCommitText(","); viewModel.sentenceBuffer.append(",") }

            // Space bar
            Box(
                modifier = Modifier
                    .weight(4.5f)
                    .height(48.dp)
                    .padding(horizontal = 2.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(KeyboardColors.KeyBg)
                    .clickable { viewModel.onSpaceTyped(onCommitText) },
                contentAlignment = Alignment.Center
            ) {
                Text("space", color = KeyboardColors.DimText, fontSize = 13.sp)
            }

            // Period — triggers grammar check
            KeyButton(
                label = ".",
                modifier = Modifier.weight(1f),
                bgColor = KeyboardColors.Accent.copy(alpha = 0.25f),
                fontSize = 22
            ) { viewModel.onPeriodTyped(onCommitText) }

            // Question mark — also triggers grammar check
            KeyButton(
                label = "?",
                modifier = Modifier.weight(1f),
                bgColor = KeyboardColors.Accent.copy(alpha = 0.15f),
                fontSize = 20
            ) { viewModel.onQuestionMarkTyped(onCommitText) }

            // Exclamation — also triggers grammar check
            KeyButton(
                label = "!",
                modifier = Modifier.weight(1f),
                bgColor = KeyboardColors.Accent.copy(alpha = 0.15f),
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

// ── Grammar Suggestion Bar ─────────────────────────────────
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
        // Error list
        result.errors.forEach { err ->
            Row(
                modifier = Modifier.padding(vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Error type badge
                Text(
                    text = err.type.uppercase(),
                    color = KeyboardColors.Accent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(KeyboardColors.Accent.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                )
                Spacer(Modifier.width(6.dp))

                // "original" → "fixed"
                Text(
                    text = "${err.original}",
                    color = KeyboardColors.ErrorRed,
                    fontSize = 12.sp,
                    textDecoration = TextDecoration.LineThrough
                )
                Text(
                    text = " → ${err.fixed}",
                    color = KeyboardColors.CorrectGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Explanation
            if (err.explanation.isNotBlank()) {
                Text(
                    text = err.explanation,
                    color = KeyboardColors.StatusGray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Corrected sentence preview
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