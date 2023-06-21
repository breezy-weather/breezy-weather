package org.breezyweather.main.widgets;

import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.ui.widgets.slidingItem.SlidingItemTouchCallback;
import org.breezyweather.main.MainActivityViewModel;
import org.breezyweather.R;
import org.breezyweather.common.basic.GeoActivity;
import org.breezyweather.common.utils.helpers.SnackbarHelper;

public class LocationItemTouchCallback extends SlidingItemTouchCallback {

    private final GeoActivity mActivity;
    private final MainActivityViewModel mViewModel;

    private final @Px int mElevation;

    private boolean mDragged;
    private int mDragFrom;
    private int mDragTo;
    private final @NonNull TouchReactor mReactor;

    public interface TouchReactor {
        void resetViewHolderAt(int position);
        void reorderByDrag(int from, int to);
        void startSelectProviderActivityBySwipe();
    }

    public LocationItemTouchCallback(GeoActivity activity, MainActivityViewModel viewModel,
                                     @NonNull TouchReactor callback) {
        super();

        mActivity = activity;
        mViewModel = viewModel;

        mElevation = activity.getResources().getDimensionPixelSize(R.dimen.touch_rise_z);

        mDragged = false;
        mReactor = callback;
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        switch (actionState) {
            case ItemTouchHelper.ACTION_STATE_IDLE:
                if (mDragged) {
                    mDragged = false;
                    mViewModel.swapLocations(mDragFrom, mDragTo);
                }
                break;

            case ItemTouchHelper.ACTION_STATE_DRAG:
                if (!mDragged && viewHolder != null) {
                    mDragged = true;
                    mDragFrom = viewHolder.getBindingAdapterPosition();
                    mDragTo = mDragFrom;
                }
                break;
        }
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        mDragTo = target.getBindingAdapterPosition();
        mReactor.reorderByDrag(viewHolder.getBindingAdapterPosition(), mDragTo);
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getBindingAdapterPosition();
        Location location = mViewModel.getTotalLocationList().getValue().getFirst().get(position);

        switch (direction) {
            case ItemTouchHelper.START: {
                if (location.isCurrentPosition()) {
                    mReactor.startSelectProviderActivityBySwipe();
                    mReactor.resetViewHolderAt(position);
                } else {
                    location = Location.copy(
                            location,
                            location.isCurrentPosition(),
                            !location.isResidentPosition()
                    );
                    mViewModel.updateLocation(location);

                    if (location.isResidentPosition()) {
                        SnackbarHelper.showSnackbar(
                                mActivity.getString(R.string.location_resident_message),
                                mActivity.getString(R.string.action_learn_more),
                                v -> new MaterialAlertDialogBuilder(mActivity)
                                        .setTitle(R.string.location_resident)
                                        .setMessage(R.string.location_resident_dialog)
                                        .show()
                        );
                    }
                }
                break;
            }
            case ItemTouchHelper.END:
                if (mViewModel.getTotalLocationList().getValue().getFirst().size() <= 1) {
                    // TODO: force update.
                    mViewModel.updateLocation(location);
                    SnackbarHelper.showSnackbar(
                            mActivity.getString(R.string.location_message_list_cannot_be_empty)
                    );
                } else {
                    location = mViewModel.deleteLocation(position);
                    SnackbarHelper.showSnackbar(
                            mActivity.getString(R.string.location_message_deleted),
                            mActivity.getString(R.string.action_cancel),
                            new CancelDeleteListener(location, position)
                    );
                }
                break;
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        ViewCompat.setElevation(viewHolder.itemView,
                (dY != 0 || isCurrentlyActive) ? mElevation : 0);
    }

    // on click listener.

    private class CancelDeleteListener implements View.OnClickListener {

        private final Location mLocation;
        private final int mIndex;

        CancelDeleteListener(Location location, int index) {
            mLocation = location;
            mIndex = index;
        }

        @Override
        public void onClick(View view) {
            mViewModel.addLocation(mLocation, mIndex);
        }
    }
}