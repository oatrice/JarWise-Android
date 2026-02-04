package com.oatrice.jarwise.di

import com.oatrice.jarwise.domain.use_case.CreateTransferUseCase
import com.oatrice.jarwise.domain.use_case.UnlinkTransactionsUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { CreateTransferUseCase(get()) }
    factory { UnlinkTransactionsUseCase(get()) }
}
