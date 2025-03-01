package com.jump.scanmonitor.service.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.jump.scanmonitor.model.ConnectionType
import com.jump.scanmonitor.model.NetworkStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

/**
 * Monitors network connectivity changes and provides current status through a StateFlow.
 * 
 * This class uses Android's ConnectivityManager to track network changes and exposes
 * the current status through a StateFlow that can be observed by the application.
 * Implements requirement F-003: Offline Mode Handling and F-003-RQ-002: Display appropriate
 * message when offline by providing real-time network connectivity information.
 */
class NetworkMonitor(context: Context) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _networkStatus = MutableStateFlow(getInitialNetworkStatus())
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _networkStatus.value = NetworkStatus(isConnected = true, type = getConnectionType())
            Timber.d("Network became available: ${_networkStatus.value}")
        }
        
        override fun onLost(network: Network) {
            _networkStatus.value = NetworkStatus(isConnected = false, type = ConnectionType.NONE)
            Timber.d("Network connection lost: ${_networkStatus.value}")
        }
        
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            _networkStatus.value = NetworkStatus(isConnected = true, type = getConnectionType(networkCapabilities))
            Timber.d("Network capabilities changed: ${_networkStatus.value}")
        }
    }
    
    init {
        val request = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
        Timber.d("NetworkMonitor initialized with status: ${_networkStatus.value}")
    }
    
    /**
     * Checks if the device currently has network connectivity.
     * 
     * @return True if the device has an active network connection, false otherwise.
     */
    fun isConnected(): Boolean {
        return networkStatus.value.isConnected
    }
    
    /**
     * Determines the current network status when the monitor is initialized.
     * 
     * @return The current network status with connection type.
     */
    private fun getInitialNetworkStatus(): NetworkStatus {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val isConnected = capabilities != null
        val type = if (isConnected) getConnectionType(capabilities) else ConnectionType.NONE
        
        return NetworkStatus(isConnected = isConnected, type = type)
    }
    
    /**
     * Determines the type of network connection from NetworkCapabilities.
     * 
     * @param capabilities The network capabilities to analyze, or null to use the current active network.
     * @return The type of network connection (WIFI, CELLULAR, OTHER, or NONE).
     */
    private fun getConnectionType(capabilities: NetworkCapabilities? = null): ConnectionType {
        val caps = capabilities ?: connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        
        return when {
            caps == null -> ConnectionType.NONE
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
            else -> ConnectionType.OTHER
        }
    }
    
    /**
     * Unregisters the network callback to prevent memory leaks when the monitor is no longer needed.
     * This should be called when the application is being destroyed or when the network monitoring
     * is no longer required.
     */
    fun cleanup() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            Timber.d("NetworkMonitor callback unregistered")
        } catch (e: Exception) {
            Timber.e(e, "Error unregistering network callback")
        }
    }
}