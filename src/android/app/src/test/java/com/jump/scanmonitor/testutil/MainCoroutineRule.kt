package com.jump.scanmonitor.testutil

import org.junit.rules.TestWatcher // Version: 4.13.2
import org.junit.runner.Description
import kotlinx.coroutines.Dispatchers // Version: 1.7.0
import kotlinx.coroutines.test.TestCoroutineDispatcher // Version: 1.7.0
import kotlinx.coroutines.test.TestCoroutineScope // Version: 1.7.0
import kotlinx.coroutines.test.resetMain // Version: 1.7.0
import kotlinx.coroutines.test.setMain // Version: 1.7.0
import kotlinx.coroutines.test.runBlockingTest

/**
 * A JUnit rule that configures Kotlin coroutines for testing by replacing the Main dispatcher 
 * with a TestCoroutineDispatcher and providing utilities for controlling coroutines execution in tests.
 */
class MainCoroutineRule : TestWatcher() {
    
    /**
     * Test dispatcher used to control coroutine execution in tests.
     */
    val testDispatcher = TestCoroutineDispatcher()
    
    /**
     * Called before each test to set up the test environment by replacing the Main dispatcher.
     */
    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }
    
    /**
     * Called after each test to clean up the test environment by resetting the Main dispatcher.
     */
    override fun finished(description: Description) {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
        super.finished(description)
    }
    
    /**
     * Convenience method to run a block of code in a controlled test environment with the test dispatcher.
     *
     * @param block The suspending block of code to execute
     * @return The result of the block execution
     */
    fun <T> runBlockingTest(block: suspend () -> T): T {
        return testDispatcher.runBlockingTest { block() }
    }
}