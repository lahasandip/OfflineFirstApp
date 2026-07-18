package  com.example.offlinefirstnotesapp.core.network

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class OfflineCacheInterceptor(
    private val isNetworkAvailable: () -> Boolean
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        if (!isNetworkAvailable()) {
            val cacheControl = cacheControl()
            request = request.newBuilder()
                .header("Cache-Control", cacheControl.toString())
                .removeHeader("Pragma")
                .build()
        }

        return chain.proceed(request)
    }
    private fun cacheControl(): CacheControl {
        return CacheControl.Builder()
            .maxStale(7, TimeUnit.DAYS)
            .build()
    }

}