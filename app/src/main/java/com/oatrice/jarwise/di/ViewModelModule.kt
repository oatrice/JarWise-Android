package com.oatrice.jarwise.di

import com.oatrice.jarwise.ui.MainViewModel
import com.oatrice.jarwise.ui.SlipViewModel
import com.oatrice.jarwise.ui.managejars.ManageJarsViewModel
import com.oatrice.jarwise.ui.managewallets.ManageWalletsViewModel
import com.oatrice.jarwise.ui.login.LoginViewModel
import com.oatrice.jarwise.ui.settings.SettingsViewModel
import com.oatrice.jarwise.ui.migration.MigrationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel(get(), get(), get(), get(), get()) }
    viewModel { SlipViewModel(get(), get()) }
    viewModel { ManageJarsViewModel(get(), get()) }
    viewModel { ManageWalletsViewModel(get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { MigrationViewModel(get(), get()) }
}
