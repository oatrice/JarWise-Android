package com.oatrice.jarwise.utils

import com.oatrice.jarwise.data.Transaction
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionDisplayUtilsTest {

    @Test
    fun `getDisplayDetails - with note`() {
        val transaction = Transaction(
            id = 1, amount = 100.0, date = "2024-05-20", 
            note = "Buying Milk", jarId = "Necessities"
        )
        
        val result = TransactionDisplayUtils.getDisplayDetails(transaction)
        
        assertEquals("Buying Milk", result.first) // Title is Note
        assertEquals("Necessities", result.second) // Subtitle is Jar Name
    }

    @Test
    fun `getDisplayDetails - empty note`() {
        val transaction = Transaction(
            id = 1, amount = 100.0, date = "2024-05-20", 
            note = "", jarId = "Savings"
        )
        
        val result = TransactionDisplayUtils.getDisplayDetails(transaction)
        
        assertEquals("Savings", result.first)
        assertEquals("", result.second) // Subtitle empty
    }

    @Test
    fun `getDisplayDetails - blank note`() {
        // Just checking how strings are handled
        val transaction = Transaction(
            id = 1, amount = 100.0, date = "2024-05-20", 
            note = "   ", jarId = "Savings"
        )
        
        val result = TransactionDisplayUtils.getDisplayDetails(transaction)
        
        assertEquals("Savings", result.first)
        assertEquals("", result.second)
    }

    @Test
    fun getDisplayDetails_withoutNote_returnsJarNameAsTitle() {
        val transaction = Transaction(
            id = 1,
            amount = 100.0,
            date = "2024-05-20",
            note = "",
            jarId = "necessities" // Use ID string
        )

        val (title, subtitle) = TransactionDisplayUtils.getDisplayDetails(transaction)

        assertEquals("Necessities", title)
        assertEquals("", subtitle)
    }

    @Test
    fun formatCurrency_thb_returnsCorrectFormat() {
        val result = TransactionDisplayUtils.formatCurrency(1000.0, "THB")
        assertEquals("฿1,000.00", result)
    }

    @Test
    fun formatCurrency_usd_returnsCorrectFormat() {
        val result = TransactionDisplayUtils.formatCurrency(1250.5, "USD")
        assertEquals("$1,250.50", result)
    }

    @Test
    fun formatCurrency_eur_returnsCorrectFormat() {
        val result = TransactionDisplayUtils.formatCurrency(0.0, "EUR")
        assertEquals("€0.00", result)
    }

    @Test
    fun formatCurrency_jpy_returnsCorrectFormat() {
        val result = TransactionDisplayUtils.formatCurrency(5000.0, "JPY")
        assertEquals("¥5,000", result)
    }
}
