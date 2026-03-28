package com.oatrice.jarwise.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oatrice.jarwise.data.model.ReportResponse
import com.oatrice.jarwise.data.repository.ReportRepository
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ReportsUiState(
    val isLoading: Boolean = false,
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val net: Double = 0.0,
    val trendData: ChartEntryModelProducer = ChartEntryModelProducer(),
    val categoryData: ChartEntryModelProducer = ChartEntryModelProducer(),
    val jarData: List<JarDistribution> = emptyList(),
    val comparisonData: ChartEntryModelProducer = ChartEntryModelProducer()
)

data class JarDistribution(
    val name: String,
    val amount: Double,
    val color: Long
)

class ReportsViewModel(
    private val repository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private val colors = listOf(0xFF6366F1, 0xFF8B5CF6, 0xFFA78BFA, 0xFFC4B5FD, 0xFF60A5FA, 0xFF93C5FD)

    init {
        fetchReport("month")
    }

    fun fetchReport(range: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            val now = Calendar.getInstance()
            val start = Calendar.getInstance()

            when (range) {
                "month" -> start.set(Calendar.DAY_OF_MONTH, 1)
                "quarter" -> start.add(Calendar.MONTH, -3)
                "year" -> start.set(Calendar.DAY_OF_YEAR, 1)
            }

            val startDate = sdf.format(start.time)
            val endDate = sdf.format(now.time)

            repository.getReport(startDate, endDate).collect { response ->
                if (response != null) {
                    updateUiState(response)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    private fun updateUiState(report: ReportResponse) {
        val incomeTrend = report.trend.mapIndexed { i, p -> FloatEntry(i.toFloat(), p.income.toFloat()) }
        val expenseTrend = report.trend.mapIndexed { i, p -> FloatEntry(i.toFloat(), p.expense.toFloat()) }

        val categoryIncome = report.byCategory.mapIndexed { i, c -> FloatEntry(i.toFloat(), c.income.toFloat()) }
        val categoryExpense = report.byCategory.mapIndexed { i, c -> FloatEntry(i.toFloat(), c.expense.toFloat()) }

        val jarDist = report.byJar.mapIndexed { i, j ->
            JarDistribution(j.name, j.amount, colors[i % colors.size])
        }

        val comparisonEntries = report.comparison?.let {
            val current = listOf(
                FloatEntry(0f, it.current.income.toFloat()),
                FloatEntry(1f, it.current.expense.toFloat()),
                FloatEntry(2f, it.current.net.toFloat())
            )
            val previous = listOf(
                FloatEntry(0f, it.previous.income.toFloat()),
                FloatEntry(1f, it.previous.expense.toFloat()),
                FloatEntry(2f, it.previous.net.toFloat())
            )
            listOf(current, previous)
        } ?: emptyList()

        _uiState.value = ReportsUiState(
            isLoading = false,
            income = report.summary.income,
            expense = report.summary.expense,
            net = report.summary.net,
            trendData = ChartEntryModelProducer(listOf(incomeTrend, expenseTrend)),
            categoryData = ChartEntryModelProducer(listOf(categoryIncome, categoryExpense)),
            comparisonData = ChartEntryModelProducer(comparisonEntries),
            jarData = jarDist
        )
    }

    fun exportReport(range: String, onResult: (ByteArray?) -> Unit) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            val now = Calendar.getInstance()
            val start = Calendar.getInstance()

            when (range) {
                "month" -> start.set(Calendar.DAY_OF_MONTH, 1)
                "quarter" -> start.add(Calendar.MONTH, -3)
                "year" -> start.set(Calendar.DAY_OF_YEAR, 1)
            }

            val startDate = sdf.format(start.time)
            val endDate = sdf.format(now.time)

            repository.exportReport(startDate, endDate).collect { response ->
                onResult(response?.bytes())
            }
        }
    }
}
