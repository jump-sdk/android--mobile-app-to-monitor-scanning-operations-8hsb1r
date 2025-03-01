# ScanMonitorApps Build Instructions

## Overview

This document provides comprehensive instructions for building the ScanMonitorApps Android application, which helps Jump staff monitor ticket scanner activities during sports games. The build process supports both development and release builds, with instructions for proper API configuration, signing, and distribution.

ScanMonitorApps uses Gradle as its build system, with Kotlin as the primary programming language. The application integrates with the Datadog API to retrieve scanning metrics and displays them in a simple, intuitive UI.

## Prerequisites

Before building ScanMonitorApps, you need to set up your development environment with the following tools:

- **JDK 17 or higher**
  - Required for Android development with the latest tooling
  - Download from [Adoptium](https://adoptium.net/temurin/releases/?version=17)

- **Android Studio Hedgehog (2023.1.1+)**
  - Recommended IDE for Android development
  - Download from [Android Developer website](https://developer.android.com/studio)

- **Android SDK**
  - Minimum API level 24 (Android 7.0)
  - Target API level 33 (Android 13)
  - Build Tools version 33.0.2 or higher

- **Gradle 8.2 or higher**
  - Included with the project via Gradle Wrapper
  - No separate installation required

For automated environment setup, you can use the provided script:

```bash
./scripts/setup/setup_development_environment.sh
```

This script will guide you through installing and configuring all required tools and dependencies.

## Setting Up API Keys

ScanMonitorApps requires Datadog API keys for retrieving scan metrics. Follow these steps to configure the keys:

1. Obtain a Datadog API key and Application key from your Datadog account administrator
2. Create a `local.properties` file in the root of the Android project (if it doesn't exist)
3. Add the following lines to `local.properties`:

```
DATADOG_API_KEY=your_api_key
DATADOG_APP_KEY=your_application_key
```

> **Security Note**: Never commit `local.properties` to version control. This file is already included in `.gitignore`.

Refer to the [Datadog Integration Guide](../../api/datadog_integration.md) for detailed information about Datadog API configuration.

## Development Builds

Development builds are used for testing and debugging during the development process. They are not signed with a release key and have debugging enabled.

### Building a Development APK

1. Ensure you have completed the prerequisites and API key setup
2. Open a terminal and navigate to the project's Android directory:

```bash
cd src/android
```

3. Run the Gradle task to build a debug APK:

```bash
./gradlew assembleDebug
```

4. The debug APK will be generated at:

```
app/build/outputs/apk/debug/app-debug.apk
```

### Running the Development Build

You can install the debug APK directly to a connected device or emulator:

```bash
./gradlew installDebug
```

Alternatively, you can run the application directly from Android Studio by clicking the "Run" button or pressing Shift+F10.

## Release Builds

Release builds are optimized, signed versions of the application intended for distribution to Jump staff.

### Building a Release APK

1. Ensure you have completed the prerequisites, API key setup, and signing configuration
2. Open a terminal and navigate to the project's Android directory:

```bash
cd src/android
```

3. Run the Gradle task to build a release APK:

```bash
./gradlew assembleRelease
```

4. The unsigned release APK will be generated at:

```
app/build/outputs/apk/release/app-release-unsigned.apk
```

5. If you've configured signing in `local.properties` or Gradle properties, a signed APK will be generated at:

```
app/build/outputs/apk/release/app-release.apk
```

### Building an Android App Bundle (AAB)

For Google Play distribution, it's recommended to use Android App Bundle format:

1. Navigate to the project's Android directory
2. Run the Gradle task to build a release bundle:

```bash
./gradlew bundleRelease
```

3. The signed AAB will be generated at:

```
app/build/outputs/bundle/release/app-release.aab
```

### Using the Automated Build Script

For a streamlined build process, you can use the provided release build script:

```bash
./scripts/build/build_release.sh -v 1.0.0 -c 1
```

Options:
- `-v VERSION_NAME`: Semantic version (e.g., 1.0.0)
- `-c VERSION_CODE`: Numeric version code (e.g., 1)
- `-k KEYSTORE_PATH`: Path to keystore (optional, defaults to keystore.jks)
- `-b`: Build AAB instead of APK
- `-s`: Skip tests
- `-o OUTPUT_DIR`: Custom output directory

The script will handle building, signing, and versioning automatically.

## Signing Configuration

All release builds must be signed with a valid keystore. Follow these steps to configure signing:

### Creating a Keystore

If you don't have a keystore, create one using the following command:

```bash
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```

Follow the prompts to set passwords and key details.

### Configuring Signing in local.properties

Add the following lines to your `local.properties` file:

```
KEYSTORE_PATH=/path/to/your/keystore.jks
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=your_key_alias
KEY_PASSWORD=your_key_password
```

Alternatively, you can set these as environment variables before building.

### Verifying Signed APKs

To verify that an APK has been signed correctly:

```bash
$ANDROID_HOME/build-tools/33.0.2/apksigner verify --verbose app/build/outputs/apk/release/app-release.apk
```

## Continuous Integration

ScanMonitorApps uses GitHub Actions for continuous integration. The CI pipeline automatically builds and tests the application on every pull request and merge to the main branch.

### CI Workflow

1. Code is pushed to a branch or a pull request is created
2. GitHub Actions triggers the CI workflow
3. Static analysis tools (Detekt, Android Lint) check code quality
4. Unit tests are executed
5. Debug APK is built
6. Test reports are generated

### Manually Triggering CI Builds

You can manually trigger a CI build by:

1. Going to the GitHub repository
2. Navigating to "Actions" tab
3. Selecting the "Build" workflow
4. Clicking "Run workflow"

## Distribution

ScanMonitorApps can be distributed to Jump staff using two primary methods:

### Google Play Internal Testing

1. Build a signed AAB using the instructions above
2. Access the Google Play Console
3. Navigate to the ScanMonitorApps application
4. Select "Internal testing" track
5. Upload the AAB file
6. Add testers' email addresses to the testing group
7. Publish the release

Testers will receive an email with instructions to join the testing program and download the app.

### Direct APK Distribution

For quick testing or environments without Google Play:

1. Build a signed APK using the instructions above
2. Distribute the APK file via email, download link, or mobile device management system
3. Users need to enable "Install from Unknown Sources" in their device settings
4. Users can install the APK by opening it on their device

## Troubleshooting

### Common Build Issues

#### Gradle Sync Failed

**Issue**: Android Studio fails to sync with Gradle
**Solution**:
- Ensure you have the correct JDK version (17+)
- Check internet connection for dependency downloads
- Try "File > Invalidate Caches / Restart" in Android Studio

#### Signing Failed

**Issue**: Release build fails with signing errors
**Solution**:
- Verify keystore path is correct in local.properties
- Ensure keystore passwords are correct
- Check that the key alias exists in the keystore

#### API Key Issues

**Issue**: App builds but crashes with API authentication errors
**Solution**:
- Verify Datadog API keys are correctly set in local.properties
- Ensure the API keys have appropriate permissions in Datadog
- Check network connectivity

#### Missing google-services.json

**Issue**: Build fails with error about missing google-services.json
**Solution**:
- This file is required for Firebase integration
- Follow Firebase setup instructions to generate and place this file in app/ directory
- If you don't need Firebase, modify the build.gradle to make it optional

### Getting Help

If you encounter issues not covered here:

1. Check the build logs for specific error messages
2. Review the [Datadog Integration Guide](../../api/datadog_integration.md)
3. Contact the development team for assistance