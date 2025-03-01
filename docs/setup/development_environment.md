# ScanMonitorApps Development Environment Setup

## Overview

This guide provides comprehensive instructions for setting up the development environment for the ScanMonitorApps Android application. The ScanMonitorApps is a mobile application designed to help Jump staff monitor ticket scanner activities during sports games, providing real-time visibility into scanning operations.

The development environment requires specific tools and configurations to ensure consistent development and testing experiences across the development team. This document will guide you through:

- Installing required software components
- Configuring your development environment
- Setting up API keys for external services
- Troubleshooting common setup issues

## Prerequisites

Before starting the development environment setup, ensure your system meets the following requirements:

### Hardware Requirements

- **CPU**: Minimum 2 cores, recommended 4+ cores
- **RAM**: Minimum 8GB, recommended 16GB
- **Storage**: At least 10GB free space for Android SDK, IDE, and project files
- **Display**: 1280x800 minimum resolution

### Software Requirements

| Component | Version | Purpose |
|-----------|---------|---------|
| Windows/macOS/Linux | Recent OS version | Base operating system |
| Internet Connection | Broadband | For downloading dependencies and API calls |
| Git | Latest | Version control |
| Web Browser | Chrome/Firefox/Safari | For API documentation and Firebase Console |

## Manual Setup

### Installing JDK 17

Java Development Kit (JDK) 17 is required for Android development with the latest tooling.

