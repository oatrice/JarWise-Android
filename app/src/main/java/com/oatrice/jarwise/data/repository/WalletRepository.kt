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

class WalletRepository(private val walletDao: WalletDao) {

    val wallets: Flow<List<Wallet>> = walletDao.getAllWallets().map { entities ->
        entities.map { it.toWallet() }
    }

    suspend fun insertWallet(wallet: Wallet) {
        walletDao.insertWallet(wallet.toEntity())
    }

    suspend fun updateWallet(wallet: Wallet) {
        walletDao.updateWallet(wallet.toEntity())
    }

    suspend fun deleteWallet(id: String) {
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
}
