package com.creditguard.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.creditguard.util.PendingPaymentTracker
import com.creditguard.util.UpiHelper

class NotificationActionReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null || intent.action != "PAY") return
        
        val amount = intent.getLongExtra("amount", 0L)
        val merchant = intent.getStringExtra("merchant") ?: "Unknown"
        val transactionId = intent.getLongExtra("transaction_id", 0L)
        
        if (amount <= 0L || transactionId <= 0L) return
        
        val amountRupees = amount.toDouble() / 100.0
        val payIntent = UpiHelper.createPaymentIntentForTransaction(context, amountRupees, merchant)
            ?: return
        
        payIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        
        try {
            context.startActivity(payIntent)
            // Track pending payment - will be matched when UPI debit SMS arrives
            PendingPaymentTracker.setPendingPayment(context, amountRupees, listOf(transactionId))
        } catch (_: Exception) {
            // UPI app not found or failed to launch
        }
    }
}
