rootProject.name = "scanmonitor"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            // Android core dependencies
            version("kotlin", "2.0.0")
            version("androidGradlePlugin", "8.0.0")
            version("androidxCore", "1.9.0")
            version("androidxLifecycle", "2.6.1")
            version("androidxActivity", "1.7.2")
            version("androidxCompose", "1.4.3")
            version("material", "1.9.0")
            
            // Network and serialization
            version("retrofit", "2.9.0")
            version("kotlinxSerialization", "1.8.0")
            version("okhttp", "4.11.0")
            version("coil", "2.4.0")
            
            // DI
            version("koin", "3.4.0")
            
            // Utilities
            version("timber", "5.0.1")
            
            // Testing
            version("junit", "4.13.2")
            version("androidxJunit", "1.1.5")
            version("espresso", "3.5.1")
            
            // Firebase
            version("firebase", "32.2.0")
            
            // Define library bundles
            bundle("androidx", listOf(
                "androidx.core:core-ktx:${findVersion("androidxCore")}",
                "androidx.lifecycle:lifecycle-runtime-ktx:${findVersion("androidxLifecycle")}",
                "androidx.activity:activity-compose:${findVersion("androidxActivity")}"
            ))
            
            bundle("compose", listOf(
                "androidx.compose.ui:ui:${findVersion("androidxCompose")}",
                "androidx.compose.ui:ui-graphics:${findVersion("androidxCompose")}",
                "androidx.compose.ui:ui-tooling-preview:${findVersion("androidxCompose")}",
                "androidx.compose.material3:material3:${findVersion("androidxCompose")}"
            ))
            
            bundle("network", listOf(
                "com.squareup.retrofit2:retrofit:${findVersion("retrofit")}",
                "com.squareup.retrofit2:converter-kotlinx-serialization:${findVersion("retrofit")}",
                "org.jetbrains.kotlinx:kotlinx-serialization-json:${findVersion("kotlinxSerialization")}",
                "com.squareup.okhttp3:okhttp:${findVersion("okhttp")}",
                "com.squareup.okhttp3:logging-interceptor:${findVersion("okhttp")}"
            ))
        }
    }
}

include(":app")