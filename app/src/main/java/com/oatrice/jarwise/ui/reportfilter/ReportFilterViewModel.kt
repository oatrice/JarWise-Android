package com.oatrice.jarwise.ui.reportfilter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oatrice.jarwise.data.repository.JarConfigSource
import com.oatrice.jarwise.data.repository.WalletSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReportFilterViewModel(
    private val jarConfigSource: JarConfigSource,
    private val walletSource: WalletSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportFilterUiState())
    val uiState: StateFlow<ReportFilterUiState> = _uiState.asStateFlow()

    private var allJarIds: List<String> = emptyList()
    private var allWalletIds: List<String> = emptyList()

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

            jarConfigSource.initializeDefaultsIfEmpty()
            walletSource.initializeDefaultsIfEmpty()

            val jarConfigs = jarConfigSource.getAllJarConfigs()
            val wallets = walletSource.getAllWallets()

            allJars = jarConfigs.associate { it.id to it.name }
            allWallets = wallets.associate { it.id to it.name }

            allJarIds = jarConfigs.map { it.id }
            allWalletIds = wallets.map { it.id }

            updateUiState()
        }
    }

    private fun updateUiState() {
        _uiState.value = ReportFilterUiState(
            isLoading = false,
            jars = allJarIds.map { id ->
                SelectableItem(id = id, name = allJars[id].orEmpty(), isSelected = id in selectedJarIds)
            },
            wallets = allWalletIds.map { id ->
                SelectableItem(id = id, name = allWallets[id].orEmpty(), isSelected = id in selectedWalletIds)
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
