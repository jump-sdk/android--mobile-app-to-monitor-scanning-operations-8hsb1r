package com.jump.scanmonitor.model

import kotlinx.serialization.Serializable

/**
 * Top-level container for Datadog metric query responses, containing series data and metadata.
 * 
 * This class models the response structure from Datadog's metrics query API and provides
 * access to scan count data and query execution information.
 * 
 * @property series List of data series containing scan count metrics
 * @property metadata Information about the query execution
 */
@Serializable
data class ApiResponse(
    val series: List<Series>,
    val metadata: Metadata
)

/**
 * Represents a series of data points returned by the Datadog query.
 * 
 * Each series contains a list of data points (timestamp and value pairs) along with
 * information about the query that generated this series.
 * 
 * @property pointlist List of data points, each represented as a list where the first element
 *                    is the timestamp (as Double) and the second element is the value (as Double)
 * @property queryIndex Index of the query that produced this series
 * @property aggr Aggregation method used for the data points
 */
@Serializable
data class Series(
    val pointlist: List<List<Double>>,
    val queryIndex: Int,
    val aggr: String
) {
    /**
     * Extracts the most recent data point from the series.
     * 
     * @return The latest data point or null if the series is empty
     */
    fun getLatestPoint(): Point? {
        return pointlist.lastOrNull()?.let { 
            Point(it[0].toLong(), it[1]) 
        }
    }
    
    /**
     * Extracts the total scan count value from the latest data point.
     * 
     * @return The scan count value as an integer, or 0 if no data is available
     */
    fun getTotalCount(): Int {
        return getLatestPoint()?.value?.toInt() ?: 0
    }
}

/**
 * Represents a single data point with timestamp and value.
 * 
 * @property timestamp The Unix timestamp in milliseconds
 * @property value The metric value (scan count)
 */
@Serializable
data class Point(
    val timestamp: Long,
    val value: Double
)

/**
 * Contains metadata information about the Datadog query execution.
 * 
 * @property status The status of the query execution (e.g., "ok")
 * @property requestId Unique identifier for the API request
 * @property aggr Aggregation method used for the query
 */
@Serializable
data class Metadata(
    val status: String,
    val requestId: String,
    val aggr: String
)