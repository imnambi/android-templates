package com.nvn.imdb.model

import androidx.annotation.MenuRes
import androidx.annotation.StringRes

data class MainToolBar(
    @StringRes val title: Int,
    @MenuRes val menuId: Int,
    val show: Boolean = true
)