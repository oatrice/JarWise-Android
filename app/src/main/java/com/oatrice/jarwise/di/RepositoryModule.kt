package com.oatrice.jarwise.di

import com.oatrice.jarwise.data.repository.*
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single { UserPreferencesRepository(androidContext()) }
    single { CurrencyRepository(get()) }
    single { JarConfigRepository(get()) }
    single<JarConfigSource> { get<JarConfigRepository>() }
    single { WalletRepository(get()) }
    single<WalletSource> { get<WalletRepository>() }
    single { SlipRepository(androidContext()) }
    single { MigrationRepository(get(), androidContext()) }
    single<TransactionRepository> { TransactionRepositoryImpl(get(), get()) }
}