#### For Windows:
1. Download JDK 17 from [Adoptium](https://adoptium.net/temurin/releases/?version=17)
2. Run the installer and follow the installation wizard
3. Set the `JAVA_HOME` environment variable:
   - Right-click on 'This PC' or 'My Computer' and select 'Properties'
   - Click on 'Advanced system settings'
   - Click on 'Environment Variables'
   - Under 'System variables', click 'New'
   - Set Variable name as `JAVA_HOME` and Variable value as the JDK installation path (e.g., `C:\Program Files\Eclipse Adoptium\jdk-17.0.7.7-hotspot`)
   - Edit the `Path` variable and add `%JAVA_HOME%\bin`
4. Verify installation by opening Command Prompt and running:
   ```
   java -version
   ```

#### For macOS:
1. Install using Homebrew:
   ```
   brew tap homebrew/cask-versions
   brew install --cask temurin17
   ```
2. Set the `JAVA_HOME` environment variable in your shell profile file (~/.bash_profile, ~/.zshrc, etc.):
   ```
   export JAVA_HOME=$(/usr/libexec/java_home -v 17)
   export PATH=$JAVA_HOME/bin:$PATH
   ```
3. Reload your shell profile:
   ```
   source ~/.bash_profile   # or source ~/.zshrc
   ```
4. Verify installation:
   ```
   java -version
   ```

#### For Linux (Ubuntu/Debian):
1. Update package repository:
   ```
   sudo apt-get update
   ```
2. Install OpenJDK 17:
   ```
   sudo apt-get install openjdk-17-jdk
   ```
3. Set the `JAVA_HOME` environment variable:
   ```
   echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
   echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
   source ~/.bashrc
   ```
4. Verify installation:
   ```
   java -version
   ```

### Installing Android Studio

Android Studio Hedgehog (2023.1.1+) is the recommended IDE for ScanMonitorApps development.

1. Download Android Studio Hedgehog (2023.1.1+) from the [Android Developer website](https://developer.android.com/studio)
2. Run the installer and follow the installation wizard
3. During the setup wizard, choose 'Standard' installation to automatically install the Android SDK, platform tools, and other recommended components
4. After installation completes, launch Android Studio

### Configuring Android SDK

The ScanMonitorApps application requires Android SDK version 24 (Android 7.0) or higher.

1. Open Android Studio
2. Go to Tools > SDK Manager
3. In the "SDK Platforms" tab:
   - Check "Android 7.0 (Nougat)" (API level 24)
   - Check "Android 13" (API level 33)
   - Click "Apply" to install these platforms
4. In the "SDK Tools" tab:
   - Check "Android SDK Build-Tools"
   - Check "Android SDK Command-line Tools"
   - Check "Android SDK Platform-Tools"
   - Check "Android Emulator"
   - Click "Apply" to install these tools
5. Set the `ANDROID_HOME` environment variable:

   #### For Windows:
   - Follow the same steps as for setting `JAVA_HOME`
   - Set Variable name as `ANDROID_HOME` and Variable value as the Android SDK installation path (typically `C:\Users\YOUR_USERNAME\AppData\Local\Android\Sdk`)
   - Add `%ANDROID_HOME%\platform-tools` and `%ANDROID_HOME%\tools` to the `Path` variable

   #### For macOS/Linux:
   - Add the following to your shell profile:
   ```
   export ANDROID_HOME=$HOME/Library/Android/sdk   # macOS
   export ANDROID_HOME=$HOME/Android/Sdk           # Linux
   export PATH=$PATH:$ANDROID_HOME/platform-tools
   export PATH=$PATH:$ANDROID_HOME/tools
   ```

### Cloning the Repository

1. Install Git if not already installed (download from [git-scm.com](https://git-scm.com/))
2. Open Terminal (macOS/Linux) or Command Prompt/Git Bash (Windows)
3. Navigate to the directory where you want to store the project
4. Clone the repository:
   ```
   git clone https://github.com/jump/scanmonitorapps.git
   ```
5. Navigate into the cloned repository directory:
   ```
   cd scanmonitorapps
   ```

### Setting Up API Keys

#### Configuring Datadog API Keys

1. Obtain a Datadog API key and Application key from your organization's Datadog administrator
2. Create a file named `local.properties` in the `src/android` directory if it doesn't exist
3. Add the following lines to the file:
   ```
   DATADOG_API_KEY=your_api_key
   DATADOG_APP_KEY=your_application_key
   ```
4. Save the file

For more details on Datadog integration, refer to the [Datadog Integration Guide](../../api/datadog_integration.md).

#### Configuring Firebase (Optional but Recommended)

Firebase is used for monitoring and crash reporting in the ScanMonitorApps application.

1. Follow the [Firebase Setup Guide](../../../infrastructure/config/firebase_setup.md) for detailed instructions
2. The main steps include:
   - Creating a Firebase project
   - Registering your Android app with package name `com.jump.scanmonitor`
   - Downloading `google-services.json` and placing it in the `src/android/app` directory

## Automated Setup

For a more streamlined setup process, we provide an automated setup script that handles most of the configuration steps.

### Using the Setup Script

1. Ensure you have a bash shell available (Linux, macOS, or WSL on Windows)
2. Make the script executable:
   ```
   chmod +x scripts/setup/setup_development_environment.sh
   ```
3. Run the script:
   ```
   ./scripts/setup/setup_development_environment.sh
   ```
4. The script will:
   - Check for required tools (JDK, Git, etc.)
   - Help install missing dependencies
   - Clone the repository if needed
   - Configure environment variables
   - Set up API keys and Firebase configuration
   - Validate the setup

### Script Limitations

The automated script may have limitations depending on your operating system and environment. In case of issues:

- For JDK installation problems, follow the manual JDK installation steps
- For Android SDK issues, use Android Studio's SDK Manager
- For API key configuration, manually edit the `local.properties` file

## IDE Configuration

### Android Studio Configuration

After installing Android Studio and cloning the repository, you'll need to configure the IDE for optimal development of ScanMonitorApps.

1. Open the project in Android Studio:
   - Start Android Studio
   - Select "Open an Existing Project"
   - Navigate to the `src/android` directory in the cloned repository
   - Click "Open"

2. Configure code style:
   - Go to File > Settings (or Android Studio > Preferences on macOS)
   - Navigate to Editor > Code Style > Kotlin
   - Set "Hard wrap at" to 100 columns
   - Check "Use single import" instead of wildcard imports
   - Click "Apply"

3. Configure memory settings (recommended for better performance):
   - Go to Help > Edit Custom VM Options
   - Set the following parameters:
   ```
   -Xms512m
   -Xmx2048m
   ```

4. Install recommended plugins:
   - Kotlin Multiplatform Mobile
   - Firebase Services
   - Markdown
   - Git Integration
   - Detekt (for code quality checks)

5. Enable annotation processing:
   - Go to File > Settings > Build, Execution, Deployment > Compiler > Annotation Processors
   - Check "Enable annotation processing"

### Setting Up Android Virtual Device (AVD)

1. Go to Tools > AVD Manager
2. Click "+ Create Virtual Device"
3. Select a device definition (Pixel 4 recommended)
4. Select a system image (API level 33 recommended)
5. Configure the AVD with:
   - RAM: 2GB+
   - Internal Storage: 2GB+
   - SD Card: 1GB+

### Gradle Configuration

The project uses Gradle 8.2 via the Gradle wrapper (`gradlew`). You generally don't need to install Gradle separately.

To customize Gradle behavior:
1. Create or edit `gradle.properties` in the `src/android` directory
2. Add the following recommended settings:
   ```
   org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
   org.gradle.parallel=true
   org.gradle.caching=true
   android.useAndroidX=true
   android.enableJetifier=false
   kotlin.code.style=official
   ```

## API Key Configuration

ScanMonitorApps requires API keys to communicate with external services. Here's how to configure them:

### Datadog API Keys

The application requires Datadog API and Application keys to retrieve ticket scanning metrics.

1. Obtain the keys from your Datadog administrator or create them in the Datadog platform:
   - Go to [Datadog API page](https://app.datadoghq.com/organization-settings/api-keys)
   - Create or copy the API Key
   - Navigate to [Application Keys](https://app.datadoghq.com/organization-settings/application-keys) to create or copy an Application Key

2. Store the keys in `local.properties`:
   ```
   DATADOG_API_KEY=your_api_key_here
   DATADOG_APP_KEY=your_application_key_here
   ```

3. Alternatively, set environment variables:
   ```
   export DATADOG_API_KEY=your_api_key_here
   export DATADOG_APP_KEY=your_application_key_here
   ```

4. The keys are consumed by the build system and included in the `BuildConfig` class during compilation, without being committed to version control.

### Release Keystore Configuration (For Release Builds)

For building release versions of the app, you'll need a signing keystore:

1. Create a keystore if you don't have one:
   ```
   keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
   ```

2. Add keystore information to your `local.properties`:
   ```
   KEYSTORE_PATH=/path/to/your/keystore.jks
   KEYSTORE_PASSWORD=your_keystore_password
   KEY_ALIAS=your_key_alias
   KEY_PASSWORD=your_key_password
   ```

## Firebase Configuration

Firebase provides monitoring, analytics, and crash reporting for ScanMonitorApps. Setting it up requires:

1. Create a Firebase project:
   - Go to the [Firebase Console](https://console.firebase.google.com/)
   - Click "Add project"
   - Enter a project name (e.g., "ScanMonitorApps")
   - Optional: Enable Google Analytics
   - Click "Create project"

2. Add an Android app to the project:
   - In your Firebase project, click the Android icon
   - Enter package name: `com.jump.scanmonitor`
   - Enter app nickname: "ScanMonitorApps"
   - Register the app

3. Download the configuration file:
   - Firebase will generate a `google-services.json` file
   - Download this file and place it in the `src/android/app` directory

4. The project is already configured to use Firebase services in `build.gradle.kts`, but verify these entries exist:
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

For detailed Firebase setup instructions, refer to the [Firebase Setup Guide](../../../infrastructure/config/firebase_setup.md).

## Troubleshooting

### Common Issues and Solutions

#### Java-Related Issues

| Issue | Solution |
|-------|----------|
| "JAVA_HOME is not set" | Set the JAVA_HOME environment variable as described in the JDK installation section |
| "Unsupported Java version" | Ensure you're using JDK 17, run `java -version` to check |
| "Multiple Java versions found" | Set JAVA_HOME to point to JDK 17 specifically |

#### Android SDK Issues

| Issue | Solution |
|-------|----------|
| "Android SDK not found" | Set ANDROID_HOME environment variable correctly |
| "SDK Platform API level X not found" | Open Android Studio > SDK Manager and install the required platform |
| "Build tools not found" | Install Build Tools through SDK Manager |

#### Gradle Issues

| Issue | Solution |
|-------|----------|
| "Gradle sync failed" | Check internet connection, Gradle version compatibility |
| "Gradle task X failed" | Check the error message for specifics, may be related to missing dependencies |
| "Unsupported Gradle version" | The project uses Gradle wrapper, use `./gradlew` instead of a globally installed Gradle |

#### API Key Issues

| Issue | Solution |
|-------|----------|
| "Datadog API authentication error" | Verify API keys are correctly set in local.properties |
| "Missing google-services.json" | Download from Firebase Console and place in src/android/app directory |
| "Firebase initialization error" | Verify the package name in the Firebase console matches `com.jump.scanmonitor` |

#### Build Issues

| Issue | Solution |
|-------|----------|
| "Could not find some.dependency:version" | Check internet connection, update Gradle, try running with `--refresh-dependencies` |
| "Compilation error in X.kt" | Check the code for syntax errors or incompatible API usage |
| "Resource not found" | Ensure all resources (layouts, drawables, etc.) are correctly defined |

### Getting More Help

If you encounter issues not covered here:

1. Check the full error message in Android Studio's logcat or Gradle console
2. Search the project documentation for specific error keywords
3. Consult the [build instructions](../build_instructions.md) for build-related issues
4. Contact the development team through the project communication channels

## Next Steps

After setting up your development environment, you can:

1. **Build and run the application**:
   - Connect a physical Android device (API level 24+) or start an emulator
   - Click the Run button (green triangle) in Android Studio or run:
     ```
     cd src/android
     ./gradlew installDebug
     ```

2. **Explore the codebase**:
   - Main application entry point: `ScanMonitorApplication.kt`
   - UI implementation: Locate Composable functions in the UI package
   - Data retrieval: Examine the repository and API service implementations

3. **Make your first change**:
   - Make a small modification to the UI
   - Run the app to see your change
   - Create a branch and commit your change

4. **Run tests**:
   - Execute unit tests: `./gradlew test`
   - Execute UI tests: `./gradlew connectedCheck`

5. **Read additional documentation**:
   - [Build Instructions](../build_instructions.md)
   - [Datadog Integration Guide](../../api/datadog_integration.md)
   - Technical Specifications in the project documentation

By following the steps in this guide, you should have a fully functional development environment for ScanMonitorApps. If you have any questions or suggestions for improving this guide, please contact the development team.