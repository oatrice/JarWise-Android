package com.oatrice.jarwise.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for JarConfig CRUD operations
 */
@Dao
interface JarConfigDao {
    
    @Query("SELECT * FROM jar_configs ORDER BY id")
    fun getAllFlow(): Flow<List<JarConfig>>
    
    @Query("SELECT * FROM jar_configs ORDER BY id")
    suspend fun getAll(): List<JarConfig>
    
    @Query("SELECT * FROM jar_configs WHERE id = :id")
    suspend fun getById(id: String): JarConfig?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(configs: List<JarConfig>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: JarConfig)
    
    @Update
    suspend fun update(config: JarConfig)
    
    @Query("DELETE FROM jar_configs")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM jar_configs")
    suspend fun count(): Int
}
