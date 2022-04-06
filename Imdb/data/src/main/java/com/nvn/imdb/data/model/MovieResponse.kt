package com.nvn.imdb.data.model

data class MovieResponse(
    val Search: List<Content>?,
    val totalResults: String,
    val Response: String,
    val Error: String
)

data class Content(
    val Title: String,
    val Year: String,
    val Type: String,
    val Poster: String,
    val imdbID: String
)

data class Error(
    val message: String
)