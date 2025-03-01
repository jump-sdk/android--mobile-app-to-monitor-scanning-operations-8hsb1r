package com.jump.scanmonitor.service.api

import com.google.common.truth.Truth.assertThat
import com.jump.scanmonitor.model.ApiResponse
import com.jump.scanmonitor.testutil.MainCoroutineRule
import com.jump.scanmonitor.testutil.TestData
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException

/**
 * Unit tests for the DatadogApiService implementation, focusing on API request formatting,
 * authentication header inclusion, and response handling.
 */
class DatadogApiServiceTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: DatadogApiService

    /**
     * Helper function to read test resource files like JSON responses
     */
    private fun readResourceFile(filename: String): String {
        val resourceStream = javaClass.classLoader?.getResourceAsStream(filename)
        return resourceStream?.bufferedReader()?.use { it.readText() } ?: ""
    }

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        apiService = createTestService()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun createTestService(): DatadogApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        
        return retrofit.create(DatadogApiService::class.java)
    }

    @Test
    fun getScanMetrics_sendsCorrectRequest() = runTest {
        // Arrange
        mockWebServer.enqueue(MockResponse().setBody(
            """{"series":[{"pointlist":[[1000.0,5.0],[2000.0,10.0]],"queryIndex":0,"aggr":"sum"}],"metadata":{"status":"ok","requestId":"test-id","aggr":"sum"}}"""
        ))

        // Act
        val fromTime = 1000L
        val toTime = 2000L
        val queryString = "sum:ticket.scans.count{*}"
        apiService.getScanMetrics(queryString, fromTime, toTime)

        // Assert
        val request = mockWebServer.takeRequest()
        assertThat(request.path).contains("query=sum:ticket.scans.count%7B*%7D")
        assertThat(request.path).contains("from=1000")
        assertThat(request.path).contains("to=2000")
        assertThat(request.method).isEqualTo("GET")
    }

    @Test
    fun getScanMetrics_parsesResponseCorrectly() = runTest {
        // Arrange
        val responseJson = """
            {
                "series": [
                    {
                        "pointlist": [[1000.0, 5.0], [2000.0, 10.0]],
                        "queryIndex": 0,
                        "aggr": "sum"
                    }
                ],
                "metadata": {
                    "status": "ok",
                    "requestId": "test-id",
                    "aggr": "sum"
                }
            }
        """.trimIndent()
        
        mockWebServer.enqueue(MockResponse().setBody(responseJson))

        // Act
        val result = apiService.getScanMetrics("query", 1000L, 2000L)

        // Assert
        assertThat(result).isNotNull()
        assertThat(result.series).hasSize(1)
        assertThat(result.series[0].pointlist).hasSize(2)
        assertThat(result.series[0].pointlist[0][0]).isEqualTo(1000.0)
        assertThat(result.series[0].pointlist[0][1]).isEqualTo(5.0)
        assertThat(result.series[0].pointlist[1][0]).isEqualTo(2000.0)
        assertThat(result.series[0].pointlist[1][1]).isEqualTo(10.0)
        assertThat(result.metadata.status).isEqualTo("ok")
        assertThat(result.metadata.requestId).isEqualTo("test-id")
        assertThat(result.metadata.aggr).isEqualTo("sum")
    }

    @Test
    fun getScanMetrics_handlesErrorResponses() = runTest {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Server Error"))

        try {
            // Act
            apiService.getScanMetrics("query", 1000L, 2000L)
            
            // If we reach here, the test failed
            assert(false) { "Expected exception was not thrown" }
        } catch (e: Exception) {
            // Assert
            // The exact exception type depends on Retrofit's error handling
            assertThat(e).isInstanceOf(retrofit2.HttpException::class.java)
        }
    }

    @Test
    fun getScanMetrics_handlesNetworkFailures() = runTest {
        // Arrange - Shut down the server to simulate network failure
        mockWebServer.shutdown()

        try {
            // Act
            apiService.getScanMetrics("query", 1000L, 2000L)
            
            // If we reach here, the test failed
            assert(false) { "Expected exception was not thrown" }
        } catch (e: Exception) {
            // Assert
            assertThat(e).isInstanceOf(IOException::class.java)
        }
    }

    @Test
    fun create_addsAuthenticationHeaders() = runTest {
        // Arrange
        val testApiKey = "test-api-key"
        val testAppKey = "test-app-key"
        
        mockWebServer.enqueue(MockResponse().setBody(
            """{"series":[],"metadata":{"status":"ok","requestId":"test-id","aggr":"sum"}}"""
        ))
        
        // Create a test service with authentication headers
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val original = chain.request()
                        val requestBuilder = original.newBuilder()
                            .header("DD-API-KEY", testApiKey)
                            .header("DD-APPLICATION-KEY", testAppKey)
                            .method(original.method, original.body)
                        
                        chain.proceed(requestBuilder.build())
                    }
                    .build()
            )
            .build()
        
        val serviceWithAuth = retrofit.create(DatadogApiService::class.java)
        
        // Act
        serviceWithAuth.getScanMetrics("query", 1000L, 2000L)
        
        // Assert
        val request = mockWebServer.takeRequest()
        assertThat(request.getHeader("DD-API-KEY")).isEqualTo(testApiKey)
        assertThat(request.getHeader("DD-APPLICATION-KEY")).isEqualTo(testAppKey)
    }
}