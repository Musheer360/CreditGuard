package com.creditguard.util

object UpiDebitParser {
    
    private val debitPatterns = listOf(
        Regex("""(?:debited|sent|paid|transferred).{0,30}?(?:Rs\.?|INR|₹)\s*([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""(?:Rs\.?|INR|₹)\s*([\d,]+(?:\.\d{1,2})?).{0,30}?(?:debited|sent|paid|transferred)""", RegexOption.IGNORE_CASE),
        Regex("""(?:Rs\.?|INR|₹)\s*([\d,]+(?:\.\d{1,2})?).{0,20}?(?:from|to).{0,30}?(?:UPI|IMPS|NEFT)""", RegexOption.IGNORE_CASE)
    )
    
    private val upiKeywords = listOf("upi", "imps", "neft", "gpay", "phonepe", "paytm", "bhim", "sent to", "paid to", "transferred")
    
    fun isUpiDebit(sender: String, body: String): Boolean {
        val lowerBody = body.lowercase()
        val hasUpiKeyword = upiKeywords.any { lowerBody.contains(it) }
        val hasDebit = lowerBody.contains("debit") || lowerBody.contains("sent") || 
                       lowerBody.contains("paid") || lowerBody.contains("transfer")
        val hasAmount = debitPatterns.any { it.containsMatchIn(body) }
        
        // Exclude credit card transactions (those are handled separately)
        val isCreditCard = lowerBody.contains("credit card") || lowerBody.contains("creditcard")
        
        return hasUpiKeyword && hasDebit && hasAmount && !isCreditCard
    }
    
    fun extractAmount(body: String): Double? {
        for (pattern in debitPatterns) {
            pattern.find(body)?.let { match ->
                val amountStr = match.groupValues[1].replace(",", "")
                amountStr.toDoubleOrNull()?.let { if (it > 0) return it }
            }
        }
        return null
    }
}
