package com.creditguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.creditguard.util.PendingPaymentTracker
import com.creditguard.util.UpiHelper

/**
 * Transparent activity that launches UPI payment directly from notification tap.
 * This activity finishes immediately after launching the UPI intent.
 */
class PaymentLauncherActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val amount = intent.getLongExtra("amount", 0L)
        val merchant = intent.getStringExtra("merchant") ?: "Unknown"
        val transactionId = intent.getLongExtra("transaction_id", 0)
        val amountRupees = amount.toDouble() / 100.0
        
        if (amount > 0 && transactionId > 0) {
            // Set up pending payment tracking
            PendingPaymentTracker.setPendingPayment(this, amountRupees, listOf(transactionId))
            
            // Create and launch UPI payment intent
            val payIntent = UpiHelper.createPaymentIntentForTransaction(this, amountRupees, merchant)
            if (payIntent != null) {
                try {
                    startActivity(payIntent)
                } catch (_: Exception) {
                    // If UPI app launch fails, open main app as fallback
                    startMainActivity()
                }
            } else {
                // No UPI ID configured, open settings
                startMainActivity()
            }
        } else {
            // Invalid data, open main app
            startMainActivity()
        }
        
        finish()
    }
    
    private fun startMainActivity() {
        val mainIntent = android.content.Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
    }
}
