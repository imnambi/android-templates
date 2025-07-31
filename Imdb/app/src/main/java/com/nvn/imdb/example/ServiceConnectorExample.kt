package com.nvn.imdb.example

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nvn.imdb.data.model.MovieResponse
import com.nvn.imdb.data.remote.connector.AuthenticationHandler
import com.nvn.imdb.data.remote.connector.ConnectionManager
import com.nvn.imdb.data.remote.connector.NetworkState
import com.nvn.imdb.data.remote.connector.ServiceConnector
import com.nvn.imdb.data.remote.connector.ServiceFactory
import com.nvn.imdb.data.remote.connector.create
import com.nvn.imdb.data.remote.network.response.NetworkResponse
import com.nvn.imdb.data.source.EnhancedMovieSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Example ViewModel demonstrating various ways to use the ServiceConnector
 * This shows best practices for network calls, caching, error handling, and reactive programming
 */
@HiltViewModel
class ServiceConnectorExampleViewModel @Inject constructor(
    private val serviceConnector: ServiceConnector,
    private val enhancedMovieSource: EnhancedMovieSource,
    private val connectionManager: ConnectionManager,
    private val authenticationHandler: AuthenticationHandler,
    private val serviceFactory: ServiceFactory
) : ViewModel() {
    
    companion object {
        private const val TAG = "ServiceConnectorExample"
    }
    
    private val _movieState = MutableStateFlow<MovieState>(MovieState.Loading)
    val movieState: StateFlow<MovieState> = _movieState.asStateFlow()
    
    private val _networkState = MutableStateFlow<NetworkState?>(null)
    val networkState: StateFlow<NetworkState?> = _networkState.asStateFlow()
    
    init {
        observeNetworkState()
    }
    
    /**
     * Example 1: Basic usage with automatic caching and retry
     */
    fun searchMoviesBasic(query: String) {
        viewModelScope.launch {
            _movieState.value = MovieState.Loading
            
            try {
                val result = enhancedMovieSource.getMovieList(query)
                handleMovieResult(result)
            } catch (e: Exception) {
                Log.e(TAG, "Error searching movies", e)
                _movieState.value = MovieState.Error("Failed to search movies: ${e.message}")
            }
        }
    }
    
    /**
     * Example 2: Reactive programming with Flow
     * Emits cached data first, then fresh data
     */
    fun searchMoviesReactive(query: String) {
        viewModelScope.launch {
            _movieState.value = MovieState.Loading
            
            enhancedMovieSource.getMovieListAsFlow(query)
                .onEach { result ->
                    handleMovieResult(result)
                }
                .catch { e ->
                    Log.e(TAG, "Error in reactive search", e)
                    _movieState.value = MovieState.Error("Failed to search movies: ${e.message}")
                }
                .collect()
        }
    }
    
    /**
     * Example 3: Force refresh (bypass cache)
     */
    fun refreshMovies(query: String) {
        viewModelScope.launch {
            _movieState.value = MovieState.Loading
            
            try {
                val result = enhancedMovieSource.refreshMovieList(query)
                handleMovieResult(result)
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing movies", e)
                _movieState.value = MovieState.Error("Failed to refresh movies: ${e.message}")
            }
        }
    }
    
    /**
     * Example 4: Check cached data without network call
     */
    fun getCachedMovies(query: String) {
        viewModelScope.launch {
            try {
                val cachedResult = enhancedMovieSource.getCachedMovieList(query)
                if (cachedResult != null) {
                    handleMovieResult(cachedResult)
                } else {
                    _movieState.value = MovieState.Error("No cached data available")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting cached movies", e)
                _movieState.value = MovieState.Error("Failed to get cached data: ${e.message}")
            }
        }
    }
    
    /**
     * Example 5: Authentication setup
     */
    fun setupAuthentication(apiKey: String) {
        viewModelScope.launch {
            try {
                authenticationHandler.storeApiKey(apiKey)
                Log.d(TAG, "API key stored successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error storing API key", e)
            }
        }
    }
    
    /**
     * Example 6: Manual cache management
     */
    fun clearMovieCache() {
        viewModelScope.launch {
            try {
                enhancedMovieSource.clearMovieCache()
                Log.d(TAG, "Movie cache cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing cache", e)
            }
        }
    }
    
    /**
     * Example 7: Network state monitoring
     */
    private fun observeNetworkState() {
        viewModelScope.launch {
            connectionManager.observeNetworkState()
                .onEach { networkState ->
                    _networkState.value = networkState
                    Log.d(TAG, "Network state changed: $networkState")
                    
                    // Handle network state changes
                    when {
                        networkState.isConnected -> {
                            Log.d(TAG, "Network connected: ${networkState.networkType}")
                        }
                        else -> {
                            Log.d(TAG, "Network disconnected")
                        }
                    }
                }
                .catch { e ->
                    Log.e(TAG, "Error observing network state", e)
                }
                .collect()
        }
    }
    
    /**
     * Example 8: Create different service instances
     */
    fun demonstrateServiceFactory() {
        viewModelScope.launch {
            try {
                // Create a service with default configuration
                val defaultService = serviceFactory.create<com.nvn.imdb.data.remote.api.MovieService>()
                
                // Create a service with custom base URL
                val customService = serviceFactory.create<com.nvn.imdb.data.remote.api.MovieService>(
                    baseUrl = "https://api.example.com/"
                )
                
                Log.d(TAG, "Services created successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating services", e)
            }
        }
    }
    
    /**
     * Example 9: Direct ServiceConnector usage
     */
    fun directServiceConnectorUsage(query: String) {
        viewModelScope.launch {
            _movieState.value = MovieState.Loading
            
            try {
                val result = serviceConnector.executeRequest(
                    request = { 
                        // Direct API call
                        serviceFactory.create<com.nvn.imdb.data.remote.api.MovieService>()
                            .getMovieList(query)
                    },
                    cacheKey = "direct_movie_search_$query",
                    forceRefresh = false
                )
                
                handleMovieResult(result)
            } catch (e: Exception) {
                Log.e(TAG, "Error in direct service connector usage", e)
                _movieState.value = MovieState.Error("Failed to search movies: ${e.message}")
            }
        }
    }
    
    /**
     * Handle movie search results
     */
    private fun handleMovieResult(result: NetworkResponse<MovieResponse, Error>) {
        when (result) {
            is NetworkResponse.Success -> {
                _movieState.value = MovieState.Success(result.body)
                Log.d(TAG, "Movies loaded successfully: ${result.body.search?.size} movies")
            }
            is NetworkResponse.ApiError -> {
                val errorMessage = "API Error ${result.code}: ${result.body}"
                _movieState.value = MovieState.Error(errorMessage)
                Log.e(TAG, errorMessage)
            }
            is NetworkResponse.NetworkError -> {
                val errorMessage = "Network Error: ${result.error.message}"
                _movieState.value = MovieState.Error(errorMessage)
                Log.e(TAG, errorMessage, result.error)
            }
            is NetworkResponse.UnknownError -> {
                val errorMessage = "Unknown Error: ${result.error?.message}"
                _movieState.value = MovieState.Error(errorMessage)
                Log.e(TAG, errorMessage, result.error)
            }
        }
    }
    
    /**
     * Check current network availability
     */
    fun checkNetworkStatus(): Boolean {
        val isAvailable = enhancedMovieSource.isNetworkAvailable()
        Log.d(TAG, "Network available: $isAvailable")
        return isAvailable
    }
}

/**
 * Sealed class representing different states of movie data
 */
sealed class MovieState {
    object Loading : MovieState()
    data class Success(val movieResponse: MovieResponse) : MovieState()
    data class Error(val message: String) : MovieState()
}