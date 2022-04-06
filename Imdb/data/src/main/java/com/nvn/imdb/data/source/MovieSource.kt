package com.nvn.imdb.data.source

import com.nvn.imdb.data.model.Content
import com.nvn.imdb.data.model.MovieResponse
import com.nvn.imdb.data.remote.network.response.NetworkResponse
import kotlinx.coroutines.flow.Flow

interface MovieSource {
    suspend fun getMovieList(query: String): NetworkResponse<MovieResponse, Error>
    suspend fun getDetailMovie(id: String): NetworkResponse<Content, Error>
}