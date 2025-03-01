# ScanMonitorApps

## Project Overview

ScanMonitorApps is a lightweight Android mobile application designed to help Jump staff monitor ticket scanner activities during sports games. The application addresses the critical need for real-time visibility into scanning operations, enabling staff to ensure smooth entry processes and quickly identify potential issues.

The application follows a client-only architecture that directly integrates with Datadog APIs to retrieve and display ticket scanning metrics. This approach eliminates the need for backend services while providing immediate operational awareness to staff members.

### Key Features

- **Scanning Metrics Dashboard**: Simple, intuitive display of total scan counts over the last 2 hours
- **Datadog API Integration**: Direct integration with Datadog monitoring infrastructure
- **Offline Mode Handling**: Graceful handling of connectivity issues with appropriate user feedback
- **Auto-Refresh Functionality**: Automatic refreshing of scanning metrics at regular intervals

## Prerequisites

To develop and build ScanMonitorApps, you'll need:

- Android Studio Hedgehog (2023.1.1+)
- JDK 17
- Android SDK (API level 24+)
- Datadog API credentials (API key and Application key)
- Kotlin 2.0.0+
- Git

## Getting Started

### Setting Up the Development Environment

1. Install Android Studio Hedgehog (2023.1.1+) from [developer.android.com](https://developer.android.com/studio)
2. Ensure JDK 17 is installed and configured in Android Studio
3. Clone the repository:
   ```
   git clone https://github.com/jump/scan-monitor-apps.git
   cd scan-monitor-apps
   ```

### Datadog API Configuration

1. Create or obtain Datadog API credentials:
   - Log in to your Datadog account
   - Navigate to Organization Settings → API Keys
   - Generate a new API key or use an existing one
   - Navigate to Organization Settings → Application Keys
   - Generate a new Application key or use an existing one

2. Note both the API key and Application key for the next step

### Build Configuration

To securely store your Datadog API credentials without committing them to source control:

1. Create or edit the `local.properties` file in the project root (this file is automatically ignored by Git)
2. Add the following lines:
   ```
   datadog.apiKey=your_api_key_here
   datadog.appKey=your_application_key_here
   ```
3. These values will be automatically included in the build process via the BuildConfig class

## Project Structure

ScanMonitorApps follows the MVVM (Model-View-ViewModel) architecture pattern for clear separation of concerns and better testability. The codebase is organized into the following packages:

### Key Packages

- **model**: Data models and domain entities
- **viewmodel**: ViewModels that manage UI state and business logic
- **repository**: Data access layer coordinating between API and cache
- **service**:
  - **api**: Datadog API integration using Retrofit
  - **cache**: Local data caching mechanisms
  - **network**: Network connectivity monitoring
- **ui**: User interface components
  - **components**: Reusable UI components
  - **screens**: Application screens
  - **theme**: Material Design theme implementation
- **di**: Dependency injection configuration using Koin
- **util**: Utility classes and extension functions

### Architecture Components

```
┌─────────────────────┐
│                     │
│     UI Layer        │ ◄─── User interaction
│                     │
└─────────┬───────────┘
          │
          │ observes
          ▼
┌─────────────────────┐
│                     │
│  ViewModel Layer    │ ◄─── State management
│                     │
└─────────┬───────────┘
          │
          │ requests data
          ▼
┌─────────────────────┐
│                     │
│  Repository Layer   │ ◄─── Data coordination
│                     │
└─────────┬───────────┘
          │
          ├────────────┐
          │            │
          ▼            ▼
┌─────────────────┐  ┌─────────────────┐
│                 │  │                 │
│  API Service    │  │  Cache Service  │ ◄─── Data sources
│                 │  │                 │
└─────────────────┘  └─────────────────┘
```

## Development Workflow

### Coding Standards

This project follows the official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) with the following additional guidelines:

- Use meaningful variable and function names
- Document public APIs with KDoc comments
- Separate business logic from UI components
- Use extension functions for reusable code
- Follow Material Design guidelines for UI components

For code style enforcement, the project uses Detekt for static analysis. Run checks with:
```
./gradlew detekt
```

### Version Control

- **Branch naming**: Use descriptive names with prefixes:
  - `feature/` for new features
  - `bugfix/` for bug fixes
  - `refactor/` for code refactoring
  - `chore/` for maintenance tasks

- **Commit messages**: Follow conventional commits format
  ```
  type(scope): description
  
  [optional body]
  ```

- **Pull Requests**: Include:
  - Clear description of changes
  - Reference to related issues
  - Testing performed
  - Screenshots for UI changes

