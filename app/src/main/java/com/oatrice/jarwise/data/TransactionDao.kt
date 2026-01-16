package com.oatrice.jarwise.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<Transaction>>

    @Insert
    suspend fun insert(transaction: Transaction)
    
    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
