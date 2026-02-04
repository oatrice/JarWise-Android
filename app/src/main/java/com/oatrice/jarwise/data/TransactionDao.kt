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
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Query("UPDATE transactions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
    
    @androidx.room.Delete
    suspend fun delete(transaction: Transaction)

    @Query("UPDATE transactions SET linkedTransactionId = NULL WHERE id = :id")
    suspend fun unlinkTransaction(id: Long)

    @Query("UPDATE transactions SET linkedTransactionId = NULL WHERE linkedTransactionId = :idStr")
    suspend fun unlinkRelatedTransaction(idStr: String)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
