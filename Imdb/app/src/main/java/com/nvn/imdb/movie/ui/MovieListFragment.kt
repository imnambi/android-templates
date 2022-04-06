package com.nvn.imdb.movie.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nvn.imdb.R
import com.nvn.imdb.SearchViewModel
import com.nvn.imdb.base.BaseActivity
import com.nvn.imdb.base.BaseFragment
import com.nvn.imdb.databinding.FragmentHomeBinding
import com.nvn.imdb.model.MainToolBar
import com.nvn.imdb.movie.MovieAdapter
import com.nvn.imdb.movie.MovieViewModel
import com.nvn.imdb.util.Extensions.autoFitColumns
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MovieListFragment"

@AndroidEntryPoint
class MovieListFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    @Inject
    lateinit var adapter: MovieAdapter

    private val movieViewModel: MovieViewModel by viewModels()

    private val searchViewModel: SearchViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val baseActivity = activity as BaseActivity<*>
        baseActivity.setToolbar(MainToolBar(R.string.movie_list_title, R.menu.home_menu))

        initRecyclerView()

        movieViewModel.fetchMovieList()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchViewModel.searchText.collect {
                    movieViewModel.queryText.value = it
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                movieViewModel.movieResponse.collect {
                    it?.let { movieResponse ->
                        movieResponse.Search?.let { contents ->
                            if (contents.isNotEmpty()) {
                                adapter.updateDataSet(
                                    adapter.DiffUtilCallbackImpl(
                                        adapter.dataSet,
                                        contents.toMutableList()
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

    }

    private fun initRecyclerView() {
        adapter.setDataSet(mutableListOf())
        binding?.rvFragHomeMovieList?.autoFitColumns(
            resources.getDimension(R.dimen.movie_poster_width).toInt()
        )
        binding?.rvFragHomeMovieList?.adapter = adapter
    }

}