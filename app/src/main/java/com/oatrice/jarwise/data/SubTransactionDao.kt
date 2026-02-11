package com.oatrice.jarwise.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SubTransactionDao {
    @Query("SELECT * FROM sub_transactions WHERE parentId = :parentId")
    fun getByParentId(parentId: Long): Flow<List<SubTransaction>>

    @Insert
    suspend fun insert(subTransaction: SubTransaction): Long

    @Delete
    suspend fun delete(subTransaction: SubTransaction)

    @Query("DELETE FROM sub_transactions WHERE parentId = :parentId")
    suspend fun deleteAllByParentId(parentId: Long)
}
