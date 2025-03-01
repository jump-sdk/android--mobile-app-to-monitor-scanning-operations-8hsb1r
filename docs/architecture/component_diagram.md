# ScanMonitorApps Component Diagrams

## Introduction

This document provides component diagrams for the ScanMonitorApps application, illustrating the relationships and interactions between different modules and classes. The diagrams follow the MVVM architecture pattern with Repository and use Mermaid notation for clarity.

This document complements the [High-Level Architecture](high_level_architecture.md) by providing more detailed views of specific components and their relationships. While the high-level architecture document focuses on the overall structure and patterns, this document delves into specific class relationships, methods, and interactions.

## High-Level Component Diagram

The following diagram shows the high-level components of the ScanMonitorApps application and their interactions.

```mermaid
classDiagram
    class MainActivity {
        +onCreate(Bundle?): Unit
    }
    
    class MetricsDashboardScreen {
        +Compose UI Elements
    }
    
    class ScanMetricsViewModel {
        -repository: ScanMetricsRepository
        -networkMonitor: NetworkMonitor
        -_uiState: MutableStateFlow~UiState~
        +uiState: StateFlow~UiState~
        -refreshJob: Job?
        +refreshMetrics(): Unit
        -loadMetrics(): Unit
        -startAutoRefresh(): Unit
        +onCleared(): Unit
    }
    
    class ScanMetricsRepository {
        -apiService: DatadogApiService
        -cache: MetricsCache
        -mapper: MetricsMapper
        +getMetrics(forceRefresh: Boolean): Result~ScanMetrics~
        +refreshMetrics(): Result~ScanMetrics~
        -buildQuery(): String
        -getTimeRange(): Pair~Long, Long~
    }
    
    class DatadogApiService {
        <<interface>>
        +getScanMetrics(query: String, from: Long, to: Long): ApiResponse
    }
    
    class DatadogApiServiceImpl {
        -apiKey: String
        -applicationKey: String
        -retrofit: Retrofit
        -createOkHttpClient(): OkHttpClient
        +create(): DatadogApiService
    }
    
    class MetricsCache {
        -sharedPreferences: SharedPreferences
        +saveMetrics(metrics: ScanMetrics): Unit
        +getMetrics(): ScanMetrics?
        +getLastUpdateTime(): Long
        +clear(): Unit
        +isCacheAvailable(): Boolean
    }
    
    class NetworkMonitor {
        -connectivityManager: ConnectivityManager
        -_networkStatus: MutableStateFlow~NetworkStatus~
        +networkStatus: StateFlow~NetworkStatus~
        -networkCallback: NetworkCallback
        +isConnected(): Boolean
        -getInitialNetworkStatus(): NetworkStatus
        -getConnectionType(): ConnectionType
        +cleanup(): Unit
    }
    
    class MetricsMapper {
        +mapApiResponseToMetrics(response: ApiResponse): ScanMetrics
        -extractTotalCount(response: ApiResponse): Int
        -extractTimestamp(response: ApiResponse): Long
    }
    
    %% Model Classes
    class ScanMetrics {
        +count: Int
        +timestamp: Long
    }
    
    class UiState {
        +loading: Boolean
        +data: ScanMetrics?
        +error: Exception?
        +isConnected: Boolean
        +isStale: Boolean
    }
    
    class ApiResponse {
        +series: List~Series~
        +metadata: Metadata
    }
    
    class Result~T~ {
        <<sealed class>>
    }
    
    class Success~T~ {
        +data: T
        +isFromCache: Boolean
        +isStale: Boolean
    }
    
    class Error {
        +exception: Exception
    }
    
    class NetworkStatus {
        +isConnected: Boolean
        +type: ConnectionType
    }
    
    %% External Systems
    class DatadogAPI {
        <<external>>
    }
    
    %% Relationships
    MainActivity --> MetricsDashboardScreen: hosts
    MetricsDashboardScreen --> ScanMetricsViewModel: observes
    ScanMetricsViewModel --> ScanMetricsRepository: uses
    ScanMetricsViewModel --> NetworkMonitor: monitors
    ScanMetricsViewModel --> UiState: exposes
    ScanMetricsRepository --> DatadogApiService: calls
    ScanMetricsRepository --> MetricsCache: uses
    ScanMetricsRepository --> MetricsMapper: uses
    ScanMetricsRepository ..> Result: returns
    DatadogApiServiceImpl ..|> DatadogApiService: implements
    DatadogApiServiceImpl --> DatadogAPI: communicates with
    DatadogApiService ..> ApiResponse: returns
    MetricsMapper --> ApiResponse: transforms
    MetricsMapper --> ScanMetrics: produces
    Result <|-- Success
    Result <|-- Error
    Success --> "1" ScanMetrics: contains
    Error --> "1" Exception: contains
    UiState --> "0..1" ScanMetrics: contains
    UiState --> "0..1" Exception: contains
```

