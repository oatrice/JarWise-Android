package com.oatrice.jarwise.data.api

import com.oatrice.jarwise.data.model.GraphResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GraphApi {
    @GET("api/v1/graph/expenses")
    suspend fun getExpenseGraphData(
        @Query("id") id: String,
        @Query("period") period: String,
        @Query("type") type: String = "category"
    ): GraphResponse
}
