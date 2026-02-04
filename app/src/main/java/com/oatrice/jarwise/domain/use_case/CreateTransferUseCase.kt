package com.oatrice.jarwise.domain.use_case

import com.oatrice.jarwise.data.Transaction
import com.oatrice.jarwise.data.repository.TransactionRepository
import java.util.UUID
import javax.inject.Inject

class CreateTransferUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        amount: Double,
        fromWalletId: String,
        toWalletId: String,
        date: String,
        note: String
    ): Result<Unit> {
        if (amount <= 0) return Result.failure(IllegalArgumentException("Amount must be positive"))
        if (fromWalletId == toWalletId) return Result.failure(IllegalArgumentException("Cannot transfer to the same wallet"))

        // We encounter a problem here: IDs are auto-generated (Long) by Room.
        // We cannot know the ID before insertion to link them.
        // We have two options:
        // 1. Insert first, get ID, then insert second with link, then update first.
        // 2. Use UUID string as ID (as per prompt plan "val id: String = UUID...").
        //    BUT inspecting existing code `Transaction.kt` shows `val id: Long = 0`.
        //    The PROMPT PLAN showed "val id: String = UUID...". The EXISTING CODE has "val id: Long".
        //    CONTRADICTION FOUND.
        //    Guidance says "If Plans conflict with Default Guidance, FOLLOW PLANS". 
        //    BUT existing code overrides plans usually unless we refactor everything.
        //    Refactoring ID from Long to String is HUGE (affects all relations, DAOs).
        //    The plan assumed a new project or different state.
        //    I must adapt to EXISTING CODE (Long ID).
        
        // Strategy for Long IDs:
        // 1. Insert Expense -> Returns ID1.
        // 2. Insert Income (with linkedTransactionId = ID1) -> Returns ID2.
        // 3. Update Expense (set linkedTransactionId = ID2).
        
        // This needs to be atomic. The Repository `createTransfer` accepts objects.
        // I should probably modify `createTransfer` to handle this logic or do it here and call insert/update.
        // But `createTransfer` in repo is cleaner.
        // Let's modify `TransactionRepository` to take basic data instead of Transaction objects? 
        // Or keep `createTransfer` taking objects but ignoring their linkedId initially?
        
        // Better: Pass the raw data to Repository and let it handle linkage logic inside a transaction?
        // Or keep logic in UseCase but call repo methods?
        // If I put logic in UseCase, I need `db.withTransaction` which is not available here.
        // So Logic MUST be in Repository for atomicity given the ID generation constraint.
        
        // Let's update Repository implementation to handle the ID generation dance.
        // I'll update `TransactionRepository.kt` to take params OR keep as is but implementation does the dance.
        // But `createTransfer` signature was `(expense, income)`.
        // I will change it availability to `createTransfer(amount, fromId, toId, date, note)`.
        // NO, UseCase should prepare objects.
        // Let's change `createTransfer` signature in Repository to `createLinkedTransfer(expense: Transaction, income: Transaction)`.
        // And inside, we iterate.
        
        return try {
            // We create objects with temporary IDs (0).
            val expense = Transaction(
                amount = amount,
                jarId = "transfer-out", // Or specific jar
                walletId = fromWalletId,
                date = date,
                note = note,
                type = "expense",
                status = "completed"
            )
            
            val income = Transaction(
                amount = amount,
                jarId = "transfer-in", // Or specific jar
                walletId = toWalletId,
                date = date,
                note = note,
                type = "income",
                status = "completed"
            )
            
            repository.createTransfer(expense, income)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
