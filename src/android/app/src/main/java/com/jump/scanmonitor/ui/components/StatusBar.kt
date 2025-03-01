package com.jump.scanmonitor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import com.jump.scanmonitor.R
import com.jump.scanmonitor.model.ConnectionType
import com.jump.scanmonitor.model.NetworkStatus
import com.jump.scanmonitor.ui.theme.Error
import com.jump.scanmonitor.ui.theme.OfflineBackground
import com.jump.scanmonitor.ui.theme.Primary
import com.jump.scanmonitor.ui.theme.Secondary
import com.jump.scanmonitor.ui.theme.StaleDataBackground
import com.jump.scanmonitor.util.formatTimestamp
import com.jump.scanmonitor.util.isDataStale

/**
 * A composable function that displays a status bar with network connectivity information,
 * data freshness status, and last update timestamp.
 *
 * @param isOffline Indicates if the device is currently offline
 * @param isStale Indicates if the data being displayed is considered stale
 * @param lastUpdated Timestamp of when the data was last updated, null if no data has been loaded
 */
@Composable
fun StatusBar(
    isOffline: Boolean,
    isStale: Boolean,
    lastUpdated: Long?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(getBackgroundColor(isOffline, isStale))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status icon
        Icon(
            painter = painterResource(id = getStatusIcon(isOffline, isStale)),
            contentDescription = null, // Decorative icon
            tint = getStatusColor(isOffline, isStale)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Status text
        Text(
            text = getStatusText(isOffline, isStale),
            style = MaterialTheme.typography.caption,
            color = getStatusColor(isOffline, isStale)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Last updated timestamp
        if (lastUpdated != null) {
            Text(
                text = "Updated: ${formatTimestamp(lastUpdated)}",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Helper function to determine the appropriate status icon based on connectivity and data freshness.
 *
 * @param isOffline Indicates if the device is currently offline
 * @param isStale Indicates if the data being displayed is considered stale
 * @return Android resource ID for the appropriate icon drawable
 */
private fun getStatusIcon(isOffline: Boolean, isStale: Boolean): Int {
    return when {
        isOffline -> R.drawable.ic_cloud_off
        isStale -> R.drawable.ic_access_time
        else -> R.drawable.ic_cloud_done
    }
}

/**
 * Helper function to determine the appropriate status text based on connectivity and data freshness.
 *
 * @param isOffline Indicates if the device is currently offline
 * @param isStale Indicates if the data being displayed is considered stale
 * @return Status message to display
 */
private fun getStatusText(isOffline: Boolean, isStale: Boolean): String {
    return when {
        isOffline -> "Offline Mode"
        isStale -> "Data may be outdated"
        else -> "Live Data"
    }
}

/**
 * Helper function to determine the appropriate color for status elements based on connectivity and data freshness.
 *
 * @param isOffline Indicates if the device is currently offline
 * @param isStale Indicates if the data being displayed is considered stale
 * @return Color to use for icon and text
 */
private fun getStatusColor(isOffline: Boolean, isStale: Boolean): Color {
    return when {
        isOffline -> Error
        isStale -> Secondary
        else -> Primary
    }
}

/**
 * Helper function to determine the appropriate background color based on connectivity and data freshness.
 *
 * @param isOffline Indicates if the device is currently offline
 * @param isStale Indicates if the data being displayed is considered stale
 * @return Background color for the status bar
 */
private fun getBackgroundColor(isOffline: Boolean, isStale: Boolean): Color {
    return when {
        isOffline -> OfflineBackground
        isStale -> StaleDataBackground
        else -> MaterialTheme.colors.surface
    }
}