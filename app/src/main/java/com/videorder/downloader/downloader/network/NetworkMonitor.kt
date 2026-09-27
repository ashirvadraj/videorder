package com.videorder.downloader.downloader.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NetworkType {
    NONE,
    WIFI,
    CELLULAR,
    OTHER
}

class NetworkMonitor(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _currentNetworkType = MutableStateFlow(determineCurrentNetwork())
    val currentNetworkType: StateFlow<NetworkType> = _currentNetworkType.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _currentNetworkType.value = determineCurrentNetwork()
        }

        override fun onLost(network: Network) {
            _currentNetworkType.value = determineCurrentNetwork()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            _currentNetworkType.value = determineCurrentNetwork()
        }
    }

    init {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isConnected(): Boolean {
        return _currentNetworkType.value != NetworkType.NONE
    }

    fun isWifiConnected(): Boolean {
        return _currentNetworkType.value == NetworkType.WIFI
    }

    private fun determineCurrentNetwork(): NetworkType {
        val activeNetwork = connectivityManager.activeNetwork ?: return NetworkType.NONE
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return NetworkType.NONE

        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> NetworkType.OTHER
            else -> NetworkType.NONE
        }
    }
}
