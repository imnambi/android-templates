package com.nvn.imdb.data.remote.api

import com.nvn.imdb.data.model.Content
import com.nvn.imdb.data.model.MovieResponse
import com.nvn.imdb.data.remote.network.response.NetworkResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MovieService {

    @GET("?apikey=36f1157f")
    suspend fun getMovieList(@Query("s") query: String): NetworkResponse<MovieResponse, Error>

    @GET("?apikey=36f1157f")
    suspend fun getMovieDetail(@Query("i") query: String): NetworkResponse<Content, Error>

}