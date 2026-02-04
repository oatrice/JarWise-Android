package com.oatrice.jarwise.domain.use_case

import com.oatrice.jarwise.data.repository.TransactionRepository
import javax.inject.Inject

class UnlinkTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transactionId: Long): Result<Unit> {
        return try {
            repository.unlinkTransaction(transactionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
