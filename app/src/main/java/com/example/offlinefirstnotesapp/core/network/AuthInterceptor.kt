package com.example.offlinefirstnotesapp.core.network

import com.example.offlinefirstnotesapp.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        val token = BuildConfig.API_KEY
        
        val newRequest = request.newBuilder()
            .header("apikey", token)
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(newRequest)
    }
}
