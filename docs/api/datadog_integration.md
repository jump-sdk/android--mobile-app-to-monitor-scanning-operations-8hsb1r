# Datadog API Integration

## Overview

The ScanMonitorApps application integrates with the Datadog API to provide real-time visibility into ticket scanning operations during sports games. This integration enables Jump staff to monitor scanner activities without requiring access to desktop systems, improving operational awareness and response time.

This document explains how the application interacts with the Datadog API to retrieve ticket scanning metrics, including the technical details of the integration, authentication methods, data processing, caching strategies, and error handling approaches.

## API Configuration

The ScanMonitorApps application communicates with the Datadog API to retrieve scan metrics using the following configuration:

| Configuration Item | Value | Description |
| --- | --- | --- |
| Base URL | `https://api.datadoghq.com/` | Datadog API endpoint base URL |
| API Endpoint | `/api/v1/query` | Metrics query endpoint |
| HTTP Method | GET | Used to retrieve data without modification |
| Request Timeout | 15 seconds | Maximum time to wait for API response |
| Retry Attempts | 3 | Number of retry attempts for failed requests |
| Content Type | application/json | Format of API communication |

The configuration is defined in `datadog_metrics.json` and implemented in the `DatadogApiServiceImpl` class. This approach allows for easy updates to API configuration without code changes.

## Authentication

Authentication with the Datadog API is implemented using API keys and application keys. These credentials are required for all requests to the Datadog API.

### Authentication Headers

The application uses the following HTTP headers for authentication:

```
DD-API-KEY: {api_key}
DD-APPLICATION-KEY: {application_key}
```

### Secure Credential Storage

For security reasons, API credentials are stored in the application's `BuildConfig` constants, not within the source code or JSON configuration files. This approach:

1. Prevents credentials from being exposed in source control
2. Allows different keys to be used for development and production environments
3. Provides a secure way to manage sensitive credentials

The implementation in `DatadogApiServiceImpl` adds these headers to each request:

```kotlin
.addInterceptor { chain ->
    val original = chain.request()
    val requestBuilder = original.newBuilder()
        .header("DD-API-KEY", apiKey)
        .header("DD-APPLICATION-KEY", applicationKey)
        .method(original.method, original.body)
    
    chain.proceed(requestBuilder.build())
}
```

## Metrics Query

The application retrieves ticket scanning metrics using Datadog's metrics query API. The query is configured to retrieve the total count of ticket scans over a specific time period.

### Query Format

The primary metrics query used by the application is:

```
sum:ticket.scans.count{*}
```

This query:
- Uses the `sum` aggregator to calculate the total count
- Targets the `ticket.scans.count` metric which tracks each ticket scan event
- The `{*}` selector includes all scans regardless of specific tags

### Time Range Parameters

The query includes time range parameters to focus on the last 2 hours of scan activity:

| Parameter | Description | Implementation |
| --- | --- | --- |
| `from` | Start time in milliseconds | Current time minus 2 hours (7200 seconds) |
| `to` | End time in milliseconds | Current time |

This time range is defined in the configuration file as:

```json
"time_range": {
  "duration": 7200,
  "unit": "seconds"
}
```

### Query Execution

The query is executed via the `getScanMetrics()` method in the `DatadogApiService` interface:

```kotlin
@GET("api/v1/query")
suspend fun getScanMetrics(
    @Query("query") query: String,
    @Query("from") from: Long,
    @Query("to") to: Long
): ApiResponse
```

## Response Structure

The Datadog API returns a structured JSON response containing scan metrics data. This response is parsed into the `ApiResponse` data model.

### Response Model Hierarchy

```
ApiResponse
├── series: List<Series>
│   └── pointlist: List<List<Double>>
│   └── queryIndex: Int
│   └── aggr: String
└── metadata: Metadata
    └── status: String
    └── requestId: String
    └── aggr: String
```

### Key Components

1. **Series**: Contains the actual metrics data points
   - `pointlist`: A list of data points, where each point is a list containing:
     - Index 0: Timestamp (as Double)
     - Index 1: Value (as Double)
   - `queryIndex`: Identifies which query produced these results
   - `aggr`: Indicates the aggregation method used

2. **Metadata**: Contains information about the API request
   - `status`: Status of the query execution (e.g., "ok")
   - `requestId`: Unique identifier for the API request
   - `aggr`: Aggregation method used

### Data Point Structure

The actual scan count data is found in the `pointlist` array within each series. The `Series` class includes helper methods to extract the latest data point and total count:

```kotlin
fun getLatestPoint(): Point? {
    return pointlist.lastOrNull()?.let { 
        Point(it[0].toLong(), it[1]) 
    }
}

fun getTotalCount(): Int {
    return getLatestPoint()?.value?.toInt() ?: 0
}
```

## Data Transformation

