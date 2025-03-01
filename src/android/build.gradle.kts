// Top-level build file for the ScanMonitorApps Android project
// Defines project-wide configurations, repositories, plugins, and dependency versions

// Configure buildscript repositories and dependencies needed for this build
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
        classpath("org.jetbrains.kotlin:kotlin-serialization:2.0.0")
        classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.0")
    }
}

// Plugin management for all modules
plugins {
    id("com.android.application") version "8.0.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.0" apply false
}

// Project-wide version constants for dependencies
val kotlinVersion by extra("2.0.0")
val androidGradlePluginVersion by extra("8.0.0")
val composeVersion by extra("1.4.3")
val materialVersion by extra("1.9.0")
val coroutinesVersion by extra("1.7.0")
val retrofitVersion by extra("2.9.0")
val kotlinSerializationVersion by extra("1.8.0")
val koinVersion by extra("3.4.0")
val coilVersion by extra("2.4.0")
val timberVersion by extra("5.0.0")
val detektVersion by extra("1.23.0")
val minSdkVersion by extra(24)
val targetSdkVersion by extra(33)
val compileSdkVersion by extra(33)

// Configuration applied to all projects in the build
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

// Configuration applied to all subprojects (app, libraries, etc.)
subprojects {
    apply {
        plugin("io.gitlab.arturbosch.detekt")
    }

    afterEvaluate {
        // Configure Kotlin tasks if the kotlin plugin is applied
        if (plugins.hasPlugin("kotlin-android")) {
            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
                kotlinOptions {
                    jvmTarget = "17"
                    freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
                }
            }
        }

        // Configure Java compilation if Java is used
        tasks.withType<JavaCompile> {
            options.compilerArgs.add("-parameters")
            sourceCompatibility = JavaVersion.VERSION_17.toString()
            targetCompatibility = JavaVersion.VERSION_17.toString()
        }
    }

    // Detekt configuration for static code analysis
    detekt {
        toolVersion = detektVersion
        config = files("${project.rootDir}/detekt-config.yml")
        buildUponDefaultConfig = true
        autoCorrect = true
    }
}

// Clean task to remove build directories
tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
    subprojects.forEach {
        delete(it.buildDir)
    }
}