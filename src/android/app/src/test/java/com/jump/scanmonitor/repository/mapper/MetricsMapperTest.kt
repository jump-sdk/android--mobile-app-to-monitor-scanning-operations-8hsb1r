package com.jump.scanmonitor.repository.mapper

import com.jump.scanmonitor.model.ApiResponse
import com.jump.scanmonitor.model.Series
import com.jump.scanmonitor.model.Metadata
import com.jump.scanmonitor.model.ScanMetrics
import com.jump.scanmonitor.testutil.TestData
import com.jump.scanmonitor.testutil.MainCoroutineRule
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.serialization.json.Json
import org.junit.Rule
import org.junit.Test
import org.junit.Before
import com.google.common.truth.Truth.assertThat
import java.io.InputStreamReader

/**
 * Unit tests for [MetricsMapper] to verify correct transformation of API responses
 * into domain model objects.
 */
class MetricsMapperTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var mapper: MetricsMapper

    /**
     * Set up the test environment before each test.
     */
    @Before
    fun setup() {
        mapper = MetricsMapper()
    }

    /**
     * Tests that mapApiResponseToMetrics correctly extracts data from a valid API response.
     */
    @Test
    fun mapApiResponseToMetrics_withValidResponse_returnsCorrectScanMetrics() {
        // Given - Create response with known count (100)
        val expectedCount = 100
        val response = TestData.createApiResponse(
            pointValues = listOf(0.0, 50.0, expectedCount.toDouble())
        )

        // When
        val result = mapper.mapApiResponseToMetrics(response)

        // Then
        assertThat(result.count).isEqualTo(expectedCount)
        assertThat(result.timestamp).isNotEqualTo(0L)
    }

    /**
     * Tests that mapApiResponseToMetrics handles empty series data gracefully.
     */
    @Test
    fun mapApiResponseToMetrics_withEmptySeriesList_returnsZeroCount() {
        // Given - Create response with empty series list
        val response = ApiResponse(
            series = emptyList(),
            metadata = Metadata("ok", "test-request-id", "sum")
        )

        // When
        val result = mapper.mapApiResponseToMetrics(response)

        // Then
        assertThat(result.count).isEqualTo(0)
        assertThat(result.timestamp).isGreaterThan(0L)
    }

    /**
     * Tests that extractTotalCount correctly extracts count from series data.
     */
    @Test
    fun extractTotalCount_withValidSeries_returnsCorrectCount() {
        // Given
        val expectedCount = 250
        val response = TestData.createApiResponse(
            pointValues = listOf(0.0, 100.0, expectedCount.toDouble())
        )

        // When
        val count = mapper.extractTotalCount(response)

        // Then
        assertThat(count).isEqualTo(expectedCount)
    }

    /**
     * Tests that extractTotalCount returns zero for empty series data.
     */
    @Test
    fun extractTotalCount_withEmptySeries_returnsZero() {
        // Given
        val response = ApiResponse(
            series = emptyList(),
            metadata = Metadata("ok", "test-request-id", "sum")
        )

        // When
        val count = mapper.extractTotalCount(response)

        // Then
        assertThat(count).isEqualTo(0)
    }

    /**
     * Tests that extractTimestamp correctly extracts timestamp from series data.
     */
    @Test
    fun extractTimestamp_withValidSeries_returnsCorrectTimestamp() {
        // Given
        val expectedTimestamp = System.currentTimeMillis()
        val response = TestData.createApiResponse(
            pointValues = listOf(100.0),
            times = listOf(expectedTimestamp)
        )

        // When
        val timestamp = mapper.extractTimestamp(response)

        // Then
        assertThat(timestamp).isEqualTo(expectedTimestamp)
    }

    /**
     * Tests that extractTimestamp returns current time for empty series data.
     */
    @Test
    fun extractTimestamp_withEmptySeries_returnsCurrentTime() {
        // Given
        val beforeTest = System.currentTimeMillis()
        val response = ApiResponse(
            series = emptyList(),
            metadata = Metadata("ok", "test-request-id", "sum")
        )

        // When
        val timestamp = mapper.extractTimestamp(response)
        val afterTest = System.currentTimeMillis()

        // Then - timestamp should be between beforeTest and afterTest
        assertThat(timestamp).isAtLeast(beforeTest)
        assertThat(timestamp).isAtMost(afterTest)
    }

    /**
     * Tests loading and parsing a sample Datadog response JSON file.
     */
    @Test
    fun loadApiResponseFromJson_parsesCorrectly() = runBlockingTest {
        // Given
        val jsonString = """
            {
              "status": "ok",
              "series": [
                {
                  "queryIndex": 0,
                  "aggr": "sum",
                  "pointlist": [
                    [1633046400000.0, 25.0],
                    [1633050000000.0, 258.0],
                    [1633053600000.0, 945.0]
                  ]
                }
              ],
              "metadata": {
                "status": "ok",
                "requestId": "test-sample-123",
                "aggr": "sum"
              }
            }
        """.trimIndent()

        // When - parse the JSON into ApiResponse
        val format = Json { ignoreUnknownKeys = true }
        val response = format.decodeFromString<ApiResponse>(jsonString)
        
        // Then - verify response structure
        assertThat(response.series).hasSize(1)
        assertThat(response.series[0].pointlist).hasSize(3)
        
        // And - map to domain model and verify values
        val metrics = mapper.mapApiResponseToMetrics(response)
        assertThat(metrics.count).isEqualTo(945)
    }
}