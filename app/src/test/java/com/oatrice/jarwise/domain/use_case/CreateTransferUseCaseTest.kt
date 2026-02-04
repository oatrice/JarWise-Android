package com.oatrice.jarwise.domain.use_case

import com.oatrice.jarwise.data.Transaction
import com.oatrice.jarwise.data.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateTransferUseCaseTest {

    // Fake Repository to avoid Mockito issues with suspend functions returning Unit
    class FakeTransactionRepository : TransactionRepository {
        val transactions = mutableListOf<Transaction>()

        override fun getAllTransactions(): Flow<List<Transaction>> = flowOf(transactions)
        
        override suspend fun insertTransaction(transaction: Transaction) {
            transactions.add(transaction)
        }
        
        override suspend fun updateTransaction(transaction: Transaction) {
            // no-op for this test
        }
        
        override suspend fun deleteTransaction(transaction: Transaction) {
             // no-op
        }

        override suspend fun createTransfer(expenseTransaction: Transaction, incomeTransaction: Transaction) {
            transactions.add(expenseTransaction)
            transactions.add(incomeTransaction)
        }

        override suspend fun unlinkTransaction(transactionId: Long) {
            // no-op
        }
    }

    private val repository = FakeTransactionRepository()
    private val useCase = CreateTransferUseCase(repository)

    @Test
    fun `invoke should create transfer with correct data`() = runTest {
        // Arrange
        val amount = 100.0
        val fromWalletId = "wallet-A"
        val toWalletId = "wallet-B"
        val date = "2024-01-01"
        val note = "Test transfer"

        // Act
        val result = useCase(amount, fromWalletId, toWalletId, date, note)

        // Assert
        assertTrue(result.isSuccess)
        
        assertEquals(2, repository.transactions.size)
        
        val expense = repository.transactions.find { it.type == "expense" }!!
        val income = repository.transactions.find { it.type == "income" }!!
        
        assertEquals("Expense check", amount, expense.amount, 0.0)
        assertEquals("Expense wallet", fromWalletId, expense.walletId)
        
        assertEquals("Income check", amount, income.amount, 0.0)
        assertEquals("Income wallet", toWalletId, income.walletId)
    }
}
