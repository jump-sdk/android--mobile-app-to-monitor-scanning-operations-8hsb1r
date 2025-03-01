package com.jump.scanmonitor.model

import kotlinx.serialization.Serializable

/**
 * Immutable data class that encapsulates the complete UI state for displaying scan metrics,
 * including loading status, data, errors, connectivity status, and data freshness indicators.
 *
 * This class serves as the single source of truth for the UI layer, providing all necessary
 * information to render the appropriate view state:
 * - Loading indicator when data is being fetched
 * - Error messages when retrieval fails
 * - Offline mode indicators when network is unavailable
 * - Staleness warnings when data might be outdated
 * - The actual scan metrics when available
 *
 * The class is marked as @Serializable to enable JSON serialization/deserialization
 * for caching and state persistence if needed.
 *
 * @property loading Indicates whether data is currently being loaded.
 * @property data The scan metrics data to display, null when unavailable.
 * @property error Any exception that occurred during data retrieval, null when successful.
 * @property isConnected Network connectivity status (true when online, false when offline).
 * @property isStale Indicates whether the data might be outdated (cached but not fresh).
 */
@Serializable
data class UiState(
    val loading: Boolean = false,
    val data: ScanMetrics? = null,
    val error: Exception? = null,
    val isConnected: Boolean = true,
    val isStale: Boolean = false
)