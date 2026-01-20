package com.creditguard.util

import android.content.Context
import android.content.SharedPreferences
import com.creditguard.CreditGuardApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PendingPaymentTracker {
    private const val PREFS_NAME = "pending_payments"
    private const val KEY_AMOUNT = "pending_amount"
    private const val KEY_TIMESTAMP = "pending_timestamp"
    private const val KEY_TX_IDS = "pending_tx_ids"
    private const val TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun setPendingPayment(context: Context, amount: Double, transactionIds: List<Long>) {
        getPrefs(context).edit()
            .putFloat(KEY_AMOUNT, amount.toFloat())
            .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            .putString(KEY_TX_IDS, transactionIds.joinToString(","))
            .apply()
    }
    
    fun getPendingAmount(context: Context): Double? {
        val prefs = getPrefs(context)
        val timestamp = prefs.getLong(KEY_TIMESTAMP, 0)
        if (System.currentTimeMillis() - timestamp > TIMEOUT_MS) {
            clear(context)
            return null
        }
        val amount = prefs.getFloat(KEY_AMOUNT, 0f)
        return if (amount > 0) amount.toDouble() else null
    }
    
    fun getPendingTransactionIds(context: Context): List<Long> {
        val prefs = getPrefs(context)
        val ids = prefs.getString(KEY_TX_IDS, "") ?: ""
        return ids.split(",").mapNotNull { it.toLongOrNull() }
    }
    
    fun clear(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
    
    /**
     * Checks if the debited amount matches a pending payment and marks transactions as paid.
     * This version is optimized for use within a BroadcastReceiver - caller must handle goAsync().
     */
    fun checkAndMarkPaid(context: Context, debitedAmount: Double): Boolean {
        return try {
            val pendingAmount = getPendingAmount(context) ?: return false
            
            // Check if amounts match (within 1 rupee tolerance for rounding)
            if (kotlin.math.abs(pendingAmount - debitedAmount) <= 1.0) {
                val txIds = getPendingTransactionIds(context)
                if (txIds.isNotEmpty()) {
                    // Launch coroutine for DB operations
                    // Note: The caller (SmsReceiver) handles goAsync() for its own lifecycle
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val app = context.applicationContext as? CreditGuardApp
                            val dao = app?.database?.transactionDao()
                            txIds.forEach { id -> dao?.markPaid(id) }
                        } catch (_: Exception) {
                            // Ignore DB errors - non-critical
                        }
                    }
                    clear(context)
                    
                    // Store success for UI to show
                    try {
                        context.applicationContext.getSharedPreferences("payment_success", Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean("show_success", true)
                            .putFloat("amount", debitedAmount.toFloat())
                            .putInt("count", txIds.size)
                            .apply()
                    } catch (_: Exception) {
                        // Ignore SharedPreferences errors - non-critical
                    }
                    
                    return true
                }
            }
            false
        } catch (_: Exception) {
            false
        }
    }
    
    fun getAndClearSuccess(context: Context): PaymentSuccess? {
        val prefs = context.applicationContext.getSharedPreferences("payment_success", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("show_success", false)) return null
        
        val success = PaymentSuccess(
            amount = prefs.getFloat("amount", 0f).toDouble(),
            count = prefs.getInt("count", 0)
        )
        prefs.edit().clear().apply()
        return success
    }
    
    data class PaymentSuccess(val amount: Double, val count: Int)
}
