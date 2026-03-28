package com.oatrice.jarwise.data.api

import com.oatrice.jarwise.data.model.ReportResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ReportApi {
    @GET("api/v1/reports")
    suspend fun getReport(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("jar_id") jarId: String? = null
    ): ReportResponse
}
