package com.oatrice.jarwise.data.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.AccountBalance
import com.oatrice.jarwise.data.WalletDao
import com.oatrice.jarwise.data.WalletEntity
import com.oatrice.jarwise.model.Wallet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

open class WalletRepository(private val walletDao: WalletDao) : WalletSource {

    open val wallets: Flow<List<Wallet>> = walletDao.getAllWallets().map { entities ->
        entities.map { it.toWallet() }
    }

    open suspend fun insertWallet(wallet: Wallet) {
        walletDao.insertWallet(wallet.toEntity())
    }

    open suspend fun updateWallet(wallet: Wallet) {
        walletDao.updateWallet(wallet.toEntity())
    }

    open suspend fun deleteWallet(id: String) {
        walletDao.deleteWallet(id)
    }

    // Mapper Functions
    private fun WalletEntity.toWallet(): Wallet {
        return Wallet(
            id = id,
            name = name,
            balance = balance,
            color = Color(colorArgb),
            icon = getIconByName(iconName),
            parentId = parentId,
            level = level
        )
    }

    private fun Wallet.toEntity(): WalletEntity {
        return WalletEntity(
            id = id,
            name = name,
            balance = balance,
            colorArgb = color.value.toLong().toInt(), // Convert ULong Color to Int
            iconName = getIconName(icon),
            parentId = parentId,
            level = level
        )
    }

    // Helper to map String -> ImageVector (Basic implementation)
    // In a real app, this should be consistent with how icons are selected/stored
    private fun getIconByName(name: String): ImageVector {
        return when (name) {
            "AccountBalanceWallet" -> Icons.Default.AccountBalanceWallet
            "AttachMoney" -> Icons.Default.AttachMoney
            "CreditCard" -> Icons.Default.CreditCard
            "AccountBalance" -> Icons.Default.AccountBalance
            else -> Icons.Default.AccountBalanceWallet // Default
        }
    }

    private fun getIconName(icon: ImageVector): String {
        return icon.name.substringAfterLast(".") // Extract simple name
    }

    /**
     * Initialize default wallets if database is empty
     */
    override suspend fun initializeDefaultsIfEmpty() {
        val currentWallets = walletDao.getAllWallets().first()
        if (currentWallets.isEmpty()) { 
             val defaults = listOf(
                 Wallet(id = "wallet-cash", name = "Cash", balance = 0.0, color = Color(0xFF22C55E), icon = Icons.Default.AccountBalanceWallet),
                 Wallet(id = "wallet-bank", name = "Bank Account", balance = 0.0, color = Color(0xFF3B82F6), icon = Icons.Default.AccountBalance),
                 Wallet(id = "wallet-credit", name = "Credit Card", balance = 0.0, color = Color(0xFFA855F7), icon = Icons.Default.CreditCard)
             )
             
             defaults.forEach { insertWallet(it) }
        }
    }

    override suspend fun getAllWallets(): List<Wallet> {
        return wallets.first()
    }
}
