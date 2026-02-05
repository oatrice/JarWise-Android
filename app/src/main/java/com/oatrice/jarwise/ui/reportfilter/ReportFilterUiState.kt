package com.oatrice.jarwise.ui.reportfilter

data class SelectableItem(
    val id: String,
    val name: String,
    val isSelected: Boolean
)

data class ReportFilterUiState(
    val isLoading: Boolean = true,
    val jars: List<SelectableItem> = emptyList(),
    val wallets: List<SelectableItem> = emptyList()
)
