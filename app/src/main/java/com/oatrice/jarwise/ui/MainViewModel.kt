package com.oatrice.jarwise.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.oatrice.jarwise.data.Transaction
import com.oatrice.jarwise.data.TransactionDao
import com.oatrice.jarwise.data.repository.CurrencyRepository
import com.oatrice.jarwise.utils.TransactionDisplayUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(
    private val dao: TransactionDao,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    val transactions = dao.getAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val selectedCurrency = currencyRepository.selectedCurrency.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "THB"
    )

    val formattedTotalBalance = combine(transactions, selectedCurrency) { txs: List<Transaction>, currency: String ->
        val total = txs.sumOf { it.amount }
        TransactionDisplayUtils.formatCurrency(total, currency)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "..."
    )

    fun saveTransaction(amount: Double, jarId: String, note: String, date: String? = null) {
        viewModelScope.launch {
            val transaction = Transaction(
                amount = amount,
                jarId = jarId,
                note = note,
                date = date ?: getCurrentIsoDate()
            )
            dao.insert(transaction)
        }
    }

    fun updateCurrency(code: String) {
        viewModelScope.launch {
            currencyRepository.setCurrency(code)
        }
    }

    private fun getCurrentIsoDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    class Factory(
        private val dao: TransactionDao,
        private val currencyRepository: CurrencyRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(dao, currencyRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
