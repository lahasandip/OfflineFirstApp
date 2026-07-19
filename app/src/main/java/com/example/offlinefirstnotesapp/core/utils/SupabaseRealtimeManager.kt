package com.example.offlinefirstnotesapp.core.utils

import android.util.Log
import com.example.offlinefirstnotesapp.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import kotlin.time.Duration.Companion.milliseconds

/**
 * Manages Supabase Realtime WebSocket connection for live data updates.
 */
class SupabaseRealtimeManager(private val client: OkHttpClient) {
    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var webSocket: WebSocket? = null

    private val _events = MutableSharedFlow<Unit>()
    val events: SharedFlow<Unit> = _events // Emits whenever a relevant database change occurs

    private val wsUrl = "wss://rwzlihgedgsrgdxzrgyg.supabase.co/realtime/v1/websocket?apikey=${BuildConfig.API_KEY}&vsn=1.0.0"

    fun connect() {
        val request = Request.Builder().url(wsUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("Realtime", "WebSocket Connected")
                joinChannel()
                startHeartbeat()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("Realtime", "Message: $text")
                // Trigger sync if we receive a postgres change event
                if (text.contains("postgres_changes") && (text.contains("INSERT") || text.contains("UPDATE") || text.contains("DELETE"))) {
                    scope.launch { _events.emit(Unit) }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("Realtime", "WebSocket Failure: ${t.message}")
                reconnect()
            }
        })
    }

    private fun joinChannel() {
        // Correct payload for Supabase Realtime V2 postgres changes
        val joinMsg = mapOf(
            "topic" to "realtime:public",
            "event" to "phx_join",
            "payload" to mapOf(
                "config" to mapOf(
                    "postgres_changes" to listOf(
                        mapOf(
                            "event" to "*",
                            "schema" to "public",
                            "table" to "notes"
                        )
                    )
                )
            ),
            "ref" to "1"
        )
        webSocket?.send(gson.toJson(joinMsg))
    }

    private fun startHeartbeat() {
        scope.launch {
            while (true) {
                delay(30000)
                val heartbeatMsg = mapOf(
                    "topic" to "phoenix",
                    "event" to "heartbeat",
                    "payload" to emptyMap<String, Any>(),
                    "ref" to "heartbeat"
                )
                webSocket?.send(gson.toJson(heartbeatMsg))
            }
        }
    }

    private fun reconnect() {
        scope.launch {
            delay(5000.milliseconds)
            connect()
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
    }
}