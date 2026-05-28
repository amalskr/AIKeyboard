package com.ceylonapz.aikeyboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import kotlinx.coroutines.delay

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
    val EmojiRowBg: Color,
    val SpecialKeyBg: Color
)

private val DarkKeyboardColors = KeyboardColorScheme(
    Bg = Color(0xFF1B2A1B),
    KeyBg = Color(0xFF2C3D2C),
    KeyPressed = Color(0xFF3D4F3D),
    KeyText = Color(0xFFEAEEE3),
    Accent = Color(0xFF8FAA7A),
    SuggestionBg = Color(0xFF1B2A1B),
    CorrectGreen = Color(0xFF8BC78F),
    ErrorBg = Color(0xFF3A1F1F),
    StatusGray = Color(0xFFA8B5A0),
    DimText = Color(0xFF7A8A6A),
    EmojiRowBg = Color(0xFF1B2A1B),
    SpecialKeyBg = Color(0xFF3D4F3D)
)

private val LightKeyboardColors = KeyboardColorScheme(
    Bg = Color(0xFFDDE7D2),
    KeyBg = Color(0xFFFFFFFF),
    KeyPressed = Color(0xFFC2D2B4),
    KeyText = Color(0xFF1A1A1A),
    Accent = Color(0xFF6F8B5C),
    SuggestionBg = Color(0xFFDDE7D2),
    CorrectGreen = Color(0xFF2E7D32),
    ErrorBg = Color(0xFFFDE8E8),
    StatusGray = Color(0xFF4F5F40),
    DimText = Color(0xFF7A8A6A),
    EmojiRowBg = Color(0xFFDDE7D2),
    SpecialKeyBg = Color(0xFFCFDDC1)
)

val LocalKeyboardColors = staticCompositionLocalOf { DarkKeyboardColors }

object KeyboardColors {
    val current: KeyboardColorScheme
        @Composable get() = LocalKeyboardColors.current
}

// ── Layout Data ──────────────────────────────────────────
val NUMBER_ROW = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
val ROW1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
val ROW2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
val ROW3 = listOf("z", "x", "c", "v", "b", "n", "m")

// Symbol page 1
val SYMBOL_ROW1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
val SYMBOL_ROW2 = listOf("-", "/", ":", ";", "(", ")", "\$", "&", "@", "\"")
val SYMBOL_ROW3 = listOf(".", ",", "?", "!", "'")

// Symbol page 2
val SYMBOL2_ROW1 = listOf("[", "]", "{", "}", "#", "%", "^", "*", "+", "=")
val SYMBOL2_ROW2 = listOf("_", "\\", "|", "~", "<", ">", "€", "£", "¥", "•")
val SYMBOL2_ROW3 = listOf(".", ",", "?", "!", "'")

// ── Emoji Data ──────────────────────────────────────────
data class EmojiCategory(val icon: String, val name: String, val emojis: List<String>)

