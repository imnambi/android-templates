package com.nvn.imdb.base

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLongClickListener
import android.view.View.OnTouchListener
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

/**
 * Base RecyclerView adapter, takes care of all the common routine needed to when constructing RecyclerAdapter.
 *
 *
 * The common routine includes.
 * - Maintaining the data-set,
 * - Caching of [LayoutInflater],
 * - Provides Efficient click delegation,
 * - etc.
 *<? extends Object>
 *
 * Created by Sridhar on 02-11-2019 at 06:54 PM.
 *
 * @param <V> Type of View Holder.
 * @param <M> Type of Model object used in the data-set.
 * @param <D> Type of data-set must be an child of [List].
</D></M></V> */
abstract class BaseRecyclerViewListAdapter<V : RecyclerView.ViewHolder, M, D : MutableList<M>>(
    context: Context
) : RecyclerView.Adapter<V>() {
    /**
     * Return the data-set, can be null if no data-set reference is assgined via [.setDataSet] so far.
     *
     * @return The data-set reference.
     */
    /**
     * Data-set to populate on the recycler view.
     */
    var dataSet: MutableList<M> = mutableListOf()
        private set

    /**
     * This instance will get a valid reference to inflater object at the time of constructing this class.
     * This variable is made global just because it is used every the time when a new view has to
     * be brought in to the screen.
     *
     *
     * This instance is passed as one of the arguments in onCreateViewHolder method, to inflate new views.
     */
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    /**
     * Accessor method to get the ItemViewClickDelegate instance.
     *
     * @return instance of ItemViewClickDelegate.
     */
    /**
     * ItemViewClickDelegate instance to delegate the various click events to the caller of
     * [.setItemViewClickDelegate].
     */
    protected var itemViewClickDelegate: ItemViewClickDelegate<M>?
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): V {
        return onCreateViewHolder(parent, viewType, mInflater)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    /**
     * This version of onCreateViewHolder method provide the inflater instance as the additional parameter, which
     * will be handy to inflate the view.
     *
     * @param parent    RootView of the layout.
     * @param viewType  View type.
     * @param mInflater Inflater instance.
     * @return ViewHolder instance.
     */
    protected abstract fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
        mInflater: LayoutInflater
    ): V

    /**
     * Callback instance to notify when the view is clicked.
     *
     * @param mItemViewClickDelegate callback instance.
     */
    fun setItemViewClickDelegate(mItemViewClickDelegate: ItemViewClickDelegate<M>) {
        itemViewClickDelegate = mItemViewClickDelegate
    }

    /**
     * Method to set the reference to mDataSet object.
     *
     * @param mDataSet data-set reference.
     */
    @UiThread
    fun setDataSet(mDataSet: D) {
        dataSet = mDataSet
        notifyDataSetChanged()
    }

    @MainThread
    fun updateDataSet( diffUtilCallback: DiffUtilCallback) {
        val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
        dataSet.clear()
        dataSet.addAll(diffUtilCallback.getNewList())
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Clear the entire data-set.
     */
    fun clearDataSet() {
        dataSet.let {
            it.clear()
            notifyDataSetChanged()
        }
    }

    /**
     * Try to retrieve the item at `pos`.
     *
     * @param pos The position to retrieve the item.
     * @return The object at pos upon successful, null on error scenarios.
     */
    fun getItem(pos: Int): M? {
        return dataSet.let {
            if (pos >= 0 && it.size > pos) it[pos] else null
        }
    }

    /**
     * Callback Interface, the click events are delivered to this instance.
     */
    interface ItemViewClickDelegate<M> {
        /**
         * This method called when the recycler item view is clicked.
         *
         * @param viewClicked     View object that is clicked.
         * @param adapterPosition Corresponding adapter position of the clicked view.
         * @param item            Corresponding model associated with the `adapterPosition`.
         */
        fun onItemViewClick(viewClicked: View?, adapterPosition: Int, item: M?)

        /**
         * This method called when the recycler item view is long-clicked.
         *
         * @param viewClicked     View object that is clicked.
         * @param adapterPosition Corresponding adapter position of the clicked view.
         * @param item            Corresponding model associated with the `adapterPosition`.
         */
        fun onItemViewLongClick(viewClicked: View?, adapterPosition: Int, item: M?): Boolean {
            return false
        }

        /**
         * This method called when the recycler item view is long-clicked.
         *
         * @param viewClicked     View object that is clicked.
         * @param event       MotionEvent object that is clicked.
         * @param adapterPosition Corresponding adapter position of the clicked view.
         * @param item            Corresponding model associated with the `adapterPosition`.
         */
        fun onItemViewTouched(
            viewClicked: View,
            event: MotionEvent,
            adapterPosition: Int,
            item: M?
        ): Boolean {
            return false
        }
    }

    /**
     * Helper class to map and delegate the click events with its corresponding adapter position
     * to the registered callback instance [.setItemViewClickDelegate].
     */
    inner class SimpleViewClickListener
    /**
     * Keeping the view holder reference, to query the corresponding view position when the view is clicked.
     *
     * @param viewHolder Base reference to the view holder object.
     */(
        /**
         * Base reference to the view holder object.
         */
        var viewHolder: RecyclerView.ViewHolder
    ) : View.OnClickListener, OnLongClickListener, OnTouchListener {
        override fun onClick(v: View) {
            itemViewClickDelegate?.let {
                val position = viewHolder.adapterPosition
                it.onItemViewClick(v, position, getItem(position))
            }
        }

        override fun onLongClick(v: View): Boolean {
            val isItemViewLongPressed = itemViewClickDelegate?.let {
                val pos = viewHolder.adapterPosition
                it.onItemViewLongClick(
                    v,
                    pos,
                    getItem(pos)
                )
            }
            return isItemViewLongPressed != null && isItemViewLongPressed
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val isItemViewTouched = itemViewClickDelegate?.let {
                val pos = viewHolder.adapterPosition
                it.onItemViewTouched(v, event, pos, getItem(pos))
            }
            return isItemViewTouched != null && isItemViewTouched
        }
    }

    abstract inner class DiffUtilCallback(
        private val oldList: D?,
        private val newList: D
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList?.size ?: 0
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList?.get(oldItemPosition) == newList[newItemPosition]
        }

        fun getNewList(): D {
            return newList
        }
    }


    /**
     * The context object is mainly used to initialize the Layout inflater, since we need layout inflater in every
     * single onCreateViewHolder method call, its effective to instantiate the inflater at the time of constructing
     * this adapter.
     *
     * @param context Context from which the adapter class is invoked.
     */
    init {
        itemViewClickDelegate = null
    }
}