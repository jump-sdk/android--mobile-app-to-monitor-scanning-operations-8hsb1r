package com.jump.scanmonitor.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jump.scanmonitor.model.ConnectionType
import com.jump.scanmonitor.model.NetworkStatus
import com.jump.scanmonitor.model.Result
import com.jump.scanmonitor.model.ScanMetrics
import com.jump.scanmonitor.model.UiState
import com.jump.scanmonitor.repository.ScanMetricsRepository
import com.jump.scanmonitor.service.network.NetworkMonitor
import com.jump.scanmonitor.testutil.MainCoroutineRule
import com.jump.scanmonitor.testutil.TestData
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when` as whenever
import org.mockito.MockitoAnnotations

/**
 * Unit tests for [ScanMetricsViewModel] to verify its behavior under various conditions
 * including data loading, error handling, network status changes, and auto-refresh functionality.
 */
class ScanMetricsViewModelTest {

    // Run tasks synchronously for LiveData testing
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Manages coroutines for testing
    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var repository: ScanMetricsRepository

    @Mock
    private lateinit var networkMonitor: NetworkMonitor

    private lateinit var viewModel: ScanMetricsViewModel
    private val networkStatusFlow = MutableStateFlow(TestData.CONNECTED_WIFI)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        // Setup networkMonitor mock
        whenever(networkMonitor.networkStatus).thenReturn(networkStatusFlow)
        whenever(networkMonitor.isConnected()).thenReturn(true)
        
        // Default setup - will be overridden in specific tests
        whenever(repository.getMetrics(false)).thenReturn(TestData.SUCCESS_RESULT)
        
        viewModel = ScanMetricsViewModel(repository, networkMonitor)
    }

    @After
    fun tearDown() {
        reset(repository, networkMonitor)
    }

    @Test
    fun initialState_isLoading() = runTest {
        // Create a new ViewModel without loading data to test initial state
        whenever(repository.getMetrics(false)).thenReturn(TestData.SUCCESS_RESULT)
        val newViewModel = ScanMetricsViewModel(repository, networkMonitor)
        
        // Assert that initial state shows loading
        val initialState = newViewModel.uiState.first()
        assertThat(initialState.loading).isTrue()
        assertThat(initialState.data).isNull()
        assertThat(initialState.error).isNull()
    }

    @Test
    fun loadMetrics_success_updatesState() = runTest {
        // Arrange
        val expectedMetrics = ScanMetrics(count = 150)
        whenever(repository.getMetrics(false)).thenReturn(Result.Success(expectedMetrics))
        
        // Act - create a new ViewModel to trigger loadMetrics
        val newViewModel = ScanMetricsViewModel(repository, networkMonitor)
        
        // Assert - skip initial loading state and get the actual data state
        val loadedState = newViewModel.uiState.drop(1).first()
        assertThat(loadedState.loading).isFalse()
        assertThat(loadedState.data).isEqualTo(expectedMetrics)
        assertThat(loadedState.error).isNull()
        assertThat(loadedState.isStale).isFalse()
    }

    @Test
    fun loadMetrics_error_updatesState() = runTest {
        // Arrange
        val expectedError = IOException("Network error")
        whenever(repository.getMetrics(false)).thenReturn(Result.Error(expectedError))
        
        // Act - create a new ViewModel to trigger loadMetrics
        val newViewModel = ScanMetricsViewModel(repository, networkMonitor)
        
        // Assert - skip initial loading state and get the error state
        val errorState = newViewModel.uiState.drop(1).first()
        assertThat(errorState.loading).isFalse()
        assertThat(errorState.data).isNull()
        assertThat(errorState.error).isEqualTo(expectedError)
    }

    @Test
    fun refreshMetrics_success_updatesState() = runTest {
        // Arrange
        val initialMetrics = ScanMetrics(count = 100)
        val updatedMetrics = ScanMetrics(count = 150)
        
        // Setup initial state
        whenever(repository.getMetrics(false)).thenReturn(Result.Success(initialMetrics))
        val newViewModel = ScanMetricsViewModel(repository, networkMonitor)
        
        // Force loading to complete
        newViewModel.uiState.drop(1).first()
        
        // Setup refresh response
        whenever(repository.getMetrics(true)).thenReturn(Result.Success(updatedMetrics))
        
        // Act
        newViewModel.refreshMetrics()
        
        // Assert - Get state after refresh (skip loading state)
        val refreshedState = newViewModel.uiState.drop(1).first() 
        assertThat(refreshedState.loading).isFalse()
        assertThat(refreshedState.data).isEqualTo(updatedMetrics)
        assertThat(refreshedState.error).isNull()
        assertThat(refreshedState.isStale).isFalse()
    }

    @Test
    fun refreshMetrics_error_updatesState() = runTest {
        // Arrange
        val initialMetrics = ScanMetrics(count = 100)
        val expectedError = IOException("API error")
        
        // Setup initial state with data
        whenever(repository.getMetrics(false)).thenReturn(Result.Success(initialMetrics))
        val newViewModel = ScanMetricsViewModel(repository, networkMonitor)
        
        // Force initial loading to complete
        newViewModel.uiState.drop(1).first()
        
        // Setup refresh error
        whenever(repository.getMetrics(true)).thenReturn(Result.Error(expectedError))
        
        // Act
        newViewModel.refreshMetrics()
        
        // Assert - skip loading state and get the error state
        val errorState = newViewModel.uiState.drop(1).first()
        assertThat(errorState.loading).isFalse()
        assertThat(errorState.error).isEqualTo(expectedError)
        // Data from before should still be present
        assertThat(errorState.data).isEqualTo(initialMetrics)
        // Should be marked as stale since we have old data with a new error
        assertThat(errorState.isStale).isTrue()
    }

    @Test
    fun networkStatusChange_updatesConnectedState() = runTest {
        // Arrange - ViewModel already initialized in setup()
        
        // Act - change network status to disconnected
        networkStatusFlow.value = TestData.DISCONNECTED
        
        // Assert - verify that UI state is updated with disconnected status
        val disconnectedState = viewModel.uiState.drop(1).first()
        assertThat(disconnectedState.isConnected).isFalse()
        
        // Act - change network status back to connected
        networkStatusFlow.value = TestData.CONNECTED_WIFI
        
        // Assert - verify that UI state is updated with connected status
        val connectedState = viewModel.uiState.drop(1).first()
        assertThat(connectedState.isConnected).isTrue()
    }

    @Test
    fun networkStatusChange_triggersRefreshWhenReconnected() = runTest {
        // Arrange - Setup with error state initially
        val expectedError = IOException("Network error")
        val expectedMetrics = ScanMetrics(count = 200)
        
        whenever(repository.getMetrics(false)).thenReturn(Result.Error(expectedError))
        val newViewModel = ScanMetricsViewModel(repository, networkMonitor)
        
        // Wait for initial error state
        newViewModel.uiState.drop(1).first()
        
        // Configure repository to return success for subsequent calls
        whenever(repository.getMetrics(true)).thenReturn(Result.Success(expectedMetrics))
        
        // Reset to ensure we can verify calls after this point
        reset(repository)
        whenever(repository.getMetrics(true)).thenReturn(Result.Success(expectedMetrics))
        
        // Act - simulate network disconnect and reconnect
        networkStatusFlow.value = TestData.DISCONNECTED
        networkStatusFlow.value = TestData.CONNECTED_WIFI
        
        // Assert
        // Verify repository was called again after reconnection
        verify(repository, times(1)).getMetrics(true)
        
        // Verify UI is updated with the new data
        val updatedState = newViewModel.uiState.drop(1).first()
        assertThat(updatedState.data).isEqualTo(expectedMetrics)
        assertThat(updatedState.error).isNull()
    }

    @Test
    fun autoRefresh_triggersPeriodicUpdates() = runTest {
        // Arrange
        whenever(repository.getMetrics(false)).thenReturn(TestData.SUCCESS_RESULT)
        val newViewModel = ScanMetricsViewModel(repository, networkMonitor)
        
        // Force initial loading to complete
        newViewModel.uiState.drop(1).first()
        
        // Clear previous invocations
        reset(repository)
        whenever(repository.getMetrics(true)).thenReturn(TestData.SUCCESS_RESULT)
        
        // Act - advance time to trigger auto-refresh
        advanceTimeBy(5 * 60 * 1000 + 100) // 5 minutes + buffer
        
        // Assert
        verify(repository, times(1)).getMetrics(true)
        
        // Act - advance time for another refresh
        advanceTimeBy(5 * 60 * 1000 + 100) // 5 minutes + buffer
        
        // Assert
        verify(repository, times(2)).getMetrics(true)
    }

    @Test
    fun cachedData_markedAsStale() = runTest {
        // Arrange
        val cachedMetrics = ScanMetrics(count = 100)
        whenever(repository.getMetrics(false)).thenReturn(
            Result.Success(
                data = cachedMetrics,
                isFromCache = true,
                isStale = true
            )
        )
        
        // Act
        val newViewModel = ScanMetricsViewModel(repository, networkMonitor)
        
        // Assert
        val state = newViewModel.uiState.drop(1).first()
        assertThat(state.data).isEqualTo(cachedMetrics)
        assertThat(state.isStale).isTrue()
    }

    @Test
    fun onCleared_cancelsRefreshJob() = runTest {
        // Arrange
        whenever(repository.getMetrics(false)).thenReturn(TestData.SUCCESS_RESULT)
        val newViewModel = ScanMetricsViewModel(repository, networkMonitor)
        
        // Force initial loading to complete
        newViewModel.uiState.drop(1).first()
        
        // Clear repository invocations to start fresh
        reset(repository)
        whenever(repository.getMetrics(true)).thenReturn(TestData.SUCCESS_RESULT)
        
        // Act - call onCleared to cancel auto-refresh
        newViewModel.onCleared()
        
        // Advance time to when auto-refresh would have happened
        advanceTimeBy(10 * 60 * 1000) // 10 minutes
        
        // Assert - verify no more calls to repository after onCleared
        verify(repository, times(0)).getMetrics(true)
    }
    
    // Test helper classes
    
    /**
     * A fake implementation of NetworkMonitor for testing that allows control over the network status.
     */
    class FakeNetworkMonitor {
        private val _networkStatus = MutableStateFlow(TestData.CONNECTED_WIFI)
        val networkStatus = _networkStatus
        
        fun setNetworkStatus(status: NetworkStatus) {
            _networkStatus.value = status
        }
        
        fun isConnected(): Boolean {
            return _networkStatus.value.isConnected
        }
    }
    
    /**
     * A fake implementation of ScanMetricsRepository for testing that allows control over the returned results.
     */
    class FakeScanMetricsRepository {
        private var nextResult: Result<ScanMetrics>? = null
        var getMetricsCalledWithForceRefresh = false
        var getMetricsCallCount = 0
        
        fun setNextResult(result: Result<ScanMetrics>) {
            nextResult = result
        }
        
        suspend fun getMetrics(forceRefresh: Boolean = false): Result<ScanMetrics> {
            getMetricsCallCount++
            if (forceRefresh) {
                getMetricsCalledWithForceRefresh = true
            }
            return nextResult ?: Result.Error(IllegalStateException("No result configured for test"))
        }
        
        suspend fun refreshMetrics(): Result<ScanMetrics> {
            return getMetrics(true)
        }
        
        fun clearInvocations() {
            getMetricsCallCount = 0
            getMetricsCalledWithForceRefresh = false
        }
    }
}