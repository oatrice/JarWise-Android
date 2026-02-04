package com.oatrice.jarwise.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.oatrice.jarwise.data.Transaction
import com.oatrice.jarwise.data.TransactionDao
import com.oatrice.jarwise.data.repository.CurrencyRepository
import com.oatrice.jarwise.data.repository.JarConfigRepository
import com.oatrice.jarwise.model.Jar
import com.oatrice.jarwise.ui.managejars.ManageJarsViewModel
import com.oatrice.jarwise.utils.TransactionDisplayUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(
    private val dao: TransactionDao,
    private val currencyRepository: CurrencyRepository,
    private val jarConfigRepository: JarConfigRepository,
    private val createTransferUseCase: com.oatrice.jarwise.domain.use_case.CreateTransferUseCase,
    private val logger: com.oatrice.jarwise.utils.AppLogger
) : ViewModel() {

    init {
        viewModelScope.launch {
            jarConfigRepository.initializeDefaultsIfEmpty()
        }
    }

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
        // Calculate Net Balance
        // Income is positive, Expense is negative.
        // Identify transfers to optionally exclude, though net effect is 0.
        // Safe bet: Exclude transfers to avoid any weird "double counting" visual issues if user inspects.
        val total = txs
            .filter { it.linkedTransactionId == null } // Exclude transfers from Net Worth (optional but cleaner)
            .sumOf { 
                when (it.type) {
                    "expense" -> -it.amount
                    "income" -> it.amount
                    else -> 0.0
                }
            }
        TransactionDisplayUtils.formatCurrency(total, currency)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "..."
    )

    // Real Jars Data Integrator
    val jars = combine(
        jarConfigRepository.getAllJarConfigsFlow(),
        transactions
    ) { configs, txs ->
        if (configs.isEmpty()) {
            emptyList()
        } else {
            configs.map { config ->
                val balance = txs.filter { it.jarId == config.id }.sumOf { it.amount }
                // Simple level/goal logic for MVP
                val level = (balance / 1000).toInt().coerceAtLeast(1)
                val goal = 5000.0 // Hardcoded goal for now (Issue #67)

                Jar(
                    id = config.id,
                    name = config.name,
                    current = balance,
                    goal = goal,
                    level = level,
                    icon = ManageJarsViewModel.getIconFromName(config.iconName),
                    color = ManageJarsViewModel.getColorFromName(config.colorName),
                    shadowColor = ManageJarsViewModel.getColorFromName(config.colorName), // Reuse same color for shadow/bar for now
                    barColor = ManageJarsViewModel.getColorFromName(config.colorName)
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun saveTransaction(amount: Double, jarId: String, walletId: String, note: String, date: String? = null, type: String = "expense") {
        viewModelScope.launch {
            val transaction = Transaction(
                amount = amount,
                jarId = jarId,
                walletId = walletId,
                note = note,
                date = date ?: getCurrentIsoDate(),
                type = type
            )
            dao.insert(transaction)
            logger.d("Transaction", "Saved new transaction ($type)")
        }
    }

    fun saveTransfer(amount: Double, fromWalletId: String, toWalletId: String, note: String, date: String? = null) {
        viewModelScope.launch {
            createTransferUseCase(
                amount = amount,
                fromWalletId = fromWalletId,
                toWalletId = toWalletId,
                date = date ?: getCurrentIsoDate(),
                note = note
            )
            logger.d("Transaction", "Saved new transfer")
        }
    }

    fun saveDraft(amount: Double, jarId: String, walletId: String, note: String, date: String? = null, type: String = "expense") {
        viewModelScope.launch {
            val transaction = Transaction(
                amount = amount,
                jarId = jarId,
                walletId = walletId,
                note = note,
                date = date ?: getCurrentIsoDate(),
                status = "draft",
                type = type
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


}
