package com.nvn.imdb.data.remote.connector

import android.util.Log
import com.nvn.imdb.data.di.qualifiers.IODispatcher
import com.nvn.imdb.data.remote.network.response.NetworkResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ServiceConnector with retry logic, caching, and network monitoring
 */
@Singleton
class ServiceConnectorImpl @Inject constructor(
    private val connectionManager: ConnectionManager,
    private val cacheManager: CacheManager,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : ServiceConnector {
    
    companion object {
        private const val TAG = "ServiceConnector"
        private const val DEFAULT_RETRY_COUNT = 3
        private const val DEFAULT_RETRY_DELAY_MS = 1000L
        private const val DEFAULT_CACHE_TTL_MS = 300_000L // 5 minutes
    }
    
    override suspend fun <T, E> executeRequest(
        request: suspend () -> NetworkResponse<T, E>,
        cacheKey: String?,
        forceRefresh: Boolean
    ): NetworkResponse<T, E> = withContext(ioDispatcher) {
        
        // Check cache first if not forcing refresh
        if (!forceRefresh && cacheKey != null) {
            val cachedData = cacheManager.get<NetworkResponse<T, E>>(cacheKey)
            if (cachedData != null) {
                Log.d(TAG, "Returning cached data for key: $cacheKey")
                return@withContext cachedData
            }
        }
        
        // Check network connectivity
        if (!connectionManager.isNetworkAvailable()) {
            Log.w(TAG, "No network connection available")
            // Try to return cached data even if expired
            if (cacheKey != null) {
                val cachedData = cacheManager.get<NetworkResponse<T, E>>(cacheKey)
                if (cachedData != null) {
                    Log.d(TAG, "Returning expired cached data due to no network: $cacheKey")
                    return@withContext cachedData
                }
            }
            return@withContext NetworkResponse.NetworkError(
                Exception("No network connection available")
            ) as NetworkResponse<T, E>
        }
        
        // Execute request with retry logic
        val result = executeWithRetry(request)
        
        // Cache successful results
        if (result is NetworkResponse.Success && cacheKey != null) {
            cacheManager.put(cacheKey, result, DEFAULT_CACHE_TTL_MS)
            Log.d(TAG, "Cached successful response for key: $cacheKey")
        }
        
        return@withContext result
    }
    
    override fun <T, E> executeRequestAsFlow(
        request: suspend () -> NetworkResponse<T, E>,
        cacheKey: String?
    ): Flow<NetworkResponse<T, E>> = flow {
        
        // Emit cached data first if available
        if (cacheKey != null) {
            val cachedData = cacheManager.get<NetworkResponse<T, E>>(cacheKey)
            if (cachedData != null) {
                Log.d(TAG, "Emitting cached data for key: $cacheKey")
                emit(cachedData)
            }
        }
        
        // Then execute fresh request
        val freshResult = executeRequest(request, cacheKey, forceRefresh = true)
        emit(freshResult)
        
    }.flowOn(ioDispatcher)
    
    override fun isNetworkAvailable(): Boolean {
        return connectionManager.isNetworkAvailable()
    }
    
    override suspend fun clearCache(cacheKey: String?) {
        if (cacheKey != null) {
            cacheManager.remove(cacheKey)
            Log.d(TAG, "Cleared cache for key: $cacheKey")
        } else {
            cacheManager.clear()
            Log.d(TAG, "Cleared all cache")
        }
    }
    
    override suspend fun <T> getCachedData(cacheKey: String): T? {
        return cacheManager.get(cacheKey)
    }
    
    override suspend fun <T> cacheData(cacheKey: String, data: T, ttlMillis: Long) {
        cacheManager.put(cacheKey, data, ttlMillis)
        Log.d(TAG, "Manually cached data for key: $cacheKey with TTL: ${ttlMillis}ms")
    }
    
    /**
     * Execute request with exponential backoff retry logic
     */
    private suspend fun <T, E> executeWithRetry(
        request: suspend () -> NetworkResponse<T, E>,
        maxRetries: Int = DEFAULT_RETRY_COUNT
    ): NetworkResponse<T, E> {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                val result = request()
                
                // If successful or client error (4xx), don't retry
                when (result) {
                    is NetworkResponse.Success -> return result
                    is NetworkResponse.ApiError -> {
                        // Don't retry client errors (4xx)
                        if (result.code in 400..499) {
                            Log.w(TAG, "Client error (${result.code}), not retrying")
                            return result
                        }
                        // Retry server errors (5xx)
                        Log.w(TAG, "Server error (${result.code}), attempt ${attempt + 1}/$maxRetries")
                    }
                    is NetworkResponse.NetworkError -> {
                        Log.w(TAG, "Network error, attempt ${attempt + 1}/$maxRetries: ${result.error.message}")
                        lastException = result.error
                    }
                    is NetworkResponse.UnknownError -> {
                        Log.w(TAG, "Unknown error, attempt ${attempt + 1}/$maxRetries: ${result.error?.message}")
                        lastException = result.error
                    }
                }
                
                // If not the last attempt, wait before retrying with exponential backoff
                if (attempt < maxRetries - 1) {
                    val delayMs = DEFAULT_RETRY_DELAY_MS * (1L shl attempt) // Exponential backoff
                    Log.d(TAG, "Waiting ${delayMs}ms before retry")
                    delay(delayMs)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Exception during request execution, attempt ${attempt + 1}/$maxRetries", e)
                lastException = e
                
                if (attempt < maxRetries - 1) {
                    val delayMs = DEFAULT_RETRY_DELAY_MS * (1L shl attempt)
                    delay(delayMs)
                }
            }
        }
        
        // All retries failed, return network error
        return NetworkResponse.NetworkError(
            lastException ?: Exception("All retry attempts failed")
        ) as NetworkResponse<T, E>
    }
    
    /**
     * Get network connection type
     */
    fun getNetworkType(): NetworkType {
        return connectionManager.getNetworkType()
    }
    
    /**
     * Check if connection is metered
     */
    fun isConnectionMetered(): Boolean {
        return connectionManager.isConnectionMetered()
    }
    
    /**
     * Get cache statistics
     */
    suspend fun getCacheStats(): CacheStats {
        return cacheManager.getStats()
    }
    
    /**
     * Clean up expired cache entries
     */
    suspend fun cleanupExpiredCache() {
        cacheManager.cleanupExpired()
        Log.d(TAG, "Cleaned up expired cache entries")
    }
}