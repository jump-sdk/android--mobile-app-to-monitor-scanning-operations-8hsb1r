package com.jump.scanmonitor.util

import java.text.SimpleDateFormat // JDK
import java.util.Date // JDK
import java.util.Locale // JDK
import java.util.concurrent.TimeUnit // JDK

/**
 * Utility functions for time-related operations in the ScanMonitorApps application.
 * Provides methods for formatting timestamps, checking data freshness, and calculating
 * time ranges for API requests.
 */
object TimeUtils {

    /**
     * Formats a timestamp (in milliseconds) into a human-readable string (e.g., '2 min ago', 'Just now').
     *
     * @param timestamp The timestamp in milliseconds to format
     * @return A human-readable string representing the relative time
     */
    fun formatTimestamp(timestamp: Long): String {
        val currentTime = System.currentTimeMillis()
        val diffInMillis = currentTime - timestamp
        val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
        val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

        return when {
            diffInMinutes < 1 -> "Just now"
            diffInMinutes < 60 -> "$diffInMinutes min ago"
            diffInHours < 24 -> "$diffInHours hour${if (diffInHours > 1) "s" else ""} ago"
            diffInDays < 7 -> "$diffInDays day${if (diffInDays > 1) "s" else ""} ago"
            else -> formatDateTime(timestamp, "MMM d, yyyy")
        }
    }

    /**
     * Formats a timestamp (in milliseconds) into a date-time string with a specific pattern.
     *
     * @param timestamp The timestamp in milliseconds to format
     * @param pattern The date format pattern to use
     * @return The formatted date-time string
     */
    fun formatDateTime(timestamp: Long, pattern: String): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat(pattern, Locale.US)
        return formatter.format(date)
    }

    /**
     * Determines if data is stale based on its timestamp and a freshness threshold.
     *
     * @param timestamp The timestamp in milliseconds to check
     * @param thresholdMinutes The staleness threshold in minutes
     * @return True if data is stale, false otherwise
     */
    fun isDataStale(timestamp: Long, thresholdMinutes: Long): Boolean {
        val diffInMinutes = getTimeDifferenceInMinutes(timestamp)
        return diffInMinutes > thresholdMinutes
    }

    /**
     * Gets the start and end timestamps for the last two hours (used for API requests).
     *
     * @return A Pair of timestamps representing start time (two hours ago) and end time (now)
     */
    fun getTimeRangeForLastTwoHours(): Pair<Long, Long> {
        val currentTime = System.currentTimeMillis()
        val twoHoursAgo = currentTime - TimeUnit.HOURS.toMillis(2)
        return Pair(twoHoursAgo, currentTime)
    }

    /**
     * Calculates the time difference between a timestamp and the current time in minutes.
     *
     * @param timestamp The timestamp in milliseconds
     * @return Time difference in minutes
     */
    fun getTimeDifferenceInMinutes(timestamp: Long): Long {
        val currentTime = System.currentTimeMillis()
        val diffInMillis = currentTime - timestamp
        return TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
    }

    /**
     * Determines if data should be refreshed based on last update time and refresh interval.
     *
     * @param lastUpdateTime The timestamp in milliseconds when data was last updated
     * @param refreshIntervalMinutes The refresh interval in minutes
     * @return True if data should be refreshed, false otherwise
     */
    fun shouldRefreshData(lastUpdateTime: Long, refreshIntervalMinutes: Int): Boolean {
        val diffInMinutes = getTimeDifferenceInMinutes(lastUpdateTime)
        return diffInMinutes >= refreshIntervalMinutes
    }
}