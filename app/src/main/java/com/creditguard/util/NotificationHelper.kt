package com.creditguard.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.creditguard.MainActivity
import com.creditguard.data.model.Transaction
import com.creditguard.receiver.NotificationActionReceiver

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
        val payIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "PAY"
            putExtra("transaction_id", transaction.id)
            putExtra("amount", transaction.amount)
            putExtra("merchant", transaction.merchant)
        }
        val payPending = PendingIntent.getBroadcast(
            context, transaction.id.toInt(), payIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val openIntent = Intent(context, MainActivity::class.java)
        val openPending = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("₹${String.format("%,.0f", transaction.amount)} spent")
            .setContentText("${transaction.merchant} • ${transaction.bank}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("You spent ₹${String.format("%,.2f", transaction.amount)} at ${transaction.merchant}.\nTap 'Pay Now' to set aside this amount."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPending)
            .addAction(android.R.drawable.ic_menu_send, "Pay Now", payPending)
            .build()
        
        context.getSystemService(NotificationManager::class.java)
            .notify(transaction.id.toInt(), notification)
    }
}
