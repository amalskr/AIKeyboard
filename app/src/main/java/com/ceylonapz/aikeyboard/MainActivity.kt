package com.ceylonapz.aikeyboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
        val myId = "${ctx.packageName}/.AIKeyboardService"
        isEnabled.value = Settings.Secure.getString(
            ctx.contentResolver,
            Settings.Secure.ENABLED_INPUT_METHODS
        )?.contains(myId) == true
        isSelected.value = Settings.Secure.getString(
            ctx.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        )?.contains(myId) == true
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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo / Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(accent, Color(0xFF9D84FF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("AI", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "AI Grammar Keyboard",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Text(
                "Powered by AI",
                fontSize = 14.sp,
                color = subtitleColor
            )

            Spacer(Modifier.height(48.dp))

            // ── Step 1: Enable keyboard ────────────────────
            SetupStep(
                stepNumber = 1,
                title = "Enable Keyboard",
                description = "Turn on AI Grammar Keyboard in system settings",
                isDone = isEnabled.value,
                buttonText = if (isEnabled.value) "Enabled ✓" else "Open Settings",
                isDark = isDark,
                onClick = {
                    ctx.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
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
                    val imm = ctx.getSystemService(InputMethodManager::class.java)
                    imm.showInputMethodPicker()
                }
            )

            Spacer(Modifier.height(16.dp))

            // ── Step 3: Test it ────────────────────────────
            SetupStep(
                stepNumber = 3,
                title = "Try It Out",
                description = "Type a sentence with errors and press '.' to check grammar",
                isDone = false,
                buttonText = "Open any app and type!",
                enabled = isSelected.value,
                isDark = isDark,
                onClick = { }
            )

            Spacer(Modifier.height(48.dp))

            // Refresh status button
            OutlinedButton(
                onClick = {
                    val myId = "${ctx.packageName}/.AIKeyboardService"
                    isEnabled.value = Settings.Secure.getString(
                        ctx.contentResolver,
                        Settings.Secure.ENABLED_INPUT_METHODS
                    )?.contains(myId) == true
                    isSelected.value = Settings.Secure.getString(
                        ctx.contentResolver,
                        Settings.Secure.DEFAULT_INPUT_METHOD
                    )?.contains(myId) == true
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = subtitleColor
                )
            ) {
                Text("↻ Refresh Status")
            }
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
    var text by remember { mutableStateOf("") }

    TextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("Enter text") },
        placeholder = { Text("Type something...") },
        singleLine = true
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AIKeyboardTheme {
        SetupScreen()
    }
}