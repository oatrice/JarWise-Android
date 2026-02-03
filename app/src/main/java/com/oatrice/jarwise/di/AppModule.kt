package com.oatrice.jarwise.di

import org.koin.dsl.module

import com.oatrice.jarwise.utils.AppLogger
import com.oatrice.jarwise.utils.AndroidAppLogger

val appModule = module {
    single<AppLogger> { AndroidAppLogger(get()) }
}
