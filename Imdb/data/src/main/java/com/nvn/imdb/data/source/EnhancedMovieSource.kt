package com.nvn.imdb.data.source

import com.nvn.imdb.data.model.Content
import com.nvn.imdb.data.model.MovieResponse
import com.nvn.imdb.data.remote.api.MovieService
import com.nvn.imdb.data.remote.connector.ServiceConnector
import com.nvn.imdb.data.remote.network.response.NetworkResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced MovieSource that uses ServiceConnector for improved reliability,
 * caching, and network handling
 */
@Singleton
class EnhancedMovieSource @Inject constructor(
    private val movieService: MovieService,
    private val serviceConnector: ServiceConnector
) : MovieSource {
    
    companion object {
        private const val CACHE_TTL_MOVIES = 300_000L // 5 minutes
        private const val CACHE_TTL_MOVIE_DETAIL = 600_000L // 10 minutes
    }
    
    /**
     * Get movie list with caching and retry logic
     */
    override suspend fun getMovieList(query: String): NetworkResponse<MovieResponse, Error> {
        val cacheKey = "movie_list_$query"
        
        return serviceConnector.executeRequest(
            request = { movieService.getMovieList(query) },
            cacheKey = cacheKey
        )
    }
    
    /**
     * Get movie detail with extended caching
     */
    override suspend fun getDetailMovie(id: String): NetworkResponse<Content, Error> {
        val cacheKey = "movie_detail_$id"
        
        return serviceConnector.executeRequest(
            request = { movieService.getMovieDetail(id) },
            cacheKey = cacheKey
        )
    }
    
    /**
     * Get movie list as Flow for reactive programming
     * Emits cached data first, then fresh data
     */
    fun getMovieListAsFlow(query: String): Flow<NetworkResponse<MovieResponse, Error>> {
        val cacheKey = "movie_list_$query"
        
        return serviceConnector.executeRequestAsFlow(
            request = { movieService.getMovieList(query) },
            cacheKey = cacheKey
        )
    }
    
    /**
     * Get movie detail as Flow
     */
    fun getMovieDetailAsFlow(id: String): Flow<NetworkResponse<Content, Error>> {
        val cacheKey = "movie_detail_$id"
        
        return serviceConnector.executeRequestAsFlow(
            request = { movieService.getMovieDetail(id) },
            cacheKey = cacheKey
        )
    }
    
    /**
     * Force refresh movie list (bypass cache)
     */
    suspend fun refreshMovieList(query: String): NetworkResponse<MovieResponse, Error> {
        val cacheKey = "movie_list_$query"
        
        return serviceConnector.executeRequest(
            request = { movieService.getMovieList(query) },
            cacheKey = cacheKey,
            forceRefresh = true
        )
    }
    
    /**
     * Force refresh movie detail (bypass cache)
     */
    suspend fun refreshMovieDetail(id: String): NetworkResponse<Content, Error> {
        val cacheKey = "movie_detail_$id"
        
        return serviceConnector.executeRequest(
            request = { movieService.getMovieDetail(id) },
            cacheKey = cacheKey,
            forceRefresh = true
        )
    }
    
    /**
     * Clear movie-related cache
     */
    suspend fun clearMovieCache() {
        serviceConnector.clearCache("movie_")
    }
    
    /**
     * Check if network is available
     */
    fun isNetworkAvailable(): Boolean {
        return serviceConnector.isNetworkAvailable()
    }
    
    /**
     * Get cached movie list if available
     */
    suspend fun getCachedMovieList(query: String): NetworkResponse<MovieResponse, Error>? {
        val cacheKey = "movie_list_$query"
        return serviceConnector.getCachedData(cacheKey)
    }
    
    /**
     * Get cached movie detail if available
     */
    suspend fun getCachedMovieDetail(id: String): NetworkResponse<Content, Error>? {
        val cacheKey = "movie_detail_$id"
        return serviceConnector.getCachedData(cacheKey)
    }
}