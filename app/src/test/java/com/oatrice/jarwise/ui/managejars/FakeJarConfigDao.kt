package com.oatrice.jarwise.ui.managejars

import com.oatrice.jarwise.data.JarConfig
import com.oatrice.jarwise.data.JarConfigDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeJarConfigDao : JarConfigDao {

    private val db = MutableStateFlow<List<JarConfig>>(emptyList())

    override fun getAllFlow(): Flow<List<JarConfig>> {
        return db.asStateFlow()
    }

    override suspend fun getAll(): List<JarConfig> {
        return db.value
    }

    override suspend fun getById(id: String): JarConfig? {
        return db.value.find { it.id == id }
    }

    override suspend fun insertAll(configs: List<JarConfig>) {
        db.value = configs.toList() // Force new instance
    }

    override suspend fun insert(config: JarConfig) {
        db.update { list ->
            val mutableList = list.toMutableList()
            val index = mutableList.indexOfFirst { it.id == config.id }
            if (index != -1) {
                mutableList[index] = config
            } else {
                mutableList.add(config)
            }
            mutableList
        }
    }

    override suspend fun update(config: JarConfig) {
        insert(config) // Reuse insert logic which handles replace
    }

    override suspend fun deleteAll() {
        db.value = emptyList()
    }

    override suspend fun count(): Int {
        return db.value.size
    }
}
