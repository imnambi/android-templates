package com.nvn.imdb.data.remote.connector

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles authentication for API requests including token management and refresh
 */
@Singleton
class AuthenticationHandler @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "AuthenticationHandler"
        private const val PREFS_NAME = "secure_auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
        private const val KEY_API_KEY = "api_key"
    }
    
    private val mutex = Mutex()
    private val encryptedPrefs: SharedPreferences by lazy {
        createEncryptedPreferences()
    }
    
    /**
     * Create encrypted shared preferences for secure token storage
     */
    private fun createEncryptedPreferences(): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create encrypted preferences, falling back to regular", e)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }
    
    /**
     * Store access token securely
     */
    suspend fun storeAccessToken(token: String, expiryTimeMillis: Long = 0L) {
        mutex.withLock {
            encryptedPrefs.edit()
                .putString(KEY_ACCESS_TOKEN, token)
                .putLong(KEY_TOKEN_EXPIRY, expiryTimeMillis)
                .apply()
        }
        Log.d(TAG, "Access token stored securely")
    }
    
    /**
     * Store refresh token securely
     */
    suspend fun storeRefreshToken(token: String) {
        mutex.withLock {
            encryptedPrefs.edit()
                .putString(KEY_REFRESH_TOKEN, token)
                .apply()
        }
        Log.d(TAG, "Refresh token stored securely")
    }
    
    /**
     * Store API key securely
     */
    suspend fun storeApiKey(apiKey: String) {
        mutex.withLock {
            encryptedPrefs.edit()
                .putString(KEY_API_KEY, apiKey)
                .apply()
        }
        Log.d(TAG, "API key stored securely")
    }
    
    /**
     * Get current access token
     */
    suspend fun getAccessToken(): String? {
        return mutex.withLock {
            encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
        }
    }
    
    /**
     * Get current refresh token
     */
    suspend fun getRefreshToken(): String? {
        return mutex.withLock {
            encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
        }
    }
    
    /**
     * Get API key
     */
    suspend fun getApiKey(): String? {
        return mutex.withLock {
            encryptedPrefs.getString(KEY_API_KEY, null)
        }
    }
    
    /**
     * Check if access token is expired
     */
    suspend fun isTokenExpired(): Boolean {
        return mutex.withLock {
            val expiryTime = encryptedPrefs.getLong(KEY_TOKEN_EXPIRY, 0L)
            if (expiryTime == 0L) return@withLock false // No expiry set
            System.currentTimeMillis() >= expiryTime
        }
    }
    
    /**
     * Clear all stored tokens
     */
    suspend fun clearTokens() {
        mutex.withLock {
            encryptedPrefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_TOKEN_EXPIRY)
                .remove(KEY_API_KEY)
                .apply()
        }
        Log.d(TAG, "All tokens cleared")
    }
    
    /**
     * Check if user is authenticated
     */
    suspend fun isAuthenticated(): Boolean {
        val token = getAccessToken()
        return !token.isNullOrEmpty() && !isTokenExpired()
    }
    
    /**
     * Get authentication interceptor for OkHttp
     */
    fun getAuthInterceptor(): Interceptor {
        return AuthInterceptor()
    }
    
    /**
     * OkHttp interceptor that adds authentication headers
     */
    private inner class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            
            // Get stored tokens (blocking call for interceptor)
            val accessToken = encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
            val apiKey = encryptedPrefs.getString(KEY_API_KEY, null)
            
            val authenticatedRequest = originalRequest.newBuilder().apply {
                // Add Bearer token if available
                if (!accessToken.isNullOrEmpty()) {
                    addHeader("Authorization", "Bearer $accessToken")
                }
                
                // Add API key if available (common for many APIs)
                if (!apiKey.isNullOrEmpty()) {
                    // You can customize this based on your API requirements
                    addHeader("X-API-Key", apiKey)
                    // Or add as query parameter
                    // url(originalRequest.url.newBuilder().addQueryParameter("apikey", apiKey).build())
                }
                
                // Add common headers
                addHeader("Accept", "application/json")
                addHeader("Content-Type", "application/json")
            }.build()
            
            return chain.proceed(authenticatedRequest)
        }
    }
}

/**
 * Authentication configuration data class
 */
data class AuthConfig(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val apiKey: String? = null,
    val tokenExpiryMillis: Long = 0L
)

/**
 * Extension function to apply authentication to a request
 */
fun Request.Builder.addAuthentication(
    accessToken: String? = null,
    apiKey: String? = null
): Request.Builder {
    if (!accessToken.isNullOrEmpty()) {
        addHeader("Authorization", "Bearer $accessToken")
    }
    if (!apiKey.isNullOrEmpty()) {
        addHeader("X-API-Key", apiKey)
    }
    return this
}