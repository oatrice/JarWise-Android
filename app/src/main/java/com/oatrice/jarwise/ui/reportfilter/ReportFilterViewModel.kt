package com.oatrice.jarwise.ui.reportfilter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oatrice.jarwise.data.repository.WalletSource
import com.oatrice.jarwise.utils.JARS_METADATA
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReportFilterViewModel(
    private val walletSource: WalletSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportFilterUiState())
    val uiState: StateFlow<ReportFilterUiState> = _uiState.asStateFlow()

    private var allJars: Map<String, String> = emptyMap()
    private var allWallets: Map<String, String> = emptyMap()

    private val selectedJarIds = mutableSetOf<String>()
    private val selectedWalletIds = mutableSetOf<String>()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = ReportFilterUiState(isLoading = true)

            allJars = JARS_METADATA.associate { it.id to it.name }
            try {
                walletSource.initializeDefaultsIfEmpty()
                val wallets = walletSource.getAllWallets()
                allWallets = wallets.associate { it.id to it.name }
            } catch (error: Exception) {
                allWallets = emptyMap()
            }

            updateUiState()
        }
    }

    private fun updateUiState() {
        _uiState.value = ReportFilterUiState(
            isLoading = false,
            jars = allJars.map { (id, name) ->
                SelectableItem(id = id, name = name, isSelected = id in selectedJarIds)
            },
            wallets = allWallets.map { (id, name) ->
                SelectableItem(id = id, name = name, isSelected = id in selectedWalletIds)
            }
        )
    }

    fun toggleJarSelection(jarId: String) {
        if (!selectedJarIds.remove(jarId)) {
            selectedJarIds.add(jarId)
        }
        updateUiState()
    }

    fun toggleWalletSelection(walletId: String) {
        if (!selectedWalletIds.remove(walletId)) {
            selectedWalletIds.add(walletId)
        }
        updateUiState()
    }

    fun clearSelections() {
        selectedJarIds.clear()
        selectedWalletIds.clear()
        updateUiState()
    }

    fun setSelections(jarIds: Set<String>, walletIds: Set<String>) {
        selectedJarIds.clear()
        selectedJarIds.addAll(jarIds)
        selectedWalletIds.clear()
        selectedWalletIds.addAll(walletIds)
        updateUiState()
    }

    fun getSelectedFilterIds(): Pair<Set<String>, Set<String>> {
        return Pair(selectedJarIds.toSet(), selectedWalletIds.toSet())
    }
}
