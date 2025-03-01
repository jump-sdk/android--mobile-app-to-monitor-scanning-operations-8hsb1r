plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
    id("io.gitlab.arturbosch.detekt")
}

android {
    namespace = "com.jump.scanmonitor"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.jump.scanmonitor"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // These would typically be stored securely and accessed via environment variables
            // or a secure properties file in a production environment
            storeFile = file("keystore/release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: ""
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = false
        dataBinding = false
        aidl = false
        renderScript = false
        shaders = false
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }

    packaging {
        resources {
            excludes += listOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md"
            )
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    // Add BuildConfig fields for Datadog API keys
    buildConfigField("String", "DATADOG_API_KEY", "\"${System.getenv("DATADOG_API_KEY") ?: "development_api_key"}\"")
    buildConfigField("String", "DATADOG_APP_KEY", "\"${System.getenv("DATADOG_APP_KEY") ?: "development_app_key"}\"")
}

dependencies {
    // Core Android dependencies - androidx.core:core-ktx version 1.10.1
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    // Material Design - com.google.android.material:material version 1.9.0
    implementation("com.google.android.material:material:1.9.0")
    
    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    
    // Jetpack Compose - androidx.compose.ui:ui version 1.4.3
    implementation("androidx.compose.ui:ui:1.4.3")
    implementation("androidx.compose.material:material:1.4.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.3")
    implementation("androidx.activity:activity-compose:1.7.2")
    
    // Kotlin Coroutines - org.jetbrains.kotlinx:kotlinx-coroutines-core version 1.7.0
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.0")
    
    // Retrofit and OkHttp for API communication - com.squareup.retrofit2:retrofit version 2.9.0
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    
    // Koin for dependency injection - io.insert-koin:koin-android version 3.4.0
    implementation("io.insert-koin:koin-android:3.4.0")
    implementation("io.insert-koin:koin-androidx-compose:3.4.0")
    
    // Coil for image loading - io.coil-kt:coil version 2.4.0
    implementation("io.coil-kt:coil:2.4.0")
    implementation("io.coil-kt:coil-compose:2.4.0")
    
    // Timber for logging - com.jakewharton.timber:timber version 5.0.0
    implementation("com.jakewharton.timber:timber:5.0.0")
    
    // Firebase - com.google.firebase:firebase-bom version 32.1.1
    implementation(platform("com.google.firebase:firebase-bom:32.1.1"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.0")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("io.insert-koin:koin-test:3.4.0")
    testImplementation("io.insert-koin:koin-test-junit4:3.4.0")
    
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.4.3")
    androidTestImplementation("io.insert-koin:koin-test:3.4.0")
    androidTestImplementation("io.insert-koin:koin-test-junit4:3.4.0")
    
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.3")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.4.3")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xopt-in=androidx.compose.material.ExperimentalMaterialApi"
        )
    }
}

detekt {
    config = files("$projectDir/detekt-config.yml")
    buildUponDefaultConfig = true
    autoCorrect = false
    
    reports {
        html.enabled = true
        xml.enabled = false
        txt.enabled = false
        sarif.enabled = true
    }
    
    // Detection thresholds
    basePath = projectDir.absolutePath
    
    // Define build failure conditions
    failFast = false
}