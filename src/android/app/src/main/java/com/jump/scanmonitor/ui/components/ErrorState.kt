package com.jump.scanmonitor.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * A composable function that displays an error state with an icon, error message, and action buttons.
 * This component is shown when the application fails to retrieve scan metrics data.
 *
 * @param error The exception that caused the error
 * @param onRetry Callback function to trigger a retry attempt
 * @param onHelp Optional callback function to show help information, if null the help button won't be shown
 */
@Composable
fun ErrorState(
    error: Exception,
    onRetry: () -> Unit,
    onHelp: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Error icon
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = "Error",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colors.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error title
        Text(
            text = "Unable to load scan data",
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Error message
        Text(
            text = getErrorMessage(error),
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Error suggestion
        Text(
            text = getErrorSuggestion(error),
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onHelp != null) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Retry")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = onHelp,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Help")
                }
            } else {
                // Center the retry button if there's no help button
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = onRetry
                ) {
                    Text("Retry")
                }
                
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * Returns a user-friendly error message based on the exception type.
 *
 * @param error The exception that caused the error
 * @return A user-friendly error message
 */
private fun getErrorMessage(error: Exception): String {
    return when (error) {
        is IOException -> "Network connection error"
        is SocketTimeoutException -> "Connection timed out"
        else -> {
            // Try to provide a more specific message based on the error class name
            val errorName = error.javaClass.simpleName
            when {
                errorName.contains("Http", ignoreCase = true) -> "API communication error"
                errorName.contains("Auth", ignoreCase = true) -> "Authentication error"
                errorName.contains("Timeout", ignoreCase = true) -> "Request timed out"
                else -> "An unexpected error occurred"
            }
        }
    }
}

/**
 * Returns a suggestion based on the exception type to help the user resolve the issue.
 *
 * @param error The exception that caused the error
 * @return A user-friendly suggestion for resolving the error
 */
private fun getErrorSuggestion(error: Exception): String {
    return when (error) {
        is IOException -> "Check your network connection and try again"
        is SocketTimeoutException -> "The server is taking too long to respond. Please try again later."
        else -> {
            // Try to provide a more specific suggestion based on the error class name
            val errorName = error.javaClass.simpleName
            when {
                errorName.contains("Http", ignoreCase = true) -> 
                    "There was a problem communicating with the server. Please try again."
                errorName.contains("Auth", ignoreCase = true) -> 
                    "Please contact support for assistance with API access"
                errorName.contains("Timeout", ignoreCase = true) -> 
                    "The request is taking too long. Please try again later."
                else -> "Try again or contact support if the problem persists"
            }
        }
    }
}