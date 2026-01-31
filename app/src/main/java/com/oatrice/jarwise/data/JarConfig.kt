package com.oatrice.jarwise.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for storing jar configuration
 * Allows users to customize jar name, percentage, color, and icon
 */
@Entity(tableName = "jar_configs")
data class JarConfig(
    @PrimaryKey val id: String,
    val name: String,
    val percentage: Int,      // 0-100, total across all jars must = 100
    val colorName: String,    // "blue", "green", "pink", "yellow", "purple", "red", "cyan", "orange"
    val iconName: String      // "home", "dollar", "gamepad", "school", "flight", "heart", "work", "savings"
) {
    companion object {
        /**
         * Default 6 Jars configuration based on T. Harv Eker's money management system
         */
        val DEFAULTS = listOf(
            JarConfig("1", "Necessities", 55, "blue", "home"),
            JarConfig("2", "Financial Freedom", 10, "green", "dollar"),
            JarConfig("3", "Play", 10, "pink", "gamepad"),
            JarConfig("4", "Education", 10, "yellow", "school"),
            JarConfig("5", "Long-term Savings", 10, "purple", "flight"),
            JarConfig("6", "Give", 5, "red", "heart")
        )
    }
}
