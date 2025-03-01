package com.jump.scanmonitor.testutil

import com.jump.scanmonitor.model.Result
import com.jump.scanmonitor.model.ScanMetrics
import com.jump.scanmonitor.repository.ScanMetricsRepository
import kotlinx.coroutines.delay // Coroutines 1.7.0+

/**
 * A fake implementation of ScanMetricsRepository used for testing.
 * 
 * This class allows controlling the response data and error states in tests,
 * tracking method invocations, and simulating various repository behaviors
 * without actual API calls.
 */
class FakeRepository : ScanMetricsRepository {
    
    // The result to return for the next repository call
    var nextResult: Result<ScanMetrics>? = null
    
    // A queue of results to return in sequence
    private val queuedResults = mutableListOf<Result<ScanMetrics>>()
    
    // Counters to track method calls
    var getMetricsCallCount = 0
        private set
    
    var refreshMetricsCallCount = 0
        private set
    
    // Tracks if getMetrics was called with forceRefresh=true
    var getMetricsCalledWithForceRefresh = false
        private set
    
    // Simulated network delay in milliseconds
    var simulatedDelay = 0L
    
    /**
     * Sets the result to be returned on the next repository call.
     *
     * @param result The Result to return
     */
    fun setNextResult(result: Result<ScanMetrics>) {
        nextResult = result
    }
    
    /**
     * Queues multiple results to be returned in sequence.
     *
     * @param results The list of Results to return in order
     */
    fun queueResults(results: List<Result<ScanMetrics>>) {
        queuedResults.addAll(results)
    }
    
    /**
     * Sets a delay to simulate network latency.
     *
     * @param delayMs The delay in milliseconds
     */
    fun setSimulatedDelay(delayMs: Long) {
        simulatedDelay = delayMs
    }
    
    /**
     * Resets call counters for test verification.
     */
    fun clearInvocations() {
        getMetricsCallCount = 0
        refreshMetricsCallCount = 0
        getMetricsCalledWithForceRefresh = false
    }
    
    /**
     * Fake implementation of getMetrics that returns controlled test data.
     *
     * @param forceRefresh Flag to simulate forced refresh behavior
     * @return The predefined result or default error
     */
    override suspend fun getMetrics(forceRefresh: Boolean): Result<ScanMetrics> {
        getMetricsCallCount++
        getMetricsCalledWithForceRefresh = forceRefresh
        
        // Simulate network delay if set
        if (simulatedDelay > 0) {
            delay(simulatedDelay)
        }
        
        // Return the next result from the queue if available
        if (queuedResults.isNotEmpty()) {
            return queuedResults.removeAt(0)
        }
        
        // Return the next result if set
        return nextResult ?: Result.Error(IllegalStateException("No test result configured for FakeRepository"))
    }
    
    /**
     * Fake implementation of refreshMetrics that delegates to getMetrics.
     *
     * @return The result from getMetrics with forceRefresh=true
     */
    override suspend fun refreshMetrics(): Result<ScanMetrics> {
        refreshMetricsCallCount++
        return getMetrics(true)
    }
}