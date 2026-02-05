package com.oatrice.jarwise.data.repository

import com.oatrice.jarwise.model.Wallet

interface WalletSource {
    suspend fun getAllWallets(): List<Wallet>
    suspend fun initializeDefaultsIfEmpty()
}
