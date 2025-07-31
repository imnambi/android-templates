package com.nvn.imdb.data.remote.connector

import com.nvn.imdb.data.remote.network.response.NetworkResponse
import kotlinx.coroutines.flow.Flow

/**
 * Generic service connector interface for consistent API communication
 * Provides standardized methods for making API calls with retry logic,
 * error handling, and caching capabilities
 */
interface ServiceConnector {
    
    /**
     * Execute a network request with automatic retry and error handling
     * @param request The suspend function that makes the API call
     * @param cacheKey Optional cache key for storing/retrieving cached data
     * @param forceRefresh Whether to bypass cache and force a fresh request
     * @return NetworkResponse containing the result or error
     */
    suspend fun <T, E> executeRequest(
        request: suspend () -> NetworkResponse<T, E>,
        cacheKey: String? = null,
        forceRefresh: Boolean = false
    ): NetworkResponse<T, E>
    
    /**
     * Execute a network request that returns a Flow for reactive programming
     * @param request The suspend function that makes the API call
     * @param cacheKey Optional cache key for storing/retrieving cached data
     * @return Flow of NetworkResponse containing the result or error
     */
    fun <T, E> executeRequestAsFlow(
        request: suspend () -> NetworkResponse<T, E>,
        cacheKey: String? = null
    ): Flow<NetworkResponse<T, E>>
    
    /**
     * Check if the device has network connectivity
     * @return true if connected, false otherwise
     */
    fun isNetworkAvailable(): Boolean
    
    /**
     * Clear cache for a specific key or all cache if key is null
     * @param cacheKey The cache key to clear, null to clear all
     */
    suspend fun clearCache(cacheKey: String? = null)
    
    /**
     * Get cached data for a specific key
     * @param cacheKey The cache key to retrieve
     * @return Cached data or null if not found
     */
    suspend fun <T> getCachedData(cacheKey: String): T?
    
    /**
     * Store data in cache
     * @param cacheKey The cache key to store data under
     * @param data The data to cache
     * @param ttlMillis Time to live in milliseconds, default is 5 minutes
     */
    suspend fun <T> cacheData(cacheKey: String, data: T, ttlMillis: Long = 300_000L)
}