package com.creditguard.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creditguard.data.model.Transaction
import com.creditguard.ui.theme.*
import com.creditguard.util.UpiHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

// Haptic helper for dynamic intensity
object Haptics {
    fun tick(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }
    
    fun confirm(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
    
    fun reject(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
    
    fun heavyClick(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
    
    fun dynamicSwipe(context: Context, progress: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibrator = (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            val amplitude = (50 + (progress * 200)).toInt().coerceIn(1, 255)
            vibrator.vibrate(VibrationEffect.createOneShot(10, amplitude))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val amplitude = (50 + (progress * 200)).toInt().coerceIn(1, 255)
            vibrator.vibrate(VibrationEffect.createOneShot(10, amplitude))
        }
    }
    
    fun success(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibrator = (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 50, 30), intArrayOf(0, 150, 0, 200), -1))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 50, 30), intArrayOf(0, 150, 0, 200), -1))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    transactions: List<Transaction>,
    unpaidTotal: Double,
    monthlySpend: Double,
    onPayClick: (Transaction) -> Unit,
    onMarkPaid: (Long) -> Unit,
    onMarkUnpaid: (Long) -> Unit,
    onMarkAllPaid: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    
    var showPayAllConfirmation by remember { mutableStateOf(false) }
    var showSinglePayConfirmation by remember { mutableStateOf<Transaction?>(null) }
    
    if (showPayAllConfirmation) {
        Haptics.tick(view)
        ConfirmationDialog(
            title = "Confirm Payment",
            message = "Did you complete the payment of ₹${formatAmount(unpaidTotal)}?",
            onConfirm = {
                Haptics.success(context)
                onMarkAllPaid()
                showPayAllConfirmation = false
            },
            onDismiss = {
                Haptics.reject(view)
                showPayAllConfirmation = false
            }
        )
    }
    
    showSinglePayConfirmation?.let { tx ->
        Haptics.tick(view)
        ConfirmationDialog(
            title = "Confirm Payment",
            message = "Did you complete the payment of ₹${formatAmount(tx.amount)} for ${tx.merchant}?",
            onConfirm = {
                Haptics.success(context)
                onMarkPaid(tx.id)
                showSinglePayConfirmation = null
            },
            onDismiss = {
                Haptics.reject(view)
                showSinglePayConfirmation = null
            }
        )
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
    ) {
        item {
            Spacer(Modifier.height(32.dp))
            Text("set aside", color = SecondaryText, fontSize = 14.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "₹${formatAmount(unpaidTotal)}",
                color = Color.White,
                fontSize = 56.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-2).sp
            )
            Spacer(Modifier.height(32.dp))
        }
        
        if (unpaidTotal > 0) {
            item {
                PayButton(amount = unpaidTotal, context = context, onPaymentInitiated = { showPayAllConfirmation = true })
                Spacer(Modifier.height(48.dp))
            }
        }
        
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("this month", "₹${formatAmount(monthlySpend)}")
                StatItem("status", if (unpaidTotal == 0.0) "all clear" else "pending", if (unpaidTotal == 0.0) Success else WarningAmber)
            }
            Spacer(Modifier.height(48.dp))
        }
        
        item {
            Text("recent", color = SecondaryText, fontSize = 12.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(16.dp))
        }
        
        if (transactions.isEmpty()) {
            item { EmptyState() }
        } else {
            val displayedTransactions = transactions.take(20)
            itemsIndexed(displayedTransactions, key = { _, tx -> tx.id }) { index, tx ->
                SwipeableTransactionRow(
                    tx = tx,
                    context = context,
                    onPaymentInitiated = { showSinglePayConfirmation = tx },
                    onMarkUnpaid = onMarkUnpaid
                )
                if (index < displayedTransactions.size - 1) {
                    Spacer(Modifier.height(1.dp).fillMaxWidth().alpha(0.1f).background(Color.White))
                }
            }
        }
        
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun ConfirmationDialog(title: String, message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val view = LocalView.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Medium) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = { Haptics.confirm(view); onConfirm() }) {
                Text("Yes, Paid", color = Success)
            }
        },
        dismissButton = {
            TextButton(onClick = { Haptics.tick(view); onDismiss() }) {
                Text("No", color = SecondaryText)
            }
        },
        containerColor = CardSurface,
        titleContentColor = Color.White,
        textContentColor = SecondaryText
    )
}

