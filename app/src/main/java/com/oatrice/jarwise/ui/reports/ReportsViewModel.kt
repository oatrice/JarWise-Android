package com.oatrice.jarwise.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportsUiState(
    val income: Double = 45000.0,
    val expense: Double = 28500.0,
    val net: Double = 16500.0,
    val trendData: ChartEntryModelProducer = ChartEntryModelProducer(),
    val categoryData: ChartEntryModelProducer = ChartEntryModelProducer(),
    val jarData: List<JarDistribution> = emptyList(), // Vico doesn't have native Pie yet, will use custom or simple list first
    val comparisonData: ChartEntryModelProducer = ChartEntryModelProducer()
)

data class JarDistribution(
    val name: String,
    val amount: Double,
    val color: Long
)

class ReportsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        viewModelScope.launch {
            // Trend (Income vs Expense)
            // Vico Multi-line: need multiple series
            val incomeEntries = listOf(
                FloatEntry(0f, 42000f),
                FloatEntry(1f, 38000f),
                FloatEntry(2f, 41000f),
                FloatEntry(3f, 44000f),
                FloatEntry(4f, 45000f),
            )
            val expenseEntries = listOf(
                FloatEntry(0f, 31000f),
                FloatEntry(1f, 27000f),
                FloatEntry(2f, 29500f),
                FloatEntry(3f, 32000f),
                FloatEntry(4f, 28500f),
            )
            
            // Category (Bar)
            val categoryEntries = listOf(
                FloatEntry(0f, 9500f), // Food
                FloatEntry(1f, 5200f), // Transport
                FloatEntry(2f, 4800f), // Shopping
                FloatEntry(3f, 4000f), // Bills
                FloatEntry(4f, 2500f), // Health
            )

            // Comparison (Grouped Bar) - Current vs Previous
            val currentEntries = listOf(
                FloatEntry(0f, 45000f), // Income
                FloatEntry(1f, 28500f), // Expense
            )
            val previousEntries = listOf(
                FloatEntry(0f, 44000f), // Income
                FloatEntry(1f, 32000f), // Expense
            )
            
            _uiState.value = ReportsUiState(
                trendData = ChartEntryModelProducer(listOf(incomeEntries, expenseEntries)),
                categoryData = ChartEntryModelProducer(listOf(categoryEntries)),
                comparisonData = ChartEntryModelProducer(listOf(currentEntries, previousEntries)),
                jarData = listOf(
                    JarDistribution("Food", 9500.0, 0xFF6366F1),
                    JarDistribution("Transport", 5200.0, 0xFF8B5CF6),
                    JarDistribution("Shopping", 4800.0, 0xFFA78BFA),
                    JarDistribution("Bills", 4000.0, 0xFFC4B5FD),
                    JarDistribution("Health", 2500.0, 0xFF60A5FA),
                )
            )
        }
    }
}
