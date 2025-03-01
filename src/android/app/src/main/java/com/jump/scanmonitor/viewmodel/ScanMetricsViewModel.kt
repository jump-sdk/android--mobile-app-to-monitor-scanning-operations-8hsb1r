package com.jump.scanmonitor.viewmodel

import androidx.lifecycle.ViewModel // androidx.lifecycle 2.6.1
import androidx.lifecycle.viewModelScope
import com.jump.scanmonitor.model.NetworkStatus
import com.jump.scanmonitor.model.Result
import com.jump.scanmonitor.model.ScanMetrics
import com.jump.scanmonitor.model.UiState
import com.jump.scanmonitor.repository.ScanMetricsRepository
import com.jump.scanmonitor.service.network.NetworkMonitor
import kotlinx.coroutines.Job // kotlinx.coroutines 1.7.0+
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber // timber 5.0.0+

/**
 * ViewModel that manages and coordinates data flow between the repository layer and UI,
 * handling network status changes, data refreshing, and maintaining observable UI state.
 *
 * This ViewModel implements the following requirements:
 * - F-001: Scanning Metrics Dashboard - Provides data for the dashboard display
 * - F-002: Datadog API Integration - Coordinates with repository to fetch API data
 * - F-003: Offline Mode Handling - Manages UI state for connectivity changes
 * - F-004: Auto-Refresh Functionality - Periodically refreshes data at set intervals
 */
class ScanMetricsViewModel(
    private val repository: ScanMetricsRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    // Internal mutable state
    private val _uiState = MutableStateFlow(UiState(loading = true))
    
    // Publicly exposed immutable state for UI observation
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    // Job for auto-refresh functionality
    private var refreshJob: Job? = null

    init {
        Timber.d("Initializing ScanMetricsViewModel")
        
        // Monitor network status
        viewModelScope.launch {
            networkMonitor.networkStatus.collect { status ->
                Timber.d("Network status changed: $status")
                
                _uiState.update { it.copy(isConnected = status.isConnected) }
                
                // If connection is restored and there was an error, retry
                if (status.isConnected && _uiState.value.error != null) {
                    Timber.d("Connection restored, triggering refresh")
                    refreshMetrics()
                }
            }
        }
        
        // Initial data load
        loadMetrics()
        
        // Start auto-refresh
        startAutoRefresh()
    }

    /**
     * Manually triggers a refresh of scan metrics data from the repository.
     * This can be called from the UI in response to a pull-to-refresh gesture.
     * Implements requirement F-004-RQ-002: Allow manual refresh via pull-to-refresh.
     */
    fun refreshMetrics() {
        Timber.d("Manual refresh triggered")
        
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            
            when (val result = repository.getMetrics(forceRefresh = true)) {
                is Result.Success -> {
                    Timber.d("Refresh successful: count=${result.data.count}")
                    _uiState.update { 
                        it.copy(
                            loading = false,
                            data = result.data,
                            error = null,
                            isStale = false
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error refreshing metrics")
                    _uiState.update { 
                        it.copy(
                            loading = false,
                            error = result.exception,
                            isStale = it.data != null
                        )
                    }
                }
            }
        }
    }

    /**
     * Loads scan metrics data from the repository, either from cache or API.
     * This is called during initialization to populate the initial UI state.
     * Implements requirements F-001-RQ-001: Display total scan counts for the last 2 hours
     * and F-003-RQ-004: Display last retrieved data with timestamp.
     */
    private fun loadMetrics() {
        Timber.d("Initial data load")
        
        viewModelScope.launch {
            when (val result = repository.getMetrics()) {
                is Result.Success -> {
                    Timber.d("Initial load successful: count=${result.data.count}, fromCache=${result.isFromCache}")
                    _uiState.update { 
                        it.copy(
                            loading = false,
                            data = result.data,
                            error = null,
                            isStale = result.isStale
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error loading initial metrics")
                    _uiState.update { 
                        it.copy(
                            loading = false,
                            error = result.exception
                        )
                    }
                }
            }
        }
    }

    /**
     * Starts a repeating timer to automatically refresh metrics data at regular intervals.
     * This implements requirement F-004-RQ-001: Automatically refresh data every 5 minutes.
     */
    private fun startAutoRefresh() {
        Timber.d("Starting auto-refresh timer with interval: $AUTO_REFRESH_INTERVAL ms")
        
        // Cancel any existing job
        refreshJob?.cancel()
        
        refreshJob = viewModelScope.launch {
            while (isActive) {
                delay(AUTO_REFRESH_INTERVAL)
                Timber.d("Auto-refresh interval elapsed")
                refreshMetrics()
            }
        }
    }

    /**
     * Lifecycle method called when ViewModel is being destroyed, used for cleanup.
     * Cancels the auto-refresh job to prevent memory leaks.
     */
    override fun onCleared() {
        Timber.d("ViewModel cleared, canceling auto-refresh job")
        refreshJob?.cancel()
        super.onCleared()
    }

    companion object {
        /**
         * Interval for auto-refreshing metrics data in milliseconds (5 minutes)
         */
        private const val AUTO_REFRESH_INTERVAL = 5 * 60 * 1000L
    }
}