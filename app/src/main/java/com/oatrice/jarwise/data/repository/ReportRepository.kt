package com.oatrice.jarwise.data.repository

import com.oatrice.jarwise.data.api.ReportApi
import com.oatrice.jarwise.data.model.ReportResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface ReportRepository {
    fun getReport(startDate: String, endDate: String, jarId: String? = null): Flow<ReportResponse?>
}

class ReportRepositoryImpl(
    private val api: ReportApi
) : ReportRepository {

    override fun getReport(startDate: String, endDate: String, jarId: String?): Flow<ReportResponse?> = flow {
        try {
            val response = api.getReport(startDate, endDate, jarId)
            emit(response)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(null)
        }
    }
}
