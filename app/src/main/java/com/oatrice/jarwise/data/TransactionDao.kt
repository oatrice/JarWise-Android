package com.oatrice.jarwise.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE status = 'draft' ORDER BY date DESC")
    fun getDrafts(): Flow<List<Transaction>>

    @Query("SELECT COUNT(*) FROM transactions WHERE status = 'draft'")
    fun getDraftCount(): Flow<Int>

    @Insert
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Query("UPDATE transactions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
    
    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
