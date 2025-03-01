package com.jump.scanmonitor.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jump.scanmonitor.model.ScanMetrics
import com.jump.scanmonitor.ui.theme.Primary
import com.jump.scanmonitor.ui.theme.TextPrimary
import com.jump.scanmonitor.ui.theme.TextSecondary

/**
 * A composable that displays the ticket scanning metrics in a clear, readable format.
 * 
 * This component shows the total scan count for the last 2 hours with proper formatting
 * and styling to ensure optimal visibility in various stadium environments. The layout
 * is designed with high contrast and appropriate text sizes to be legible at arm's length
 * in different lighting conditions.
 *
 * @param metrics The scan metrics data to display, nullable to handle loading states
 * @param modifier Optional modifier for customizing layout
 */
@Composable
fun MetricsDisplay(
    metrics: ScanMetrics?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title text
        Text(
            text = "TOTAL SCANS",
            style = MaterialTheme.typography.h6,
            color = TextPrimary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Main count value with large, bold text
        Text(
            text = formatCount(metrics?.count ?: 0),
            style = MaterialTheme.typography.h1,
            color = Primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Time range indicator
        Text(
            text = "Last 2 Hours",
            style = MaterialTheme.typography.subtitle1,
            color = TextSecondary
        )
    }
}

/**
 * Formats an integer count with thousands separators for improved readability.
 * 
 * @param count The integer count to format
 * @return A string representation of the count with thousands separators
 */
private fun formatCount(count: Int): String {
    return String.format("%,d", count)
}