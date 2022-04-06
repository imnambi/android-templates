package com.nvn.imdb.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.load

object BindingAdapter {

    @JvmStatic
    @BindingAdapter("load_url")
    fun loadUrl(imageView: ImageView, url: String?) {
        url?.run {
            imageView.load(this)
        }
    }
}