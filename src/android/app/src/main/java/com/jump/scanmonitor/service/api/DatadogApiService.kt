package com.jump.scanmonitor.service.api

import com.jump.scanmonitor.model.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface defining the contract for Datadog API communication.
 * 
 * This interface provides methods to retrieve scanning metrics from the Datadog API,
 * which is used by the Jump staff to monitor ticket scanner activities during games.
 * 
 * The interface is implemented by Retrofit at runtime, handling all HTTP communication
 * details including request building and response parsing.
 */
interface DatadogApiService {
    
    /**
     * Retrieves scanning metrics from the Datadog API for a specified time range.
     * 
     * This method makes a GET request to Datadog's metrics query endpoint and
     * returns scan count data for the specified time period.
     * 
     * @param query The Datadog query string, typically 'sum:ticket.scans.count{*}'
     * @param from The start timestamp in milliseconds (typically 2 hours before current time)
     * @param to The end timestamp in milliseconds (typically current time)
     * @return ApiResponse containing series data with scan counts
     * 
     * @throws IOException if a network or connection error occurs
     * @throws HttpException if the server returns an error response
     */
    @GET("api/v1/query")
    suspend fun getScanMetrics(
        @Query("query") query: String,
        @Query("from") from: Long,
        @Query("to") to: Long
    ): ApiResponse
}