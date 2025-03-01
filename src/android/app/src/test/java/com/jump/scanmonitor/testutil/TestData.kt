package com.jump.scanmonitor.testutil

import com.jump.scanmonitor.model.ApiResponse
import com.jump.scanmonitor.model.ConnectionType
import com.jump.scanmonitor.model.Metadata
import com.jump.scanmonitor.model.NetworkStatus
import com.jump.scanmonitor.model.Result
import com.jump.scanmonitor.model.ScanMetrics
import com.jump.scanmonitor.model.Series
import com.jump.scanmonitor.model.UiState
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Provides test data objects and factory methods for unit testing the ScanMonitor application.
 * 
 * This utility object contains predefined constants for common test scenarios and factory methods
 * for creating customizable test data objects. It helps maintain consistency across tests and
 * reduces test code duplication.
 */
object TestData {
    
    /**
     * Reference timestamp for test data (current system time)
     */
    val CURRENT_TIME: Long = System.currentTimeMillis()
    
    /**
     * Default ScanMetrics object with predefined values for testing
     */
    val DEFAULT_SCAN_METRICS = ScanMetrics(
        count = 100,
        timestamp = CURRENT_TIME
    )
    
    /**
     * Default ApiResponse object with predefined values for testing
     */
    val DEFAULT_API_RESPONSE = ApiResponse(
        series = listOf(
            Series(
                pointlist = listOf(
                    listOf(CURRENT_TIME.toDouble() - 7200000, 0.0),
                    listOf(CURRENT_TIME.toDouble() - 3600000, 50.0),
                    listOf(CURRENT_TIME.toDouble(), 100.0)
                ),
                queryIndex = 0,
                aggr = "sum"
            )
        ),
        metadata = Metadata(
            status = "ok",
            requestId = "test-request-id",
            aggr = "sum"
        )
    )
    
    /**
     * UI state representing the loading state
     */
    val LOADING_STATE = UiState(
        loading = true,
        data = null,
        error = null,
        isConnected = true,
        isStale = false
    )
    
    /**
     * UI state representing the success state with data
     */
    val SUCCESS_STATE = UiState(
        loading = false,
        data = DEFAULT_SCAN_METRICS,
        error = null,
        isConnected = true,
        isStale = false
    )
    
    /**
     * UI state representing the error state
     */
    val ERROR_STATE = UiState(
        loading = false,
        data = null,
        error = IOException("Test error"),
        isConnected = true,
        isStale = false
    )
    
    /**
     * UI state representing the offline state with cached data
     */
    val OFFLINE_STATE = UiState(
        loading = false,
        data = DEFAULT_SCAN_METRICS,
        error = null,
        isConnected = false,
        isStale = true
    )
    
    /**
     * NetworkStatus representing WiFi connectivity
     */
    val CONNECTED_WIFI = NetworkStatus(
        isConnected = true,
        type = ConnectionType.WIFI
    )
    
    /**
     * NetworkStatus representing cellular connectivity
     */
    val CONNECTED_CELLULAR = NetworkStatus(
        isConnected = true,
        type = ConnectionType.CELLULAR
    )
    
    /**
     * NetworkStatus representing no connectivity
     */
    val DISCONNECTED = NetworkStatus(
        isConnected = false,
        type = ConnectionType.NONE
    )
    
    /**
     * Success result with fresh data from network
     */
    val SUCCESS_RESULT = Result.Success(
        data = DEFAULT_SCAN_METRICS,
        isFromCache = false,
        isStale = false
    )
    
    /**
     * Success result with data from cache
     */
    val CACHED_RESULT = Result.Success(
        data = DEFAULT_SCAN_METRICS,
        isFromCache = true,
        isStale = false
    )
    
    /**
     * Success result with stale data
     */
    val STALE_RESULT = Result.Success(
        data = DEFAULT_SCAN_METRICS,
        isFromCache = true,
        isStale = true
    )
    