The application transforms the complex Datadog API response into a simpler domain model (`ScanMetrics`) for use within the application. This transformation follows the Single Responsibility Principle, separating API communication from data representation.

### Domain Model

The `ScanMetrics` class represents the simplified data model used by the UI:

```kotlin
data class ScanMetrics(
    val count: Int,
    val timestamp: Long = System.currentTimeMillis()
)
```

This model contains:
- `count`: The total number of ticket scans over the last 2 hours
- `timestamp`: When the data was retrieved (used for freshness indication)

### Mapping Process

The transformation from `ApiResponse` to `ScanMetrics` typically follows these steps:

1. Extract the latest data point from the first series in the response
2. Convert the value from Double to Int for the count
3. Use the timestamp from the data point or current time
4. Create a new `ScanMetrics` instance with these values

This transformation is usually performed by a dedicated mapper component or within the Repository layer.

## Caching Strategy

The application implements a caching strategy to improve performance, reduce API calls, and provide data during offline periods.

### Cache Implementation

Metrics data is cached using Android's SharedPreferences with JSON serialization:

```kotlin
// Save metrics to cache
fun saveMetrics(metrics: ScanMetrics) {
    sharedPreferences.edit()
        .putString(KEY_METRICS, Json.encodeToString(metrics))
        .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
        .apply()
}

// Retrieve metrics from cache
fun getMetrics(): ScanMetrics? {
    val metricsJson = sharedPreferences.getString(KEY_METRICS, null) ?: return null
    return try {
        Json.decodeFromString<ScanMetrics>(metricsJson)
    } catch (e: Exception) {
        null
    }
}
```

### Cache Configuration

The caching behavior is configured in `datadog_metrics.json`:

```json
"cache_settings": {
  "enabled": true,
  "max_age": 600,
  "storage_key": "scan_metrics_cache"
}
```

### Cache Invalidation

Data freshness is determined by comparing the cache timestamp with the current time:

- **Fresh**: Cache is less than 10 minutes old (configurable via `max_age`)
- **Stale**: Cache is more than 10 minutes old but still usable when offline
- **Invalid**: No cache exists or cache data is corrupted

The repository layer generally handles cache invalidation logic during data retrieval:

```kotlin
if (!forceRefresh) {
    cache.getMetrics()?.let { cachedMetrics ->
        // Check if cache is recent enough (less than 10 minutes old)
        val isFresh = (System.currentTimeMillis() - cachedMetrics.timestamp) < 10 * 60 * 1000
        return Result.Success(cachedMetrics, isFromCache = true, isStale = !isFresh)
    }
}
```

## Error Handling

The application implements robust error handling for Datadog API interactions to ensure a good user experience even when issues occur.

### Error Types

| Error Type | Cause | Handling Strategy |
| --- | --- | --- |
| Network Errors | No internet connection, timeout | Fallback to cached data, show offline indicator |
| Authentication Errors | Invalid API/app keys | Show auth error message, log details |
| Rate Limiting | Too many API requests | Implement exponential backoff, retry later |
| Server Errors | Datadog API issues | Retry with backoff, fallback to cache |
| Parsing Errors | Unexpected response format | Log error, fallback to cache or default values |

### Error Handling Implementation

The application uses a combination of try-catch blocks and Result wrappers to handle errors gracefully:

```kotlin
try {
    val response = apiService.getScanMetrics(
        query = "sum:ticket.scans.count{*}",
        from = System.currentTimeMillis() - 2 * 60 * 60 * 1000,
        to = System.currentTimeMillis()
    )
    
    val metrics = mapper.mapApiResponseToMetrics(response)
    cache.saveMetrics(metrics)
    
    return Result.Success(metrics)
} catch (e: Exception) {
    Timber.e(e, "Failed to retrieve scan metrics")
    return Result.Error(e)
}
```

### User-Facing Error Messages

The application shows appropriate user-facing messages based on the error type:

| Error Scenario | User Message | Additional UI Elements |
| --- | --- | --- |
| No network | "Offline Mode" | Show cached data with timestamp |
| Authentication failure | "API configuration error" | Suggest contacting support |
| Server error | "Unable to load scan data" | Show retry button |
| Generic error | "Error retrieving scan data" | Show retry button |

Error message templates are configured in `datadog_metrics.json` for easy customization.

## Performance Optimization

The application implements several optimizations to ensure efficient API usage and minimize network traffic.

### Query Optimization

1. **Time Range Precision**: Requesting exactly the needed time range (2 hours) reduces data volume
2. **Data Point Reduction**: Using appropriate aggregation to limit the number of data points

### Network Optimizations

1. **Connection Pooling**: The OkHttp client reuses connections for multiple requests
2. **Timeouts**: Custom connect and read timeouts set to 15 seconds
3. **Retry Management**: Configurable retry attempts with exponential backoff

### Caching Optimizations