## Dependency Injection

The following diagram illustrates the dependency injection structure using Koin.

```mermaid
classDiagram
    class ScanMonitorApplication {
        +onCreate(): Unit
    }
    
    class AppModule {
        <<module>>
        +viewModels()
        +repositories()
        +services()
        +utilities()
    }
    
    class Koin {
        <<external>>
    }
    
    ScanMonitorApplication --> AppModule: registers
    ScanMonitorApplication --> Koin: initializes
    
    AppModule ..> ScanMetricsViewModel: provides
    AppModule ..> ScanMetricsRepository: provides
    AppModule ..> DatadogApiServiceImpl: provides
    AppModule ..> DatadogApiService: provides
    AppModule ..> MetricsCache: provides
    AppModule ..> NetworkMonitor: provides
    AppModule ..> MetricsMapper: provides
```

## UI Component Diagram

The following diagram shows the UI component hierarchy and composition.

```mermaid
classDiagram
    class MainActivity {
        +onCreate(Bundle?): Unit
        +setContent()
    }
    
    class MetricsDashboardScreen {
        <<composable>>
    }
    
    class MetricsDashboardContent {
        <<composable>>
        -UiState: uiState
        -Function: onRefresh
    }
    
    class StatusBar {
        <<composable>>
        -Boolean: isOffline
        -Boolean: isStale
        -Long?: lastUpdated
    }
    
    class MetricsDisplay {
        <<composable>>
        -ScanMetrics?: metrics
    }
    
    class LoadingIndicator {
        <<composable>>
    }
    
    class ErrorState {
        <<composable>>
        -Exception: error
        -Function: onRetry
    }
    
    class SwipeRefresh {
        <<composable>>
        -Boolean: refreshing
        -Function: onRefresh
    }
    
    MainActivity --> MetricsDashboardScreen: hosts
    MetricsDashboardScreen --> MetricsDashboardContent: composes
    MetricsDashboardContent --> StatusBar: includes
    MetricsDashboardContent --> SwipeRefresh: includes
    MetricsDashboardContent --> LoadingIndicator: conditionally includes
    MetricsDashboardContent --> ErrorState: conditionally includes
    SwipeRefresh --> MetricsDisplay: contains
    
    %% UI State relationship
    MetricsDashboardScreen --> ScanMetricsViewModel: observes
```

## Data Flow Diagram

The following diagram illustrates the data flow through the application components.

```mermaid
sequenceDiagram
    participant UI as UI Components
    participant ViewModel as ScanMetricsViewModel
    participant Repository as ScanMetricsRepository
    participant APIService as DatadogApiService
    participant Cache as MetricsCache
    participant Mapper as MetricsMapper
    participant DatadogAPI as Datadog API
    
    UI->>ViewModel: Observe uiState
    ViewModel->>ViewModel: Initialize with loading state
    ViewModel->>Repository: getMetrics()
    
    alt Force refresh or no cache
        Repository->>APIService: getScanMetrics(query, from, to)
        APIService->>DatadogAPI: HTTP GET request
        DatadogAPI-->>APIService: JSON response
        APIService-->>Repository: ApiResponse
        Repository->>Mapper: mapApiResponseToMetrics(response)
        Mapper-->>Repository: ScanMetrics
        Repository->>Cache: saveMetrics(metrics)
        Repository-->>ViewModel: Result.Success(metrics)
    else API failure with cache available
        Repository->>APIService: getScanMetrics(query, from, to)
        APIService->>DatadogAPI: HTTP GET request
        DatadogAPI-->>APIService: Error response
        APIService-->>Repository: Exception
        Repository->>Cache: getMetrics()
        Cache-->>Repository: ScanMetrics?
        Repository-->>ViewModel: Result.Success(cachedMetrics, isFromCache=true, isStale=true)
    else API failure with no cache
        Repository->>APIService: getScanMetrics(query, from, to)
        APIService->>DatadogAPI: HTTP GET request
        DatadogAPI-->>APIService: Error response
        APIService-->>Repository: Exception
        Repository->>Cache: getMetrics()
        Cache-->>Repository: null
        Repository-->>ViewModel: Result.Error(exception)
    else Use cache (not forcing refresh)
        Repository->>Cache: getMetrics()
        Cache-->>Repository: ScanMetrics?
        alt Fresh cache
            Repository-->>ViewModel: Result.Success(cachedMetrics, isFromCache=true, isStale=false)
        else Stale cache
            Repository->>APIService: getScanMetrics(query, from, to)
            APIService->>DatadogAPI: HTTP GET request
            DatadogAPI-->>APIService: JSON response
            APIService-->>Repository: ApiResponse
            Repository->>Mapper: mapApiResponseToMetrics(response)
            Mapper-->>Repository: ScanMetrics
            Repository->>Cache: saveMetrics(metrics)
            Repository-->>ViewModel: Result.Success(metrics)
        end
    end
    
    ViewModel->>ViewModel: Update uiState
    ViewModel-->>UI: Updated uiState
    UI->>UI: Render based on uiState
```

