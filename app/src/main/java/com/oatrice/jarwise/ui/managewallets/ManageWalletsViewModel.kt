package com.oatrice.jarwise.ui.managewallets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oatrice.jarwise.data.GeneratedMockData
import com.oatrice.jarwise.model.Wallet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class ManageWalletsViewModel(
    private val walletRepository: com.oatrice.jarwise.data.repository.WalletRepository
) : ViewModel() {

    val wallets: StateFlow<List<Wallet>> = walletRepository.wallets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            walletRepository.initializeDefaultsIfEmpty()
        }
    }

    private val _uiEvent = MutableStateFlow<UiEvent?>(null)
    val uiEvent: StateFlow<UiEvent?> = _uiEvent.asStateFlow()

    fun addWallet(wallet: Wallet) {
        viewModelScope.launch {
            val id = if (wallet.id.isNotBlank()) wallet.id else (System.currentTimeMillis()).toString()
            
            // Calculate Level based on Parent
            var level = 0
            if (wallet.parentId != null) {
                val parent = wallets.value.find { it.id == wallet.parentId }
                if (parent != null) {
                    level = parent.level + 1
                }
            }

            val newWallet = wallet.copy(id = id, level = level)
            walletRepository.insertWallet(newWallet)
        }
    }

    fun updateWallet(updatedWallet: Wallet) {
        val currentList = wallets.value
        // Ensure wallet exists in current snapshot
        if (currentList.none { it.id == updatedWallet.id }) return
        
        // Validation 1: Self-parenting
        if (updatedWallet.parentId == updatedWallet.id) return

        // Validation 2: Circular Dependency
        if (updatedWallet.parentId != null && isDescendant(updatedWallet.parentId, updatedWallet.id, currentList)) {
            _uiEvent.value = UiEvent.ShowError("Cannot set a descendant as parent (Circular Dependency).")
            return
        }

        // Validation 3: Max Depth (3 Levels)
        var newLevel = 0
        if (updatedWallet.parentId != null) {
            val parent = currentList.find { it.id == updatedWallet.parentId }
            if (parent != null) {
                newLevel = parent.level + 1
            }
        }

        val subtreeDepth = getMaxSubtreeDepth(updatedWallet.id, 0, currentList)
        if (newLevel + subtreeDepth > 2) {
            _uiEvent.value = UiEvent.ShowError("Maximum hierarchy depth (3 levels) exceeded.")
            return
        }

        viewModelScope.launch {
            // Update target wallet
            walletRepository.updateWallet(updatedWallet.copy(level = newLevel))

            // Recursive update children
            updateChildrenLevels(updatedWallet.id, newLevel, currentList)
        }
    }

    private suspend fun updateChildrenLevels(parentId: String, parentLevel: Int, currentList: List<Wallet>) {
       val children = currentList.filter { it.parentId == parentId }
       children.forEach { child ->
           val newChildLevel = parentLevel + 1
           walletRepository.updateWallet(child.copy(level = newChildLevel))
           // We need to re-fetch children from list to continue recursion? 
           // Actually, using 'currentList' snapshot is risky if DB changes in between, but acceptable for now.
           // Better: re-query or pass down.
           updateChildrenLevels(child.id, newChildLevel, currentList)
       }
    }

    fun deleteWallet(id: String) {
        val hasChildren = wallets.value.any { it.parentId == id }
        if (hasChildren) {
            _uiEvent.value = UiEvent.ShowError("Cannot delete a wallet that has sub-accounts. Please move or delete them first.")
            return
        }
        
        viewModelScope.launch {
            walletRepository.deleteWallet(id)
        }
    }

    fun clearEvent() {
        _uiEvent.value = null
    }

    // Helper: Check if 'potentialDescendantId' is a descendant of 'currentWalletId'
    private fun isDescendant(potentialDescendantId: String, currentWalletId: String, currentList: List<Wallet>): Boolean {
        val children = currentList.filter { it.parentId == currentWalletId }
        for (child in children) {
            if (child.id == potentialDescendantId) return true
            if (isDescendant(potentialDescendantId, child.id, currentList)) return true
        }
        return false
    }

    // Helper: Get max depth of subtree starting from rootId
    private fun getMaxSubtreeDepth(rootId: String, currentDepth: Int, currentList: List<Wallet>): Int {
        val children = currentList.filter { it.parentId == rootId }
        if (children.isEmpty()) return currentDepth
        return children.maxOfOrNull { getMaxSubtreeDepth(it.id, currentDepth + 1, currentList) } ?: currentDepth
    }

    sealed class UiEvent {
        data class ShowError(val message: String) : UiEvent()
    }
    
    // View Model Factory

}
