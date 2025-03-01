package com.jump.scanmonitor.repository.mapper

import com.jump.scanmonitor.model.ApiResponse
import com.jump.scanmonitor.model.Series
import com.jump.scanmonitor.model.ScanMetrics
import timber.log.Timber

/**
 * Mapper responsible for transforming Datadog API responses into domain model objects.
 * 
 * This class extracts scan count metrics and timestamps from the complex API response
 * structure to create simplified ScanMetrics objects for display and caching.
 */
class MetricsMapper {

    /**
     * Transforms a Datadog API response into a ScanMetrics domain object.
     *
     * @param response The API response from Datadog containing raw metrics data
     * @return A simplified domain model containing the total scan count and timestamp
     */
    fun mapApiResponseToMetrics(response: ApiResponse): ScanMetrics {
        val count = extractTotalCount(response)
        val timestamp = extractTimestamp(response)
        return ScanMetrics(count = count, timestamp = timestamp)
    }

    /**
     * Extracts the total scan count from an API response.
     *
     * @param response The API response from Datadog
     * @return The total scan count, or 0 if no valid count is available
     */
    fun extractTotalCount(response: ApiResponse): Int {
        if (response.series.isEmpty()) {
            Timber.w("API response contains no series data for scan count extraction")
            return 0
        }

        val firstSeries = response.series.first()
        return firstSeries.getTotalCount()
    }

    /**
     * Extracts the timestamp from an API response.
     *
     * @param response The API response from Datadog
     * @return The timestamp from the latest data point, or current time if none is available
     */
    fun extractTimestamp(response: ApiResponse): Long {
        if (response.series.isEmpty()) {
            Timber.w("API response contains no series data for timestamp extraction")
            return System.currentTimeMillis()
        }

        val firstSeries = response.series.first()
        val latestPoint = firstSeries.getLatestPoint()
        
        return latestPoint?.timestamp ?: System.currentTimeMillis()
    }
}