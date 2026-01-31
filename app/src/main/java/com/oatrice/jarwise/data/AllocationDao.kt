package com.oatrice.jarwise.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AllocationDao {
    @Query("SELECT * FROM allocations WHERE userId = :userId AND parentId IS NULL ORDER BY sortOrder ASC")
    fun getTopLevelJars(userId: String): Flow<List<Allocation>>

    @Query("SELECT * FROM allocations WHERE userId = :userId AND parentId = :parentId ORDER BY sortOrder ASC")
    fun getChildrenOf(userId: String, parentId: Long): Flow<List<Allocation>>

    @Query("SELECT * FROM allocations WHERE userId = :userId AND id = :id")
    suspend fun getById(userId: String, id: Long): Allocation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(allocation: Allocation): Long

    @Update
    suspend fun update(allocation: Allocation)

    @Delete
    suspend fun delete(allocation: Allocation)

    @Query("UPDATE allocations SET parentId = NULL, level = 0, targetPercent = 0 WHERE id = :id AND userId = :userId")
    suspend fun promoteToJar(userId: String, id: Long)

    @Query("UPDATE allocations SET parentId = :newParentId, level = 1, targetPercent = NULL WHERE id = :id AND userId = :userId")
    suspend fun demoteToCategory(userId: String, id: Long, newParentId: Long)
}
