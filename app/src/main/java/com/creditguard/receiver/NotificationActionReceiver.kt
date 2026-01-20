package com.creditguard.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.creditguard.CreditGuardApp
import com.creditguard.util.UpiHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null || intent.action != "PAY") return
        
        val amount = intent.getDoubleExtra("amount", 0.0)
        val merchant = intent.getStringExtra("merchant") ?: "Unknown"
        val transactionId = intent.getLongExtra("transaction_id", 0)
        
        if (amount <= 0 || transactionId <= 0) return
        
        val payIntent = UpiHelper.createPaymentIntentForTransaction(context, amount, merchant)
            ?: return
        
        payIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        
        // Try to launch UPI app
        val activityLaunched = try {
            context.startActivity(payIntent)
            true
        } catch (e: Exception) {
            // UPI app not found or failed to launch
            false
        }
        
        // Only mark as paid if activity was successfully launched
        if (activityLaunched) {
            // Use goAsync() to properly handle async work in BroadcastReceiver
            // This extends the receiver lifecycle until finish() is called
            val pendingResult = goAsync()
            
            // Launch coroutine for DB work - goAsync() ensures proper lifecycle management
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val app = context.applicationContext as? CreditGuardApp
                    app?.database?.transactionDao()?.markPaid(transactionId)
                } finally {
                    // Always finish the async operation to release system resources
                    pendingResult.finish()
                }
            }
        }
    }
}
