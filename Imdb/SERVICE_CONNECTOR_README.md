# Android Service Connector for Kotlin

A comprehensive service connector system for Android applications built with Kotlin, providing robust networking capabilities with caching, retry logic, authentication, and reactive programming support.

## Features

### 🚀 Core Features
- **Automatic Retry Logic**: Exponential backoff retry mechanism for failed requests
- **Smart Caching**: In-memory caching with TTL (Time To Live) support
- **Network Monitoring**: Real-time network connectivity monitoring
- **Authentication Management**: Secure token and API key management
- **Reactive Programming**: Flow-based reactive API calls
- **Service Factory**: Centralized service instance management
- **Error Handling**: Comprehensive error handling and recovery

### 🛡️ Security Features
- **Encrypted Storage**: Secure credential storage using Android's EncryptedSharedPreferences
- **Authentication Interceptor**: Automatic authentication header injection
- **Token Management**: Access token and refresh token handling

### 📱 Android Integration
- **Hilt Dependency Injection**: Full integration with Dagger Hilt
- **Lifecycle Aware**: Proper integration with Android lifecycle components
- **Context Aware**: Network state monitoring with Android's ConnectivityManager

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Service Connector                        │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │ Connection      │  │ Cache           │  │ Auth         │ │
│  │ Manager         │  │ Manager         │  │ Handler      │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐                   │
│  │ Service         │  │ Retry Logic &   │                   │
│  │ Factory         │  │ Error Handling  │                   │
│  └─────────────────┘  └─────────────────┘                   │
├─────────────────────────────────────────────────────────────┤
│                    Retrofit + OkHttp                        │
└─────────────────────────────────────────────────────────────┘
```

## Quick Start

### 1. Add Dependencies

The service connector is already integrated into your project. Make sure you have these dependencies in your `data/build.gradle`:

```kotlin
implementation libs.androidx.security.crypto
implementation libs.retrofit
implementation libs.hilt.android
implementation libs.coroutines.android
```

### 2. Basic Usage

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val serviceConnector: ServiceConnector,
    private val movieService: MovieService
) : ViewModel() {
    
    fun searchMovies(query: String) {
        viewModelScope.launch {
            val result = serviceConnector.executeRequest(
                request = { movieService.getMovieList(query) },
                cacheKey = "movies_$query"
            )
            
            when (result) {
                is NetworkResponse.Success -> {
                    // Handle success
                    val movies = result.body
                }
                is NetworkResponse.Error -> {
                    // Handle error
                    val error = result.error
                }
            }
        }
    }
}
```

### 3. Reactive Programming with Flow

```kotlin
fun searchMoviesReactive(query: String): Flow<NetworkResponse<MovieResponse, Error>> {
    return serviceConnector.executeRequestAsFlow(
        request = { movieService.getMovieList(query) },
        cacheKey = "movies_$query"
    )
}
```

## Components

### ServiceConnector Interface

The main interface providing consistent API communication:

```kotlin
interface ServiceConnector {
    suspend fun <T, E> executeRequest(
        request: suspend () -> NetworkResponse<T, E>,
        cacheKey: String? = null,
        forceRefresh: Boolean = false
    ): NetworkResponse<T, E>
    
    fun <T, E> executeRequestAsFlow(
        request: suspend () -> NetworkResponse<T, E>,
        cacheKey: String? = null
    ): Flow<NetworkResponse<T, E>>
    
    fun isNetworkAvailable(): Boolean
    suspend fun clearCache(cacheKey: String? = null)
    suspend fun <T> getCachedData(cacheKey: String): T?
    suspend fun <T> cacheData(cacheKey: String, data: T, ttlMillis: Long = 300_000L)
}
```

### ConnectionManager

Monitors network connectivity and provides network state information:

```kotlin
@Singleton
class ConnectionManager @Inject constructor(context: Context) {
    fun isNetworkAvailable(): Boolean
    fun getNetworkType(): NetworkType
    fun observeNetworkState(): Flow<NetworkState>
    fun isConnectionMetered(): Boolean
}
```

### CacheManager

Handles in-memory caching with TTL support:

```kotlin
@Singleton
class CacheManager @Inject constructor() {
    suspend fun <T> put(key: String, data: T, ttlMillis: Long = 300_000L)
    suspend fun <T> get(key: String): T?
    suspend fun contains(key: String): Boolean
    suspend fun remove(key: String)
    suspend fun clear(keyPattern: String? = null)
}
```

