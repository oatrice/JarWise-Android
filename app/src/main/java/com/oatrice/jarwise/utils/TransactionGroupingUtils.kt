package com.oatrice.jarwise.utils

import com.oatrice.jarwise.data.Transaction
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class representing a group of transactions for a single day
 */
data class DailyTransactionGroup(
    val dateKey: String,      // YYYY-MM-DD format for sorting
    val dateHeader: String,   // Display text: "Today", "Yesterday", or "Jan 30, 2026"
    val transactions: List<Transaction>,
    val totalIncome: Double,
    val totalExpense: Double
)

/**
 * Utility class for grouping transactions by date
 */
object TransactionGroupingUtils {
    
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    private val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    
    /**
     * Groups transactions by date and calculates daily totals
     * @param transactions List of transactions to group
     * @return List of DailyTransactionGroup sorted by date descending (most recent first)
     */
    fun groupByDate(transactions: List<Transaction>): List<DailyTransactionGroup> {
        if (transactions.isEmpty()) return emptyList()
        
        // Filter out the "income" side of transfers to show only one unified row
        val visibleTransactions = transactions.filter { tx ->
            !(tx.type == "income" && tx.linkedTransactionId != null)
        }
        
        // Group by date key
        val grouped = visibleTransactions.groupBy { transaction ->
            try {
                val date = isoFormat.parse(transaction.date)
                dateKeyFormat.format(date!!)
            } catch (e: Exception) {
                transaction.date.take(10) // Fallback: take first 10 chars
            }
        }
        
        // Convert to DailyTransactionGroup list
        return grouped.map { (dateKey, txs) ->
            val sortedTxs = txs.sortedByDescending { it.date }
            
            val totalIncome = sortedTxs
                .filter { it.type == "income" && it.linkedTransactionId == null }
                .sumOf { kotlin.math.abs(it.amount) }
                
            val totalExpense = sortedTxs
                .filter { it.type == "expense" && it.linkedTransactionId == null }
                .sumOf { -kotlin.math.abs(it.amount) }
            
            DailyTransactionGroup(
                dateKey = dateKey,
                dateHeader = formatDateHeader(dateKey),
                transactions = sortedTxs,
                totalIncome = totalIncome,
                totalExpense = totalExpense
            )
        }.sortedByDescending { it.dateKey }
    }
    
    /**
     * Formats date key to display header
     * "Today", "Yesterday", or "Jan 30, 2026"
     */
    private fun formatDateHeader(dateKey: String): String {
        val today = dateKeyFormat.format(Date())
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }.let { dateKeyFormat.format(it.time) }
        
        return when (dateKey) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> {
                try {
                    val date = dateKeyFormat.parse(dateKey)
                    displayFormat.format(date!!)
                } catch (e: Exception) {
                    dateKey
                }
            }
        }
    }
}
