package com.oatrice.jarwise.di

import com.oatrice.jarwise.data.api.MigrationApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single {
        val logger = get<com.oatrice.jarwise.utils.AppLogger>()
        val logging = HttpLoggingInterceptor { message ->
            logger.d("API", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8081/") // Emulator localhost
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single { get<Retrofit>().create(MigrationApi::class.java) }
    single { get<Retrofit>().create(com.oatrice.jarwise.data.api.GraphApi::class.java) }
}
