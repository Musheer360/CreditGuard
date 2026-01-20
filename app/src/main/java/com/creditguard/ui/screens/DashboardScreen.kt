package com.creditguard.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Text
import com.creditguard.data.model.Transaction
import com.creditguard.ui.theme.*
import com.creditguard.util.PendingPaymentTracker
import com.creditguard.util.UpiHelper
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

// Minimal haptic helper - only for meaningful moments
private object Haptics {
    // Success: payment confirmed, save complete
    fun success(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION") context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            }
        } catch (_: Exception) {
            // Ignore haptic failures - non-critical
        }
    }
    
    // Swipe threshold reached
    fun threshold(view: View) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            }
        } catch (_: Exception) {
            // Ignore haptic failures - non-critical
        }
    }
}

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
    
    // Check for payment success on resume
    var paymentSuccess by remember { mutableStateOf<PendingPaymentTracker.PaymentSuccess?>(null) }
    
    LaunchedEffect(Unit) {
        paymentSuccess = PendingPaymentTracker.getAndClearSuccess(context)
        if (paymentSuccess != null) {
            Haptics.success(context)
        }
    }
    
    // Success overlay
    if (paymentSuccess != null) {
        SuccessOverlay(
            amount = paymentSuccess!!.amount,
            count = paymentSuccess!!.count,
            onDismiss = { paymentSuccess = null }
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
                PayButton(
                    amount = unpaidTotal,
                    context = context,
                    transactionIds = transactions.filter { !it.isPaid }.map { it.id }
                )
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
private fun SuccessOverlay(amount: Double, count: Int, onDismiss: () -> Unit) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(200))
        scale.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 300f))
        delay(2500)
        alpha.animateTo(0f, tween(300))
        onDismiss()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha.value)
            .background(PureBlack.copy(alpha = 0.95f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale.value)
        ) {
            Text("done", fontSize = 48.sp, color = Success, fontWeight = FontWeight.Light, letterSpacing = 2.sp)
            Spacer(Modifier.height(24.dp))
            Text("₹${formatAmount(amount)}", fontSize = 40.sp, fontWeight = FontWeight.Light, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text("set aside successfully", fontSize = 16.sp, color = SecondaryText, letterSpacing = 1.sp)
            if (count > 1) {
                Spacer(Modifier.height(4.dp))
                Text("$count transactions marked as paid", fontSize = 13.sp, color = TertiaryText)
            }
        }
    }
}

@Composable
private fun PayButton(amount: Double, context: Context, transactionIds: List<Long>) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (pressed) 0.98f else 1f)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(interactionSource = interaction, indication = null) {
                try {
                    PendingPaymentTracker.setPendingPayment(context, amount, transactionIds)
                    val intent = UpiHelper.createPaymentIntentForTransaction(context, amount, "Total Pending")
                    intent?.let { context.startActivity(it) }
                } catch (_: Exception) {
                    // Handle case where no UPI app is available or activity fails to start
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
    onMarkUnpaid: (Long) -> Unit
) {
    val view = LocalView.current
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var didHaptic by remember { mutableStateOf(false) }
    // Capture isPaid state at the start to avoid state changes during an active gesture.
    // This ensures consistent swipe behavior even if the underlying data changes mid-gesture.
    val isPaidState = tx.isPaid
    
    // Reset offset when isPaid changes (e.g., after marking unpaid)
    LaunchedEffect(tx.isPaid) {
        offsetX = 0f
        didHaptic = false
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = offsetX.dp)
            .pointerInput(tx.id) {
                // Use tx.id as key instead of tx.isPaid to prevent gesture cancellation
                detectHorizontalDragGestures(
                    onDragStart = { 
                        didHaptic = false 
                    },
                    onDragEnd = {
                        try {
                            // Only process swipe-to-unmark if the transaction is paid
                            if (isPaidState && offsetX < -100) {
                                onMarkUnpaid(tx.id)
                            }
                        } catch (_: Exception) {
                            // Ignore any errors during swipe action
                        }
                        offsetX = 0f
                    },
                    onDragCancel = {
                        // Reset on cancel to prevent stuck state
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        // Only allow left swipe for paid transactions
                        if (isPaidState) {
                            offsetX = (offsetX + dragAmount / 3).coerceIn(-150f, 0f)
                            // Single haptic when threshold reached
                            if (offsetX < -100 && !didHaptic) {
                                Haptics.threshold(view)
                                didHaptic = true
                            }
                        }
                    }
                )
            }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                if (!tx.isPaid) {
                    try {
                        PendingPaymentTracker.setPendingPayment(context, tx.amount, listOf(tx.id))
                        val intent = UpiHelper.createPaymentIntentForTransaction(context, tx.amount, tx.merchant)
                        intent?.let { context.startActivity(it) }
                    } catch (_: Exception) {
                        // Handle case where no UPI app is available or activity fails to start
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
