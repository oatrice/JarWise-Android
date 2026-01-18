package com.oatrice.jarwise.utils

import com.oatrice.jarwise.data.Transaction
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object TransactionDisplayUtils {

    fun getDisplayDetails(transaction: Transaction): Pair<String, String> {
        return if (transaction.note.isNotBlank()) {
            transaction.note to transaction.jarId.replaceFirstChar { it.uppercase() } // Simple capitalization for now
        } else {
            transaction.jarId.replaceFirstChar { it.uppercase() } to ""
        }
    }

    fun formatCurrency(amount: Double, currencyCode: String): String {
        return try {
            val format = NumberFormat.getCurrencyInstance(Locale.US) as java.text.DecimalFormat
            val currency = Currency.getInstance(currencyCode)
            format.currency = currency
            
            val symbol = when (currencyCode) {
                "THB" -> "฿"
                "USD" -> "$"
                "EUR" -> "€"
                "JPY" -> "¥"
                else -> currency.getSymbol(Locale.US)
            }
            
            val dfs = format.decimalFormatSymbols
            dfs.currencySymbol = symbol
            format.decimalFormatSymbols = dfs
            
            format.maximumFractionDigits = currency.defaultFractionDigits
            format.minimumFractionDigits = currency.defaultFractionDigits
            
            format.format(amount)
        } catch (e: Exception) {
            "$currencyCode $amount"
        }
    }
}
