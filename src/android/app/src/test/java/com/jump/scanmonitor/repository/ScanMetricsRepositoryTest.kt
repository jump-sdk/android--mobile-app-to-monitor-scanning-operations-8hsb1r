package com.jump.scanmonitor.repository

import com.google.common.truth.Truth.assertThat
import com.jump.scanmonitor.model.ApiResponse
import com.jump.scanmonitor.model.Result
import com.jump.scanmonitor.model.ScanMetrics
import com.jump.scanmonitor.repository.mapper.MetricsMapper
import com.jump.scanmonitor.service.api.DatadogApiService
import com.jump.scanmonitor.service.cache.MetricsCache
import com.jump.scanmonitor.testutil.MainCoroutineRule
import com.jump.scanmonitor.testutil.TestData
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException

@RunWith(MockitoJUnitRunner::class)
class ScanMetricsRepositoryTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var mockApiService: DatadogApiService

    @Mock
    private lateinit var mockCache: MetricsCache

    @Mock
    private lateinit var mockMapper: MetricsMapper

    private lateinit var repository: ScanMetricsRepository

    @Before
    fun setUp() {
        repository = ScanMetricsRepository(mockApiService, mockCache, mockMapper)
    }

    @Test
    fun getMetrics_whenApiSuccessful_returnsMappedMetrics() = runTest {
        // Arrange
        val apiResponse = TestData.DEFAULT_API_RESPONSE
        val expectedMetrics = TestData.DEFAULT_SCAN_METRICS
        
        `when`(mockApiService.getScanMetrics(anyString(), anyLong(), anyLong())).thenReturn(apiResponse)
        `when`(mockMapper.mapApiResponseToMetrics(apiResponse)).thenReturn(expectedMetrics)
        
        // Act
        val result = repository.getMetrics()
        
        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        val successResult = result as Result.Success
        assertThat(successResult.data).isEqualTo(expectedMetrics)
        assertThat(successResult.isFromCache).isFalse()
        assertThat(successResult.isStale).isFalse()
        
        verify(mockApiService).getScanMetrics(anyString(), anyLong(), anyLong())
        verify(mockMapper).mapApiResponseToMetrics(apiResponse)
    }

    @Test
    fun getMetrics_whenApiSuccessful_savesToCache() = runTest {
        // Arrange
        val apiResponse = TestData.DEFAULT_API_RESPONSE
        val expectedMetrics = TestData.DEFAULT_SCAN_METRICS
        
        `when`(mockApiService.getScanMetrics(anyString(), anyLong(), anyLong())).thenReturn(apiResponse)
        `when`(mockMapper.mapApiResponseToMetrics(apiResponse)).thenReturn(expectedMetrics)
        
        // Act
        repository.getMetrics()
        
        // Assert
        verify(mockCache).saveMetrics(expectedMetrics)
    }

    @Test
    fun getMetrics_whenApiFailsAndNoCacheAvailable_returnsError() = runTest {
        // Arrange
        val expectedException = IOException("Network error")
        
        `when`(mockApiService.getScanMetrics(anyString(), anyLong(), anyLong())).thenThrow(expectedException)
        `when`(mockCache.getMetrics()).thenReturn(null)
        
        // Act
        val result = repository.getMetrics()
        
        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        val errorResult = result as Result.Error
        assertThat(errorResult.exception).isEqualTo(expectedException)
        
        verify(mockApiService).getScanMetrics(anyString(), anyLong(), anyLong())
        verify(mockCache).getMetrics()
    }

    @Test
    fun getMetrics_whenApiFailsAndCacheAvailable_returnsCacheData() = runTest {
        // Arrange
        val expectedException = IOException("Network error")
        val cachedMetrics = TestData.DEFAULT_SCAN_METRICS
        
        `when`(mockApiService.getScanMetrics(anyString(), anyLong(), anyLong())).thenThrow(expectedException)
        `when`(mockCache.getMetrics()).thenReturn(cachedMetrics)
        
        // Act
        val result = repository.getMetrics()
        
        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        val successResult = result as Result.Success
        assertThat(successResult.data).isEqualTo(cachedMetrics)
        assertThat(successResult.isFromCache).isTrue()
        assertThat(successResult.isStale).isTrue()
        
        verify(mockApiService).getScanMetrics(anyString(), anyLong(), anyLong())
        verify(mockCache).getMetrics()
    }

    @Test
    fun getMetrics_whenForceRefreshAndCacheAvailable_callsApi() = runTest {
        // Arrange
        val apiResponse = TestData.DEFAULT_API_RESPONSE
        val expectedMetrics = TestData.DEFAULT_SCAN_METRICS
        val cachedMetrics = TestData.createScanMetrics(count = 50) // Different from expected
        
        `when`(mockCache.getMetrics()).thenReturn(cachedMetrics)
        `when`(mockApiService.getScanMetrics(anyString(), anyLong(), anyLong())).thenReturn(apiResponse)
        `when`(mockMapper.mapApiResponseToMetrics(apiResponse)).thenReturn(expectedMetrics)
        
        // Act
        val result = repository.getMetrics(forceRefresh = true)
        
        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        val successResult = result as Result.Success
        assertThat(successResult.data).isEqualTo(expectedMetrics) // Should use API data, not cache
        assertThat(successResult.isFromCache).isFalse()
        
        verify(mockApiService).getScanMetrics(anyString(), anyLong(), anyLong())
        verify(mockCache, never()).getMetrics() // Should not check cache with forceRefresh
    }

    @Test
    fun getMetrics_whenRecentCacheAvailable_returnsCacheWithoutApiCall() = runTest {
        // Arrange
        val currentTime = System.currentTimeMillis()
        val recentCachedMetrics = ScanMetrics(
            count = 75,
            timestamp = currentTime - 5 * 60 * 1000 // 5 minutes old (less than 10 min threshold)
        )
        
        `when`(mockCache.getMetrics()).thenReturn(recentCachedMetrics)
        
        // Act
        val result = repository.getMetrics()
        
        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        val successResult = result as Result.Success
        assertThat(successResult.data).isEqualTo(recentCachedMetrics)
        assertThat(successResult.isFromCache).isTrue()
        assertThat(successResult.isStale).isFalse() // Recent cache should not be marked stale
        
        verify(mockApiService, never()).getScanMetrics(anyString(), anyLong(), anyLong())
    }

    @Test
    fun getMetrics_whenStaleCacheAvailable_callsApi() = runTest {
        // Arrange
        val currentTime = System.currentTimeMillis()
        val staleCachedMetrics = ScanMetrics(
            count = 50,
            timestamp = currentTime - 15 * 60 * 1000 // 15 minutes old (more than 10 min threshold)
        )
        val apiResponse = TestData.DEFAULT_API_RESPONSE
        val expectedMetrics = TestData.DEFAULT_SCAN_METRICS
        
        `when`(mockCache.getMetrics()).thenReturn(staleCachedMetrics)
        `when`(mockApiService.getScanMetrics(anyString(), anyLong(), anyLong())).thenReturn(apiResponse)
        `when`(mockMapper.mapApiResponseToMetrics(apiResponse)).thenReturn(expectedMetrics)
        
        // Act
        val result = repository.getMetrics()
        
        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        val successResult = result as Result.Success
        assertThat(successResult.data).isEqualTo(expectedMetrics) // Should use fresh API data
        assertThat(successResult.isFromCache).isFalse()
        
        verify(mockApiService).getScanMetrics(anyString(), anyLong(), anyLong())
    }

    @Test
    fun refreshMetrics_callsGetMetricsWithForceRefresh() = runTest {
        // Arrange
        val apiResponse = TestData.DEFAULT_API_RESPONSE
        val expectedMetrics = TestData.DEFAULT_SCAN_METRICS
        
        `when`(mockApiService.getScanMetrics(anyString(), anyLong(), anyLong())).thenReturn(apiResponse)
        `when`(mockMapper.mapApiResponseToMetrics(apiResponse)).thenReturn(expectedMetrics)
        
        // Create a spy of the repository to verify method calls
        val repositorySpy = spy(repository)
        
        // Act
        repositorySpy.refreshMetrics()
        
        // Assert
        verify(repositorySpy).getMetrics(forceRefresh = true)
    }
}