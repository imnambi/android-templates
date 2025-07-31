package com.nvn.imdb.data.remote.connector

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages in-memory caching with TTL (Time To Live) support
 */
@Singleton
class CacheManager @Inject constructor() {
    
    private val cache = mutableMapOf<String, CacheEntry<*>>()
    private val mutex = Mutex()
    
    /**
     * Store data in cache with TTL
     */
    suspend fun <T> put(key: String, data: T, ttlMillis: Long = 300_000L) {
        mutex.withLock {
            val expiryTime = System.currentTimeMillis() + ttlMillis
            cache[key] = CacheEntry(data, expiryTime)
        }
    }
    
    /**
     * Get data from cache if not expired
     */
    suspend fun <T> get(key: String): T? {
        mutex.withLock {
            val entry = cache[key] ?: return null
            
            if (System.currentTimeMillis() > entry.expiryTime) {
                cache.remove(key)
                return null
            }
            
            @Suppress("UNCHECKED_CAST")
            return entry.data as? T
        }
    }
    
    /**
     * Check if cache contains valid (non-expired) entry for key
     */
    suspend fun contains(key: String): Boolean {
        mutex.withLock {
            val entry = cache[key] ?: return false
            
            if (System.currentTimeMillis() > entry.expiryTime) {
                cache.remove(key)
                return false
            }
            
            return true
        }
    }
    
    /**
     * Remove specific cache entry
     */
    suspend fun remove(key: String) {
        mutex.withLock {
            cache.remove(key)
        }
    }
    
    /**
     * Clear all cache entries or entries matching a pattern
     */
    suspend fun clear(keyPattern: String? = null) {
        mutex.withLock {
            if (keyPattern == null) {
                cache.clear()
            } else {
                val keysToRemove = cache.keys.filter { it.contains(keyPattern, ignoreCase = true) }
                keysToRemove.forEach { cache.remove(it) }
            }
        }
    }
    
    /**
     * Clean up expired entries
     */
    suspend fun cleanupExpired() {
        mutex.withLock {
            val currentTime = System.currentTimeMillis()
            val expiredKeys = cache.entries
                .filter { currentTime > it.value.expiryTime }
                .map { it.key }
            
            expiredKeys.forEach { cache.remove(it) }
        }
    }
    
    /**
     * Get cache statistics
     */
    suspend fun getStats(): CacheStats {
        mutex.withLock {
            val currentTime = System.currentTimeMillis()
            val totalEntries = cache.size
            val expiredEntries = cache.values.count { currentTime > it.expiryTime }
            val validEntries = totalEntries - expiredEntries
            
            return CacheStats(
                totalEntries = totalEntries,
                validEntries = validEntries,
                expiredEntries = expiredEntries
            )
        }
    }
}

/**
 * Represents a cache entry with expiry time
 */
private data class CacheEntry<T>(
    val data: T,
    val expiryTime: Long
)

/**
 * Cache statistics
 */
data class CacheStats(
    val totalEntries: Int,
    val validEntries: Int,
    val expiredEntries: Int
)