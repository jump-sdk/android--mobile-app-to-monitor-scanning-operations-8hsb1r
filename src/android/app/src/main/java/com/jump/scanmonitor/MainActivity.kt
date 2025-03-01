package com.jump.scanmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jump.scanmonitor.ui.screens.MetricsDashboardScreen
import com.jump.scanmonitor.ui.theme.ScanMonitorTheme
import timber.log.Timber

/**
 * Main activity class that serves as the entry point for the ScanMonitorApps application.
 * It hosts the Compose UI and sets up the MetricsDashboardScreen as the primary UI component.
 *
 * This activity implements a single-screen architecture as specified in the technical requirements,
 * providing a container for the scanning metrics dashboard (F-001: Scanning Metrics Dashboard).
 */
class MainActivity : ComponentActivity() {

    /**
     * Lifecycle method called when the activity is first created.
     * Sets up the Compose UI with the ScanMonitorTheme and MetricsDashboardScreen.
     * 
     * @param savedInstanceState If the activity is being re-initialized after previously 
     * being shut down, this contains the data it most recently supplied in onSaveInstanceState.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Timber.d("Creating MainActivity")
        
        // Set up the Compose UI content
        setContent {
            // Apply the application theme for consistent styling
            ScanMonitorTheme {
                // Display the metrics dashboard as the main screen
                MetricsDashboardScreen()
            }
        }
    }
}