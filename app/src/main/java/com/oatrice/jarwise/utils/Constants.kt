package com.oatrice.jarwise.utils

import androidx.compose.ui.graphics.Color

data class JarMetadata(
    val id: String,
    val name: String,
    val icon: String, // Emoji
    val color: Color
)

val JARS_METADATA = listOf(
    JarMetadata("necessities", "Necessities", "🏠", Color(0xFF3B82F6)), // Blue
    JarMetadata("education", "Education", "📚", Color(0xFF22C55E)),   // Green
    JarMetadata("savings", "Savings", "🐷", Color(0xFFEAB308)),     // Yellow
    JarMetadata("play", "Play", "🎮", Color(0xFFEC4899)),        // Pink
    JarMetadata("investment", "Investment", "📈", Color(0xFFA855F7)), // Purple
    JarMetadata("give", "Give", "🎁", Color(0xFFEF4444))          // Red
)

fun getJarDetails(jarId: String): JarMetadata {
    return JARS_METADATA.find { it.id == jarId } ?: JARS_METADATA[0]
}