### AuthenticationHandler

Manages secure authentication credentials:

```kotlin
@Singleton
class AuthenticationHandler @Inject constructor(context: Context) {
    suspend fun storeAccessToken(token: String, expiryTimeMillis: Long = 0L)
    suspend fun storeApiKey(apiKey: String)
    suspend fun getAccessToken(): String?
    suspend fun isAuthenticated(): Boolean
    fun getAuthInterceptor(): Interceptor
}
```

### ServiceFactory

Creates and manages service instances:

```kotlin
@Singleton
class ServiceFactory @Inject constructor(retrofitBuilder: Retrofit.Builder) {
    fun <T> createService(serviceClass: Class<T>, baseUrl: String? = null): T
    inline fun <reified T> create(baseUrl: String? = null): T
}
```

## Usage Patterns

### 1. Basic API Call with Caching

```kotlin
suspend fun getMovies(query: String): NetworkResponse<MovieResponse, Error> {
    return serviceConnector.executeRequest(
        request = { movieService.getMovieList(query) },
        cacheKey = "movies_$query"
    )
}
```

### 2. Force Refresh (Bypass Cache)

```kotlin
suspend fun refreshMovies(query: String): NetworkResponse<MovieResponse, Error> {
    return serviceConnector.executeRequest(
        request = { movieService.getMovieList(query) },
        cacheKey = "movies_$query",
        forceRefresh = true
    )
}
```

### 3. Reactive Programming

```kotlin
fun getMoviesFlow(query: String): Flow<NetworkResponse<MovieResponse, Error>> {
    return serviceConnector.executeRequestAsFlow(
        request = { movieService.getMovieList(query) },
        cacheKey = "movies_$query"
    )
}
```

### 4. Network State Monitoring

```kotlin
fun observeNetworkState() {
    connectionManager.observeNetworkState()
        .onEach { networkState ->
            when {
                networkState.isConnected -> {
                    // Handle connected state
                    when (networkState.networkType) {
                        NetworkType.WIFI -> { /* WiFi connected */ }
                        NetworkType.CELLULAR -> { /* Cellular connected */ }
                        else -> { /* Other connection */ }
                    }
                }
                else -> {
                    // Handle disconnected state
                }
            }
        }
        .launchIn(viewModelScope)
}
```

### 5. Authentication Setup

```kotlin
suspend fun setupAuthentication() {
    // Store API key
    authenticationHandler.storeApiKey("your-api-key")
    
    // Store access token with expiry
    val expiryTime = System.currentTimeMillis() + 3600000 // 1 hour
    authenticationHandler.storeAccessToken("access-token", expiryTime)
    
    // Check authentication status
    val isAuthenticated = authenticationHandler.isAuthenticated()
}
```

### 6. Cache Management

```kotlin
// Get cached data
val cachedMovies = serviceConnector.getCachedData<MovieResponse>("movies_batman")

// Manual caching
serviceConnector.cacheData("custom_key", movieData, ttlMillis = 600_000L)

// Clear specific cache
serviceConnector.clearCache("movies_batman")

// Clear all cache
serviceConnector.clearCache()
```

### 7. Service Factory Usage

```kotlin
// Create service with default configuration
val movieService = serviceFactory.create<MovieService>()

// Create service with custom base URL
val customService = serviceFactory.create<MovieService>("https://api.custom.com/")

// Create service with custom configuration
val configuredService = serviceFactory.createWithConfig<MovieService>(
    baseUrl = "https://api.example.com/"
) {
    addConverterFactory(GsonConverterFactory.create())
    addCallAdapterFactory(RxJava3CallAdapterFactory.create())
}
```

## Error Handling

The service connector provides comprehensive error handling:

```kotlin
when (result) {
    is NetworkResponse.Success -> {
        // Handle successful response
        val data = result.body
    }
    is NetworkResponse.ApiError -> {
        // Handle API errors (4xx, 5xx)
        val errorCode = result.code
        val errorBody = result.body
    }
    is NetworkResponse.NetworkError -> {
        // Handle network errors (no connection, timeout, etc.)
        val exception = result.error
    }
    is NetworkResponse.UnknownError -> {
        // Handle unknown errors
        val throwable = result.error
    }
}
```

## Configuration

### Retry Configuration

