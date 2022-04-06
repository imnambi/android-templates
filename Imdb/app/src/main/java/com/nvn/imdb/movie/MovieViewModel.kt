package com.nvn.imdb.movie

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nvn.imdb.data.MovieRepository
import com.nvn.imdb.data.model.Content
import com.nvn.imdb.data.model.MovieResponse
import com.nvn.imdb.data.remote.network.response.NetworkResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MovieViewModel"

@HiltViewModel
class MovieViewModel @Inject constructor() : ViewModel() {

    val queryText = MutableStateFlow("")
    val movieResponse = MutableStateFlow<MovieResponse?>(null)

    @Inject
    lateinit var repository: MovieRepository

    fun fetchMovieList() {
        viewModelScope.launch {
            queryText.collect { string ->
                if (string.isNotEmpty()) {
                    when (val response = repository.fetchMovieList(query = string)) {
                        is NetworkResponse.Success -> movieResponse.emit(response.body)
                        is NetworkResponse.ApiError -> Log.d(TAG, response.body.toString())
                        is NetworkResponse.NetworkError -> Log.d(TAG, "Network error")
                        is NetworkResponse.UnknownError -> Log.d(TAG, "Unknown error")
                    }
                }
            }
        }
    }


}