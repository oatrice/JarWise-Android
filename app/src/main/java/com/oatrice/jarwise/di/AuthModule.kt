package com.oatrice.jarwise.di

import com.oatrice.jarwise.data.auth.AuthService
import com.oatrice.jarwise.data.auth.GoogleAuthService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val authModule = module {
    single<AuthService> { GoogleAuthService(androidContext()) }
}
