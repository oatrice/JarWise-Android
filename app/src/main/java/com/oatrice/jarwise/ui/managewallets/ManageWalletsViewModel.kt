package com.oatrice.jarwise.ui.managewallets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oatrice.jarwise.data.GeneratedMockData
import com.oatrice.jarwise.model.Wallet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ManageWalletsViewModel : ViewModel() {

    private val _wallets = MutableStateFlow<List<Wallet>>(emptyList())
    val wallets: StateFlow<List<Wallet>> = _wallets.asStateFlow()

    private val _uiEvent = MutableStateFlow<UiEvent?>(null)
    val uiEvent: StateFlow<UiEvent?> = _uiEvent.asStateFlow()

    init {
        // Initialize with Mock Data
        _wallets.value = GeneratedMockData.wallets
    }

    fun addWallet(wallet: Wallet) {
        val id = if (wallet.id.isNotBlank()) wallet.id else (System.currentTimeMillis()).toString()
        
        // Calculate Level based on Parent
        var level = 0
        if (wallet.parentId != null) {
            val parent = _wallets.value.find { it.id == wallet.parentId }
            if (parent != null) {
                level = parent.level + 1
            }
        }

        val newWallet = wallet.copy(id = id, level = level)
        
        _wallets.update { it + newWallet }
    }

    fun updateWallet(updatedWallet: Wallet) {
        // Ensure wallet exists
        if (_wallets.value.none { it.id == updatedWallet.id }) return
        
        // Validation 1: Self-parenting
        if (updatedWallet.parentId == updatedWallet.id) return

        // Validation 2: Circular Dependency
        if (updatedWallet.parentId != null && isDescendant(updatedWallet.parentId, updatedWallet.id)) {
            _uiEvent.value = UiEvent.ShowError("Cannot set a descendant as parent (Circular Dependency).")
            return
        }

        // Validation 3: Max Depth (3 Levels)
        // Calculate new level
        var newLevel = 0
        if (updatedWallet.parentId != null) {
            val parent = _wallets.value.find { it.id == updatedWallet.parentId }
            if (parent != null) {
                newLevel = parent.level + 1
            }
        }

        val subtreeDepth = getMaxSubtreeDepth(updatedWallet.id, 0)
        if (newLevel + subtreeDepth > 2) {
            _uiEvent.value = UiEvent.ShowError("Maximum hierarchy depth (3 levels) exceeded.")
            return
        }

        // Apply Update with Recursive Level Propagation
        _wallets.update { currentList ->
            val newList = currentList.toMutableList()
            val index = newList.indexOfFirst { it.id == updatedWallet.id }
            if (index == -1) return@update currentList

            // Update target wallet
            newList[index] = updatedWallet.copy(level = newLevel)

            // Recursive update children
            fun updateChildrenLevels(parentId: String, parentLevel: Int) {
               val children = newList.filter { it.parentId == parentId }
               children.forEach { child ->
                   val childIndex = newList.indexOfFirst { it.id == child.id }
                   if (childIndex != -1) {
                       val newChildLevel = parentLevel + 1
                       newList[childIndex] = newList[childIndex].copy(level = newChildLevel)
                       updateChildrenLevels(child.id, newChildLevel)
                   }
               }
            }
            
            updateChildrenLevels(updatedWallet.id, newLevel)
            newList
        }
    }

    fun deleteWallet(id: String) {
        val hasChildren = _wallets.value.any { it.parentId == id }
        if (hasChildren) {
            _uiEvent.value = UiEvent.ShowError("Cannot delete a wallet that has sub-accounts. Please move or delete them first.")
            return
        }
        
        _wallets.update { it.filter { w -> w.id != id } }
    }

    fun clearEvent() {
        _uiEvent.value = null
    }

    // Helper: Check if 'potentialDescendantId' is a descendant of 'currentWalletId'
    private fun isDescendant(potentialDescendantId: String, currentWalletId: String): Boolean {
        val children = _wallets.value.filter { it.parentId == currentWalletId }
        for (child in children) {
            if (child.id == potentialDescendantId) return true
            if (isDescendant(potentialDescendantId, child.id)) return true
        }
        return false
    }

    // Helper: Get max depth of subtree starting from rootId
    private fun getMaxSubtreeDepth(rootId: String, currentDepth: Int): Int {
        val children = _wallets.value.filter { it.parentId == rootId }
        if (children.isEmpty()) return currentDepth
        return children.maxOfOrNull { getMaxSubtreeDepth(it.id, currentDepth + 1) } ?: currentDepth
    }

    sealed class UiEvent {
        data class ShowError(val message: String) : UiEvent()
    }
}
