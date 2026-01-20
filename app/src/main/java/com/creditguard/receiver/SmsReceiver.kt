package com.creditguard.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.creditguard.util.NotificationHelper
import com.creditguard.util.PendingPaymentTracker
import com.creditguard.util.SmsParser
import com.creditguard.util.UpiDebitParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return
        
        val sender = messages[0].displayOriginatingAddress?.take(20) ?: return
        val body = messages.joinToString("") { it.messageBody ?: "" }.take(500)
        
        if (sender.isBlank() || body.isBlank()) return
        
        // First check if this is a UPI debit (payment confirmation)
        if (UpiDebitParser.isUpiDebit(sender, body)) {
            val amount = UpiDebitParser.extractAmount(body)
            if (amount != null) {
                PendingPaymentTracker.checkAndMarkPaid(context, amount)
            }
            return
        }
        
        // Then check if it's a credit card transaction
        val transaction = SmsParser.parse(sender, body) ?: return
        
        CoroutineScope(Dispatchers.IO).launch {
            val app = context.applicationContext as? com.creditguard.CreditGuardApp
            val insertedId = app?.database?.transactionDao()?.insert(transaction) ?: return@launch
            
            val transactionWithId = transaction.copy(id = insertedId)
            NotificationHelper.showTransactionNotification(context, transactionWithId)
        }
    }
}
