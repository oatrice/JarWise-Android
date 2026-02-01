package com.oatrice.jarwise.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "allocations",
    foreignKeys = [ForeignKey(
        entity = Allocation::class,
        parentColumns = ["id"],
        childColumns = ["parentId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["userId", "parentId"]), Index(value = ["parentId"])]
)
data class Allocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,          // 🔒 Critical for IDOR protection
    val name: String,
    val parentId: Long? = null,  // NULL = top-level Jar
    val level: Int = 0,          // 0 = Jar, 1 = Category
    val targetPercent: Int? = null,
    val icon: String = "home",
    val color: String = "blue",
    val sortOrder: Int = 0,
    val isSystemDefault: Boolean = false,
    val isActive: Boolean = true
)
