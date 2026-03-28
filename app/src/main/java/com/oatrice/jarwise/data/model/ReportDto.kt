package com.oatrice.jarwise.data.model

import com.google.gson.annotations.SerializedName

data class ReportResponse(
    @SerializedName("summary") val summary: ChartSummaryDto,
    @SerializedName("trend") val trend: List<TrendPointDto>,
    @SerializedName("by_category") val byCategory: List<CategoryAmountDto>,
    @SerializedName("by_jar") val byJar: List<JarAmountDto>,
    @SerializedName("comparison") val comparison: ComparisonDataDto? = null
)

data class ChartSummaryDto(
    @SerializedName("income") val income: Double,
    @SerializedName("expense") val expense: Double,
    @SerializedName("net") val net: Double
)

data class TrendPointDto(
    @SerializedName("date") val date: String,
    @SerializedName("income") val income: Double,
    @SerializedName("expense") val expense: Double
)

data class CategoryAmountDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("income") val income: Double,
    @SerializedName("expense") val expense: Double,
    @SerializedName("amount") val amount: Double,
    @SerializedName("prev_income") val prevIncome: Double = 0.0,
    @SerializedName("prev_expense") val prevExpense: Double = 0.0
)

data class JarAmountDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("income") val income: Double,
    @SerializedName("expense") val expense: Double,
    @SerializedName("amount") val amount: Double,
    @SerializedName("prev_income") val prevIncome: Double = 0.0,
    @SerializedName("prev_expense") val prevExpense: Double = 0.0
)

data class ComparisonDataDto(
    @SerializedName("current") val current: ChartSummaryDto,
    @SerializedName("previous") val previous: ChartSummaryDto
)
