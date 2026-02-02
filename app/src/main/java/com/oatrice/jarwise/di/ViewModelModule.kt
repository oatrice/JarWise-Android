package com.oatrice.jarwise.di

import com.oatrice.jarwise.ui.MainViewModel
import com.oatrice.jarwise.ui.SlipViewModel
import com.oatrice.jarwise.ui.managejars.ManageJarsViewModel
import com.oatrice.jarwise.ui.managewallets.ManageWalletsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel(get(), get(), get()) }
    viewModel { SlipViewModel(get(), get()) }
    viewModel { ManageJarsViewModel(get()) }
    viewModel { ManageWalletsViewModel(get()) }
}
