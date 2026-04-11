/*
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.ui.common.adapters

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.collections.immutable.toPersistentList
import java.util.Collections

abstract class SyncListAdapter<T : Any, VH : RecyclerView.ViewHolder>(
    private var mModelList: List<T>,
    private val mCallback: DiffUtil.ItemCallback<T>,
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
        val result = DiffUtil.calculateDiff(
            object : DiffUtil.Callback() {
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
            },
            true
        )
        mModelList = newList
        result.dispatchUpdatesTo(this)
    }

    fun submitMove(from: Int, to: Int) {
        Collections.swap(mModelList, from, to)
        notifyItemMoved(from, to)
    }

    val currentList: List<T>
        get() = mModelList.toPersistentList()

    fun getItem(position: Int): T {
        return mModelList[position]
    }

    override fun getItemCount(): Int {
        return mModelList.size
    }
}
