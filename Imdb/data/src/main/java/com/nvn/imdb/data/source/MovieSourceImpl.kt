package com.nvn.imdb.data.source

import com.nvn.imdb.data.remote.api.MovieService
import com.nvn.imdb.data.model.Content
import com.nvn.imdb.data.model.MovieResponse
import com.nvn.imdb.data.remote.network.response.NetworkResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MovieSourceImpl @Inject constructor(private val movieService: MovieService) : MovieSource {
    override suspend fun getMovieList(query: String): NetworkResponse<MovieResponse, Error> {
        return movieService.getMovieList(query)
    }

    override suspend fun getDetailMovie(id: String): NetworkResponse<Content, Error> {
        return movieService.getMovieDetail(id)
    }
}