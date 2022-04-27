package wangdaye.com.geometricweather.main.widgets;

import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.ui.widgets.slidingItem.SlidingItemTouchCallback;
import wangdaye.com.geometricweather.common.utils.helpers.SnackbarHelper;
import wangdaye.com.geometricweather.main.MainActivityViewModel;

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
                    mViewModel.moveLocation(mDragFrom, mDragTo);
                }
                break;

            case ItemTouchHelper.ACTION_STATE_DRAG:
                if (!mDragged && viewHolder != null) {
                    mDragged = true;
                    mDragFrom = viewHolder.getAdapterPosition();
                    mDragTo = mDragFrom;
                }
                break;
        }
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        mDragTo = target.getAdapterPosition();
        mReactor.reorderByDrag(viewHolder.getAdapterPosition(), mDragTo);
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        Location location = mViewModel.getTotalLocationList().getValue().getLocationList().get(position);

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
                                mActivity.getString(R.string.feedback_resident_location),
                                mActivity.getString(R.string.learn_more),
                                v -> new MaterialAlertDialogBuilder(mActivity)
                                        .setTitle(R.string.resident_location)
                                        .setMessage(R.string.feedback_resident_location_description)
                                        .show()
                        );
                    }
                }
                break;
            }
            case ItemTouchHelper.END:
                if (mViewModel.getTotalLocationList().getValue().getLocationList().size() <= 1) {
                    // TODO: force update.
                    mViewModel.updateLocation(location);
                    SnackbarHelper.showSnackbar(
                            mActivity.getString(R.string.feedback_location_list_cannot_be_null)
                    );
                } else {
                    location = mViewModel.deleteLocation(position);
                    SnackbarHelper.showSnackbar(
                            mActivity.getString(R.string.feedback_delete_succeed),
                            mActivity.getString(R.string.cancel),
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