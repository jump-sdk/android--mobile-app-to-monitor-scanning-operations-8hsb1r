package com.jump.scanmonitor.service.cache

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.assertThat
import com.jump.scanmonitor.model.ScanMetrics
import com.jump.scanmonitor.testutil.TestData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests the functionality of the MetricsCache class which provides local caching for scan metrics data.
 */
class MetricsCacheTest {

    // Mocks
    private lateinit var mockContext: Context
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    
    // Class under test
    private lateinit var metricsCache: MetricsCache
    
    @Before
    fun setup() {
        // Initialize mocks
        mockContext = mock()
        mockSharedPreferences = mock()
        mockEditor = mock()
        
        // Configure mock behavior
        whenever(mockContext.getSharedPreferences("scan_metrics_cache", Context.MODE_PRIVATE))
            .thenReturn(mockSharedPreferences)
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putString(Mockito.anyString(), Mockito.anyString())).thenReturn(mockEditor)
        whenever(mockEditor.putLong(Mockito.anyString(), Mockito.anyLong())).thenReturn(mockEditor)
        whenever(mockEditor.clear()).thenReturn(mockEditor)
        
        // Initialize the class under test
        metricsCache = MetricsCache(mockContext)
    }
    
    @After
    fun tearDown() {
        // No specific teardown needed
    }
    
    @Test
    fun saveMetrics_storesMetricsInSharedPreferences() {
        // Given
        val testMetrics = TestData.createScanMetrics(count = 150)
        val serializedMetrics = Json.encodeToString(testMetrics)
        
        // When
        metricsCache.saveMetrics(testMetrics)
        
        // Then
        verify(mockEditor).putString("metrics_data", serializedMetrics)
        verify(mockEditor).putLong(Mockito.eq("last_update_time"), Mockito.anyLong())
        verify(mockEditor).apply()
    }
    
    @Test
    fun getMetrics_returnsNullWhenNoCache() {
        // Given
        whenever(mockSharedPreferences.getString("metrics_data", null)).thenReturn(null)
        
        // When
        val result = metricsCache.getMetrics()
        
        // Then
        assertThat(result).isNull()
    }
    
    @Test
    fun getMetrics_returnsDeserializedMetrics() {
        // Given
        val testMetrics = TestData.createScanMetrics(count = 150)
        val serializedMetrics = Json.encodeToString(testMetrics)
        whenever(mockSharedPreferences.getString("metrics_data", null)).thenReturn(serializedMetrics)
        
        // When
        val result = metricsCache.getMetrics()
        
        // Then
        assertThat(result).isNotNull()
        assertThat(result?.count).isEqualTo(testMetrics.count)
        assertThat(result?.timestamp).isEqualTo(testMetrics.timestamp)
    }
    
    @Test
    fun getLastUpdateTime_returnsCorrectTimestamp() {
        // Given
        val testTimestamp = 1234567890L
        whenever(mockSharedPreferences.getLong("last_update_time", 0)).thenReturn(testTimestamp)
        
        // When
        val result = metricsCache.getLastUpdateTime()
        
        // Then
        assertThat(result).isEqualTo(testTimestamp)
    }
    
    @Test
    fun clear_removesAllCachedData() {
        // When
        metricsCache.clear()
        
        // Then
        verify(mockEditor).clear()
        verify(mockEditor).apply()
    }
    
    @Test
    fun isCacheAvailable_returnsTrueWhenCacheExists() {
        // Given
        whenever(mockSharedPreferences.contains("metrics_data")).thenReturn(true)
        
        // When
        val result = metricsCache.isCacheAvailable()
        
        // Then
        assertThat(result).isTrue()
    }
    
    @Test
    fun isCacheAvailable_returnsFalseWhenCacheDoesNotExist() {
        // Given
        whenever(mockSharedPreferences.contains("metrics_data")).thenReturn(false)
        
        // When
        val result = metricsCache.isCacheAvailable()
        
        // Then
        assertThat(result).isFalse()
    }
    
    @Test
    fun getMetrics_handlesDeserializationErrors() {
        // Given
        whenever(mockSharedPreferences.getString("metrics_data", null)).thenReturn("invalid json")
        
        // When
        val result = metricsCache.getMetrics()
        
        // Then
        assertThat(result).isNull()
    }
}