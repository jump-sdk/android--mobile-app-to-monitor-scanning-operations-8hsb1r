package com.jump.scanmonitor.di

import com.jump.scanmonitor.BuildConfig
import com.jump.scanmonitor.repository.ScanMetricsRepository
import com.jump.scanmonitor.repository.mapper.MetricsMapper
import com.jump.scanmonitor.service.api.DatadogApiService
import com.jump.scanmonitor.service.api.DatadogApiServiceImpl
import com.jump.scanmonitor.service.cache.MetricsCache
import com.jump.scanmonitor.service.network.NetworkMonitor
import com.jump.scanmonitor.viewmodel.ScanMetricsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin dependency injection module that provides all necessary dependencies for the ScanMonitor application.
 * This module defines singleton instances and factory methods for creating application components.
 *
 * The module follows a clean architecture approach with clear separation between:
 * - UI layer (ViewModels)
 * - Domain layer (Repositories)
 * - Data layer (API Services, Cache Services)
 */
val appModule = module {
    // ViewModels
    viewModel { 
        ScanMetricsViewModel(
            repository = get(),
            networkMonitor = get()
        )
    }
    
    // Repositories
    single { 
        ScanMetricsRepository(
            apiService = get(),
            cache = get(),
            mapper = get()
        )
    }
    
    // Services
    single { 
        DatadogApiServiceImpl(
            apiKey = BuildConfig.DATADOG_API_KEY,
            applicationKey = BuildConfig.DATADOG_APP_KEY
        ).create() 
    }
    
    // Utilities
    single { MetricsCache(androidContext()) }
    single { NetworkMonitor(androidContext()) }
    single { MetricsMapper() }
}