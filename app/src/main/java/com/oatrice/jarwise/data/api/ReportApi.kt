package com.oatrice.jarwise.data.api

import com.oatrice.jarwise.data.model.ReportResponse
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ReportApi {
    @GET("api/v1/reports")
    suspend fun getReport(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("jar_id") jarId: String? = null
    ): ReportResponse

    @Streaming
    @GET("api/v1/reports/export")
    suspend fun exportReport(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("jar_id") jarId: String? = null
    ): ResponseBody
}
