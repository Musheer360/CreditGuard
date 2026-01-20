package com.creditguard.ui.screens

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creditguard.ui.theme.*
import com.creditguard.util.SecurePreferences
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(onClearHistory: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val prefs = remember { SecurePreferences.getSecurePreferences(context) }
    
    var upiId by remember { mutableStateOf(prefs.getString("vault_upi_id", "") ?: "") }
    var vaultName by remember { mutableStateOf(prefs.getString("vault_name", "") ?: "") }
    var saved by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var savePressed by remember { mutableStateOf(false) }
    var clearPressed by remember { mutableStateOf(false) }
    
    // Success haptic pattern
    fun successHaptic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibrator = (context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 50, 30), intArrayOf(0, 150, 0, 200), -1))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 50, 30), intArrayOf(0, 150, 0, 200), -1))
        }
    }
    
    LaunchedEffect(saved) {
        if (saved) { delay(2000); saved = false }
    }
    
    if (showClearConfirmation) {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        AlertDialog(
            onDismissRequest = { 
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                showClearConfirmation = false 
            },
            title = { Text("Clear Transaction History", fontWeight = FontWeight.Medium) },
            text = { Text("Are you sure you want to delete all transaction history? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                    }
                    onClearHistory()
                    showClearConfirmation = false
                }) {
                    Text("Clear All", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    showClearConfirmation = false 
                }) {
                    Text("Cancel", color = SecondaryText)
                }
            },
            containerColor = CardSurface,
            titleContentColor = Color.White,
            textContentColor = SecondaryText
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        
        Text("settings", color = SecondaryText, fontSize = 14.sp, letterSpacing = 2.sp)
        
        Spacer(Modifier.height(48.dp))
        
        Text("upi id", color = TertiaryText, fontSize = 11.sp, letterSpacing = 1.sp)
        Spacer(Modifier.height(12.dp))
        MinimalTextField(value = upiId, onValueChange = { 
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            upiId = it 
        }, placeholder = "yourname@upi")
        
        Spacer(Modifier.height(32.dp))
        
        Text("payee name", color = TertiaryText, fontSize = 11.sp, letterSpacing = 1.sp)
        Spacer(Modifier.height(12.dp))
        MinimalTextField(value = vaultName, onValueChange = { 
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            vaultName = it 
        }, placeholder = "Savings Account")
        
        Spacer(Modifier.height(48.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(if (savePressed) 0.98f else 1f)
                .clip(CircleShape)
                .background(if (saved) Success else Color.White)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when {
                                event.changes.any { it.pressed } && !savePressed -> {
                                    savePressed = true
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS)
                                    }
                                }
                                event.changes.none { it.pressed } && savePressed -> {
                                    savePressed = false
                                }
                            }
                        }
                    }
                }
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    successHaptic()
                    prefs.edit()
                        .putString("vault_upi_id", upiId)
                        .putString("vault_name", vaultName)
                        .apply()
                    saved = true
                }
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (saved) "saved ✓" else "save",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
        }
        
        Spacer(Modifier.height(64.dp))
        
        Text("how it works", color = TertiaryText, fontSize = 11.sp, letterSpacing = 1.sp)
        Spacer(Modifier.height(16.dp))
        
        InfoItem("1", "set your upi id above")
        InfoItem("2", "spend on credit card")
        InfoItem("3", "tap notification to set aside")
        InfoItem("4", "money ready when bill comes")
        
        Spacer(Modifier.height(48.dp))
        
        Text("your data never leaves your device", color = TertiaryText, fontSize = 12.sp)
        
        Spacer(Modifier.height(48.dp))
        
        Text("data", color = TertiaryText, fontSize = 11.sp, letterSpacing = 1.sp)
        Spacer(Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(if (clearPressed) 0.98f else 1f)
                .clip(CircleShape)
                .border(1.dp, ErrorRed.copy(alpha = 0.5f), CircleShape)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when {
                                event.changes.any { it.pressed } && !clearPressed -> {
                                    clearPressed = true
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                }
                                event.changes.none { it.pressed } && clearPressed -> {
                                    clearPressed = false
                                }
                            }
                        }
                    }
                }
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    showClearConfirmation = true
                }
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "clear transaction history",
                color = ErrorRed,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
        }
        
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun MinimalTextField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Light),
        cursorBrush = SolidColor(Color.White),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TertiaryText.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                if (value.isEmpty()) {
                    Text(placeholder, color = TertiaryText, fontSize = 18.sp, fontWeight = FontWeight.Light)
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun InfoItem(number: String, text: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(number, color = TertiaryText, fontSize = 12.sp)
        Spacer(Modifier.width(16.dp))
        Text(text, color = SecondaryText, fontSize = 14.sp)
    }
}
