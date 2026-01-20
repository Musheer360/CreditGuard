package com.creditguard.ui.screens

import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
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
    
    // State for confirmation dialogs
    var showPayAllConfirmation by remember { mutableStateOf(false) }
    var showSinglePayConfirmation by remember { mutableStateOf<Transaction?>(null) }
    
    // Confirmation dialog for "Pay All"
    if (showPayAllConfirmation) {
        ConfirmationDialog(
            title = "Confirm Payment",
            message = "Did you complete the payment of ₹${formatAmount(unpaidTotal)}?",
            onConfirm = {
                onMarkAllPaid()
                showPayAllConfirmation = false
            },
            onDismiss = { showPayAllConfirmation = false }
        )
    }
    
    // Confirmation dialog for single transaction
    showSinglePayConfirmation?.let { tx ->
        ConfirmationDialog(
            title = "Confirm Payment",
            message = "Did you complete the payment of ₹${formatAmount(tx.amount)} for ${tx.merchant}?",
            onConfirm = {
                onMarkPaid(tx.id)
                showSinglePayConfirmation = null
            },
            onDismiss = { showSinglePayConfirmation = null }
        )
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Hero amount
        item {
            Spacer(Modifier.height(32.dp))
            Text(
                "set aside",
                color = SecondaryText,
                fontSize = 14.sp,
                letterSpacing = 2.sp
            )
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
        
        // Quick pay button - only if there's pending amount
        if (unpaidTotal > 0) {
            item {
                PayButton(
                    amount = unpaidTotal,
                    context = context,
                    onPaymentInitiated = { showPayAllConfirmation = true }
                )
                Spacer(Modifier.height(48.dp))
            }
        }
        
        // Stats row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("this month", "₹${formatAmount(monthlySpend)}")
                StatItem(
                    "status",
                    if (unpaidTotal == 0.0) "all clear" else "pending",
                    if (unpaidTotal == 0.0) Success else WarningAmber
                )
            }
            Spacer(Modifier.height(48.dp))
        }
        
        // Transactions header
        item {
            Text(
                "recent",
                color = SecondaryText,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(16.dp))
        }
        
        // Transactions or empty state
        if (transactions.isEmpty()) {
            item { EmptyState() }
        } else {
            val displayedTransactions = transactions.take(20)
            itemsIndexed(
                displayedTransactions,
                key = { _, tx -> tx.id }
            ) { index, tx ->
                TransactionRow(
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
private fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Medium) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Yes, Paid", color = Success)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(Color.White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                val intent = UpiHelper.createPaymentIntentForTransaction(context, amount, "Total Pending")
                if (intent != null) {
                    context.startActivity(intent)
                    onPaymentInitiated()
                }
            }
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "pay all  →",
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun StatItem(label: String, value: String, valueColor: Color = Color.White) {
    Column {
        Text(
            label,
            color = TertiaryText,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            color = valueColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionRow(
    tx: Transaction, 
    context: Context, 
    onPaymentInitiated: () -> Unit,
    onMarkUnpaid: (Long) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val view = LocalView.current
    val dismissState = rememberDismissState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == DismissValue.DismissedToStart && tx.isPaid) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                onMarkUnpaid(tx.id)
                true
            } else false
        }
    )
    
    SwipeToDismiss(
        state = dismissState,
        directions = if (tx.isPaid) setOf(DismissDirection.EndToStart) else emptySet(),
        background = {},
        dismissContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        if (!tx.isPaid) {
                            val intent = UpiHelper.createPaymentIntentForTransaction(context, tx.amount, tx.merchant)
                            if (intent != null) {
                                context.startActivity(intent)
                                onPaymentInitiated()
                            }
                        }
                    }
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        tx.merchant,
                        color = if (tx.isPaid) SecondaryText else Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${tx.bank} · ${dateFormat.format(Date(tx.timestamp))}",
                        color = TertiaryText,
                        fontSize = 12.sp
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "₹${formatAmount(tx.amount)}",
                        color = if (tx.isPaid) Success else Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Light
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (tx.isPaid) "swipe left to unmark" else "tap to pay",
                        color = TertiaryText,
                        fontSize = 10.sp
                    )
                }
            }
        }
    )
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "no transactions yet",
            color = SecondaryText,
            fontSize = 16.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "your credit card spends will appear here",
            color = TertiaryText,
            fontSize = 13.sp
        )
    }
}

private fun formatAmount(amount: Double): String {
    return when {
        amount >= 100000 -> String.format("%.1fL", amount / 100000)
        amount >= 1000 -> String.format("%,.0f", amount)
        else -> String.format("%.0f", amount)
    }
}