@Composable
private fun PayButton(amount: Double, context: Context, onPaymentInitiated: () -> Unit) {
    val view = LocalView.current
    var pressed by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (pressed) 0.98f else 1f)
            .clip(CircleShape)
            .background(Color.White)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when {
                            event.changes.any { it.pressed } && !pressed -> {
                                pressed = true
                                Haptics.heavyClick(view)
                            }
                            event.changes.none { it.pressed } && pressed -> {
                                pressed = false
                            }
                        }
                    }
                }
            }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                Haptics.confirm(view)
                val intent = UpiHelper.createPaymentIntentForTransaction(context, amount, "Total Pending")
                if (intent != null) {
                    context.startActivity(intent)
                    onPaymentInitiated()
                }
            }
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("pay all  →", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
    }
}

@Composable
private fun StatItem(label: String, value: String, valueColor: Color = Color.White) {
    Column {
        Text(label, color = TertiaryText, fontSize = 11.sp, letterSpacing = 1.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = valueColor, fontSize = 18.sp, fontWeight = FontWeight.Normal)
    }
}

@Composable
private fun SwipeableTransactionRow(
    tx: Transaction,
    context: Context,
    onPaymentInitiated: () -> Unit,
    onMarkUnpaid: (Long) -> Unit
) {
    val view = LocalView.current
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var lastHapticThreshold by remember { mutableIntStateOf(0) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = offsetX.dp)
            .pointerInput(tx.isPaid) {
                if (tx.isPaid) {
                    detectHorizontalDragGestures(
                        onDragStart = { lastHapticThreshold = 0 },
                        onDragEnd = {
                            if (offsetX < -100) {
                                Haptics.success(context)
                                onMarkUnpaid(tx.id)
                            } else {
                                Haptics.tick(view)
                            }
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount / 3).coerceIn(-150f, 0f)
                            val progress = abs(offsetX) / 150f
                            val threshold = (progress * 5).toInt()
                            if (threshold > lastHapticThreshold) {
                                Haptics.dynamicSwipe(context, progress)
                                lastHapticThreshold = threshold
                            }
                        }
                    )
                }
            }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                Haptics.tick(view)
                if (!tx.isPaid) {
                    Haptics.heavyClick(view)
                    val intent = UpiHelper.createPaymentIntentForTransaction(context, tx.amount, tx.merchant)
                    if (intent != null) {
                        context.startActivity(intent)
                        onPaymentInitiated()
                    }
                }
            }
            .padding(vertical = 20.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(tx.merchant, color = if (tx.isPaid) SecondaryText else Color.White, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text("${tx.bank} · ${dateFormat.format(Date(tx.timestamp))}", color = TertiaryText, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("₹${formatAmount(tx.amount)}", color = if (tx.isPaid) Success else Color.White, fontSize = 18.sp, fontWeight = FontWeight.Light)
                Spacer(Modifier.height(2.dp))
                Text(if (tx.isPaid) "← swipe to unmark" else "tap to pay", color = TertiaryText, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(Modifier.fillMaxWidth().padding(vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("no transactions yet", color = SecondaryText, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Text("your credit card spends will appear here", color = TertiaryText, fontSize = 13.sp)
    }
}

private fun formatAmount(amount: Double): String = when {
    amount >= 100000 -> String.format("%.1fL", amount / 100000)
    amount >= 1000 -> String.format("%,.0f", amount)
    else -> String.format("%.0f", amount)
}
