package com.cletaeats.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext

/* 
 * Funcionalidad que verifica el estado de red del dispositivo 
 * utilizando ConnectivityManager y Flow indirecto a través de produceState.
 */
@Composable
fun connectivityState(): State<ConnectionState> {
    val context = LocalContext.current
    return produceState(initialValue = context.currentConnectivityState) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                value = ConnectionState.Available
            }

            override fun onLost(network: Network) {
                value = ConnectionState.Unavailable
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(request, callback)

        awaitDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}

val Context.currentConnectivityState: ConnectionState
    get() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val hasInternetCapability = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        return if (hasInternetCapability == true) ConnectionState.Available else ConnectionState.Unavailable
    }

sealed class ConnectionState {
    object Available : ConnectionState()
    object Unavailable : ConnectionState()
}
