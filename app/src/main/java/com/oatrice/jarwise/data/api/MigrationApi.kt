package com.oatrice.jarwise.data.api

import com.oatrice.jarwise.data.api.model.MigrationResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MigrationApi {
    @Multipart
    @POST("api/v1/migrations/money-manager")
    suspend fun uploadMigrationFiles(
        @Part mmbakFile: MultipartBody.Part,
        @Part xlsFile: MultipartBody.Part
    ): Response<MigrationResponse>
}
