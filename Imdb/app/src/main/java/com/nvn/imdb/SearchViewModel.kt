package com.nvn.imdb

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class SearchViewModel : ViewModel() {
    private val _searchText = MutableStateFlow("spider")

    val searchText = _searchText

    fun setSearchQuery(query: String) {
        _searchText.value = query
    }

}