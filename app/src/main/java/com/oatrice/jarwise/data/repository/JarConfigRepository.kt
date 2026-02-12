package com.oatrice.jarwise.data.repository

import com.oatrice.jarwise.data.JarConfig
import com.oatrice.jarwise.data.JarConfigDao
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing jar configurations
 */
class JarConfigRepository(private val jarConfigDao: JarConfigDao) : JarConfigSource {
    
    /**
     * Get all jar configs as Flow (reactive updates)
     */
    fun getAllJarConfigsFlow(): Flow<List<JarConfig>> = jarConfigDao.getAllFlow()
    
    /**
     * Get all jar configs (one-shot)
     */
    override suspend fun getAllJarConfigs(): List<JarConfig> = jarConfigDao.getAll()
    
    /**
     * Get jar config by ID
     */
    suspend fun getJarConfigById(id: String): JarConfig? = jarConfigDao.getById(id)
    
    /**
     * Update a single jar config
     */
    suspend fun updateJarConfig(config: JarConfig) = jarConfigDao.update(config)
    
    /**
     * Save all jar configs (replace all)
     */
    suspend fun saveAllJarConfigs(configs: List<JarConfig>) = jarConfigDao.insertAll(configs)
    
    /**
     * Reset to default 6 Jars configuration
     */
    suspend fun resetToDefaults() {
        jarConfigDao.deleteAll()
        jarConfigDao.insertAll(JarConfig.DEFAULTS)
    }
    
    /**
     * Initialize default jars if database is empty
     */
    override suspend fun initializeDefaultsIfEmpty() {
        if (jarConfigDao.count() == 0) {
            jarConfigDao.insertAll(JarConfig.DEFAULTS)
        }
    }
}
