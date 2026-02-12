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
    EmojiRowBg = Color(0xFF1E1E3A),
    SpecialKeyBg = Color(0xFF1E2A4A)
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
    EmojiRowBg = Color(0xFFDDDDE8),
    SpecialKeyBg = Color(0xFFCBCBD6)
)

val LocalKeyboardColors = staticCompositionLocalOf { DarkKeyboardColors }

object KeyboardColors {
    val current: KeyboardColorScheme
        @Composable get() = LocalKeyboardColors.current
}

// ── Layout Data ──────────────────────────────────────────
val ROW1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
val ROW2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
val ROW3 = listOf("z", "x", "c", "v", "b", "n", "m")

// Symbol page 1
val SYMBOL_ROW1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
val SYMBOL_ROW2 = listOf("-", "/", ":", ";", "(", ")", "\$", "&", "@", "\"")
val SYMBOL_ROW3 = listOf(".", ",", "?", "!", "'")

// Symbol page 2
val SYMBOL2_ROW1 = listOf("[", "]", "{", "}", "#", "%", "^", "*", "+", "=")
val SYMBOL2_ROW2 = listOf("_", "\\", "|", "~", "<", ">", "\u20AC", "\u00A3", "\u00A5", "\u2022")
val SYMBOL2_ROW3 = listOf(".", ",", "?", "!", "'")

// ── Emoji Data ──────────────────────────────────────────
data class EmojiCategory(val icon: String, val name: String, val emojis: List<String>)

