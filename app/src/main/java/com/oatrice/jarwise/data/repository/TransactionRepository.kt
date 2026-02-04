package com.oatrice.jarwise.data.repository

import androidx.room.withTransaction
import com.oatrice.jarwise.data.AppDatabase
import com.oatrice.jarwise.data.Transaction
import com.oatrice.jarwise.data.TransactionDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun createTransfer(expenseTransaction: Transaction, incomeTransaction: Transaction)
    suspend fun unlinkTransaction(transactionId: Long)
}

class TransactionRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAll()
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    override suspend fun createTransfer(expenseTransaction: Transaction, incomeTransaction: Transaction) {
        db.withTransaction {
            // 1. Insert Expense
            val expenseId = transactionDao.insert(expenseTransaction)
            
            // 2. Insert Income, linked to Expense
            val incomeWithLink = incomeTransaction.copy(linkedTransactionId = expenseId.toString())
            val incomeId = transactionDao.insert(incomeWithLink)
            
            // 3. Update Expense, linked to Income
            val expenseWithLink = expenseTransaction.copy(id = expenseId, linkedTransactionId = incomeId.toString())
            transactionDao.update(expenseWithLink)
        }
    }

    override suspend fun unlinkTransaction(transactionId: Long) {
        db.withTransaction {
            // Logic to find and unlink should be handled by UseCase or here if we want to be atomic on ID
            // Ideally, we fetch the transaction, find the linked one, and set both to null.
            // But since this is a repository method, let's assume we just update the specific one or let UseCase handle the logic.
            // Given the complexity of "Unlinking logic: ... remove current and reciprocal link",
            // it's safer to have a specific method in DAO to nullify linkedTransactionId for a given ID?
            // "UPDATE transactions SET linkedTransactionId = NULL WHERE id = :id OR linkedTransactionId = :id"
            // Wait, that's dangerous if IDs overlap (unlikely with UUIDs but here we use Long auto-inc? No, Transaction has Long ID).
            // Transaction entity uses `val id: Long = 0`.
            // But `linkedTransactionId` is String? Let me check Transaction.kt again.
            // Yes, `linkedTransactionId: String?`. This seems like a mismatch if ID is Long.
            // Ah, the plan said "val linkedTransactionId: String?".
            // But Transaction.kt has "@PrimaryKey(autoGenerate = true) val id: Long = 0".
            // So linkedTransactionId should probably be Long? Or we convert Long to String.
            // Let's assume we store Long as String or change linkedTransactionId to Long.
            // A String is more flexible if we change IDs later (UUID), but for now it's Long.
            // Let's stick to String to match the prompt/plan, but we must be careful.
            
            // To implement Unlink:
            // We need to find the transaction with this ID, get its linked ID.
            // Then update both to null.
            transactionDao.unlinkTransaction(transactionId)
            transactionDao.unlinkRelatedTransaction(transactionId.toString())
        }
    }
}
