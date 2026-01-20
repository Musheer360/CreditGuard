package com.creditguard.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build

object UpiHelper {
    
    private val UPI_ID_REGEX = Regex("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$")
    
    private fun isValidUpiId(upiId: String): Boolean {
        return upiId.length in 3..50 && UPI_ID_REGEX.matches(upiId)
    }
    
    private fun sanitizeInput(input: String): String {
        return input.take(100).replace(Regex("[^a-zA-Z0-9@._\\s-]"), "")
    }
    
    fun createPaymentIntent(
        upiId: String,
        payeeName: String,
        amount: Double,
        note: String
    ): Intent? {
        val cleanUpiId = sanitizeInput(upiId)
        val cleanPayeeName = sanitizeInput(payeeName)
        val cleanNote = sanitizeInput(note)
        
        if (!isValidUpiId(cleanUpiId) || amount <= 0 || amount > 100000) return null
        
        val uri = Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", cleanUpiId)
            .appendQueryParameter("pn", cleanPayeeName.take(50))
            .appendQueryParameter("am", String.format("%.2f", amount))
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tn", cleanNote.take(100))
            .build()
        
        return Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
    
    fun createPaymentIntentForTransaction(
        context: Context,
        amount: Double,
        merchant: String
    ): Intent? {
        val securePrefs = SecurePreferences.getSecurePreferences(context)
        
        val upiId = securePrefs.getString("vault_upi_id", "") ?: ""
        val vaultName = securePrefs.getString("vault_name", "Repayment Vault") ?: "Repayment Vault"
        
        val note = "CreditGuard: ${sanitizeInput(merchant)} repayment"
        return createPaymentIntent(upiId, vaultName, amount, note)
    }
    
    @Suppress("DEPRECATION")
    fun hasUpiApps(context: Context): Boolean {
        val testIntent = Intent(Intent.ACTION_VIEW, Uri.parse("upi://pay"))
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(
                testIntent, 
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            ).isNotEmpty()
        } else {
            context.packageManager.queryIntentActivities(testIntent, 0).isNotEmpty()
        }
    }
}
