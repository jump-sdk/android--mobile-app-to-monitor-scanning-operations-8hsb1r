package com.jump.scanmonitor.model

import kotlinx.serialization.Serializable

/**
 * Immutable data class that represents the core scanning metrics information
 * to be displayed in the UI and cached locally.
 *
 * This class serves as the primary domain model for the scanning metrics dashboard,
 * containing the total count of scans over the previous 2 hours and a timestamp
 * indicating when the data was retrieved.
 *
 * The class is marked as @Serializable to enable JSON serialization/deserialization
 * for caching purposes during offline access.
 *
 * @property count The total number of ticket scans processed in the last 2 hours.
 * @property timestamp The time when these metrics were retrieved, defaults to current system time.
 */
@Serializable
data class ScanMetrics(
    val count: Int,
    val timestamp: Long = System.currentTimeMillis()
)