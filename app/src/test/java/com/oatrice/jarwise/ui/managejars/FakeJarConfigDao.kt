package com.oatrice.jarwise.ui.managejars

import com.oatrice.jarwise.data.Allocation
import com.oatrice.jarwise.data.AllocationDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeJarConfigDao : AllocationDao {

    private val db = MutableStateFlow<List<Allocation>>(emptyList())


    override fun getTopLevelJars(userId: String): Flow<List<Allocation>> {
        return db.map { list -> 
            list.filter { it.userId == userId && it.parentId == null }
                .sortedBy { it.sortOrder }
        }
    }

    override fun getChildrenOf(userId: String, parentId: Long): Flow<List<Allocation>> {
        return db.map { list ->
            list.filter { it.userId == userId && it.parentId == parentId }
                .sortedBy { it.sortOrder }
        }
    }

    override suspend fun getById(userId: String, id: Long): Allocation? {
        return db.value.find { it.userId == userId && it.id == id }
    }

    override fun getAllAllocations(userId: String): Flow<List<Allocation>> {
        return db.map { list ->
            list.filter { it.userId == userId && it.isActive }
                .sortedBy { it.sortOrder }
        }
    }

    override suspend fun promoteToJar(userId: String, id: Long) {
         db.update { list ->
            val mutable = list.toMutableList()
            val index = mutable.indexOfFirst { it.id == id && it.userId == userId }
            if (index != -1) {
                mutable[index] = mutable[index].copy(parentId = null, level = 0, targetPercent = 0)
            }
            mutable
        }
    }

    override suspend fun demoteToCategory(userId: String, id: Long, newParentId: Long) {
        db.update { list ->
            val mutable = list.toMutableList()
            val index = mutable.indexOfFirst { it.id == id && it.userId == userId }
            if (index != -1) {
                mutable[index] = mutable[index].copy(parentId = newParentId, level = 1, targetPercent = null)
            }
            mutable
        }
    }
    
    // Existing generic insert logic needs to be kept (but AllocationDao uses specific insert signature)
    override suspend fun insert(allocation: Allocation): Long {
         db.update { list ->
            val mutableList = list.toMutableList()
            // Simulate auto-generate ID if 0
            val itemToSave = if (allocation.id == 0L) {
               allocation.copy(id = (mutableList.maxOfOrNull { it.id } ?: 0L) + 1)
            } else {
               allocation
            }
            
            val index = mutableList.indexOfFirst { it.id == itemToSave.id }
            if (index != -1) {
                mutableList[index] = itemToSave
            } else {
                mutableList.add(itemToSave)
            }
            mutableList
        }
        return db.value.find { it.name == allocation.name }?.id ?: 0L // Approximation
    }
    
    // Helper for tests to populate initial data easily
    suspend fun insertAll(allocations: List<Allocation>) {
        db.update { current ->
            val mutable = current.toMutableList()
            allocations.forEach { alloc ->
                 if (mutable.none { it.id == alloc.id }) mutable.add(alloc)
            }
            mutable
        }
    }

    override suspend fun update(allocation: Allocation) {
        insert(allocation)
    }

    override suspend fun delete(allocation: Allocation) {
         db.update { list ->
            list.filter { it.id != allocation.id }
        }
    }

    // Helper for tests
    fun getAll(): List<Allocation> = db.value
}
