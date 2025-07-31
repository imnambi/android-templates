package com.nvn.imdb.data.di

import android.content.Context
import com.nvn.imdb.data.remote.connector.AuthenticationHandler
import com.nvn.imdb.data.remote.connector.CacheManager
import com.nvn.imdb.data.remote.connector.ConnectionManager
import com.nvn.imdb.data.remote.connector.ServiceConnector
import com.nvn.imdb.data.remote.connector.ServiceConnectorImpl
import com.nvn.imdb.data.remote.connector.ServiceFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Dependency injection module for service connector components
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceConnectorModule {
    
    /**
     * Bind ServiceConnectorImpl to ServiceConnector interface
     */
    @Binds
    @Singleton
    abstract fun bindServiceConnector(
        serviceConnectorImpl: ServiceConnectorImpl
    ): ServiceConnector
    
    companion object {
        
        /**
         * Provide ConnectionManager
         */
        @Provides
        @Singleton
        fun provideConnectionManager(
            @ApplicationContext context: Context
        ): ConnectionManager {
            return ConnectionManager(context)
        }
        
        /**
         * Provide CacheManager
         */
        @Provides
        @Singleton
        fun provideCacheManager(): CacheManager {
            return CacheManager()
        }
        
        /**
         * Provide AuthenticationHandler
         */
        @Provides
        @Singleton
        fun provideAuthenticationHandler(
            @ApplicationContext context: Context
        ): AuthenticationHandler {
            return AuthenticationHandler(context)
        }
        
        /**
         * Provide ServiceFactory
         */
        @Provides
        @Singleton
        fun provideServiceFactory(
            retrofitBuilder: Retrofit.Builder
        ): ServiceFactory {
            return ServiceFactory(retrofitBuilder)
        }
    }
}