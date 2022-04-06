package com.nvn.imdb.movie

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nvn.imdb.base.BaseRecyclerViewListAdapter
import com.nvn.imdb.data.model.Content
import com.nvn.imdb.databinding.PartialItemMovieListBinding
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class MovieAdapter @Inject constructor(@ActivityContext val context: Context) :
    BaseRecyclerViewListAdapter<MovieAdapter.MovieViewHolder, Content, MutableList<Content>>(context) {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
        mInflater: LayoutInflater
    ): MovieViewHolder {
        val binding = PartialItemMovieListBinding.inflate(mInflater, parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val item = getItem(position)
        item?.let {
            holder.bind(it)
        }
    }


    class MovieViewHolder(private val item: PartialItemMovieListBinding) :
        RecyclerView.ViewHolder(item.root) {
        fun bind(content: Content) {
            item.model = content
        }
    }

    inner class DiffUtilCallbackImpl(
        private val prevContents: MutableList<Content>,
        private val newContents: MutableList<Content>
    ) :
        DiffUtilCallback(prevContents, newContents) {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return prevContents[oldItemPosition].imdbID == newContents[newItemPosition].imdbID
        }
    }

}