## Building the Application

### Debug Build

To build and run a debug version of the application:

1. Open the project in Android Studio
2. Select the 'app' module
3. Click 'Run' (or press Shift+F10)

Alternatively, use Gradle:
```
./gradlew assembleDebug
```

The debug APK will be located at `app/build/outputs/apk/debug/app-debug.apk`

### Release Build

To create a signed release build:

1. Create a signing key if you don't have one:
   ```
   keytool -genkey -v -keystore scan-monitor.keystore -alias scan-monitor -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Configure signing in `local.properties`:
   ```
   signing.keystore=path/to/scan-monitor.keystore
   signing.key.alias=scan-monitor
   signing.key.password=your_key_password
   signing.store.password=your_store_password
   ```

3. Build the release version:
   ```
   ./gradlew assembleRelease
   ```

The release APK will be located at `app/build/outputs/apk/release/app-release.apk`

## Testing

ScanMonitorApps employs a comprehensive testing strategy covering unit, integration, and UI tests.

### Running Tests

- **Unit Tests**:
  ```
  ./gradlew test
  ```

- **Instrumentation Tests**:
  ```
  ./gradlew connectedAndroidTest
  ```

- **All Tests**:
  ```
  ./gradlew check
  ```

### Test Structure

- **Unit Tests**: Located in `src/test/java` - test individual components in isolation
  - ViewModelTests
  - RepositoryTests
  - ServiceTests
  - UtilityTests

- **Integration Tests**: Located in `src/androidTest/java` - test component interactions
  - RepositoryIntegrationTests
  - APIIntegrationTests

- **UI Tests**: Located in `src/androidTest/java` - test UI components and user flows
  - MetricsDashboardTest
  - OfflineModeTest

Tests use the following frameworks:
- JUnit 4 for test structure
- Mockito for mocking dependencies
- Turbine for testing Flows
- Espresso for UI testing

## Dependencies

ScanMonitorApps uses the following major dependencies:

- **Android Jetpack** (Latest): Google-recommended components for modern Android development
- **Material Components** (1.9.0+): Material Design UI elements
- **Kotlin Coroutines** (1.7.0+): Asynchronous programming
- **Retrofit** (2.9.0+): Type-safe HTTP client for API communication
- **KotlinX Serialization** (1.8.0): JSON serialization/deserialization
- **Koin** (3.4.0+): Lightweight dependency injection
- **Coil** (2.4.0+): Image loading library
- **Timber** (5.0.0+): Logging utility

### Updating Dependencies

Dependencies should be reviewed and updated regularly to ensure security and compatibility:

1. Check for updates using Android Studio's built-in dependency checker
2. Test thoroughly after updating dependencies
3. Update dependency versions in the app-level `build.gradle.kts` file

## Deployment

ScanMonitorApps is distributed to Jump staff through Google Play Internal Testing.

### CI/CD

The project uses GitHub Actions for continuous integration and delivery:

- **CI Pipeline**: Automatically triggered on pull requests
  - Runs static analysis
  - Executes unit tests
  - Builds debug APK

- **CD Pipeline**: Triggered on merges to main branch
  - Runs all tests
  - Builds release APK
  - Signs the APK
  - Uploads to Google Play Internal Testing

The CI/CD configuration can be found in `.github/workflows/`.

## Troubleshooting

### Common Issues

- **Build fails with "Datadog API key not found"**:
  - Ensure `local.properties` contains the correct Datadog API credentials
  - Check that the property names match those expected in `build.gradle.kts`

- **Application shows "Unable to load scan data"**:
  - Verify Datadog API credentials are valid
  - Check device has network connectivity
  - Ensure the Datadog query is correctly configured

- **Tests fail with "No such method" errors**:
  - Check that mockito-inline is included in test dependencies
  - Ensure you're not trying to mock final classes without proper configuration

### Debugging Tips

- Use Timber for logging during development
- Enable "Show layout bounds" in Developer Options to debug UI issues
- Use Network Profiler to inspect API requests and responses
- Check Logcat for exceptions and error messages

## Additional Resources

### Documentation

Additional documentation is available in the `/docs` directory:

- Architecture Decision Records (ADRs)
- API documentation
- UI/UX design specifications
- Testing strategy

### Datadog Resources

- [Datadog API Documentation](https://docs.datadoghq.com/api/latest/)
- [Metrics Query Guide](https://docs.datadoghq.com/metrics/query_language/)
- [Authentication Guide](https://docs.datadoghq.com/account_management/api-app-keys/)