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
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8
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
    single { get<AppDatabase>().subTransactionDao() }

    // Services
    single<SlipDetectorService> { SlipDetectorServiceImpl(androidContext()) }

    // Backup
    single<com.oatrice.jarwise.data.backup.CloudStorageService> { 
        com.oatrice.jarwise.data.backup.GoogleDriveService(androidContext(), get()) 
    }
    
    single { 
        com.oatrice.jarwise.data.backup.BackupManager(
            cloudStorageService = get(),
            externalScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob()),
            dbFileProvider = { androidContext().getDatabasePath("jarwise-db") },
            logger = get()
        ) 
    }
}
