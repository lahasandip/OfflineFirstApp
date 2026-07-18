package com.example.offlinefirstnotesapp.core.network

import android.content.Context
import android.util.Log
import com.example.offlinefirstnotesapp.core.utils.NetworkConnectivityObserver
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class AppRetrofit(
    private val context: Context,
    private val connectivityObserver: NetworkConnectivityObserver
) {
    private val baseUrl = "https://rwzlihgedgsrgdxzrgyg.supabase.co/rest/v1/"
    private val cache = Cache(File(context.cacheDir, "http-cache"), 10 * 1024 * 1024)

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("AppRetrofit", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor())
            .addInterceptor(OfflineCacheInterceptor { connectivityObserver.isOnline() })
            .addInterceptor(OfflineFallbackInterceptor { connectivityObserver.isOnline() })
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun createRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .build()
    }
}