val EMOJI_CATEGORIES = listOf(
    EmojiCategory("\uD83D\uDE00", "Smileys", listOf(
        "\uD83D\uDE00", "\uD83D\uDE03", "\uD83D\uDE04", "\uD83D\uDE01", "\uD83D\uDE06",
        "\uD83D\uDE05", "\uD83D\uDE02", "\uD83E\uDD23", "\uD83D\uDE0A", "\uD83D\uDE07",
        "\uD83D\uDE42", "\uD83D\uDE43", "\uD83D\uDE09", "\uD83D\uDE0C", "\uD83D\uDE0D",
        "\uD83E\uDD70", "\uD83D\uDE18", "\uD83D\uDE17", "\uD83D\uDE19", "\uD83D\uDE1A",
        "\uD83D\uDE0B", "\uD83D\uDE1B", "\uD83D\uDE1C", "\uD83E\uDD2A", "\uD83D\uDE1D",
        "\uD83E\uDD11", "\uD83E\uDD17", "\uD83E\uDD2D", "\uD83E\uDD2B", "\uD83E\uDD14",
        "\uD83E\uDD10", "\uD83E\uDD28", "\uD83D\uDE10", "\uD83D\uDE11", "\uD83D\uDE36",
        "\uD83D\uDE0F", "\uD83D\uDE12", "\uD83D\uDE44", "\uD83D\uDE2C", "\uD83E\uDD25",
        "\uD83D\uDE14", "\uD83D\uDE1E", "\uD83D\uDE1F", "\uD83D\uDE15", "\uD83D\uDE41",
        "\uD83D\uDE23", "\uD83D\uDE16", "\uD83D\uDE2B", "\uD83D\uDE29", "\uD83E\uDD7A",
        "\uD83D\uDE22", "\uD83D\uDE2D", "\uD83D\uDE24", "\uD83D\uDE20", "\uD83D\uDE21",
        "\uD83E\uDD2C", "\uD83D\uDE31", "\uD83D\uDE28", "\uD83D\uDE30", "\uD83D\uDE25"
    )),
    EmojiCategory("\u2764\uFE0F", "Love", listOf(
        "\u2764\uFE0F", "\uD83E\uDDE1", "\uD83D\uDC9B", "\uD83D\uDC9A", "\uD83D\uDC99",
        "\uD83D\uDC9C", "\uD83D\uDDA4", "\uD83D\uDC94", "\u2763\uFE0F", "\uD83D\uDC95",
        "\uD83D\uDC9E", "\uD83D\uDC93", "\uD83D\uDC97", "\uD83D\uDC96", "\uD83D\uDC98",
        "\uD83D\uDC9D", "\uD83D\uDC8B", "\uD83D\uDC8C", "\uD83D\uDC90", "\uD83C\uDF39",
        "\uD83C\uDF3A", "\uD83C\uDF3B", "\uD83C\uDF37", "\uD83C\uDF38", "\uD83C\uDF3C"
    )),
    EmojiCategory("\uD83D\uDC4B", "Hands", listOf(
        "\uD83D\uDC4B", "\uD83E\uDD1A", "\uD83D\uDD90\uFE0F", "\u270B", "\uD83D\uDD96",
        "\uD83D\uDC4C", "\uD83E\uDD0F", "\u270C\uFE0F", "\uD83E\uDD1E", "\uD83E\uDD1F",
        "\uD83E\uDD18", "\uD83E\uDD19", "\uD83D\uDC48", "\uD83D\uDC49", "\uD83D\uDC46",
        "\uD83D\uDC47", "\u261D\uFE0F", "\uD83D\uDC4D", "\uD83D\uDC4E", "\u270A",
        "\uD83D\uDC4A", "\uD83E\uDD1B", "\uD83E\uDD1C", "\uD83D\uDC4F", "\uD83D\uDE4C",
        "\uD83D\uDC50", "\uD83E\uDD32", "\uD83E\uDD1D", "\uD83D\uDE4F", "\u270D\uFE0F",
        "\uD83D\uDC85", "\uD83E\uDD33", "\uD83D\uDCAA"
    )),
    EmojiCategory("\uD83D\uDE80", "Travel", listOf(
        "\uD83D\uDE80", "\u2708\uFE0F", "\uD83D\uDE97", "\uD83D\uDE95", "\uD83D\uDE8C",
        "\uD83D\uDE8E", "\uD83D\uDE82", "\uD83D\uDE86", "\uD83D\uDE88", "\uD83D\uDE9D",
        "\uD83D\uDEB2", "\uD83D\uDEF5", "\uD83D\uDE97", "\uD83C\uDFE0", "\uD83C\uDFE2",
        "\uD83C\uDFD6\uFE0F", "\uD83C\uDFD4\uFE0F", "\uD83C\uDF05", "\uD83C\uDF04",
        "\uD83C\uDF07", "\uD83C\uDF06", "\uD83C\uDFD9\uFE0F", "\uD83C\uDF03", "\uD83C\uDF09",
        "\uD83D\uDDFC", "\uD83D\uDDFD", "\uD83C\uDFDF\uFE0F", "\uD83C\uDFE1"
    )),
    EmojiCategory("\uD83C\uDF54", "Food", listOf(
        "\uD83C\uDF54", "\uD83C\uDF55", "\uD83C\uDF2E", "\uD83C\uDF2F", "\uD83C\uDF73",
        "\uD83E\uDD5E", "\uD83E\uDD53", "\uD83E\uDD69", "\uD83C\uDF57", "\uD83C\uDF56",
        "\uD83C\uDF2D", "\uD83C\uDF5F", "\uD83E\uDD6A", "\uD83C\uDF5E", "\uD83E\uDDC0",
        "\uD83E\uDD5A", "\uD83C\uDF5D", "\uD83C\uDF5C", "\uD83C\uDF72", "\uD83E\uDD58",
        "\uD83C\uDF70", "\uD83C\uDF82", "\uD83C\uDF69", "\uD83C\uDF6A", "\uD83C\uDF6B",
        "\uD83C\uDF6C", "\uD83C\uDF6D", "\uD83C\uDF6E", "\uD83C\uDF6F", "\u2615",
        "\uD83C\uDF75", "\uD83C\uDF7A", "\uD83C\uDF77", "\uD83E\uDD64", "\uD83E\uDDC3"
    )),
    EmojiCategory("\u26BD", "Activities", listOf(
        "\u26BD", "\uD83C\uDFC0", "\uD83C\uDFC8", "\u26BE", "\uD83E\uDD4E",
        "\uD83C\uDFBE", "\uD83C\uDFD0", "\uD83C\uDFC9", "\uD83E\uDD4F", "\uD83C\uDFB1",
        "\uD83C\uDFD3", "\uD83C\uDFF8", "\uD83E\uDD4A", "\uD83E\uDD4B", "\u26F3",
        "\uD83C\uDFC7", "\uD83C\uDFCA", "\uD83C\uDFC4", "\uD83C\uDFBF", "\u26F7\uFE0F",
        "\uD83C\uDFAF", "\uD83C\uDFA3", "\uD83C\uDFAE", "\uD83C\uDFB2", "\uD83C\uDFB0",
        "\uD83C\uDFB3", "\uD83C\uDFAD", "\uD83C\uDFA8", "\uD83C\uDFB5", "\uD83C\uDFB6"
    )),
    EmojiCategory("\uD83D\uDC36", "Animals", listOf(
        "\uD83D\uDC36", "\uD83D\uDC31", "\uD83D\uDC2D", "\uD83D\uDC39", "\uD83D\uDC30",
        "\uD83E\uDD8A", "\uD83D\uDC3B", "\uD83D\uDC3C", "\uD83D\uDC28", "\uD83D\uDC2F",
        "\uD83E\uDD81", "\uD83D\uDC2E", "\uD83D\uDC37", "\uD83D\uDC38", "\uD83D\uDC35",
        "\uD83D\uDC14", "\uD83D\uDC27", "\uD83D\uDC26", "\uD83E\uDD85", "\uD83E\uDD86",
        "\uD83E\uDD89", "\uD83D\uDC1D", "\uD83D\uDC1B", "\uD83E\uDD8B", "\uD83D\uDC0C",
        "\uD83D\uDC22", "\uD83D\uDC0D", "\uD83E\uDD8E", "\uD83D\uDC19", "\uD83D\uDC20",
        "\uD83D\uDC21", "\uD83D\uDC2C", "\uD83D\uDC33", "\uD83E\uDD88", "\uD83D\uDC18"
    )),
    EmojiCategory("\uD83D\uDCA1", "Objects", listOf(
        "\uD83D\uDCA1", "\uD83D\uDD26", "\uD83D\uDCBB", "\uD83D\uDCF1", "\uD83D\uDCF7",
        "\uD83C\uDFA5", "\uD83D\uDCFA", "\uD83D\uDCFB", "\u23F0", "\u231A",
        "\uD83D\uDCE7", "\uD83D\uDCC4", "\uD83D\uDCDA", "\u270F\uFE0F", "\uD83D\uDCDD",
        "\uD83D\uDD11", "\uD83D\uDD12", "\uD83D\uDD13", "\uD83D\uDEA8", "\uD83D\uDD14",
        "\uD83C\uDFC6", "\uD83C\uDF81", "\uD83C\uDF88", "\uD83C\uDF89", "\uD83C\uDF8A",
        "\u2B50", "\uD83C\uDF1F", "\uD83D\uDD25", "\uD83C\uDF08", "\u2600\uFE0F",
        "\uD83C\uDF24\uFE0F", "\u26C5", "\uD83C\uDF27\uFE0F", "\u26A1", "\u2744\uFE0F"
    ))
)

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
    val keyboardMode by viewModel.keyboardMode
    val emojis by viewModel.emojiSuggestions

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.Bg)
            .padding(horizontal = 3.dp, vertical = 4.dp)
    ) {
        // ── Emoji Suggestion Row ────────────────────────────
        if (keyboardMode != KeyboardMode.EMOJI) {
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
                        color = if (status.contains("\u2705")) colors.CorrectGreen
                        else colors.StatusGray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(2.dp))

        // ── Mode-specific layouts ───────────────────────────
        when (keyboardMode) {
            KeyboardMode.QWERTY -> QwertyLayout(viewModel, onCommitText, onDeleteOne, onSendEnter)
            KeyboardMode.SYMBOLS_1 -> SymbolLayout(viewModel, page = 1, onCommitText, onDeleteOne, onSendEnter)
            KeyboardMode.SYMBOLS_2 -> SymbolLayout(viewModel, page = 2, onCommitText, onDeleteOne, onSendEnter)
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
    onSendEnter: () -> Unit
) {
    val colors = KeyboardColors.current
    val isShift by viewModel.isShiftOn

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
            label = if (isShift) "\u21E7" else "\u21EA",
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
            label = "\u232B",
            weight = 1.4f,
            bgColor = colors.SpecialKeyBg
        ) { viewModel.onDeleteTyped(onDeleteOne) }
    }

    // Row 4: ?123 | emoji | space | . | enter
    BottomRow(
        viewModel = viewModel,
        onCommitText = onCommitText,
        onSendEnter = onSendEnter,
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
    onSendEnter: () -> Unit
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
            label = if (page == 1) "#+=\u200B" else "123",
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
            label = "\u232B",
            weight = 1.4f,
            bgColor = colors.SpecialKeyBg
        ) { viewModel.onDeleteTyped(onDeleteOne) }
    }

    // Row 4: ABC | emoji | space | . | enter
    BottomRow(
        viewModel = viewModel,
        onCommitText = onCommitText,
        onSendEnter = onSendEnter,
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
    leftLabel: String,
    onLeftKey: () -> Unit
) {
    val colors = KeyboardColors.current
    val isChecking by viewModel.isChecking

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        // ?123 or ABC
        SpecialKey(
            label = leftLabel,
            weight = 1.2f,
            bgColor = colors.SpecialKeyBg
        ) { onLeftKey() }

        // Emoji
        SpecialKey(
            label = "\uD83D\uDE0A",
            weight = 0.9f,
            bgColor = colors.SpecialKeyBg
        ) { viewModel.switchToEmoji() }

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
            Text("English", color = colors.DimText, fontSize = 13.sp)
        }

        // Period
        KeyButton(
            label = ".",
            modifier = Modifier.weight(0.8f),
            fontSize = 22
        ) { viewModel.onPeriodTyped(onCommitText) }

        // AI Grammar Check
        SpecialKey(
            label = if (isChecking) "\u25A0" else "\uD83C\uDF10",
            weight = 1.2f,
            bgColor = if (isChecking) Color(0xFFCC4444) else colors.Accent
        ) { viewModel.onGrammarCheckTapped() }
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
                        .clickable { selectedCategory = index },
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
                label = "\u232B",
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
                Text("Apply Fix \u2713", fontSize = 12.sp)
            }
        }
    }
}

// ── Emoji Suggestion Row ─────────────────────────────────
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
                Text(text = emoji, fontSize = 16.sp)
            }
        }
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
    bgColor: Color = KeyboardColors.current.SpecialKeyBg,
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
            .height(52.dp)
            .padding(3.dp)
            .clip(RoundedCornerShape(6.dp))
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
