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

fun getJarDetails(jarId: String): JarMetadata {
    return JARS_METADATA.find { it.id == jarId } ?: JARS_METADATA[0]
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
    return WALLETS_METADATA.find { it.id == walletId } ?: WALLETS_METADATA[0]
}

