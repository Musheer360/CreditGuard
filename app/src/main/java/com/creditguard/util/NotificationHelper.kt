package com.creditguard.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.creditguard.PaymentLauncherActivity
import com.creditguard.data.model.Transaction

object NotificationHelper {
    private const val CHANNEL_ID = "creditguard_transactions"
    
    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Transaction Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Credit card spend notifications"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }
    
    fun showTransactionNotification(context: Context, transaction: Transaction) {
        val notifyId = (transaction.id % Int.MAX_VALUE).toInt()
        val rupees = transaction.amount.toDouble() / 100.0
        
        // Tapping notification directly opens UPI payment via PaymentLauncherActivity
        val payIntent = Intent(context, PaymentLauncherActivity::class.java).apply {
            putExtra("transaction_id", transaction.id)
            putExtra("amount", transaction.amount)
            putExtra("merchant", transaction.merchant)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val payPending = PendingIntent.getActivity(
            context, notifyId, payIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("₹${String.format("%,.0f", rupees)} spent")
            .setContentText("${transaction.merchant} • Tap to set aside")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("You spent ₹${String.format("%,.2f", rupees)} at ${transaction.merchant}.\nTap to set aside this amount via UPI."))
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(payPending)
            .build()
        
        context.getSystemService(NotificationManager::class.java)
            .notify(notifyId, notification)
    }
}
