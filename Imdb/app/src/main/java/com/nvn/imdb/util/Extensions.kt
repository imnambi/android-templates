package com.nvn.imdb.util

import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

object Extensions {
    fun RecyclerView.autoFitColumns(columnWidth: Int) {
        val displayMetrics = this.context.resources.displayMetrics
        val noOfColumns = ((displayMetrics.widthPixels) / columnWidth)
        this.layoutManager = GridLayoutManager(this.context, noOfColumns)
    }
}