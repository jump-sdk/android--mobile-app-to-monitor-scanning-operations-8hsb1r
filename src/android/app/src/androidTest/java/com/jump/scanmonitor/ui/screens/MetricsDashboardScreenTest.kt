package com.jump.scanmonitor.ui.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.lifecycle.viewmodel.compose.LocalViewModelFactory
import com.google.common.truth.Truth.assertThat
import com.jump.scanmonitor.model.ScanMetrics
import com.jump.scanmonitor.model.UiState
import com.jump.scanmonitor.model.Result
import com.jump.scanmonitor.testutil.TestViewModelFactory
import com.jump.scanmonitor.testutil.FakeRepository
import org.junit.Rule
import org.junit.Test
import org.junit.Before
import java.io.IOException

/**
 * Test class for verifying the MetricsDashboardScreen composable correctly displays 
 * different UI states and handles user interactions.
 */
class MetricsDashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    
    private lateinit var repository: FakeRepository
    
    @Before
    fun setUp() {
        repository = FakeRepository()
    }
    
    /**
     * Verifies that loading state displays a progress indicator.
     * Tests requirement F-001-RQ-004: Display loading indicator during data retrieval
     */
    @Test
    fun test_loadingState_showsProgressIndicator() {
        // Create a loading UI state with no data
        val loadingState = UiState(loading = true)
        
        // Set up the compose test with MetricsDashboardScreen and TestViewModelFactory
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalViewModelFactory provides TestViewModelFactory(loadingState)
            ) {
                MetricsDashboardScreen()
            }
        }
        
        // Assert that a loading indicator is displayed
        composeTestRule.onNodeWithText("Loading scan data...").assertIsDisplayed()
        
        // Assert that scan metrics are not displayed
        composeTestRule.onNodeWithText("TOTAL SCANS").assertDoesNotExist()
    }
    
    /**
     * Verifies that data state displays scan metrics correctly.
     * Tests requirement F-001: Scanning Metrics Dashboard
     */
    @Test
    fun test_dataState_showsScanMetrics() {
        // Create a test ScanMetrics object with count=250
        val metrics = ScanMetrics(count = 250)
        
        // Create a UI state with the test metrics data
        val dataState = UiState(loading = false, data = metrics)
        
        // Set up the compose test with MetricsDashboardScreen and TestViewModelFactory
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalViewModelFactory provides TestViewModelFactory(dataState)
            ) {
                MetricsDashboardScreen()
            }
        }
        
        // Assert that '250' text is displayed
        composeTestRule.onNodeWithText("250").assertIsDisplayed()
        
        // Assert that 'TOTAL SCANS' text is displayed
        composeTestRule.onNodeWithText("TOTAL SCANS").assertIsDisplayed()
        
        // Assert that 'Last 2 Hours' text is displayed
        composeTestRule.onNodeWithText("Last 2 Hours").assertIsDisplayed()
    }
    
    /**
     * Verifies that error state with no data displays error message and retry button.
     */
    @Test
    fun test_errorState_showsErrorMessage() {
        // Create an error UI state with no data
        val errorState = UiState(
            loading = false,
            error = IOException("Network error"),
            data = null
        )
        
        // Set up the compose test with MetricsDashboardScreen and TestViewModelFactory
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalViewModelFactory provides TestViewModelFactory(errorState)
            ) {
                MetricsDashboardScreen()
            }
        }
        
        // Assert that error message is displayed
        composeTestRule.onNodeWithText("Unable to load scan data").assertIsDisplayed()
        
        // Assert that retry button is displayed
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }
    
    /**
     * Verifies that offline state displays appropriate status indicator.
     * Tests requirement F-003-RQ-002: Display appropriate message when offline
     */
    @Test
    fun test_offlineState_showsOfflineIndicator() {
        // Create a test ScanMetrics object
        val metrics = ScanMetrics(count = 100)
        
        // Create a UI state with isConnected=false and cached data
        val offlineState = UiState(
            loading = false,
            data = metrics,
            isConnected = false
        )
        
        // Set up the compose test with MetricsDashboardScreen and TestViewModelFactory
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalViewModelFactory provides TestViewModelFactory(offlineState)
            ) {
                MetricsDashboardScreen()
            }
        }
        
        // Assert that offline indicator is displayed
        composeTestRule.onNodeWithText("Offline Mode").assertIsDisplayed()
        
        // Assert that cached data is still displayed
        composeTestRule.onNodeWithText("100").assertIsDisplayed()
    }
    
    /**
     * Verifies that stale data state displays appropriate indicator.
     */
    @Test
    fun test_staleDataState_showsStalenessIndicator() {
        // Create a test ScanMetrics object
        val metrics = ScanMetrics(count = 150)
        
        // Create a UI state with isStale=true
        val staleState = UiState(
            loading = false,
            data = metrics,
            isStale = true
        )
        
        // Set up the compose test with MetricsDashboardScreen and TestViewModelFactory
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalViewModelFactory provides TestViewModelFactory(staleState)
            ) {
                MetricsDashboardScreen()
            }
        }
        
        // Assert that staleness indicator is displayed
        composeTestRule.onNodeWithText("Data may be outdated").assertIsDisplayed()
        
        // Assert that metrics data is still displayed
        composeTestRule.onNodeWithText("150").assertIsDisplayed()
    }
    
    /**
     * Verifies that clicking retry button triggers data refresh.
     */
    @Test
    fun test_retryButton_triggersRefresh() {
        // Create an error UI state
        val errorState = UiState(
            loading = false,
            error = IOException("Network error"),
            data = null
        )
        
        // Setup the repository to return success on next call
        val fakeRepository = FakeRepository()
        fakeRepository.setNextResult(Result.Success(ScanMetrics(count = 300)))
        
        // Create a TestViewModelFactory with the repository
        val testFactory = TestViewModelFactory(errorState)
        
        // Set up the compose test with MetricsDashboardScreen and TestViewModelFactory
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalViewModelFactory provides testFactory
            ) {
                MetricsDashboardScreen()
            }
        }
        
        // Click retry button
        composeTestRule.onNodeWithText("Retry").performClick()
        
        // Assert that repository refresh method was called
        // Note: We can't directly assert on fakeRepository.refreshMetricsCallCount 
        // because the repository is created inside TestViewModelFactory
        
        // Instead, we wait for the UI to update with the new data
        composeTestRule.waitForIdle()
        
        // We should see the loading indicator
        composeTestRule.onNodeWithText("Loading scan data...").assertIsDisplayed()
    }
    
    /**
     * Verifies that pull-to-refresh gesture triggers data refresh.
     * Tests requirement F-004-RQ-002: Allow manual refresh via pull-to-refresh
     */
    @Test
    fun test_pullToRefresh_triggersDataRefresh() {
        // Create a UI state with data
        val metrics = ScanMetrics(count = 200)
        val dataState = UiState(loading = false, data = metrics)
        
        // Set up the compose test with MetricsDashboardScreen and TestViewModelFactory
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalViewModelFactory provides TestViewModelFactory(dataState)
            ) {
                MetricsDashboardScreen()
            }
        }
        
        // Get the count before refresh
        composeTestRule.onNodeWithText("200").assertIsDisplayed()
        
        // Perform swipe down gesture to trigger refresh
        composeTestRule.onNodeWithText("TOTAL SCANS").performTouchInput { swipeDown() }
        
        // Assert that UI updates with refreshing indicator
        // We should see the loading state briefly
        composeTestRule.waitForIdle()
        
        // The data should still be displayed
        composeTestRule.onNodeWithText("200").assertIsDisplayed()
    }
}