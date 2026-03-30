package com.oatrice.jarwise.ui.reports

import com.oatrice.jarwise.data.model.*
import com.oatrice.jarwise.data.repository.ReportRepository
import com.oatrice.jarwise.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: ReportRepository = mock()
    private lateinit var viewModel: ReportsViewModel

    @Before
    fun setUp() {
        // Default mock behavior
        whenever(repository.getReport(any(), any(), anyOrNull())).thenReturn(flow { emit(null) })
    }

    @Test
    fun `fetchReport sets error state when repository returns null`() = runTest {
        viewModel = ReportsViewModel(repository)
        
        // Initial fetch call happens in init { fetchReport("month") }
        // We can call it again to be sure
        viewModel.fetchReport("month")
        
        val uiState = viewModel.uiState.value
        assertNotNull("Error message should not be null on failure", uiState.error)
        assertEquals("ไม่สามารถโหลดข้อมูลรายงานได้", uiState.error)
    }

    @Test
    fun `fetchReport cancels previous job when called concurrently`() = runTest {
        val report1 = createMockReport(100.0)
        val report2 = createMockReport(200.0)
        
        whenever(repository.getReport(any(), any(), anyOrNull())).thenAnswer {
            flow {
                delay(1000)
                emit(report1)
            }
        }
        
        viewModel = ReportsViewModel(repository)
        advanceTimeBy(100)
        
        // Second call with different data
        whenever(repository.getReport(any(), any(), anyOrNull())).thenAnswer {
            flow {
                delay(500)
                emit(report2)
            }
        }
        viewModel.fetchReport("quarter")
        
        advanceTimeBy(1000)
        
        // If cancellation works, only report2 (200.0) should be applied
        assertEquals(200.0, viewModel.uiState.value.income, 0.1)
    }

    private fun createMockReport(income: Double): ReportResponse {
        return ReportResponse(
            summary = ChartSummaryDto(income = income, expense = 50.0, net = income - 50.0),
            trend = emptyList(),
            byCategory = emptyList(),
            byJar = emptyList(),
            comparison = null
        )
    }
}
