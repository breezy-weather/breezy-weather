package org.breezyweather.common.ui.adapters

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import okhttp3.internal.toImmutableList
import java.util.Collections

abstract class SyncListAdapter<T : Any, VH : RecyclerView.ViewHolder>(
    private var mModelList: List<T>,
    private val mCallback: DiffUtil.ItemCallback<T>
) : RecyclerView.Adapter<VH>() {
    open fun submitList(newList: List<T>) {
        if (newList === mModelList) {
            return
        }
        val oldList = currentList
        if (oldList.isEmpty() && newList.isEmpty()) {
            return
        }
        if (oldList.isEmpty()) {
            val insertedCount = newList.size
            mModelList = newList
            notifyItemRangeInserted(0, insertedCount)
            return
        }
        if (newList.isEmpty()) {
            val removedCount = oldList.size
            mModelList = newList
            notifyItemRangeRemoved(0, removedCount)
            return
        }
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return oldList.size
            }

            override fun getNewListSize(): Int {
                return newList.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return mCallback.areItemsTheSame(
                    oldList[oldItemPosition],
                    newList[newItemPosition]
                )
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return mCallback.areContentsTheSame(
                    oldList[oldItemPosition],
                    newList[newItemPosition]
                )
            }
        }, true)
        mModelList = newList
        result.dispatchUpdatesTo(this)
    }

    fun submitMove(from: Int, to: Int) {
        Collections.swap(mModelList, from, to)
        notifyItemMoved(from, to)
    }

    val currentList: List<T>
        get() = mModelList.toImmutableList()

    fun getItem(position: Int): T {
        return mModelList[position]
    }

    override fun getItemCount(): Int {
        return mModelList.size
    }
}