val EMOJI_CATEGORIES = listOf(
    EmojiCategory("😀", "Smileys", listOf(
        "😀", "😃", "😄", "😁", "😆",
        "😅", "😂", "🤣", "😊", "😇",
        "🙂", "🙃", "😉", "😌", "😍",
        "🥰", "😘", "😗", "😙", "😚",
        "😋", "😛", "😜", "🤪", "😝",
        "🤑", "🤗", "🤭", "🤫", "🤔",
        "🤐", "🤨", "😐", "😑", "😶",
        "😏", "😒", "🙄", "😬", "🤥",
        "😔", "😞", "😟", "😕", "🙁",
        "😣", "😖", "😫", "😩", "🥺",
        "😢", "😭", "😤", "😠", "😡",
        "🤬", "😱", "😨", "😰", "😥"
    )),
    EmojiCategory("❤️", "Love", listOf(
        "❤️", "🧡", "💛", "💚", "💙",
        "💜", "🖤", "💔", "❣️", "💕",
        "💞", "💓", "💗", "💖", "💘",
        "💝", "💋", "💌", "💐", "🌹",
        "🌺", "🌻", "🌷", "🌸", "🌼"
    )),
    EmojiCategory("👋", "Hands", listOf(
        "👋", "🤚", "🖐️", "✋", "🖖",
        "👌", "🤏", "✌️", "🤞", "🤟",
        "🤘", "🤙", "👈", "👉", "👆",
        "👇", "☝️", "👍", "👎", "✊",
        "👊", "🤛", "🤜", "👏", "🙌",
        "👐", "🤲", "🤝", "🙏", "✍️",
        "💅", "🤳", "💪"
    )),
    EmojiCategory("🚀", "Travel", listOf(
        "🚀", "✈️", "🚗", "🚕", "🚌",
        "🚎", "🚂", "🚆", "🚈", "🚝",
        "🚲", "🛵", "🚗", "🏠", "🏢",
        "🏖️", "🏔️", "🌅", "🌄",
        "🌇", "🌆", "🏙️", "🌃", "🌉",
        "🗼", "🗽", "🏟️", "🏡"
    )),
    EmojiCategory("🍔", "Food", listOf(
        "🍔", "🍕", "🌮", "🌯", "🍳",
        "🥞", "🥓", "🥩", "🍗", "🍖",
        "🌭", "🍟", "🥪", "🍞", "🧀",
        "🥚", "🍝", "🍜", "🍲", "🥘",
        "🍰", "🎂", "🍩", "🍪", "🍫",
        "🍬", "🍭", "🍮", "🍯", "☕",
        "🍵", "🍺", "🍷", "🥤", "🧃"
    )),
    EmojiCategory("⚽", "Activities", listOf(
        "⚽", "🏀", "🏈", "⚾", "🥎",
        "🎾", "🏐", "🏉", "🥏", "🎱",
        "🏓", "🏸", "🥊", "🥋", "⛳",
        "🏇", "🏊", "🏄", "🎿", "⛷️",
        "🎯", "🎣", "🎮", "🎲", "🎰",
        "🎳", "🎭", "🎨", "🎵", "🎶"
    )),
    EmojiCategory("🐶", "Animals", listOf(
        "🐶", "🐱", "🐭", "🐹", "🐰",
        "🦊", "🐻", "🐼", "🐨", "🐯",
        "🦁", "🐮", "🐷", "🐸", "🐵",
        "🐔", "🐧", "🐦", "🦅", "🦆",
        "🦉", "🐝", "🐛", "🦋", "🐌",
        "🐢", "🐍", "🦎", "🐙", "🐠",
        "🐡", "🐬", "🐳", "🦈", "🐘"
    )),
    EmojiCategory("💡", "Objects", listOf(
        "💡", "🔦", "💻", "📱", "📷",
        "🎥", "📺", "📻", "⏰", "⌚",
        "📧", "📄", "📚", "✏️", "📝",
        "🔑", "🔒", "🔓", "🚨", "🔔",
        "🏆", "🎁", "🎈", "🎉", "🎊",
        "⭐", "🌟", "🔥", "🌈", "☀️",
        "🌤️", "⛅", "🌧️", "⚡", "❄️"
    ))
)

// ── Main Compose Keyboard ──────────────────────────────────
@Composable
fun ComposeKeyboard(
    viewModel: KeyboardViewModel,
    onCommitText: (String) -> Unit,
    onDeleteOne: () -> Unit,
    onSendEnter: () -> Unit,
    onDeleteSurrounding: (Int) -> Unit,
    onLanguageSwitch: () -> Unit
) {
    val colors = if (isSystemInDarkTheme()) DarkKeyboardColors else LightKeyboardColors

    CompositionLocalProvider(LocalKeyboardColors provides colors) {
        ComposeKeyboardContent(
            viewModel, onCommitText, onDeleteOne, onSendEnter,
            onDeleteSurrounding, onLanguageSwitch
        )
    }
}

