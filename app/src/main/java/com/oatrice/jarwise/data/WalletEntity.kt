package com.oatrice.jarwise.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val id: String,
    val name: String,
    val balance: Double,
    val colorArgb: Int, // Store Color.toArgb()
    val iconName: String, // Store icon name (e.g. "AccountBalance")
    val parentId: String?,
    val level: Int
)