The service connector uses exponential backoff with these default settings:
- **Max Retries**: 3
- **Initial Delay**: 1000ms
- **Backoff Multiplier**: 2x

### Cache Configuration

Default cache settings:
- **Default TTL**: 5 minutes (300,000ms)
- **Storage**: In-memory with thread-safe access
- **Cleanup**: Automatic expired entry cleanup

### Network Monitoring

- **Real-time**: Network state changes are monitored in real-time
- **Types**: WiFi, Cellular, Ethernet, Other
- **Metered Detection**: Automatic detection of metered connections

## Best Practices

### 1. Use Appropriate Cache Keys
```kotlin
// Good: Specific and descriptive
val cacheKey = "movie_search_${query}_page_${page}"

// Bad: Too generic
val cacheKey = "movies"
```

### 2. Handle Network State Changes
```kotlin
// Monitor network state and adapt behavior
connectionManager.observeNetworkState()
    .onEach { networkState ->
        if (!networkState.isConnected) {
            // Show offline mode UI
            // Use cached data
        }
    }
    .launchIn(viewModelScope)
```

### 3. Use Flows for Reactive UI
```kotlin
// Emit cached data first, then fresh data
serviceConnector.executeRequestAsFlow(request, cacheKey)
    .onEach { result ->
        when (result) {
            is NetworkResponse.Success -> updateUI(result.body)
            is NetworkResponse.Error -> showError(result.error)
        }
    }
    .launchIn(viewModelScope)
```

### 4. Proper Error Handling
```kotlin
// Always handle all error types
when (result) {
    is NetworkResponse.Success -> handleSuccess(result.body)
    is NetworkResponse.ApiError -> {
        when (result.code) {
            401 -> redirectToLogin()
            429 -> showRateLimitError()
            else -> showGenericError(result.body)
        }
    }
    is NetworkResponse.NetworkError -> showNetworkError()
    is NetworkResponse.UnknownError -> showUnknownError()
}
```

### 5. Cache Management
```kotlin
// Clear cache when user logs out
suspend fun logout() {
    authenticationHandler.clearTokens()
    serviceConnector.clearCache()
}

// Periodic cache cleanup
suspend fun performMaintenance() {
    (serviceConnector as? ServiceConnectorImpl)?.cleanupExpiredCache()
}
```

## Testing

### Unit Testing ServiceConnector

```kotlin
@Test
fun `test service connector with mock responses`() = runTest {
    // Mock the network response
    val mockResponse = NetworkResponse.Success(mockMovieResponse)
    coEvery { movieService.getMovieList(any()) } returns mockResponse
    
    // Execute request
    val result = serviceConnector.executeRequest(
        request = { movieService.getMovieList("batman") },
        cacheKey = "test_key"
    )
    
    // Verify result
    assertTrue(result is NetworkResponse.Success)
    assertEquals(mockMovieResponse, (result as NetworkResponse.Success).body)
}
```

## Migration Guide

### From Direct Retrofit Usage

**Before:**
```kotlin
class MovieRepository @Inject constructor(
    private val movieService: MovieService
) {
    suspend fun getMovies(query: String) = movieService.getMovieList(query)
}
```

**After:**
```kotlin
class MovieRepository @Inject constructor(
    private val serviceConnector: ServiceConnector,
    private val movieService: MovieService
) {
    suspend fun getMovies(query: String) = serviceConnector.executeRequest(
        request = { movieService.getMovieList(query) },
        cacheKey = "movies_$query"
    )
}
```

## Performance Considerations

1. **Memory Usage**: Cache is stored in memory. Monitor memory usage for large datasets.
2. **Network Efficiency**: Automatic caching reduces redundant network calls.
3. **Background Processing**: All operations run on IO dispatcher by default.
4. **Concurrent Safety**: All components are thread-safe and can be used concurrently.

## Troubleshooting

### Common Issues

1. **Cache Not Working**: Ensure you're providing consistent cache keys
2. **Authentication Issues**: Check if tokens are stored correctly and not expired
3. **Network Errors**: Verify network permissions in AndroidManifest.xml
4. **Memory Leaks**: Ensure proper lifecycle management in ViewModels

### Debug Logging

Enable debug logging to troubleshoot issues:
```kotlin
// All service connector operations are logged with TAG "ServiceConnector"
// Filter logs: adb logcat | grep "ServiceConnector"
```

## License

This service connector is part of the IMDB Android application and follows the project's licensing terms.