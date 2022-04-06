package com.nvn.imdb.data.di

import com.nvn.imdb.data.remote.api.MovieService
import com.nvn.imdb.data.source.MovieSource
import com.nvn.imdb.data.source.MovieSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MovieModule {

    @Singleton
    @Provides
    fun provideMovieService(retrofit: Retrofit.Builder): MovieService {
        return retrofit.build().create(MovieService::class.java)
    }

    @Singleton
    @Provides
    fun provideMovieDataSource(movieService: MovieService): MovieSource {
        return MovieSourceImpl(movieService)
    }

}