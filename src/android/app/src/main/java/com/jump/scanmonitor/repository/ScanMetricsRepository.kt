package com.jump.scanmonitor.repository

import com.jump.scanmonitor.model.ScanMetrics
import com.jump.scanmonitor.model.Result
import com.jump.scanmonitor.service.api.DatadogApiService
import com.jump.scanmonitor.service.cache.MetricsCache
import com.jump.scanmonitor.repository.mapper.MetricsMapper
import kotlinx.coroutines.* // Kotlin Coroutines 1.7.0+
import timber.log.Timber // Timber 5.0.0+

/**
 * Coordinates data retrieval from Datadog API and local cache, implementing
 * strategies for error handling and offline support.
 *
 * This repository serves as the single source of truth for scan metrics data, abstracting
 * the data sources (API and cache) from the rest of the application. It handles fallback
 * strategies when the API is unavailable and manages data freshness indicators.
 *
 * @param apiService Service for communicating with Datadog API
 * @param cache Service for locally caching metrics data
 * @param mapper Utility for transforming API responses to domain models
 */
class ScanMetricsRepository(
    private val apiService: DatadogApiService,
    private val cache: MetricsCache,
    private val mapper: MetricsMapper
) {

    /**
     * Retrieves scan metrics from API or cache based on the forceRefresh parameter.
     *
     * If forceRefresh is false, this method will first check for cached data and return it
     * if available and fresh (less than 10 minutes old). Otherwise, it will attempt to fetch
     * new data from the API, falling back to the cache if the API request fails.
     *
     * @param forceRefresh If true, skip cache and fetch directly from API
     * @return Result object containing either the metrics data or an error
     */
    suspend fun getMetrics(forceRefresh: Boolean = false): Result<ScanMetrics> {
        // Check cache first if not forcing refresh
        if (!forceRefresh) {
            cache.getMetrics()?.let { cachedMetrics ->
                // Check if cache is recent enough (less than 10 minutes old)
                val isFresh = (System.currentTimeMillis() - cachedMetrics.timestamp) < 10 * 60 * 1000
                return Result.Success(cachedMetrics, isFromCache = true, isStale = !isFresh)
            }
        }
        
        // Fetch from API
        return try {
            Timber.d("Fetching scan metrics from Datadog API")
            val timeRange = getTimeRange()
            val response = apiService.getScanMetrics(
                query = buildQuery(),
                from = timeRange.first,
                to = timeRange.second
            )
            
            val metrics = mapper.mapApiResponseToMetrics(response)
            
            // Cache the result
            Timber.d("Caching scan metrics: count=${metrics.count}")
            cache.saveMetrics(metrics)
            
            Result.Success(metrics)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching scan metrics from API")
            
            // Fall back to cache if available, even if it's stale
            cache.getMetrics()?.let { cachedMetrics ->
                Timber.d("Falling back to cached metrics: count=${cachedMetrics.count}")
                return Result.Success(
                    data = cachedMetrics,
                    isFromCache = true,
                    isStale = true
                )
            }
            
            // No cache available, return error
            Result.Error(e)
        }
    }

    /**
     * Forces a refresh of scan metrics from the API.
     *
     * @return Result object containing either the fresh metrics data or an error
     */
    suspend fun refreshMetrics(): Result<ScanMetrics> {
        return getMetrics(forceRefresh = true)
    }

    /**
     * Builds the appropriate query string for the Datadog API request.
     *
     * @return Formatted query string for Datadog metrics API
     */
    private fun buildQuery(): String {
        return "sum:ticket.scans.count{*}"
    }

    /**
     * Calculates the time range for the last 2 hours.
     *
     * @return Pair of start and end timestamps (in milliseconds) for the 2-hour window
     */
    private fun getTimeRange(): Pair<Long, Long> {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (2 * 60 * 60 * 1000) // 2 hours in milliseconds
        return Pair(startTime, endTime)
    }
}