    /**
     * Error result
     */
    val ERROR_RESULT = Result.Error(
        exception = IOException("Test error")
    )
    
    /**
     * Creates a ScanMetrics instance with customizable properties.
     *
     * @param count The total scan count value
     * @param timestamp The timestamp when the metrics were retrieved
     * @return A ScanMetrics instance with the specified properties
     */
    fun createScanMetrics(
        count: Int = 100,
        timestamp: Long = CURRENT_TIME
    ): ScanMetrics {
        return ScanMetrics(
            count = count,
            timestamp = timestamp
        )
    }
    
    /**
     * Creates an ApiResponse instance with customizable properties.
     *
     * @param pointValues List of scan count values for each time point
     * @param times List of timestamps corresponding to each scan count value
     * @param queryIndex The index of the query that produced this series
     * @param status The status of the API response
     * @return An ApiResponse instance with the specified properties
     * @throws IllegalArgumentException if pointValues and times have different sizes
     */
    fun createApiResponse(
        pointValues: List<Double> = listOf(0.0, 50.0, 100.0),
        times: List<Long> = listOf(
            CURRENT_TIME - TimeUnit.HOURS.toMillis(2),
            CURRENT_TIME - TimeUnit.HOURS.toMillis(1),
            CURRENT_TIME
        ),
        queryIndex: Int = 0,
        status: String = "ok"
    ): ApiResponse {
        require(pointValues.size == times.size) { "Point values and times must have the same size" }
        
        val pointlist = times.zip(pointValues) { time, value ->
            listOf(time.toDouble(), value)
        }
        
        val series = Series(
            pointlist = pointlist,
            queryIndex = queryIndex,
            aggr = "sum"
        )
        
        val metadata = Metadata(
            status = status,
            requestId = "test-request-id",
            aggr = "sum"
        )
        
        return ApiResponse(
            series = listOf(series),
            metadata = metadata
        )
    }
    
    /**
     * Creates a UiState instance with customizable properties.
     *
     * @param loading Whether the UI is in loading state
     * @param data The scan metrics data to display
     * @param error Any error that occurred during data retrieval
     * @param isConnected Whether the device has network connectivity
     * @param isStale Whether the data might be outdated
     * @return A UiState instance with the specified properties
     */
    fun createUiState(
        loading: Boolean = false,
        data: ScanMetrics? = null,
        error: Exception? = null,
        isConnected: Boolean = true,
        isStale: Boolean = false
    ): UiState {
        return UiState(
            loading = loading,
            data = data,
            error = error,
            isConnected = isConnected,
            isStale = isStale
        )
    }
    
    /**
     * Creates a NetworkStatus instance with customizable properties.
     *
     * @param isConnected Whether the device has network connectivity
     * @param type The type of network connection
     * @return A NetworkStatus instance with the specified properties
     */
    fun createNetworkStatus(
        isConnected: Boolean = true,
        type: ConnectionType = ConnectionType.WIFI
    ): NetworkStatus {
        return NetworkStatus(
            isConnected = isConnected,
            type = type
        )
    }
    
    /**
     * Creates a Success Result instance with customizable properties.
     *
     * @param data The data returned by the operation
     * @param isFromCache Whether the data was retrieved from cache
     * @param isStale Whether the data might be outdated
     * @return A Success Result instance with the specified properties
     */
    fun <T> createSuccessResult(
        data: T,
        isFromCache: Boolean = false,
        isStale: Boolean = false
    ): Result<T> {
        return Result.Success(
            data = data,
            isFromCache = isFromCache,
            isStale = isStale
        )
    }
    
    /**
     * Creates an Error Result instance with a customizable exception.
     *
     * @param exception The exception that caused the operation to fail
     * @return An Error Result instance with the specified exception
     */
    fun <T> createErrorResult(
        exception: Exception = IOException("Test error")
    ): Result<T> {
        return Result.Error(exception)
    }
}