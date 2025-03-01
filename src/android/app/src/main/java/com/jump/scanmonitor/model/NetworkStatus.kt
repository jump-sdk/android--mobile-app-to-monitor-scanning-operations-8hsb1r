package com.jump.scanmonitor.model

import kotlinx.serialization.Serializable

/**
 * Represents the different types of network connections.
 * Used to determine the specific type of connectivity available to the device.
 */
enum class ConnectionType {
    /**
     * No network connection is available
     */
    NONE,
    
    /**
     * Connected via WiFi network
     */
    WIFI,
    
    /**
     * Connected via cellular network (3G, 4G, 5G, etc.)
     */
    CELLULAR,
    
    /**
     * Connected via another type of network (e.g. Ethernet, VPN, etc.)
     */
    OTHER
}

/**
 * Data class that represents the current network connectivity status 
 * including whether the device is connected and the type of connection.
 * 
 * This class is marked as Serializable to enable caching of network status
 * and serialization for various operations in the application.
 */
@Serializable
data class NetworkStatus(
    /**
     * Indicates whether the device currently has network connectivity
     */
    val isConnected: Boolean,
    
    /**
     * Represents the type of connection currently active on the device
     */
    val type: ConnectionType
)