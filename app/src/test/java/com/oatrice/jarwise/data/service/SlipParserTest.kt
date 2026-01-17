package com.oatrice.jarwise.data.service

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar

class SlipParserTest {

    private val parser = SlipParser()

    @Test
    fun parse_extractsAmountCorrectly_simple() {
        val text = """
            Transfer Successful
            Amount 500.00 THB
            To Mr. A
        """.trimIndent()

        val result = parser.parse(text)
        assertEquals(500.00, result.amount)
    }

    @Test
    fun parse_extractsAmountCorrectly_withComma() {
        val text = """
            Payment
            Total: 1,250.50 Baht
        """.trimIndent()

        val result = parser.parse(text)
        assertEquals(1250.50, result.amount)
    }

    @Test
    fun parse_extractsAmountCorrectly_kplusStyle() {
        val text = """
            จำนวนเงิน
            30,000.00 บาท
        """.trimIndent()

        val result = parser.parse(text)
        assertEquals(30000.00, result.amount)
    }

    @Test
    fun parse_extractsDateCorrectly_ddMMMyyyy() {
        // e.g., 24 Dec 2024
        val text = """
            Transfer on 24 Dec 2024 14:30
            Amount 100.00
        """.trimIndent()

        val result = parser.parse(text)
        // 24 Dec = Month 11 (0-indexed)
        val cal = Calendar.getInstance().apply {
            time = result.date!!
        }
        assertEquals(2024, cal.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH))
        assertEquals(24, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun parse_extractsDateCorrectly_slashFormat() {
        // e.g., 15/01/2024
        val text = """
            Date: 15/01/2024
            Time: 10:00
        """.trimIndent()

        val result = parser.parse(text)
        val cal = Calendar.getInstance().apply {
            time = result.date!!
        }
        assertEquals(2024, cal.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH))
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun parse_extractsBankCorrectly_kbank() {
        val text = """
            Kasikorn Bank
            Amount 500.00
        """.trimIndent()
        val result = parser.parse(text)
        assertEquals("Kasikorn Bank", result.bankName)
    }

    @Test
    fun parse_extractsBankCorrectly_scb() {
        val text = """
            SCB EASY
            Amount 1,000.00
        """.trimIndent()
        val result = parser.parse(text)
        assertEquals("Siam Commercial Bank", result.bankName)
    }

    @Test
    fun parse_extractsBankCorrectly_bbl() {
        val text = """
            Bualuang mBanking
            Amount 200.00
        """.trimIndent()
        val result = parser.parse(text)
        assertEquals("Bangkok Bank", result.bankName)
    }
    @Test
    fun parse_extractsJarCorrectly_byKeyword() {
        // Test Necessities
        val text711 = "Payment to 7-Eleven\nAmount 50.00"
        assertEquals("necessities", parser.parse(text711).jarId)

        val textGrab = "Grab Transport\nAmount 120.00"
        assertEquals("necessities", parser.parse(textGrab).jarId)

        val textTops = "Tops Market\nAmount 500.00"
        assertEquals("necessities", parser.parse(textTops).jarId)

        // Test Play
        val textNetflix = "Netflix Subscription\nAmount 419.00"
        assertEquals("play", parser.parse(textNetflix).jarId)

        val textSteam = "Steam Games\nAmount 800.00"
        assertEquals("play", parser.parse(textSteam).jarId)

        // Test Unknown -> null
        val textUnknown = "Transfer to Mr. Nobody\nAmount 1000.00"
        assertEquals(null, parser.parse(textUnknown).jarId)
    }
}