@Composable
private fun ComposeKeyboardContent(
    viewModel: KeyboardViewModel,
    onCommitText: (String) -> Unit,
    onDeleteOne: () -> Unit,
    onSendEnter: () -> Unit,
    onDeleteSurrounding: (Int) -> Unit,
    onLanguageSwitch: () -> Unit
) {
    val colors = KeyboardColors.current
    val result by viewModel.grammarResult
    val status by viewModel.statusMessage
    val isChecking by viewModel.isChecking
    val keyboardMode by viewModel.keyboardMode
    val emojis by viewModel.emojiSuggestions
    val words by viewModel.wordSuggestions

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.Bg)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        // ── Gboard-style Top Bar (always visible in QWERTY/Symbols) ──
        if (keyboardMode != KeyboardMode.EMOJI) {
            GboardTopBar(
                words = words,
                emojis = emojis,
                isChecking = isChecking,
                onWordSelected = { word ->
                    viewModel.onWordSelected(word, onCommitText)
                },
                onEmojiSelected = { emoji ->
                    viewModel.onEmojiSelected(emoji, onCommitText)
                },
                onAiTapped = { viewModel.onGrammarCheckTapped() },
                onTap = { viewModel.vibrateKey() }
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
        if (keyboardMode != KeyboardMode.EMOJI) {
            AnimatedVisibility(
                visible = status.isNotEmpty() && (result == null || !result!!.is_error)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
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
        }

        // ── Mode-specific layouts ───────────────────────────
        when (keyboardMode) {
            KeyboardMode.QWERTY -> QwertyLayout(viewModel, onCommitText, onDeleteOne, onSendEnter, onLanguageSwitch)
            KeyboardMode.SYMBOLS_1 -> SymbolLayout(viewModel, page = 1, onCommitText, onDeleteOne, onSendEnter, onLanguageSwitch)
            KeyboardMode.SYMBOLS_2 -> SymbolLayout(viewModel, page = 2, onCommitText, onDeleteOne, onSendEnter, onLanguageSwitch)
            KeyboardMode.EMOJI -> EmojiPickerLayout(viewModel, onCommitText, onDeleteOne)
        }
    }
}

// ── QWERTY Layout ────────────────────────────────────────
@Composable
private fun QwertyLayout(
    viewModel: KeyboardViewModel,
    onCommitText: (String) -> Unit,
    onDeleteOne: () -> Unit,
    onSendEnter: () -> Unit,
    onLanguageSwitch: () -> Unit
) {
    val colors = KeyboardColors.current
    val isShift by viewModel.isShiftOn

    // Row 0: 1234567890
    KeyRow(NUMBER_ROW, isShift = false) { ch ->
        viewModel.onSymbolTyped(ch.toString(), onCommitText)
    }

    // Row 1: QWERTYUIOP
    KeyRow(ROW1, isShift) { viewModel.onCharTyped(it, onCommitText) }

    // Row 2: ASDFGHJKL
    KeyRow(ROW2, isShift, sidePadding = 18.dp) {
        viewModel.onCharTyped(it, onCommitText)
    }

    // Row 3: Shift + ZXCVBNM + Backspace
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        SpecialKey(
            label = if (isShift) "⇧" else "⇪",
            weight = 1.4f,
            bgColor = if (isShift) colors.Accent else colors.SpecialKeyBg
        ) { viewModel.onShiftToggle() }

        ROW3.forEach { key ->
            KeyButton(
                label = if (isShift) key.uppercase() else key,
                modifier = Modifier.weight(1f)
            ) { viewModel.onCharTyped(key[0], onCommitText) }
        }

        RepeatableSpecialKey(
            label = "⌫",
            weight = 1.4f,
            bgColor = colors.SpecialKeyBg
        ) { viewModel.onDeleteTyped(onDeleteOne) }
    }

    // Row 4: ?123 | emoji | globe | space | . | enter
    BottomRow(
        viewModel = viewModel,
        onCommitText = onCommitText,
        onSendEnter = onSendEnter,
        onLanguageSwitch = onLanguageSwitch,
        leftLabel = "?123",
        onLeftKey = { viewModel.switchToSymbols() }
    )
}

// ── Symbol Layout ────────────────────────────────────────
@Composable
private fun SymbolLayout(
    viewModel: KeyboardViewModel,
    page: Int,
    onCommitText: (String) -> Unit,
    onDeleteOne: () -> Unit,
    onSendEnter: () -> Unit,
    onLanguageSwitch: () -> Unit
) {
    val colors = KeyboardColors.current
    val row1 = if (page == 1) SYMBOL_ROW1 else SYMBOL2_ROW1
    val row2 = if (page == 1) SYMBOL_ROW2 else SYMBOL2_ROW2
    val row3 = if (page == 1) SYMBOL_ROW3 else SYMBOL2_ROW3

    // Row 1
    SymbolKeyRow(row1) { viewModel.onSymbolTyped(it, onCommitText) }

    // Row 2
    SymbolKeyRow(row2) { viewModel.onSymbolTyped(it, onCommitText) }

    // Row 3: page toggle + symbols + backspace
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        SpecialKey(
            label = if (page == 1) "#+=​" else "123",
            weight = 1.4f,
            bgColor = colors.SpecialKeyBg
        ) { viewModel.toggleSymbolPage() }

        row3.forEach { sym ->
            KeyButton(
                label = sym,
                modifier = Modifier.weight(1f),
                fontSize = 18
            ) { viewModel.onSymbolTyped(sym, onCommitText) }
        }

        RepeatableSpecialKey(
            label = "⌫",
            weight = 1.4f,
            bgColor = colors.SpecialKeyBg
        ) { viewModel.onDeleteTyped(onDeleteOne) }
    }

    // Row 4: ABC | emoji | globe | space | . | enter
    BottomRow(
        viewModel = viewModel,
        onCommitText = onCommitText,
        onSendEnter = onSendEnter,
        onLanguageSwitch = onLanguageSwitch,
        leftLabel = "ABC",
        onLeftKey = { viewModel.switchToQwerty() }
    )
}

