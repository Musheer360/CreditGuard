package com.creditguard.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
        
        if (amount > 0) {
            val payIntent = UpiHelper.createPaymentIntentForTransaction(context, amount, merchant)
            payIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            try {
                payIntent?.let { context.startActivity(it) }
                
                CoroutineScope(Dispatchers.IO).launch {
                    val app = context.applicationContext as? com.creditguard.CreditGuardApp
                    app?.database?.transactionDao()?.markPaid(transactionId)
                }
            } catch (e: Exception) {
                // UPI app not found
            }
        }
    }
}