1. **Minimal Cache Size**: Only storing essential data (count and timestamp)
2. **Conditional Fetching**: Avoid API calls when recent cache is available
3. **Background Refresh**: Update cache periodically without blocking UI

### API Usage Efficiency

1. **Rate Limiting Awareness**: Respecting Datadog's rate limits with proper retry behavior
2. **Manual Refresh Control**: Allowing user-triggered refreshes while preventing excessive calls
3. **Batched Updates**: Auto-refreshing at reasonable intervals (5 minutes)

## API Call Examples

### Basic API Request

```kotlin
// Example of retrieving scanning metrics using DatadogApiService
suspend fun getScanningMetrics(datadogApiService: DatadogApiService): Result<ScanMetrics> {
    try {
        val now = System.currentTimeMillis()
        val twoHoursAgo = now - (2 * 60 * 60 * 1000)
        
        val response = datadogApiService.getScanMetrics(
            query = "sum:ticket.scans.count{*}",
            from = twoHoursAgo,
            to = now
        )
        
        // Process response
        val metrics = metricsMapper.mapApiResponseToMetrics(response)
        return Result.Success(metrics)
    } catch (e: Exception) {
        Timber.e(e, "Failed to retrieve scan metrics")
        return Result.Error(e)
    }
}
```

### API Response Processing

```kotlin
// Example of processing Datadog API response
fun processApiResponse(response: ApiResponse): ScanMetrics {
    // Extract scan count from the first series
    val count = response.series.firstOrNull()?.let { series ->
        series.getLatestPoint()?.value?.toInt() ?: 0
    } ?: 0
    
    // Extract timestamp from the latest data point
    val timestamp = response.series.firstOrNull()?.let { series ->
        series.getLatestPoint()?.timestamp ?: System.currentTimeMillis()
    } ?: System.currentTimeMillis()
    
    return ScanMetrics(count = count, timestamp = timestamp)
}
```

### Retrofit API Service Configuration

```kotlin
// Example of configuring Retrofit for Datadog API
fun createDatadogApiService(apiKey: String, applicationKey: String): DatadogApiService {
    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("DD-API-KEY", apiKey)
                .header("DD-APPLICATION-KEY", applicationKey)
                .method(original.method, original.body)
            
            chain.proceed(requestBuilder.build())
        }
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.datadoghq.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .client(okHttpClient)
        .build()
    
    return retrofit.create(DatadogApiService::class.java)
}
```

## Troubleshooting

### Common Issues and Solutions

#### Authentication Failures

**Issue**: API requests return 401 Unauthorized or 403 Forbidden.

**Solution**:
1. Verify API key and application key are correct
2. Check if keys have appropriate permissions in Datadog
3. Ensure keys are being correctly included in request headers
4. Verify keys haven't expired or been revoked

#### No Data Returned

**Issue**: API returns a successful response but no scan data is present.

**Solution**:
1. Verify the metric name `ticket.scans.count` exists in Datadog
2. Check time range parameters to ensure they cover periods with scan activity
3. Confirm the query syntax is correct
4. Test the same query directly in Datadog's query interface

#### Network Failures

**Issue**: Unable to connect to Datadog API.

**Solution**:
1. Check device network connectivity
2. Verify base URL is correct
3. Confirm network permissions are granted in the application
4. Test connectivity to the API from outside the application

#### Parsing Errors

**Issue**: API response cannot be parsed into the expected data model.

**Solution**:
1. Log the raw API response for inspection
2. Verify that the API response structure matches the expected model
3. Check for null values or unexpected data types
4. Update the data models if the API structure has changed

### Logging for Troubleshooting

The application implements detailed logging for API interactions to assist with troubleshooting:

```kotlin
// Example log statements for API troubleshooting
// Before API call
Timber.d("Requesting scan metrics: query=%s, from=%d, to=%d", query, from, to)

// After successful API call
Timber.d("Received scan metrics: count=%d, status=%s", 
         response.series.firstOrNull()?.getTotalCount() ?: 0,
         response.metadata.status)

// After API error
Timber.e(e, "API request failed: %s", e.message)
```

## References

### External Documentation

- [Datadog API Documentation](https://docs.datadoghq.com/api/latest/)
- [Datadog Metrics Queries](https://docs.datadoghq.com/metrics/query_metadata/)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [OkHttp Documentation](https://square.github.io/okhttp/)

### Related Application Components

- `DatadogApiService`: Interface for API communication
- `DatadogApiServiceImpl`: Implementation of API service using Retrofit
- `ApiResponse`: Data model for API responses
- `ScanMetrics`: Domain model for metrics data
- `MetricsMapper`: Component for transforming API data to domain models
- `ScanMetricsRepository`: Repository managing data retrieval and caching

### Configuration Files

- `datadog_metrics.json`: Configuration for Datadog metrics integration