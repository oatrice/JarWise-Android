package com.oatrice.jarwise.data.service

import com.oatrice.jarwise.data.model.ParsedSlip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SlipParser {
    
    fun parse(text: String): ParsedSlip {
        val amount = extractAmount(text)
        val date = extractDate(text)
        val bankName = extractBankName(text)
        val jarId = extractJarId(text)
        return ParsedSlip(
            rawText = text,
            amount = amount,
            date = date,
            bankName = bankName,
            jarId = jarId
        )
    }

    private fun extractJarId(text: String): String? {
        val keywords = mapOf(
            "necessities" to listOf("Home", "Electricity", "Water"),
            "play" to listOf("Grab", "Tops", "Lotus", "Big C", "7-Eleven", "Netflix", "Steam", "Spotify", "Disney+", "YouTube"),
            "education" to listOf("Udemy", "Coursera", "Book", "Kindle"),
            "savings" to listOf("Deposit", "Invesment"),
            "give" to listOf("Donation", "Charity")
        )

        for ((jarId, clues) in keywords) {
            if (clues.any { text.contains(it, ignoreCase = true) }) {
                return jarId
            }
        }
        return null
    }

    private fun extractBankName(text: String): String? {
        // Simple heuristic - order matters loosely
        if (text.contains("Kasikorn", ignoreCase = true) || text.contains("KBank", ignoreCase = true) || text.contains("กสิกร", ignoreCase = true)) {
            return "Kasikorn Bank"
        }
        if (text.contains("SCB", ignoreCase = true) || text.contains("Siam Commercial", ignoreCase = true) || text.contains("ไทยพาณิชย์", ignoreCase = true)) {
            return "Siam Commercial Bank"
        }
        if (text.contains("Bangkok Bank", ignoreCase = true) || text.contains("BBL", ignoreCase = true) || text.contains("Bualuang", ignoreCase = true) || text.contains("กรุงเทพ", ignoreCase = true)) {
            return "Bangkok Bank"
        }
        if (text.contains("Krungthai", ignoreCase = true) || text.contains("KTB", ignoreCase = true) || text.contains("กรุงไทย", ignoreCase = true)) {
            return "Krungthai Bank"
        }
        if (text.contains("Krungsri", ignoreCase = true) || text.contains("BAY", ignoreCase = true) || text.contains("กรุงศรี", ignoreCase = true)) {
            return "Krungsri Bank"
        }
        if (text.contains("TTB", ignoreCase = true) || text.contains("TMB", ignoreCase = true) || text.contains("Thanachart", ignoreCase = true) || text.contains("ทีทีบี", ignoreCase = true)) {
            return "TMBThanachart Bank"
        }
        return null
    }

    private fun extractAmount(text: String): Double? {
        val patterns = listOf(
            // Matches: Amount 500.00, Total: 1,250.00, จำนวนเงิน\n30,000.00
            // \D*? skips non-digits (spaces, colons, newlines) until the number
            Regex("(?:Amount|Total|จำนวนเงิน)\\D*?([\\d,]+\\.\\d{2})", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val valueStr = match.groupValues[1].replace(",", "")
                return valueStr.toDoubleOrNull()
            }
        }
        return null
    }

    private fun extractDate(text: String): Date? {
        // 1. dd MMM yyyy (e.g., 24 Dec 2024)
        val ddMmmYyyy = Regex("(\\d{1,2})\\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+(\\d{4})", RegexOption.IGNORE_CASE)
        val match1 = ddMmmYyyy.find(text)
        if (match1 != null) {
            val dateStr = match1.value
            // Normalize spaces to single space for parsing
            val normalized = dateStr.replace(Regex("\\s+"), " ")
            // Try parsing with US locale
            return tryParseDate(normalized, "d MMM yyyy", Locale.US)
        }

        // 2. dd/MM/yyyy (e.g., 15/01/2024)
        val slashFormat = Regex("(\\d{1,2})[/-](\\d{1,2})[/-](\\d{4})")
        val match2 = slashFormat.find(text)
        if (match2 != null) {
             return tryParseDate(match2.value.replace("-", "/"), "dd/MM/yyyy", Locale.US)
        }

        return null
    }

    private fun tryParseDate(dateStr: String, pattern: String, locale: Locale): Date? {
        return try {
            SimpleDateFormat(pattern, locale).parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }
}
