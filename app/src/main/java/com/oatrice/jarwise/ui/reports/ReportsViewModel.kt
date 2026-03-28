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
    val incomeBreakdownData: ChartEntryModelProducer = ChartEntryModelProducer(),
    val expenseBreakdownData: ChartEntryModelProducer = ChartEntryModelProducer(),
    val incomeDistribution: List<JarDistribution> = emptyList(),
    val expenseDistribution: List<JarDistribution> = emptyList(),
    val comparisonData: ChartEntryModelProducer = ChartEntryModelProducer(),
    val trendLabels: List<String> = emptyList(),
    val incomeLabels: List<String> = emptyList(),
    val expenseLabels: List<String> = emptyList(),
    val comparisonLabels: List<String> = listOf("รายรับ", "รายจ่าย", "คงเหลือ")
)

data class JarDistribution(
    val id: String,
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

    fun fetchReport(range: String, customStart: String? = null, customEnd: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            
            val startDateStr: String
            val endDateStr: String

            if (customStart != null && customEnd != null) {
                startDateStr = customStart
                endDateStr = customEnd
            } else {
                val now = Calendar.getInstance()
                val start = Calendar.getInstance()

                when (range) {
                    "month" -> start.set(Calendar.DAY_OF_MONTH, 1)
                    "quarter" -> start.add(Calendar.MONTH, -3)
                    "year" -> start.set(Calendar.DAY_OF_YEAR, 1)
                    "all" -> start.set(2000, 0, 1) // Default "All Time" to year 2000
                }
                
                startDateStr = sdf.format(start.time)
                endDateStr = sdf.format(now.time)
            }

            repository.getReport(startDateStr, endDateStr).collect { response ->
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

        // Filter and process income categories
        val incomeCategories = report.byCategory.filter { it.income > 0.01 }
        val incomeBreakdownEntries = listOf(
            incomeCategories.mapIndexed { i, c -> FloatEntry(i.toFloat(), c.income.toFloat()) },
            incomeCategories.mapIndexed { i, c -> FloatEntry(i.toFloat(), c.prevIncome.toFloat()) }
        )
        val incomeDist = incomeCategories.mapIndexed { i, c ->
            JarDistribution(c.id, c.name, c.income, colors[i % colors.size])
        }
        val incomeLabels = incomeCategories.map { it.name }

        // Process expense categories
        val expenseBreakdownEntries = listOf(
            report.byCategory.mapIndexed { i, c -> FloatEntry(i.toFloat(), c.expense.toFloat()) },
            report.byCategory.mapIndexed { i, c -> FloatEntry(i.toFloat(), c.prevExpense.toFloat()) }
        )
        val expenseLabels = report.byCategory.map { it.name }
        
        // Distribution by Jar (Expense focus)
        val expenseDist = report.byJar.mapIndexed { i, j ->
            JarDistribution(j.id, j.name, j.amount, colors[i % colors.size])
        }

        val trendLabels = report.trend.map { p ->
            try {
                val inputSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                val outputSdf = SimpleDateFormat("dd/MM", Locale.US)
                val date = inputSdf.parse(p.date)
                outputSdf.format(date ?: Date())
            } catch (e: Exception) {
                ""
            }
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
            incomeBreakdownData = ChartEntryModelProducer(incomeBreakdownEntries),
            expenseBreakdownData = ChartEntryModelProducer(expenseBreakdownEntries),
            incomeDistribution = incomeDist,
            expenseDistribution = expenseDist,
            comparisonData = ChartEntryModelProducer(comparisonEntries),
            trendLabels = trendLabels,
            incomeLabels = incomeLabels,
            expenseLabels = expenseLabels
        )
    }

    fun exportReport(range: String, customStart: String? = null, customEnd: String? = null, onResult: (ByteArray?) -> Unit) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            
            val startDateStr: String
            val endDateStr: String

            if (customStart != null && customEnd != null) {
                startDateStr = customStart
                endDateStr = customEnd
            } else {
                val now = Calendar.getInstance()
                val start = Calendar.getInstance()

                when (range) {
                    "month" -> start.set(Calendar.DAY_OF_MONTH, 1)
                    "quarter" -> start.add(Calendar.MONTH, -3)
                    "year" -> start.set(Calendar.DAY_OF_YEAR, 1)
                    "all" -> start.set(2000, 0, 1)
                }
                
                startDateStr = sdf.format(start.time)
                endDateStr = sdf.format(now.time)
            }

            repository.exportReport(startDateStr, endDateStr).collect { response ->
                onResult(response?.bytes())
            }
        }
    }
}
