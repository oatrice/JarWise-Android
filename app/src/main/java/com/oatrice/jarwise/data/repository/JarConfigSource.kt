package com.oatrice.jarwise.data.repository

import com.oatrice.jarwise.data.JarConfig

interface JarConfigSource {
    suspend fun getAllJarConfigs(): List<JarConfig>
    suspend fun initializeDefaultsIfEmpty()
}
