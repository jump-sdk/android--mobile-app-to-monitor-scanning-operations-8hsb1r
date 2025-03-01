package com.jump.scanmonitor

import android.app.Application
import com.jump.scanmonitor.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.startKoin
import org.koin.core.context.modules
import timber.log.Timber

/**
 * Custom Application class that serves as the entry point for application-wide initialization.
 * It configures Koin for dependency injection and sets up Timber for logging in debug builds.
 */
class ScanMonitorApplication : Application() {

    /**
     * Called when the application is starting. Initializes dependencies and logging.
     */
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin for dependency injection
        startKoin {
            androidContext(this@ScanMonitorApplication)
            modules(appModule)
        }
        
        // Initialize Timber for logging in debug builds only
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}