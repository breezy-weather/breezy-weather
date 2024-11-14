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

package org.breezyweather.main.widgets

import android.graphics.Canvas
import android.view.View
import androidx.annotation.Px
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.ui.widgets.slidingItem.SlidingItemTouchCallback
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.main.MainActivityViewModel

class LocationItemTouchCallback(
    private val mActivity: GeoActivity,
    private val mViewModel: MainActivityViewModel,
    private val mReactor: TouchReactor,
) : SlidingItemTouchCallback() {
    @Px
    private val mElevation: Int = mActivity.resources.getDimensionPixelSize(R.dimen.touch_rise_z)
    private var mDragged = false
    private var mDragFrom = 0
    private var mDragTo = 0

    interface TouchReactor {
        fun resetViewHolderAt(position: Int)
        fun reorderByDrag(from: Int, to: Int)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_IDLE -> if (mDragged) {
                mDragged = false
                mViewModel.swapLocations(mDragFrom, mDragTo)
            }

            ItemTouchHelper.ACTION_STATE_DRAG -> if (!mDragged && viewHolder != null) {
                mDragged = true
                mDragFrom = viewHolder.bindingAdapterPosition
                mDragTo = mDragFrom
            }
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        mDragTo = target.bindingAdapterPosition
        mReactor.reorderByDrag(viewHolder.bindingAdapterPosition, mDragTo)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition
        var location = mViewModel.validLocationList.value[position]
        when (direction) {
            ItemTouchHelper.START -> {
                viewHolder.bindingAdapter!!.notifyItemChanged(position)
                mViewModel.openChooseWeatherSourcesDialog(location)
            }

            ItemTouchHelper.END -> if (mViewModel.validLocationList.value.size <= 1) {
                viewHolder.bindingAdapter!!.notifyItemChanged(position)
                SnackbarHelper.showSnackbar(
                    mActivity.getString(R.string.location_message_list_cannot_be_empty)
                )
            } else {
                location = mViewModel.deleteLocation(position)
                SnackbarHelper.showSnackbar(
                    content = mActivity.getString(R.string.location_message_deleted),
                    action = mActivity.getString(R.string.action_undo),
                    listener = CancelDeleteListener(location, position)
                )
            }
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean,
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        ViewCompat.setElevation(
            viewHolder.itemView,
            (if (dY != 0f || isCurrentlyActive) mElevation else 0).toFloat()
        )
    }

    // on click listener.
    private inner class CancelDeleteListener(
        private val mLocation: Location,
        private val mIndex: Int,
    ) : View.OnClickListener {
        override fun onClick(view: View) {
            mViewModel.addLocation(mLocation, mIndex)
        }
    }
}
