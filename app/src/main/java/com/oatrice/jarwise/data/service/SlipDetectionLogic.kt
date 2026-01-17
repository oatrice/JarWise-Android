package com.oatrice.jarwise.data.service

object SlipDetectionLogic {
    
    // PromptPay QR payload usually starts with 000201
    // BUT Transfer Slips often start with ID 00 and Length XX, followed by 0006000001 (API ID?)
    // Example: 00400006000001... or 00330006000001...
    fun followsSlipQrPattern(rawValue: String?): Boolean {
        if (rawValue == null) return false
        // Basic check for EMVCo (PromptPay) standard
        if (rawValue.startsWith("000201")) return true
        
        // Check for Thai Transfer Slip ID (0006000001) appearing early in the string
        // This handles cases where ID 00 has length > 02 (e.g. 0033... or 0040...)
        if (rawValue.contains("0006000001")) return true
        
        return rawValue.contains("Transfer", ignoreCase = true)
    }

    fun containsSlipKeywords(text: String): Boolean {
        val keywords = listOf(
            "Transfer", "โอนเงินสำเร็จ", "Baht", "บาท", "Ref.", "Slip", 
            "Transaction ID", "รหัสอ้างอิง", "จำนวนเงิน", "Amount",
            "Banking", "Mobile Banking",
            // Added based on feedback
            "THB", "Prompt", "Pay", "Prompt Pay"
        )
        // Heuristic: Must contain at least 2 keywords to be confident
        // Note: "Prompt Pay" might count as 2 if split, so be careful or just accept it.
        // Actually "Prompt Pay" contains "Prompt" and "Pay", so "Prompt Pay" string is redundant in list if we scan for words, 
        // but here we scan for substrings.
        val foundCount = keywords.count { text.contains(it, ignoreCase = true) }
        return foundCount >= 2
    }
}
