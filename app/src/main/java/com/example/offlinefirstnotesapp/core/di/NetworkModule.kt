package com.example.offlinefirstnotesapp.core.di

import com.example.offlinefirstnotesapp.core.network.AppRetrofit
import com.example.offlinefirstnotesapp.core.utils.SupabaseRealtimeManager
import org.koin.dsl.module
import retrofit2.Retrofit

val networkModule = module {
    single { AppRetrofit(get(), get()) }
    single { get<AppRetrofit>().createOkHttpClient() }
    single<Retrofit> { get<AppRetrofit>().createRetrofit(get()) }
    single { SupabaseRealtimeManager(get()) }
}
