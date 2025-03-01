package com.jump.scanmonitor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.jump.scanmonitor.ui.components.ErrorState
import com.jump.scanmonitor.ui.components.LoadingIndicator
import com.jump.scanmonitor.ui.components.StatusBar
import com.jump.scanmonitor.ui.components.MetricsDisplay
import com.jump.scanmonitor.viewmodel.ScanMetricsViewModel
import com.jump.scanmonitor.model.UiState

/**
 * Main screen of the ScanMonitorApps application that displays ticket scanning metrics.
 * This screen is responsible for showing the appropriate UI based on the current state,
 * including loading indicators, error messages, and the actual metrics display.
 * 
 * Implements:
 * - F-001: Scanning Metrics Dashboard for displaying ticket scan counts
 * - F-003: Offline Mode Handling with appropriate user feedback
 * - F-004: Auto-Refresh functionality both automatic and manual (pull-to-refresh)
 */
@Composable
fun MetricsDashboardScreen() {
    // Get ViewModel instance using the composable function
    val viewModel: ScanMetricsViewModel = viewModel()
    
    // Collect the UI state from the ViewModel
    val uiState = viewModel.uiState.collectAsState().value
    
    // Set up the main surface with theme background
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        // SwipeRefresh for pull-to-refresh functionality (F-004-RQ-002)
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = uiState.loading),
            onRefresh = { viewModel.refreshMetrics() },
            modifier = Modifier.fillMaxSize()
        ) {
            // Render appropriate content based on state
            MetricsDashboardContent(
                uiState = uiState,
                onRefresh = { viewModel.refreshMetrics() }
            )
        }
    }
}

/**
 * Helper composable that renders the appropriate content based on the current UI state.
 * 
 * @param uiState The current UI state from the ViewModel
 * @param onRefresh Callback to trigger a metrics refresh
 */
@Composable
private fun MetricsDashboardContent(
    uiState: UiState,
    onRefresh: () -> Unit
) {
    when {
        // Show loading indicator when loading with no data (F-001-RQ-004)
        uiState.loading && uiState.data == null -> {
            LoadingIndicator(message = "Loading scan data...")
        }
        // Show error state when there's an error and no data to display
        uiState.error != null && uiState.data == null -> {
            ErrorState(
                error = uiState.error,
                onRetry = onRefresh
            )
        }
        // Show metrics even during loading or with errors if we have data to show
        else -> {
            Column(modifier = Modifier.fillMaxSize()) {
                // Status bar shows connectivity and freshness information (F-003-RQ-002)
                StatusBar(
                    isOffline = !uiState.isConnected,
                    isStale = uiState.isStale,
                    lastUpdated = uiState.data?.timestamp
                )
                
                // Metrics display shows the scan count data (F-001-RQ-001, F-001-RQ-002)
                if (uiState.data != null) {
                    MetricsDisplay(metrics = uiState.data)
                }
            }
        }
    }
}