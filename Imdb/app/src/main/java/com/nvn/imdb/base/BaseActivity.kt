package com.nvn.imdb.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.nvn.imdb.model.MainToolBar

typealias Inflater<T> = (LayoutInflater) -> T

abstract class BaseActivity<T : ViewDataBinding>(
    private val inflater: Inflater<T>
) : AppCompatActivity() {

    private lateinit var _binding: T

    val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DataBindingUtil.setContentView<T>(this, getLayoutId())
    }

    abstract fun setToolbar(toolbar:MainToolBar)

    abstract fun getLayoutId(): Int
}