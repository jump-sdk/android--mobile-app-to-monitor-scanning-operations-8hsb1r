package com.jump.scanmonitor.model

import kotlin.Exception

/**
 * A sealed class that provides a generic wrapper for operation results, handling both success and error cases.
 * This pattern allows for type-safe error handling without exceptions and is used primarily for 
 * Repository operations returning data to the ViewModel.
 *
 * @param T The type of data returned in the success case
 */
sealed class Result<out T> private constructor() {
    
    /**
     * Represents a successful operation result containing the requested data.
     *
     * @param data The data returned by the operation
     * @param isFromCache Flag indicating if the data was retrieved from cache instead of the remote source
     * @param isStale Flag indicating if the data may be outdated
     */
    data class Success<T>(
        val data: T, 
        val isFromCache: Boolean = false,
        val isStale: Boolean = false
    ) : Result<T>()
    
    /**
     * Represents a failed operation result containing the exception that caused the failure.
     *
     * @param exception The exception that caused the operation to fail
     */
    data class Error(val exception: Exception) : Result<Nothing>()
}