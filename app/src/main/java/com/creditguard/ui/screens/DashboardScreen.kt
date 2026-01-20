package com.creditguard.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creditguard.data.model.Transaction
import com.creditguard.ui.theme.*
import com.creditguard.util.UpiHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    transactions: List<Transaction>,
    unpaidTotal: Double,
    monthlySpend: Double,
    onPayClick: (Transaction) -> Unit,
    onMarkPaid: (Long) -> Unit,
    onMarkAllPaid: () -> Unit
) {
    val context = LocalContext.current
    
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
                    onMarkAllPaid = onMarkAllPaid
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
            itemsIndexed(
                transactions.take(20),
                key = { _, tx -> tx.id }
            ) { index, tx ->
                TransactionRow(
                    tx = tx,
                    context = context,
                    onMarkPaid = onMarkPaid
                )
                if (index < transactions.size - 1) {
                    Spacer(Modifier.height(1.dp).fillMaxWidth().alpha(0.1f).background(Color.White))
                }
            }
        }
        
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun PayButton(amount: Double, context: Context, onMarkAllPaid: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(
                Brush.horizontalGradient(
                    listOf(GradientPurpleStart, GradientBlueStart)
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                val intent = UpiHelper.createPaymentIntentForTransaction(context, amount, "Total Pending")
                intent?.let { context.startActivity(it) }
                onMarkAllPaid()
            }
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "pay all  →",
            color = Color.White,
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

@Composable
private fun TransactionRow(tx: Transaction, context: Context, onMarkPaid: (Long) -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !tx.isPaid
            ) {
                val intent = UpiHelper.createPaymentIntentForTransaction(context, tx.amount, tx.merchant)
                intent?.let { context.startActivity(it) }
                onMarkPaid(tx.id)
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
            if (!tx.isPaid) {
                Spacer(Modifier.height(2.dp))
                Text(
                    "tap to pay",
                    color = TertiaryText,
                    fontSize = 10.sp
                )
            }
        }
    }
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
