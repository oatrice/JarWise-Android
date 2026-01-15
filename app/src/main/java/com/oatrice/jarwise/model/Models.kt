package com.oatrice.jarwise.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Jar(
    val id: String,
    val name: String,
    val current: Double,
    val goal: Double,
    val level: Int,
    val icon: ImageVector,
    val color: Color, // Background/Tint color
    val shadowColor: Color, // Glow color
    val barColor: Color // Progress bar color
)

data class Transaction(
    val id: String,
    val merchant: String,
    val category: String,
    val date: String,
    val amount: Double,
    val icon: ImageVector,
    val color: Color, // Icon background tint
    val iconTint: Color // Icon foreground color
)
