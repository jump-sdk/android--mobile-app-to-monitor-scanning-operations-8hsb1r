# Firebase Setup for ScanMonitorApps

This document provides step-by-step instructions for setting up and configuring Firebase services for the ScanMonitorApps Android application. Firebase will be used to implement monitoring and observability features, enabling comprehensive tracking of application performance, crashes, and usage patterns.

## Prerequisites

Before starting the Firebase setup process, ensure you have:

- A Google account with access to Google Cloud Platform
- Android Studio Hedgehog (2023.1.1+) installed
- Access to the ScanMonitorApps project repository
- Appropriate permissions to create and manage Firebase projects
- Firebase CLI (optional, for manual setup)

## Creating a Firebase Project

1. **Create or select a Firebase project**:
   - Go to the [Firebase Console](https://console.firebase.google.com/)
   - Click "Add project" or select an existing project
   - Enter a project name (e.g., "ScanMonitorApps")
   - Choose whether to enable Google Analytics (recommended for this project)
   - Select your Analytics account or create a new one
   - Click "Create project"

2. **Configure Firebase project settings**:
   - Once your project is created, click on the settings icon (gear) next to "Project Overview"
   - Select "Project settings"
   - Take note of your Project ID and Web API Key for later use

## Adding Firebase to Android App

1. **Register your Android app with Firebase**:
   - In the Firebase console, click "Add app" and select the Android platform
   - Enter the package name: `com.jump.scanmonitor`
   - Enter app nickname: "ScanMonitorApps"
   - Enter SHA-1 signing certificate (optional for development, required for production)
   - Click "Register app"

2. **Download configuration file**:
   - Firebase will generate a `google-services.json` file
   - Download this file and place it in the app module directory of your project (`src/android/app/`)

3. **Update Gradle files**:
   The project already includes the necessary Firebase Gradle configurations in `app/build.gradle.kts`, but verify that these entries exist:

   ```kotlin
   plugins {
       // Existing plugins...
       id("com.google.firebase.crashlytics")
       id("com.google.gms.google-services")
   }
   
   dependencies {
       // Firebase
       implementation(platform("com.google.firebase:firebase-bom:32.1.1"))
       implementation("com.google.firebase:firebase-crashlytics-ktx")
       implementation("com.google.firebase:firebase-analytics-ktx")
   }
   ```

## Configuring Firebase Services

### Configuring Firebase Crashlytics

1. **Enable Crashlytics in Firebase Console**:
   - Navigate to the Crashlytics section in your Firebase project
   - Click "Enable Crashlytics"
   - Follow the onboarding workflow

2. **Initialize Crashlytics in your app**:
   Update the `ScanMonitorApplication.kt` file to initialize Firebase and Crashlytics:

   ```kotlin
   import com.google.firebase.Firebase
   import com.google.firebase.crashlytics.FirebaseCrashlytics
   import com.google.firebase.initialize
   import android.util.Log

   class ScanMonitorApplication : Application() {
       override fun onCreate() {
           super.onCreate()
           
           // Initialize Firebase
           Firebase.initialize(this)
           
           // Initialize Koin for dependency injection
           startKoin {
               androidContext(this@ScanMonitorApplication)
               modules(appModule)
           }
           
           // Initialize Timber for logging
           if (BuildConfig.DEBUG) {
               Timber.plant(Timber.DebugTree())
           } else {
               // In production, route logs to Crashlytics
               Timber.plant(CrashlyticsTree())
           }
       }
       
       /**
        * Custom Timber tree that sends log messages to Firebase Crashlytics
        */
       private class CrashlyticsTree : Timber.Tree() {
           override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
               if (priority >= Log.INFO) {
                   FirebaseCrashlytics.getInstance().log("$tag: $message")
               }
               
               if (t != null && priority >= Log.ERROR) {
                   FirebaseCrashlytics.getInstance().recordException(t)
               }
           }
       }
   }
   ```

3. **Add custom keys for better crash analysis**:
   In your ViewModel or other appropriate locations, add contextual information:

   ```kotlin
   // Add user-scoped information
   FirebaseCrashlytics.getInstance().setCustomKey("network_status", networkMonitor.isConnected().toString())
   FirebaseCrashlytics.getInstance().setCustomKey("metrics_available", (uiState.value.data != null).toString())
   ```

### Configuring Firebase Analytics

1. **Enable Analytics in Firebase Console**:
   - Navigate to the Analytics section in your Firebase project
   - Review and configure data collection settings based on your requirements

2. **Create an Analytics Manager**:
   Create a new file `src/android/app/src/main/java/com/jump/scanmonitor/service/analytics/AnalyticsManager.kt`:

   ```kotlin
   package com.jump.scanmonitor.service.analytics

   import com.google.firebase.analytics.FirebaseAnalytics
   import com.google.firebase.analytics.ktx.analytics
   import com.google.firebase.analytics.ktx.logEvent
   import com.google.firebase.ktx.Firebase

   /**
    * Central manager for tracking analytics events in the application.
    * Provides standardized methods for logging user actions and app states.
    */
   object AnalyticsManager {
       private val analytics = Firebase.analytics
       
       /**
        * Logs a screen view event
        */
       fun logScreenView(screenName: String, screenClass: String) {
           analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
               param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
               param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
           }
       }
       
       /**
        * Logs when metrics are refreshed, either manually or automatically
        */
       fun logMetricsRefresh(isManual: Boolean, isSuccessful: Boolean) {
           analytics.logEvent("scan_metrics_refresh") {
               param("is_manual", isManual)
               param("is_successful", isSuccessful)
           }
       }
       
       /**
        * Logs API call results for monitoring
        */
       fun logApiCall(endpoint: String, isSuccessful: Boolean, responseTimeMs: Long) {
           analytics.logEvent("api_call") {
               param("endpoint", endpoint)
               param("is_successful", isSuccessful)
               param("response_time_ms", responseTimeMs)
           }
       }
       
       /**
        * Logs network status changes
        */
       fun logNetworkStatusChange(isConnected: Boolean, connectionType: String) {
           analytics.logEvent("network_status_change") {
               param("is_connected", isConnected)
               param("connection_type", connectionType)
           }
       }
   }
   ```

3. **Implement event tracking in your ViewModels**:
   In `ScanMetricsViewModel.kt`, add analytics calls:

   ```kotlin
   import com.jump.scanmonitor.service.analytics.AnalyticsManager
   
   // In refreshMetrics() function
   fun refreshMetrics() {
       Timber.d("Manual refresh triggered")
       
       viewModelScope.launch {
           _uiState.update { it.copy(loading = true) }
           
           // Log the refresh attempt
           AnalyticsManager.logMetricsRefresh(isManual = true, isSuccessful = false)
           
           when (val result = repository.getMetrics(forceRefresh = true)) {
               is Result.Success -> {
                   // Log successful refresh
                   AnalyticsManager.logMetricsRefresh(isManual = true, isSuccessful = true)
                   // Rest of the function...
               }
               is Result.Error -> {
                   // Rest of the function...
               }
           }
       }
   }
   ```

### Configuring Firebase Performance Monitoring

1. **Add Performance Monitoring dependency**:
   Add the following to your app-level `build.gradle.kts`:

   ```kotlin
   plugins {
       // Existing plugins...
       id("com.google.firebase.firebase-perf")
   }
   
   dependencies {
       // Existing dependencies...
       implementation("com.google.firebase:firebase-perf-ktx")
   }
   ```

2. **Monitor network requests automatically**:
   Firebase Performance Monitoring automatically tracks network requests, but you can add a custom interceptor to OkHttp client in `DatadogApiServiceImpl.kt`:

   ```kotlin
   import com.google.firebase.perf.metrics.AddTrace
   import com.google.firebase.perf.metrics.HttpMetric
   import com.google.firebase.perf.ktx.performance
   import com.google.firebase.ktx.Firebase
   
   private fun createOkHttpClient(): OkHttpClient {
       return OkHttpClient.Builder()
           .addInterceptor { chain ->
               val original = chain.request()
               val requestBuilder = original.newBuilder()
                   .header("DD-API-KEY", apiKey)
                   .header("DD-APPLICATION-KEY", applicationKey)
                   .method(original.method, original.body)
               
               chain.proceed(requestBuilder.build())
           }
           // Add Firebase Performance Interceptor
           .addInterceptor(FirebasePerformanceInterceptor())
           .connectTimeout(15, TimeUnit.SECONDS)
           .readTimeout(15, TimeUnit.SECONDS)
           .build()
   }
   ```

3. **Add custom traces for important operations**:
   In your `ScanMetricsRepository`, add performance tracking:

   ```kotlin
   import com.google.firebase.perf.ktx.trace
   import com.google.firebase.perf.metrics.Trace
   
   suspend fun getMetrics(forceRefresh: Boolean = false): Result<ScanMetrics> {
       return Firebase.performance.trace("fetch_metrics") {
           putAttribute("force_refresh", forceRefresh.toString())
           
           // Check cache first if not forcing refresh
           if (!forceRefresh) {
               // Existing cache check logic...
           }
           
           // Fetch from API
           try {
               val startTime = System.currentTimeMillis()
               
               // Existing API call logic...
               
               val endTime = System.currentTimeMillis()
               putAttribute("success", "true")
               putAttribute("response_time_ms", (endTime - startTime).toString())
               
               Result.Success(metrics)
           } catch (e: Exception) {
               putAttribute("success", "false")
               putAttribute("error_type", e.javaClass.simpleName)
               
               // Existing error handling logic...
           }
       }
   }
   ```

## Manual Setup Instructions

If you need to set up the project from scratch or programmatically, here's how to use Firebase CLI:

1. **Install Firebase CLI**:
   ```bash
   npm install -g firebase-tools
   ```

2. **Login to Firebase**:
   ```bash
   firebase login
   ```

3. **Initialize Firebase in your project**:
   ```bash
   cd /path/to/scanmonitor-project
   firebase init
   ```
   Select Crashlytics and Analytics from the available features.

4. **Add Firebase to Android app using CLI**:
   ```bash
   firebase apps:create android com.jump.scanmonitor "ScanMonitorApps"
   firebase apps:sdkconfig android -o src/android/app/google-services.json
   ```

5. **Enable Firebase services via CLI**:
   ```bash
   firebase --project=your-project-id crashlytics:enable
   firebase --project=your-project-id analytics:enable
   firebase --project=your-project-id perf:enable
   ```

## CI/CD Integration

To integrate Firebase with your CI/CD pipeline:

1. **Add Firebase credentials to CI/CD secrets**:
   - In your GitHub repository, go to Settings > Secrets and Variables > Actions
   - Add the following secrets:
     - `FIREBASE_TOKEN`: Generate using `firebase login:ci` on your local machine
     - `GOOGLE_SERVICES_JSON`: Base64-encoded content of your google-services.json file

2. **Update GitHub Actions workflow**:
   Add the following to `.github/workflows/android_build.yml`:

   ```yaml
   jobs:
     build:
       # Existing configuration...
       steps:
         # Existing steps...
         
         - name: Setup Firebase Configuration
           run: |
             echo ${{ secrets.GOOGLE_SERVICES_JSON }} | base64 -d > src/android/app/google-services.json
         
         - name: Build Debug APK
           run: ./gradlew assembleDebug
           env:
             DATADOG_API_KEY: ${{ secrets.DATADOG_API_KEY }}
             DATADOG_APP_KEY: ${{ secrets.DATADOG_APP_KEY }}
             FIREBASE_TOKEN: ${{ secrets.FIREBASE_TOKEN }}
   ```

3. **Upload debug builds to Firebase App Distribution (optional)**:
   Add to your workflow:

   ```yaml
   - name: Upload to Firebase App Distribution
     run: |
       ./gradlew appDistributionUploadDebug \
         -PfirebaseAppId=${{ secrets.FIREBASE_APP_ID }} \
         -PfirebaseToken=${{ secrets.FIREBASE_TOKEN }}
   ```

## Verification and Troubleshooting

### Verifying the Installation

1. **Check Firebase initialization**:
   - Run the app in debug mode
   - Look for Firebase initialization logs in Logcat
   - Ensure there are no Firebase-related errors

2. **Verify Crashlytics integration**:
   - In a development build, add a test button that generates a crash
   - Run the app, trigger the crash, then relaunch the app
   - Check Firebase Console > Crashlytics (allow 5-10 minutes for processing)
   - You should see the crash report with stack trace and device information

3. **Verify Analytics**:
   - Perform tracked actions in the app (refresh data, view metrics)
   - In Firebase Console > Analytics > DebugView, enable debug mode
   - Set your test device as a debug device:
     ```bash
     adb shell setprop debug.firebase.analytics.app com.jump.scanmonitor
     ```
   - You should see events appear in the DebugView

4. **Verify Performance Monitoring**:
   - Perform monitored operations (API calls, custom traces)
   - Check Firebase Console > Performance > Dashboard
   - Note that data processing may take several hours

### Troubleshooting Common Issues

1. **Missing google-services.json**:
   - Error: `File google-services.json is missing`
   - Solution: Ensure the file is in the correct location: `src/android/app/google-services.json`

2. **Firebase initialization failed**:
   - Error: `FirebaseApp initialization unsuccessful`
   - Solution: Check that package name in app matches Firebase registration exactly

3. **No data in Crashlytics**:
   - Issue: Crashes don't appear in dashboard
   - Solutions:
     - Verify crash is actually occurring (check logcat)
     - Ensure device has network connection when restarting after crash
     - Wait at least 30 minutes for processing
     - Make sure you're looking at the correct Firebase project

4. **No Analytics data**:
   - Issue: Events not appearing in dashboard
   - Solutions:
     - Use DebugView for immediate verification
     - Check for ad blockers or firewalls
     - Verify proper implementation of analytics calls
     - Regular Analytics data has a 24-hour processing delay

5. **Performance data missing**:
   - Issue: No performance metrics in console
   - Solutions:
     - Wait up to 12 hours for data processing
     - Verify network requests are actually occurring
     - Check implementation of custom traces
     - Make sure the plugin is properly applied in Gradle