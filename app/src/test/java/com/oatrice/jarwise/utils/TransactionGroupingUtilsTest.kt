package com.oatrice.jarwise.utils

import com.oatrice.jarwise.data.Transaction
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

/**
 * TDD Tests for Transaction Grouping functionality
 * Issue #46: Phase 2 - Transaction Grouping + Daily Totals
 */
class TransactionGroupingUtilsTest {

    // ===========================================
    // 🟥 RED Phase: Tests for Transaction Grouping
    // ===========================================

    @Test
    fun `groupTransactionsByDate should return empty list for empty transactions`() {
        val result = TransactionGroupingUtils.groupByDate(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `groupTransactionsByDate should group transactions by same date`() {
        val transactions = listOf(
            createTransaction(1, 100.0, "2026-01-30T10:00:00.000Z"),
            createTransaction(2, 200.0, "2026-01-30T15:00:00.000Z"),
            createTransaction(3, 300.0, "2026-01-29T10:00:00.000Z")
        )
        
        val result = TransactionGroupingUtils.groupByDate(transactions)
        
        assertEquals(2, result.size) // 2 different dates
        assertEquals(2, result[0].transactions.size) // Jan 30 has 2 transactions
        assertEquals(1, result[1].transactions.size) // Jan 29 has 1 transaction
    }

    @Test
    fun `groupTransactionsByDate should calculate daily totals correctly`() {
        // Use same date with different midday hours to avoid timezone edge cases
        val transactions = listOf(
            createTransaction(1, -100.0, "2026-01-15T08:00:00.000Z", type = "expense"),
            createTransaction(2, 500.0, "2026-01-15T09:00:00.000Z", type = "income"),
            createTransaction(3, -50.0, "2026-01-15T10:00:00.000Z", type = "expense")
        )
        
        val result = TransactionGroupingUtils.groupByDate(transactions)
        
        assertEquals(1, result.size)
        assertEquals(500.0, result[0].totalIncome, 0.01)
        assertEquals(-150.0, result[0].totalExpense, 0.01)
    }

    @Test
    fun `groupTransactionsByDate should format date header correctly`() {
        val today = Date()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        
        val transactions = listOf(
            createTransaction(1, 100.0, sdf.format(today))
        )
        
        val result = TransactionGroupingUtils.groupByDate(transactions)
        
        assertEquals("Today", result[0].dateHeader)
    }

    @Test
    fun `groupTransactionsByDate should sort groups by date descending`() {
        val transactions = listOf(
            createTransaction(1, 100.0, "2026-01-28T10:00:00.000Z"),
            createTransaction(2, 200.0, "2026-01-30T15:00:00.000Z"),
            createTransaction(3, 300.0, "2026-01-29T10:00:00.000Z")
        )
        
        val result = TransactionGroupingUtils.groupByDate(transactions)
        
        assertEquals(3, result.size)
        // First group should be Jan 30 (most recent)
        assertTrue(result[0].dateKey > result[1].dateKey)
        assertTrue(result[1].dateKey > result[2].dateKey)
    }

    // Helper function
    private fun createTransaction(
        id: Long,
        amount: Double,
        date: String,
        type: String = "expense",
        status: String = "completed"
    ): Transaction {
        return Transaction(
            id = id,
            amount = amount,
            note = "Test Transaction $id",
            jarId = "necessities",
            date = date,
            type = type,
            status = status
        )
    }
}
