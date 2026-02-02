package com.oatrice.jarwise

import android.app.Application
import com.oatrice.jarwise.di.appModule
import com.oatrice.jarwise.di.dataModule
import com.oatrice.jarwise.di.repositoryModule
import com.oatrice.jarwise.di.viewModelModule
import com.oatrice.jarwise.di.authModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

import org.koin.core.context.GlobalContext

class JarWiseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (GlobalContext.getOrNull() == null) {
            val koinApp = startKoin {
                androidLogger()
                androidContext(this@JarWiseApplication)
                modules(
                    appModule,
                    dataModule,
                    repositoryModule,
                    viewModelModule,
                    authModule
                )
            }
            
            // Setup Auto Backup
            val koin = koinApp.koin
            val db = koin.get<com.oatrice.jarwise.data.AppDatabase>()
            val backupManager = koin.get<com.oatrice.jarwise.data.backup.BackupManager>()
            
            db.invalidationTracker.addObserver(
                object : androidx.room.InvalidationTracker.Observer(
                    "transactions", "allocations", "wallets"
                ) {
                    override fun onInvalidated(tables: Set<String>) {
                        backupManager.triggerBackup()
                    }
                }
            )
        }
    }
}
