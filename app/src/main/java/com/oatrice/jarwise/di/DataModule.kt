package com.oatrice.jarwise.di

import androidx.room.Room
import com.oatrice.jarwise.data.AppDatabase
import com.oatrice.jarwise.data.service.SlipDetectorService
import com.oatrice.jarwise.data.service.SlipDetectorServiceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java, "jarwise-db"
        )
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6
            )
            .addCallback(AppDatabase.SEED_CALLBACK)
            .fallbackToDestructiveMigration()
            .build()
    }

    // DAOs
    single { get<AppDatabase>().transactionDao() }
    single { get<AppDatabase>().jarConfigDao() }
    single { get<AppDatabase>().allocationDao() }
    single { get<AppDatabase>().walletDao() }

    // Services
    single<SlipDetectorService> { SlipDetectorServiceImpl(androidContext()) }
}
