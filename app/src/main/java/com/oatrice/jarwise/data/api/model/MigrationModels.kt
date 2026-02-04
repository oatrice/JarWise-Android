package com.oatrice.jarwise.data.api.model

import com.google.gson.annotations.SerializedName

data class MigrationResponse(
    @SerializedName("job_id")
    val jobId: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String
)

data class MigrationStatusResponse(
    @SerializedName("job_id")
    val jobId: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    // Add other fields as necessary based on backend response
)
