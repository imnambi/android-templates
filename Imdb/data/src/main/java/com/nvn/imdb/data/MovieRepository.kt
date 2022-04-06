package com.nvn.imdb.data

import com.nvn.imdb.data.di.qualifiers.IODispatcher
import com.nvn.imdb.data.model.MovieResponse
import com.nvn.imdb.data.remote.network.response.NetworkResponse
import com.nvn.imdb.data.source.MovieSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject


class MovieRepository @Inject constructor(
    private val movieSource: MovieSource,
    @IODispatcher
    private val coroutineDispatcher: CoroutineDispatcher
) {

    suspend fun fetchMovieList(query: String): NetworkResponse<MovieResponse, Error> {
        return withContext(coroutineDispatcher) { movieSource.getMovieList(query) }
    }

}