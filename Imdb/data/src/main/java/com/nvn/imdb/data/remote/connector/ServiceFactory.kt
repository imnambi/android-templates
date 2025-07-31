package com.nvn.imdb.data.remote.connector

import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory for creating and managing different service instances
 * Provides centralized service creation with consistent configuration
 */
@Singleton
class ServiceFactory @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder
) {
    
    private val serviceCache = mutableMapOf<Class<*>, Any>()
    
    /**
     * Create or get cached service instance
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> createService(serviceClass: Class<T>, baseUrl: String? = null): T {
        // Check cache first
        val cachedService = serviceCache[serviceClass]
        if (cachedService != null) {
            return cachedService as T
        }
        
        // Create new service instance
        val retrofit = if (baseUrl != null) {
            retrofitBuilder.baseUrl(baseUrl).build()
        } else {
            retrofitBuilder.build()
        }
        
        val service = retrofit.create(serviceClass)
        
        // Cache the service
        serviceCache[serviceClass] = service as Any
        
        return service
    }
    
    /**
     * Create service with custom configuration
     */
    fun <T> createServiceWithConfig(
        serviceClass: Class<T>,
        baseUrl: String,
        configure: Retrofit.Builder.() -> Retrofit.Builder = { this }
    ): T {
        val retrofit = retrofitBuilder
            .baseUrl(baseUrl)
            .configure()
            .build()
        
        return retrofit.create(serviceClass)
    }
    
    /**
     * Clear service cache
     */
    fun clearCache() {
        serviceCache.clear()
    }
    
    /**
     * Remove specific service from cache
     */
    fun <T> removeFromCache(serviceClass: Class<T>) {
        serviceCache.remove(serviceClass)
    }
}

/**
 * Extension function for easier service creation
 */
inline fun <reified T> ServiceFactory.create(baseUrl: String? = null): T {
    return createService(T::class.java, baseUrl)
}

/**
 * Extension function for service creation with configuration
 */
inline fun <reified T> ServiceFactory.createWithConfig(
    baseUrl: String,
    noinline configure: Retrofit.Builder.() -> Retrofit.Builder = { this }
): T {
    return createServiceWithConfig(T::class.java, baseUrl, configure)
}