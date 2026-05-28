package com.ceylonapz.aikeyboard

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ceylonapz.aikeyboard.ui.theme.AIKeyboardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AIKeyboardTheme {
                SetupScreen()
            }
        }
    }
}

@Composable
fun SetupScreen() {
    val ctx = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val isEnabled = remember { mutableStateOf(false) }
    val isSelected = remember { mutableStateOf(false) }

    val bgGradient = if (isDark) {
        listOf(Color(0xFF0F0F23), Color(0xFF1A1A3E))
    } else {
        listOf(Color(0xFFF0F0F8), Color(0xFFE8E8F0))
    }
    val titleColor = if (isDark) Color.White else Color(0xFF1A1A2E)
    val subtitleColor = if (isDark) Color(0xFF888899) else Color(0xFF666677)
    val accent = Color(0xFF7C5CFC)

    // Check keyboard status when screen resumes
    LaunchedEffect(Unit) {
        refreshKeyboardStatus(ctx, isEnabled, isSelected)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(bgGradient))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo / Icon — adaptive launcher (background + oversized foreground, clipped to circle)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E6DFC)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.mipmap.ic_launcher_foreground),
                    contentDescription = "AI Grammar Keyboard icon",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(220.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "AI Grammar Keyboard",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Text(
                "By Gemini",
                fontSize = 14.sp,
                color = subtitleColor
            )
            Text(
                "v${BuildConfig.VERSION_NAME}",
                fontSize = 12.sp,
                color = subtitleColor
            )

            Spacer(Modifier.height(32.dp))

            // ── Usage card (top of feed) ───────────────────
            UsageCard(isDark = isDark)

            Spacer(Modifier.height(24.dp))

            // ── Section divider ────────────────────────────
            SectionDivider(label = "Keyboard Settings", isDark = isDark)

            Spacer(Modifier.height(20.dp))

            // ── Step 1: Enable keyboard ────────────────────
            SetupStep(
                stepNumber = 1,
                title = "Enable Keyboard",
                description = "Turn on AI Grammar Keyboard in system settings",
                isDone = isEnabled.value,
                buttonText = if (isEnabled.value) "Enabled ✓" else "Open Settings",
                isDark = isDark,
                onClick = {
                    try {
                        ctx.startActivity(
                            Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(
                            ctx,
                            "Couldn't open keyboard settings on this device.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            // ── Step 2: Select keyboard ────────────────────
            SetupStep(
                stepNumber = 2,
                title = "Switch Keyboard",
                description = "Set AI Grammar Keyboard as your default",
                isDone = isSelected.value,
                buttonText = if (isSelected.value) "Selected ✓" else "Choose Keyboard",
                enabled = isEnabled.value,
                isDark = isDark,
                onClick = {
                    val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE)
                            as? InputMethodManager
                    if (imm != null) {
                        imm.showInputMethodPicker()
                    } else {
                        Toast.makeText(
                            ctx,
                            "Keyboard picker not available on this device.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            // ── Step 3: Test it ────────────────────────────
            SetupStep(
                stepNumber = 3,
                title = "Try It Out",
                description = "Type a sentence with errors and tap Send to test the grammar checker",
                isDone = false,
                buttonText = "Open any app and type!",
                enabled = isSelected.value,
                isDark = isDark,
                onClick = { }
            )

            Spacer(Modifier.height(32.dp))

            // Refresh status button
            OutlinedButton(
                onClick = { refreshKeyboardStatus(ctx, isEnabled, isSelected) },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = subtitleColor
                )
            ) {
                Text("↻ Refresh Status")
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "© CeylonApz 2026",
                fontSize = 11.sp,
                color = subtitleColor
            )
        }
    }
}

@Composable
fun SetupStep(
    stepNumber: Int,
    title: String,
    description: String,
    isDone: Boolean,
    buttonText: String,
    enabled: Boolean = true,
    isDark: Boolean = true,
    onClick: () -> Unit
) {
    val accent = Color(0xFF7C5CFC)
    val bg = if (isDone) {
        if (isDark) Color(0xFF1A2E1A) else Color(0xFFDFF5DF)
    } else {
        if (isDark) Color(0xFF16213E) else Color(0xFFFFFFFF)
    }
    val titleColor = if (isDark) Color.White else Color(0xFF1A1A2E)
    val descColor = if (isDark) Color(0xFF888899) else Color(0xFF666677)
    val disabledBg = if (isDark) Color(0xFF333344) else Color(0xFFCCCCDD)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Step number circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isDone) Color(0xFF4CAF50) else accent
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (isDone) "✓" else "$stepNumber",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = titleColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                description,
                color = descColor,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(8.dp))
            if (stepNumber == 3) {
                SampleTextField()
            } else {
                Button(
                    onClick = onClick,
                    enabled = enabled && !isDone,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        disabledContainerColor = disabledBg
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(buttonText, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun SampleTextField() {
    val isDark = isSystemInDarkTheme()
    val accent = Color(0xFF7C5CFC)
    val scope = rememberCoroutineScope()
    val client = remember { GeminiClient() }

    var text by remember { mutableStateOf("") }
    var isChecking by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<GrammarResult?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Enter text") },
                placeholder = { Text("e.g. their going too the park") },
                singleLine = true,
                enabled = !isChecking,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    val input = text.trim()
                    if (input.isEmpty() || isChecking) return@Button
                    result = null
                    errorMsg = null
                    isChecking = true
                    scope.launch {
                        try {
                            result = client.checkGrammar(input)
                        } catch (e: Exception) {
                            errorMsg = "${e.javaClass.simpleName}: ${e.message ?: "Unknown error"}"
                        } finally {
                            isChecking = false
                        }
                    }
                },
                enabled = !isChecking && text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    disabledContainerColor = if (isDark) Color(0xFF333344) else Color(0xFFCCCCDD)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Send", fontSize = 13.sp)
                }
            }
        }

        val current = result
        if (current != null) {
            Spacer(Modifier.height(12.dp))
            GrammarResultCard(current, isDark)
        }

        val err = errorMsg
        if (err != null) {
            Spacer(Modifier.height(12.dp))
            GrammarErrorCard(err, isDark)
        }
    }
}

@Composable
private fun GrammarResultCard(result: GrammarResult, isDark: Boolean) {
    val hasError = result.is_error
    val bg = if (hasError) {
        if (isDark) Color(0xFF3E2E16) else Color(0xFFFFF3E0)
    } else {
        if (isDark) Color(0xFF1A2E1A) else Color(0xFFE8F5E9)
    }
    val labelColor = if (isDark) Color(0xFFAAAABB) else Color(0xFF666677)
    val bodyColor = if (isDark) Color.White else Color(0xFF1A1A2E)
    val highlight = Color(0xFF2E7D32)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(12.dp)
    ) {
        if (hasError) {
            Text("Original", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = labelColor)
            Text(result.originalText, fontSize = 13.sp, color = bodyColor)
            Spacer(Modifier.height(8.dp))
            Text("Corrected", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = labelColor)
            Text(
                result.correctedText,
                fontSize = 13.sp,
                color = highlight,
                fontWeight = FontWeight.Medium
            )
        } else {
            Text(
                "✓ No grammar issues found",
                fontSize = 13.sp,
                color = highlight,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun GrammarErrorCard(message: String, isDark: Boolean) {
    val bg = if (isDark) Color(0xFF3E1A1A) else Color(0xFFFFEBEE)
    val labelColor = Color(0xFFB71C1C)
    val bodyColor = if (isDark) Color.White else Color(0xFF1A1A2E)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(12.dp)
    ) {
        Text("Check failed", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = labelColor)
        Text(message, fontSize = 12.sp, color = bodyColor)
    }
}

@Composable
fun SectionDivider(label: String, isDark: Boolean) {
    val lineColor = if (isDark) Color(0xFF2C3E5C) else Color(0xFFCFD3DC)
    val labelColor = if (isDark) Color(0xFF888899) else Color(0xFF666677)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(lineColor)
        )
        Text(
            label,
            color = labelColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(lineColor)
        )
    }
}

@Composable
fun UsageCard(isDark: Boolean) {
    val ctx = LocalContext.current
    val stats = remember { UsageStats(ctx) }
    var snapshot by remember { mutableStateOf(stats.snapshot()) }

    DisposableEffect(stats) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            snapshot = stats.snapshot()
        }
        stats.registerListener(listener)
        snapshot = stats.snapshot()
        onDispose { stats.unregisterListener(listener) }
    }

    val bg = if (isDark) Color(0xFF16213E) else Color(0xFFFFFFFF)
    val titleColor = if (isDark) Color.White else Color(0xFF1A1A2E)
    val descColor = if (isDark) Color(0xFF888899) else Color(0xFF666677)
    val accent = Color(0xFF7C5CFC)
    val numberColor = if (isDark) Color.White else Color(0xFF1A1A2E)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent),
                contentAlignment = Alignment.Center
            ) {
                Text("✨", fontSize = 18.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "AI Usage",
                    color = titleColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    "How often you've used Gemini-powered features",
                    color = descColor,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Headline: total prompts
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                snapshot.totalPrompts.toString(),
                color = accent,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "total prompts",
                color = descColor,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Breakdown rows
        UsageStatRow(
            label = "Grammar checks",
            value = snapshot.grammarChecks,
            secondary = "${snapshot.fixesApplied} fix${if (snapshot.fixesApplied == 1) "" else "es"} applied",
            titleColor = numberColor,
            descColor = descColor
        )
        Spacer(Modifier.height(8.dp))
        UsageStatRow(
            label = "Smart replies",
            value = snapshot.smartReplies,
            secondary = "${snapshot.repliesUsed} chosen",
            titleColor = numberColor,
            descColor = descColor
        )

        if (snapshot.lastUsedAt > 0L) {
            Spacer(Modifier.height(12.dp))
            val relative = DateUtils.getRelativeTimeSpanString(
                snapshot.lastUsedAt,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            ).toString()
            Text(
                "Last used $relative",
                color = descColor,
                fontSize = 11.sp
            )
        } else {
            Spacer(Modifier.height(8.dp))
            Text(
                "No prompts yet — type with errors and tap ✨, or copy a message and tap 💬.",
                color = descColor,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun UsageStatRow(
    label: String,
    value: Int,
    secondary: String,
    titleColor: Color,
    descColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = titleColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(secondary, color = descColor, fontSize = 11.sp)
        }
        Text(
            value.toString(),
            color = titleColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun refreshKeyboardStatus(
    ctx: Context,
    isEnabled: androidx.compose.runtime.MutableState<Boolean>,
    isSelected: androidx.compose.runtime.MutableState<Boolean>
) {
    try {
        val pkg = ctx.packageName
        val enabled = Settings.Secure.getString(
            ctx.contentResolver,
            Settings.Secure.ENABLED_INPUT_METHODS
        ).orEmpty()
        val default = Settings.Secure.getString(
            ctx.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        ).orEmpty()
        isEnabled.value = enabled.contains("$pkg/")
        isSelected.value = default.startsWith("$pkg/")
    } catch (_: Throwable) {
        isEnabled.value = false
        isSelected.value = false
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AIKeyboardTheme {
        SetupScreen()
    }
}