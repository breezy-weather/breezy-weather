package org.breezyweather.main.widgets

import android.graphics.Canvas
import android.view.View
import androidx.annotation.Px
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.ui.widgets.slidingItem.SlidingItemTouchCallback
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.main.MainActivityViewModel

class LocationItemTouchCallback(
    private val mActivity: GeoActivity, private val mViewModel: MainActivityViewModel,
    private val mReactor: TouchReactor
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
        target: RecyclerView.ViewHolder
    ): Boolean {
        mDragTo = target.bindingAdapterPosition
        mReactor.reorderByDrag(viewHolder.bindingAdapterPosition, mDragTo)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition
        var location = mViewModel.totalLocationList.value.first[position]
        when (direction) {
            ItemTouchHelper.START -> {
                if (location.isCurrentPosition) {
                    mViewModel.openChooseCurrentLocationWeatherSourceDialog()
                } else {
                    location = location.copy(isResidentPosition = !location.isResidentPosition)
                    mViewModel.updateLocation(location)
                    if (location.isResidentPosition) {
                        SnackbarHelper.showSnackbar(
                            content = mActivity.getString(R.string.location_resident_message),
                            action = mActivity.getString(R.string.action_learn_more)
                        ) {
                            MaterialAlertDialogBuilder(mActivity)
                                .setTitle(R.string.location_resident)
                                .setMessage(R.string.location_resident_dialog)
                                .show()
                        }
                    }
                }
            }

            ItemTouchHelper.END -> if (mViewModel.totalLocationList.value.first.size <= 1) {
                // TODO: force update.
                mViewModel.updateLocation(location)
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
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
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
        private val mIndex: Int
    ) : View.OnClickListener {
        override fun onClick(view: View) {
            mViewModel.addLocation(mLocation, mIndex)
        }
    }
}