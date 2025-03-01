package com.jump.scanmonitor.service.cache

import android.content.Context
import android.content.SharedPreferences
import com.jump.scanmonitor.model.ScanMetrics
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import timber.log.Timber

/**
 * Provides local caching functionality for scan metrics data using Android's SharedPreferences.
 * 
 * This class is responsible for storing the most recent metrics data retrieved from the Datadog API,
 * allowing the application to display data when offline or when API requests fail. It handles
 * serialization/deserialization of [ScanMetrics] objects and maintains timestamps for tracking
 * data freshness.
 *
 * @param context The Android context used to access SharedPreferences
 */
class MetricsCache(private val context: Context) {
    
    /**
     * Lazy-initialized SharedPreferences instance for storing metrics data
     */
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Saves the provided metrics data to SharedPreferences after serializing it to JSON.
     * Also stores the current system time as the timestamp of when the data was cached.
     *
     * @param metrics The ScanMetrics object to be cached
     */
    fun saveMetrics(metrics: ScanMetrics) {
        try {
            sharedPreferences.edit()
                .putString(KEY_METRICS, Json.encodeToString(metrics))
                .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                .apply()
            
            Timber.d("Metrics saved to cache: count=${metrics.count}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to save metrics to cache")
        }
    }
    
    /**
     * Retrieves cached metrics data from SharedPreferences, deserializing from JSON to a ScanMetrics object.
     *
     * @return The cached ScanMetrics object, or null if no cache exists or deserialization fails
     */
    fun getMetrics(): ScanMetrics? {
        val metricsJson = sharedPreferences.getString(KEY_METRICS, null) ?: return null
        
        return try {
            val metrics = Json.decodeFromString<ScanMetrics>(metricsJson)
            Timber.d("Retrieved metrics from cache: count=${metrics.count}, timestamp=${metrics.timestamp}")
            metrics
        } catch (e: Exception) {
            Timber.e(e, "Failed to deserialize cached metrics")
            null
        }
    }
    
    /**
     * Retrieves the timestamp of when the metrics were last updated in the cache.
     *
     * @return Timestamp in milliseconds when the cache was last updated, or 0 if never updated
     */
    fun getLastUpdateTime(): Long {
        return sharedPreferences.getLong(KEY_TIMESTAMP, 0)
    }
    
    /**
     * Checks if any cached metrics data exists in SharedPreferences.
     *
     * @return True if cache exists, false otherwise
     */
    fun isCacheAvailable(): Boolean {
        return sharedPreferences.contains(KEY_METRICS)
    }
    
    /**
     * Clears all cached data from SharedPreferences.
     */
    fun clear() {
        Timber.d("Clearing metrics cache")
        sharedPreferences.edit().clear().apply()
    }
    
    companion object {
        /**
         * Name of the SharedPreferences file for storing scan metrics cache
         */
        private const val PREFS_NAME = "scan_metrics_cache"
        
        /**
         * Key for storing serialized metrics data in SharedPreferences
         */
        private const val KEY_METRICS = "metrics_data"
        
        /**
         * Key for storing timestamp of when the metrics were last cached
         */
        private const val KEY_TIMESTAMP = "last_update_time"
    }
}