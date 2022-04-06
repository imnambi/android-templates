package com.nvn.imdb

import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import com.nvn.imdb.base.BaseActivity
import com.nvn.imdb.databinding.ActivityMainBinding
import com.nvn.imdb.model.MainToolBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {


    private val searchViewModel: SearchViewModel by viewModels()

    override fun setToolbar(toolbar: MainToolBar) {
        binding.topAppBar.setTitle(toolbar.title)
        if (toolbar.menuId != 0) {
            binding.topAppBar.inflateMenu(toolbar.menuId)
            val menu = binding.topAppBar.menu.findItem(R.id.search)
            if (menu != null) {
                val searchView = menu.actionView as SearchView
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        if (!query.isNullOrEmpty()) {
                            searchViewModel.setSearchQuery(query)
                        }
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        return false
                    }
                })
            }

        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

}