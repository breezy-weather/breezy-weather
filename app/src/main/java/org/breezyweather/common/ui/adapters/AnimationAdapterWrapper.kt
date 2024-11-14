/**
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

package org.breezyweather.common.ui.adapters

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver

abstract class AnimationAdapterWrapper<A : RecyclerView.Adapter<VH>, VH : RecyclerView.ViewHolder>
@JvmOverloads constructor(
    adapter: A,
    firstOnly: Boolean = true,
) : RecyclerView.Adapter<VH>() {
    val wrappedAdapter: A
    private val mAnimatorSet: MutableMap<Int, Animator>
    private var mLastPosition: Int
    private var mFirstOnly: Boolean

    init {
        super.setHasStableIds(adapter.hasStableIds())
        wrappedAdapter = adapter
        mAnimatorSet = HashMap()
        mLastPosition = -1
        mFirstOnly = firstOnly
    }

    protected abstract fun getAnimator(view: View, pendingCount: Int): Animator?
    protected abstract fun setInitState(view: View)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return wrappedAdapter.onCreateViewHolder(parent, viewType)
    }

    override fun registerAdapterDataObserver(observer: AdapterDataObserver) {
        super.registerAdapterDataObserver(observer)
        wrappedAdapter.registerAdapterDataObserver(observer)
    }

    override fun unregisterAdapterDataObserver(observer: AdapterDataObserver) {
        super.unregisterAdapterDataObserver(observer)
        wrappedAdapter.unregisterAdapterDataObserver(observer)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        wrappedAdapter.onAttachedToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        wrappedAdapter.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onViewAttachedToWindow(holder: VH) {
        super.onViewAttachedToWindow(holder)
        wrappedAdapter.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: VH) {
        super.onViewDetachedFromWindow(holder)
        wrappedAdapter.onViewDetachedFromWindow(holder)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        wrappedAdapter.onBindViewHolder(holder, position)
        if (!mFirstOnly || position > mLastPosition) {
            clear(holder.itemView, position)
            val a = getAnimator(holder.itemView, mAnimatorSet.size)
            if (a != null) {
                setInitState(holder.itemView)
                a.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        mAnimatorSet.remove(position)
                    }
                })
                a.start()
                mAnimatorSet[position] = a
                mLastPosition = position
                return
            }
        }
        clear(holder.itemView, position)
    }

    private fun clear(view: View, position: Int) {
        val a = mAnimatorSet[position]
        if (a != null) {
            a.cancel()
            mAnimatorSet.remove(position)
        }
        view.apply {
            alpha = 1f
            rotation = 0f
            rotationX = 0f
            rotationY = 0f
            scaleX = 1f
            scaleY = 1f
            translationX = 0f
            translationY = 0f
            translationZ = 0f
        }
    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        wrappedAdapter.onViewRecycled(holder)
        clear(holder.itemView, holder.bindingAdapterPosition)
    }

    override fun getItemCount(): Int {
        return wrappedAdapter.itemCount
    }

    override fun getItemViewType(position: Int): Int {
        return wrappedAdapter.getItemViewType(position)
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(hasStableIds)
        wrappedAdapter.setHasStableIds(hasStableIds)
    }

    override fun getItemId(position: Int): Long {
        return wrappedAdapter.getItemId(position)
    }

    fun setLastPosition(lastPosition: Int) {
        mLastPosition = lastPosition
    }

    fun setFirstOnly(firstOnly: Boolean) {
        mFirstOnly = firstOnly
    }
}
