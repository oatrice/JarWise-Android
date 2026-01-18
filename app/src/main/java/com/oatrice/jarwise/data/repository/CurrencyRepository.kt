package com.oatrice.jarwise.data.repository

import kotlinx.coroutines.flow.Flow

class CurrencyRepository(private val userPreferencesRepository: UserPreferencesRepository) {
    val selectedCurrency: Flow<String> = userPreferencesRepository.currencyCode

    suspend fun setCurrency(code: String) {
        userPreferencesRepository.setCurrency(code)
    }

    fun getCurrencySymbol(code: String): String {
        return when (code) {
            "THB" -> "฿"
            "USD" -> "$"
            "EUR" -> "€"
            "JPY" -> "¥"
            else -> code
        }
    }
}
