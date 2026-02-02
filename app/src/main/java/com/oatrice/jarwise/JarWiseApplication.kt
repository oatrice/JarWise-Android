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
            startKoin {
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
        }
    }
}
