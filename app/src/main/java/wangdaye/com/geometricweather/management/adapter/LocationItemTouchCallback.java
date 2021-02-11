package wangdaye.com.geometricweather.management.adapter;

import android.graphics.Canvas;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.models.Location;
import wangdaye.com.geometricweather.management.ManagementFragmentViewModel;
import wangdaye.com.geometricweather.ui.dialogs.LearnMoreAboutResidentLocationDialog;
import wangdaye.com.geometricweather.ui.widgets.slidingItem.SlidingItemTouchCallback;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.helpters.SnackbarHelper;

public class LocationItemTouchCallback extends SlidingItemTouchCallback {

    private final GeoActivity mActivity;
    private final ManagementFragmentViewModel mViewModel;

    private boolean mDragged;
    private final @NonNull OnLocationListChangedListener mListener;

    public LocationItemTouchCallback(GeoActivity activity, ManagementFragmentViewModel viewModel,
                                     @NonNull OnLocationListChangedListener listener) {
        super();
        mActivity = activity;
        mViewModel = viewModel;
        mDragged = false;
        mListener = listener;
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        switch (actionState) {
            case ItemTouchHelper.ACTION_STATE_IDLE:
                if (mDragged) {
                    mDragged = false;
                    mViewModel.moveLocationFinish();
                    mListener.onLocationSequenceChanged(mViewModel.getLocationList());
                }
                break;

            case ItemTouchHelper.ACTION_STATE_DRAG:
                mDragged = true;
                break;
        }
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        mViewModel.moveLocation(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        Location location = mViewModel.getLocation(position);

        switch (direction) {
            case ItemTouchHelper.START: {
                if (mViewModel.getLocation(position).isCurrentPosition()) {
                    mViewModel.forceUpdateLocation(location, position);
                    mListener.onSelectProviderActivityStarted();
                } else {
                    location = new Location(
                            location, location.isCurrentPosition(), !location.isResidentPosition());
                    mViewModel.forceUpdateLocation(location, position);
                    mListener.onLocationChanged(mViewModel.getLocationList(), location);

                    if (location.isResidentPosition()) {
                        SnackbarHelper.showSnackbar(
                                mActivity.getString(R.string.feedback_resident_location),
                                mActivity.getString(R.string.learn_more),
                                v -> new LearnMoreAboutResidentLocationDialog().show(
                                        mActivity.getSupportFragmentManager(), null)
                        );
                    }
                }
                break;
            }
            case ItemTouchHelper.END:
                if (mViewModel.getLocationCount() <= 1) {
                    mViewModel.forceUpdateLocation(location, position);
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

                    mListener.onLocationRemoved(mViewModel.getLocationList(), location);
                }
                break;
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            viewHolder.itemView.setElevation(DisplayUtils.dpToPx(mActivity, dY == 0 ? 0 : 10));
        }
    }

    public interface OnLocationListChangedListener {
        void onLocationSequenceChanged(List<Location> locationList);
        void onLocationInserted(List<Location> locationList, Location location);
        void onLocationRemoved(List<Location> locationList, Location location);
        void onLocationChanged(List<Location> locationList, Location location);
        void onSelectProviderActivityStarted();
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
            mListener.onLocationInserted(mViewModel.getLocationList(), mLocation);
        }
    }
}