## Network Monitoring Flow

The following diagram shows how network connectivity changes are monitored and handled.

```mermaid
sequenceDiagram
    participant VM as ScanMetricsViewModel
    participant NM as NetworkMonitor
    participant CM as ConnectivityManager
    participant Repo as ScanMetricsRepository
    
    Note over NM,CM: NetworkMonitor initialization
    NM->>CM: Register NetworkCallback
    CM-->>NM: Initial network status
    NM->>NM: Initialize _networkStatus
    
    Note over VM,NM: ViewModel monitors network status
    VM->>NM: Collect networkStatus flow
    
    CM->>NM: onAvailable(Network)
    NM->>NM: Update _networkStatus(connected=true)
    NM-->>VM: Updated NetworkStatus
    
    alt Was previously in error state
        VM->>Repo: refreshMetrics()
    end
    
    CM->>NM: onLost(Network)
    NM->>NM: Update _networkStatus(connected=false)
    NM-->>VM: Updated NetworkStatus
    VM->>VM: Update UI state with offline flag
    
    CM->>NM: onCapabilitiesChanged(Network, NetworkCapabilities)
    NM->>NM: Update connection type
    NM-->>VM: Updated NetworkStatus
```

## Auto-Refresh Flow

The following diagram illustrates the auto-refresh mechanism for periodically updating metrics data.

```mermaid
sequenceDiagram
    participant VM as ScanMetricsViewModel
    participant Repo as ScanMetricsRepository
    participant API as DatadogApiService
    
    Note over VM: ViewModel initialization
    VM->>VM: startAutoRefresh()
    VM->>VM: Launch coroutine with refreshJob
    
    loop Every 5 minutes
        VM->>VM: Delay(5 minutes)
        VM->>VM: Update _uiState with loading=true
        VM->>Repo: getMetrics(forceRefresh=true)
        Repo->>API: getScanMetrics()
        API-->>Repo: ApiResponse or Error
        Repo-->>VM: Result<ScanMetrics>
        
        alt Success
            VM->>VM: Update _uiState with new data
        else Error
            VM->>VM: Update _uiState with error
        end
    end
    
    Note over VM: ViewModel destroyed
    VM->>VM: onCleared()
    VM->>VM: refreshJob.cancel()
```

## Offline Mode Flow

The following diagram shows how the application handles offline mode with cached data.

```mermaid
sequenceDiagram
    participant UI as UI Components
    participant VM as ScanMetricsViewModel
    participant NM as NetworkMonitor
    participant Repo as ScanMetricsRepository
    participant Cache as MetricsCache
    
    NM-->>VM: NetworkStatus(isConnected=false)
    VM->>VM: Update _uiState with isConnected=false
    
    VM->>Repo: getMetrics()
    Repo->>NM: isConnected()
    NM-->>Repo: false
    Repo->>Cache: getMetrics()
    Cache-->>Repo: cachedMetrics
    
    alt Cache available
        Repo-->>VM: Result.Success(cachedMetrics, isFromCache=true, isStale=true)
        VM->>VM: Update _uiState with cachedData + offline flags
        VM-->>UI: Updated uiState
        UI->>UI: Display cached data with offline indicator
    else No cache
        Repo-->>VM: Result.Error(OfflineException)
        VM->>VM: Update _uiState with error
        VM-->>UI: Updated uiState
        UI->>UI: Display offline error message
    end
    
    NM-->>VM: NetworkStatus(isConnected=true)
    VM->>VM: Update _uiState with isConnected=true
    VM->>Repo: refreshMetrics()
    Repo->>Repo: getMetrics(forceRefresh=true)
    Repo-->>VM: Result<ScanMetrics>
    VM->>VM: Update _uiState with new data
    VM-->>UI: Updated uiState
```

