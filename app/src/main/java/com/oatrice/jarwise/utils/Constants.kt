package com.oatrice.jarwise.utils

import androidx.compose.ui.graphics.Color

data class JarMetadata(
    val id: String,
    val name: String,
    val icon: String, // Emoji
    val color: Color
)

val JARS_METADATA = listOf(
    JarMetadata("necessities", "Necessities", "🏠", Color(0xFF3B82F6)),
    JarMetadata("education", "Education", "📚", Color(0xFF22C55E)),
    JarMetadata("savings", "Savings", "🐷", Color(0xFFEAB308)),
    JarMetadata("play", "Play", "🎮", Color(0xFFEC4899)),
    JarMetadata("investment", "Investment", "📈", Color(0xFFA855F7)),
    JarMetadata("give", "Give", "🎁", Color(0xFFEF4444)),
)

// Fallbacks
val UNKNOWN_JAR = JarMetadata("unknown", "Unknown", "❓", Color.Gray)
val UNKNOWN_WALLET = WalletMetadata("unknown", "Unknown", "❓", Color.Gray)

val TRANSFER_OUT_JAR = JarMetadata("transfer-out", "Transfer Out", "💸", Color(0xFFF87171)) // Red 400
val TRANSFER_IN_JAR = JarMetadata("transfer-in", "Transfer In", "💰", Color(0xFF4ADE80)) // Green 400

fun getJarDetails(jarId: String): JarMetadata {
    if (jarId == "transfer-out") return TRANSFER_OUT_JAR
    if (jarId == "transfer-in") return TRANSFER_IN_JAR
    return JARS_METADATA.find { it.id == jarId } ?: UNKNOWN_JAR
}

// Wallet Metadata
data class WalletMetadata(
    val id: String,
    val name: String,
    val icon: String, // Emoji
    val color: Color
)

val WALLETS_METADATA = listOf(
    WalletMetadata("wallet-cash", "Cash", "💵", Color(0xFF22C55E)),
    WalletMetadata("wallet-bank", "Bank Account", "🏦", Color(0xFF3B82F6)),
    WalletMetadata("wallet-credit", "Credit Card", "💳", Color(0xFFA855F7)),
)

fun getWalletDetails(walletId: String): WalletMetadata {
    return WALLETS_METADATA.find { it.id == walletId } ?: UNKNOWN_WALLET
}

