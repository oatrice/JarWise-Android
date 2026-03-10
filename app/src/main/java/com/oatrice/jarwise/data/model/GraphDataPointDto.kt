package com.oatrice.jarwise.data.model

import com.google.gson.annotations.SerializedName

data class GraphDataPointDto(
    @SerializedName("label") val label: String,
    @SerializedName("amount") val amount: Double
)

data class GraphResponse(
    @SerializedName("data") val data: List<GraphDataPointDto>
)
