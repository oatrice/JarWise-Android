package com.oatrice.jarwise.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val note: String,
    val jarId: String,
    val walletId: String = "wallet-cash", // Default to cash
    val date: String, // ISO 8601 string
    val type: String = "expense", // "income" | "expense"
    val status: String = "completed" // "draft" | "completed"
)