// ── Shared Bottom Row ────────────────────────────────────
@Composable
private fun BottomRow(
    viewModel: KeyboardViewModel,
    onCommitText: (String) -> Unit,
    onSendEnter: () -> Unit,
    onLanguageSwitch: () -> Unit,
    leftLabel: String,
    onLeftKey: () -> Unit
) {
    val colors = KeyboardColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ?123 or ABC
        SpecialKey(
            label = leftLabel,
            weight = 1.4f,
            bgColor = colors.SpecialKeyBg
        ) { onLeftKey() }

        // Emoji
        SpecialKey(
            label = "🙂",
            weight = 0.9f,
            bgColor = colors.SpecialKeyBg
        ) { viewModel.switchToEmoji() }

        // Globe (language switch)
        SpecialKey(
            label = "🌐",
            weight = 0.9f,
            bgColor = colors.SpecialKeyBg
        ) { viewModel.onLanguageSwitchTapped(onLanguageSwitch) }

        // Space bar
        Box(
            modifier = Modifier
                .weight(4.0f)
                .height(54.dp)
                .padding(horizontal = 3.dp, vertical = 3.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.KeyBg)
                .clickable { viewModel.onSpaceTyped(onCommitText) },
            contentAlignment = Alignment.Center
        ) {
            Text("English", color = colors.DimText, fontSize = 14.sp)
        }

        // Period
        SpecialKey(
            label = ".",
            weight = 0.9f,
            bgColor = colors.SpecialKeyBg,
            fontSize = 20
        ) { viewModel.onPeriodTyped(onCommitText) }

        // Enter
        SpecialKey(
            label = "↵",
            weight = 1.4f,
            bgColor = colors.SpecialKeyBg,
            fontSize = 20
        ) { viewModel.onEnter(onSendEnter) }
    }
}

// ── Emoji Picker Layout ──────────────────────────────────
@Composable
private fun EmojiPickerLayout(
    viewModel: KeyboardViewModel,
    onCommitText: (String) -> Unit,
    onDeleteOne: () -> Unit
) {
    val colors = KeyboardColors.current
    var selectedCategory by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        // Category tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.EmojiRowBg)
                .padding(vertical = 4.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            itemsIndexed(EMOJI_CATEGORIES) { index, category ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (index == selectedCategory) colors.Accent.copy(alpha = 0.3f)
                            else Color.Transparent
                        )
                        .clickable {
                            viewModel.vibrateKey()
                            selectedCategory = index
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(category.icon, fontSize = 20.sp)
                }
            }
        }

        // Category name
        Text(
            text = EMOJI_CATEGORIES[selectedCategory].name,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = colors.StatusGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        // Emoji grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(EMOJI_CATEGORIES[selectedCategory].emojis) { emoji ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            viewModel.onEmojiSelected(emoji, onCommitText)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 24.sp)
                }
            }
        }

        // Bottom bar: ABC + Backspace
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.Bg)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.SpecialKeyBg)
                    .clickable { viewModel.switchToQwerty() }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("ABC", color = colors.KeyText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.weight(1f))

            RepeatableBoxKey(
                label = "⌫",
                bgColor = colors.SpecialKeyBg
            ) { viewModel.onDeleteTyped(onDeleteOne) }
        }
    }
}

// ── Grammar Suggestion Bar ───────────────────────────────
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
        Text(text = "Original:", color = colors.StatusGray, fontSize = 10.sp)
        Text(text = result.originalText, color = colors.StatusGray, fontSize = 13.sp)

        Spacer(Modifier.height(6.dp))

        Text(text = "Suggested:", color = colors.StatusGray, fontSize = 10.sp)
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
                colors = ButtonDefaults.buttonColors(containerColor = colors.Accent),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text("Apply Fix ✓", fontSize = 12.sp)
            }
        }
    }
}

