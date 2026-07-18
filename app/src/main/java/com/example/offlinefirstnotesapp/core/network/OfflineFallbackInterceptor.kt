package  com.example.offlinefirstnotesapp.core.network

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

class OfflineFallbackInterceptor(
    private val isNetworkAvailable: () -> Boolean
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        return try {
            if (!isNetworkAvailable()) {
                val json = """
                    {
                      "success": false,
                      "message": "No internet connection",
                      "data": []
                    }
                """.trimIndent()

                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(503)
                    .message("Offline synthetic response")
                    .body(json.toResponseBody("application/json".toMediaType()))
                    .addHeader("Content-Type", "application/json")
                    .build()
            } else {
                chain.proceed(request)
            }
        } catch (e: IOException) {
            val json = """
                {
                  "success": false,
                  "message": "Network error: ${e.message}",
                  "data": []
                }
            """.trimIndent()

            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(500)
                .message("Synthetic error response")
                .body(json.toResponseBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .build()
        }
    }
}