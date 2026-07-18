package com.example.offlinefirstnotesapp.core.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Observes network connectivity status changes using ConnectivityManager.
 */
class NetworkConnectivityObserver(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager // System service to monitor network

    /**
     * Returns a Flow that emits the current network status whenever it changes.
     */
    fun observe(): Flow<Status> {
        return callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch { send(Status.Available) }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    launch { send(Status.Lost) }
                }
            }

            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(request, callback)

            // Emit initial status
            val isOnline = isOnline()
            launch { send(if (isOnline) Status.Available else Status.Lost) }

            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
    }

    /**
     * Checks if the device is currently online.
     */
    fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    enum class Status {
        Available, Lost
    }
}