// ── Gboard-style Top Suggestion Bar ──────────────────────
@Composable
fun GboardTopBar(
    words: List<String>,
    emojis: List<String>,
    isChecking: Boolean,
    onWordSelected: (String) -> Unit,
    onEmojiSelected: (String) -> Unit,
    onAiTapped: () -> Unit,
    onTap: () -> Unit
) {
    val colors = KeyboardColors.current
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: 4-dot grid icon (apps/menu)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    onTap()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
            contentAlignment = Alignment.Center
        ) {
            FourDotGrid(color = colors.KeyText)
        }

        VerticalDivider(colors.DimText)

        // Middle: suggestions (words + emojis)
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val visibleWords = words.take(2)
            visibleWords.forEachIndexed { index, word ->
                if (index > 0) VerticalDivider(colors.DimText)
                SuggestionChip(text = word, color = colors.KeyText) {
                    onWordSelected(word)
                }
            }
            val visibleEmojis = emojis.take(2)
            visibleEmojis.forEach { emoji ->
                VerticalDivider(colors.DimText)
                SuggestionChip(text = emoji, color = colors.KeyText) {
                    onEmojiSelected(emoji)
                }
            }
        }

        VerticalDivider(colors.DimText)

        // Right: AI Grammar Check button — rectangular white pill
        Box(
            modifier = Modifier
                .padding(start = 4.dp)
                .height(36.dp)
                .widthIn(min = 52.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (isChecking) Color(0xFFCC4444) else colors.KeyBg)
                .clickable {
                    onTap()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onAiTapped()
                }
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isChecking) "■" else "✨",
                fontSize = 18.sp,
                color = if (isChecking) Color.White else colors.KeyText
            )
        }
    }
}

@Composable
private fun FourDotGrid(color: Color) {
    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(2) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.VerticalDivider(color: Color) {
    Box(
        Modifier
            .padding(horizontal = 4.dp)
            .width(1.dp)
            .height(20.dp)
            .background(color.copy(alpha = 0.4f))
    )
}

@Composable
private fun RowScope.SuggestionChip(text: String, color: Color, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .weight(1f, fill = false)
            .padding(horizontal = 6.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── Reusable Key Components ──────────────────────────────

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
fun SymbolKeyRow(keys: List<String>, onKey: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        keys.forEach { sym ->
            KeyButton(
                label = sym,
                modifier = Modifier.weight(1f),
                fontSize = 18
            ) { onKey(sym) }
        }
    }
}

@Composable
fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    bgColor: Color = KeyboardColors.current.KeyBg,
    fontSize: Int = 20,
    onClick: () -> Unit
) {
    val colors = KeyboardColors.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .height(54.dp)
            .padding(3.dp)
            .clip(RoundedCornerShape(10.dp))
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
    bgColor: Color = KeyboardColors.current.SpecialKeyBg,
    fontSize: Int = 16,
    onClick: () -> Unit
) {
    val colors = KeyboardColors.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .weight(weight)
            .height(54.dp)
            .padding(3.dp)
            .clip(RoundedCornerShape(10.dp))
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
        Text(label, color = colors.KeyText, fontSize = fontSize.sp)
    }
}

@Composable
fun RowScope.RepeatableSpecialKey(
    label: String,
    weight: Float,
    bgColor: Color = KeyboardColors.current.SpecialKeyBg,
    initialDelay: Long = 400L,
    repeatInterval: Long = 50L,
    onAction: () -> Unit
) {
    val colors = KeyboardColors.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(initialDelay)
            while (true) {
                onAction()
                delay(repeatInterval)
            }
        }
    }

    Box(
        modifier = Modifier
            .weight(weight)
            .height(54.dp)
            .padding(3.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isPressed) colors.KeyPressed else bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onAction()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = colors.KeyText, fontSize = 16.sp)
    }
}

@Composable
fun RepeatableBoxKey(
    label: String,
    bgColor: Color = KeyboardColors.current.SpecialKeyBg,
    initialDelay: Long = 400L,
    repeatInterval: Long = 50L,
    onAction: () -> Unit
) {
    val colors = KeyboardColors.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(initialDelay)
            while (true) {
                onAction()
                delay(repeatInterval)
            }
        }
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPressed) colors.KeyPressed else bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onAction()
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = colors.KeyText, fontSize = 18.sp)
    }
}
