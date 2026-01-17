package com.oatrice.jarwise.data.service

object SlipDetectionLogic {
    
    // PromptPay QR payload usually starts with 000201
    // or contains specific bank identifiers.
    // For now, we consider any non-URL QR code or specific financial patterns as a strong signal.
    fun followsSlipQrPattern(rawValue: String?): Boolean {
        if (rawValue == null) return false
        // Basic check for EMVCo (PromptPay) standard
        return rawValue.startsWith("000201") || rawValue.contains("Transfer", ignoreCase = true)
    }

    fun containsSlipKeywords(text: String): Boolean {
        val keywords = listOf(
            "Transfer", "โอนเงินสำเร็จ", "Baht", "บาท", "Ref.", "Slip", 
            "Transaction ID", "รหัสอ้างอิง", "จำนวนเงิน", "Amount",
            "Banking", "Mobile Banking"
        )
        // Heuristic: Must contain at least 2 keywords to be confident
        val foundCount = keywords.count { text.contains(it, ignoreCase = true) }
        return foundCount >= 2
    }
}
