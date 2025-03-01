# ScanMonitorApps

## Overview

ScanMonitorApps is a mobile application designed to help Jump staff monitor ticket scanner activities during sports games. It provides real-time visibility into scanning operations, enabling staff to ensure smooth entry processes and quickly identify potential issues without leaving their posts or accessing desktop systems.

## Features

- Display of total scan counts over the last 2 hours
- Auto-refresh of metrics every 5 minutes
- Manual refresh via pull-to-refresh gesture
- Offline mode with cached data when network is unavailable
- Clear status indicators for data freshness and connectivity
- Simple, high-contrast UI designed for stadium environments

## Architecture

ScanMonitorApps uses a client-only architecture with a modern MVVM (Model-View-ViewModel) pattern. It directly integrates with the Datadog API to retrieve scanning metrics without requiring a backend server.

For detailed architecture documentation, see [High-Level Architecture](docs/architecture/high_level_architecture.md).

## Technologies

- **Programming Language**: Kotlin 2.0.0
- **UI Framework**: Jetpack Compose for modern, declarative UI
- **Architecture Components**: ViewModel, StateFlow, Coroutines
- **Networking**: Retrofit for API communication
- **Dependency Injection**: Koin 3.4.0+
- **Logging**: Timber 5.0.0+
- **API Integration**: Datadog API for metrics retrieval
- **Minimum Android Version**: API 24 (Android 7.0)

## Getting Started

### Prerequisites
- JDK 17
- Android Studio Hedgehog (2023.1.1+)
- Android SDK with API level 24+ installed
- Datadog API and Application keys

### Quick Start
1. Clone the repository
2. Configure Datadog API keys (see [Datadog Integration](docs/api/datadog_integration.md))
3. Open the project in Android Studio
4. Build and run the application

For detailed setup instructions, see [Development Environment Setup](docs/setup/development_environment.md).

## Development Guide

### Project Structure
- `src/android/app/src/main/java/com/jump/scanmonitor/`
  - `model/` - Data classes and domain models
  - `viewmodel/` - ViewModels managing UI state
  - `repository/` - Data repositories and mappers
  - `service/` - API, cache, and network services
  - `ui/` - Composable UI components and screens
  - `di/` - Dependency injection modules
  - `util/` - Utility classes

### Key Components
- `MainActivity.kt` - Entry point for the application
- `ScanMonitorApplication.kt` - Application class for initialization
- `ScanMetricsViewModel.kt` - Manages scanning metrics data and UI state
- `MetricsDashboardScreen.kt` - Main UI screen displaying metrics

### Development Workflow
1. Make changes to the codebase
2. Run tests to ensure functionality
3. Build and test on device or emulator
4. Submit pull request with detailed description

## Build and Deploy

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### Distribution
The application is distributed to Jump staff through Google Play Internal Testing track or direct APK installation.

For detailed build and distribution instructions, see [Build Instructions](docs/setup/build_instructions.md).

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

Please ensure your code follows the project's coding standards and includes appropriate tests.

## License

Copyright © 2023 Jump. All rights reserved.

This is proprietary software. Unauthorized copying, modification, distribution, or use is strictly prohibited.