## Package Structure

The following diagram illustrates the package structure of the ScanMonitorApps application.

```mermaid
classDiagram
    namespace com.jump.scanmonitor {
        class ScanMonitorApplication
        class MainActivity
    }
    
    namespace com.jump.scanmonitor.viewmodel {
        class ScanMetricsViewModel
    }
    
    namespace com.jump.scanmonitor.repository {
        class ScanMetricsRepository
    }
    
    namespace com.jump.scanmonitor.repository.mapper {
        class MetricsMapper
    }
    
    namespace com.jump.scanmonitor.service.api {
        class DatadogApiService
        class DatadogApiServiceImpl
    }
    
    namespace com.jump.scanmonitor.service.cache {
        class MetricsCache
    }
    
    namespace com.jump.scanmonitor.service.network {
        class NetworkMonitor
    }
    
    namespace com.jump.scanmonitor.model {
        class ScanMetrics
        class UiState
        class ApiResponse
        class Result
        class NetworkStatus
    }
    
    namespace com.jump.scanmonitor.ui.screens {
        class MetricsDashboardScreen
    }
    
    namespace com.jump.scanmonitor.ui.components {
        class StatusBar
        class MetricsDisplay
        class ErrorState
        class LoadingIndicator
    }
    
    namespace com.jump.scanmonitor.di {
        class AppModule
    }
    
    com.jump.scanmonitor --|> com.jump.scanmonitor.viewmodel
    com.jump.scanmonitor --|> com.jump.scanmonitor.ui.screens
    com.jump.scanmonitor.viewmodel --|> com.jump.scanmonitor.repository
    com.jump.scanmonitor.viewmodel --|> com.jump.scanmonitor.service.network
    com.jump.scanmonitor.viewmodel --|> com.jump.scanmonitor.model
    com.jump.scanmonitor.repository --|> com.jump.scanmonitor.service.api
    com.jump.scanmonitor.repository --|> com.jump.scanmonitor.service.cache
    com.jump.scanmonitor.repository --|> com.jump.scanmonitor.repository.mapper
    com.jump.scanmonitor.repository --|> com.jump.scanmonitor.model
    com.jump.scanmonitor.repository.mapper --|> com.jump.scanmonitor.model
    com.jump.scanmonitor.ui.screens --|> com.jump.scanmonitor.ui.components
    com.jump.scanmonitor.ui.screens --|> com.jump.scanmonitor.viewmodel
    com.jump.scanmonitor.ui.screens --|> com.jump.scanmonitor.model
    com.jump.scanmonitor.service.api --|> com.jump.scanmonitor.model
    com.jump.scanmonitor.service.cache --|> com.jump.scanmonitor.model
    com.jump.scanmonitor.service.network --|> com.jump.scanmonitor.model
    com.jump.scanmonitor.di --|> com.jump.scanmonitor.viewmodel
    com.jump.scanmonitor.di --|> com.jump.scanmonitor.repository
    com.jump.scanmonitor.di --|> com.jump.scanmonitor.service.api
    com.jump.scanmonitor.di --|> com.jump.scanmonitor.service.cache
    com.jump.scanmonitor.di --|> com.jump.scanmonitor.service.network
    com.jump.scanmonitor.di --|> com.jump.scanmonitor.repository.mapper
```

## State Management

The following diagram shows how state flows through the application using Kotlin StateFlow.

```mermaid
flowchart TD
    A[Repository Data Operations] --> B[Result<ScanMetrics>]
    C[Network Status Changes] --> D[NetworkStatus]
    
    B --> E[ViewModel Processing]
    D --> E
    
    E --> F[_uiState MutableStateFlow]
    F --> G[uiState StateFlow]
    G --> H[UI Observes StateFlow]
    H --> I[UI Renders Based on State]
    
    J[User Actions] --> K[ViewModel Functions]
    K --> L[Repository Operations]
    L --> B
    
    M[System Events] --> N[Network Callbacks]
    N --> D
    
    O[Auto-refresh Timer] --> K
```

## Conclusion

The component diagrams presented in this document illustrate the MVVM architecture of the ScanMonitorApps application with a Repository pattern. The application follows a client-only architecture with direct integration to the Datadog API and implements robust offline capabilities through local caching. The reactive UI updates are handled through StateFlow, providing a clean separation of concerns and a responsive user experience.

These diagrams serve as a comprehensive reference for developers working on the application, making it easier to understand the component relationships and data flows. They complement the high-level architecture document by providing more detailed views of specific components and their interactions.