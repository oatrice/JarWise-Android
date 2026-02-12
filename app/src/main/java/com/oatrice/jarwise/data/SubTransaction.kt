package com.oatrice.jarwise.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sub_transactions",
    foreignKeys = [
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["parentId"])]
)
data class SubTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val parentId: Long,
    val description: String,
    val amount: Double
)
