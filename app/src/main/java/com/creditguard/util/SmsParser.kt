package com.creditguard.util

import com.creditguard.data.model.Transaction

object SmsParser {
    
    private val amountPatterns = listOf(
        Regex("""(?:Rs\.?|INR|₹)\s*(\d[\d,]{0,11}(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""(\d[\d,]{0,11}(?:\.\d{1,2})?)\s*(?:Rs\.?|INR|₹)""", RegexOption.IGNORE_CASE)
    )
    
    private val cardLast4Patterns = listOf(
        Regex("""(?:credit\s*card|card).{0,20}?[xX*]{2,12}(\d{4})""", RegexOption.IGNORE_CASE),
        Regex("""(?:credit\s*card|card).{0,20}?(?:ending|ends)\s*(?:with\s+)?(\d{4})""", RegexOption.IGNORE_CASE),
        Regex("""[xX*]{4,12}(\d{4})(?=.*(?:credit|card))""", RegexOption.IGNORE_CASE)
    )
    
    private val merchantPatterns = listOf(
        Regex("""(?:at|@)\s+([A-Za-z0-9\s&'._-]{2,35})(?:\s+on|\s+for|\s+via|\.|,|$)""", RegexOption.IGNORE_CASE),
        Regex("""(?:to|towards)\s+([A-Za-z0-9\s&'._-]{2,35})(?:\s+on|\s+Ref|\.|,|$)""", RegexOption.IGNORE_CASE)
    )
    
    private val bankKeywords = mapOf(
        "HDFC" to "HDFC", "ICICI" to "ICICI", "SBI" to "SBI", "AXIS" to "Axis",
        "KOTAK" to "Kotak", "CITI" to "Citi", "AMEX" to "Amex", "INDUS" to "IndusInd",
        "YES" to "Yes Bank", "RBL" to "RBL", "IDFC" to "IDFC", "FEDERAL" to "Federal",
        "HSBC" to "HSBC", "SCB" to "SC", "ONECARD" to "OneCard", "SLICE" to "Slice"
    )
    
    // Must contain one of these to be a credit card transaction
    private val creditCardIndicators = listOf(
        "credit card", "creditcard", "cc ", " cc", "cr card",
        "rupay credit", "visa credit", "mastercard", "credit a/c"
    )
    
    private val spendKeywords = listOf("spent", "debited", "charged", "purchase", "transaction", "txn")
    
    private val excludeKeywords = listOf(
        "otp", "cvv", "pin", "password", "limit increased", "due date", 
        "bill generated", "emi", "reward", "cashback", "offer", "apply",
        "debit card", "debitcard", "savings", "current a/c"
    )
    
    fun isCreditCardSpend(sender: String, body: String): Boolean {
        if (sender.length > 20 || body.length > 500) return false
        
        val lowerBody = body.lowercase()
        
        // Must explicitly mention credit card
        val isCreditCard = creditCardIndicators.any { lowerBody.contains(it) }
        if (!isCreditCard) return false
        
        // Exclude non-transaction messages
        if (excludeKeywords.any { lowerBody.contains(it) }) return false
        
        val hasSpendKeyword = spendKeywords.any { lowerBody.contains(it) }
        val hasAmount = amountPatterns.any { it.containsMatchIn(body) }
        
        return hasSpendKeyword && hasAmount
    }
    
    fun parse(sender: String, body: String): Transaction? {
        if (!isCreditCardSpend(sender, body)) return null
        
        val amount = extractAmount(body) ?: return null
        if (amount <= 0 || amount > 10000000) return null
        
        val cardLast4 = extractCardLast4(body) ?: "****"
        val merchant = extractMerchant(body) ?: "Unknown"
        val bank = detectBank(sender, body)
        
        return Transaction(
            amount = amount,
            merchant = merchant.trim().take(30),
            cardLast4 = cardLast4,
            bank = bank,
            rawSms = body.take(200)
        )
    }
    
    private fun extractAmount(body: String): Double? {
        for (pattern in amountPatterns) {
            pattern.find(body)?.let { match ->
                val amountStr = match.groupValues[1].replace(",", "")
                amountStr.toDoubleOrNull()?.let { if (it > 0) return it }
            }
        }
        return null
    }
    
    private fun extractCardLast4(body: String): String? {
        for (pattern in cardLast4Patterns) {
            pattern.find(body)?.groupValues?.get(1)?.let { return it }
        }
        // Fallback: find any 4 digits after XX pattern
        Regex("""[xX*]{2,12}(\d{4})""").find(body)?.let { return it.groupValues[1] }
        return null
    }
    
    private fun extractMerchant(body: String): String? {
        for (pattern in merchantPatterns) {
            pattern.find(body)?.let { 
                val merchant = it.groupValues[1].trim()
                if (merchant.length > 2 && !merchant.all { c -> c.isDigit() }) return merchant
            }
        }
        return null
    }
    
    private fun detectBank(sender: String, body: String): String {
        val combined = "$sender $body".uppercase()
        for ((key, name) in bankKeywords) {
            if (combined.contains(key)) return name
        }
        return "Bank"
    }
}
