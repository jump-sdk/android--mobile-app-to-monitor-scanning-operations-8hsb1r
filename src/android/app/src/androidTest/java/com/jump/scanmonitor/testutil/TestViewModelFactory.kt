package com.jump.scanmonitor.testutil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jump.scanmonitor.model.ConnectionType
import com.jump.scanmonitor.model.NetworkStatus
import com.jump.scanmonitor.model.Result
import com.jump.scanmonitor.model.ScanMetrics
import com.jump.scanmonitor.model.UiState
import com.jump.scanmonitor.viewmodel.ScanMetricsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A factory class that creates ScanMetricsViewModel instances with predefined state for UI testing purposes.
 *
 * This class implements ViewModelProvider.Factory to create customized ViewModels that have
 * controlled state and behavior, making it easier to test UI components with specific scenarios
 * like loading states, error conditions, or specific metrics data.
 */
class TestViewModelFactory(
    private val initialState: UiState
) : ViewModelProvider.Factory {

    /**
     * Creates a ViewModel instance of the specified class with predefined test state.
     *
     * @param modelClass The class of the ViewModel to create
     * @return A ViewModel instance of the requested type, configured with test data
     * @throws IllegalArgumentException if the ViewModel class is not supported by this factory
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScanMetricsViewModel::class.java)) {
            // Create a fake repository that will provide controlled test data
            val fakeRepository = FakeRepository()
            
            // Configure the repository based on the initial state
            if (initialState.error != null) {
                fakeRepository.setNextResult(Result.Error(initialState.error))
            } else if (initialState.data != null) {
                fakeRepository.setNextResult(
                    Result.Success(
                        initialState.data,
                        isFromCache = initialState.isStale,
                        isStale = initialState.isStale
                    )
                )
            }
            
            // Create a fake network monitor with the connectivity from initial state
            val fakeNetworkMonitor = FakeNetworkMonitor(initialState.isConnected)
            
            // Create the ViewModel with fake dependencies
            @Suppress("UNCHECKED_CAST")
            return ScanMetricsViewModel(fakeRepository, fakeNetworkMonitor) as T
        }
        
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * A fake implementation of NetworkMonitor for testing, with configurable network status.
 *
 * This class allows tests to control the network connectivity status reported to the application,
 * enabling testing of online and offline behaviors without actual network dependencies.
 */
class FakeNetworkMonitor(isConnected: Boolean = true) {

    private val _networkStatus = MutableStateFlow(
        NetworkStatus(
            isConnected = isConnected,
            type = if (isConnected) ConnectionType.WIFI else ConnectionType.NONE
        )
    )
    
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()
    
    /**
     * Checks if the fake network is currently connected.
     *
     * @return The configured connected state
     */
    fun isConnected(): Boolean {
        return _networkStatus.value.isConnected
    }
    
    /**
     * Updates the network status for testing different connectivity conditions.
     *
     * @param status The new network status to set
     */
    fun setNetworkStatus(status: NetworkStatus) {
        _networkStatus.value = status
    }
    
    /**
     * Mock implementation of cleanup method from NetworkMonitor interface.
     */
    fun cleanup() {
        // No actual cleanup needed for fake implementation
    }
}