package wangdaye.com.geometricweather.manage.adapter;

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
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.manage.ManageFragmentViewModel;
import wangdaye.com.geometricweather.ui.dialog.LearnMoreAboutResidentLocationDialog;
import wangdaye.com.geometricweather.ui.widget.slidingItem.SlidingItemTouchCallback;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.SnackbarUtils;

public class LocationItemTouchCallback extends SlidingItemTouchCallback {

    private final GeoActivity activity;
    private final ManageFragmentViewModel viewModel;

    private boolean dragged;
    private final @NonNull OnLocationListChangedListener listener;

    public LocationItemTouchCallback(GeoActivity activity, ManageFragmentViewModel viewModel,
                                     @NonNull OnLocationListChangedListener listener) {
        super();
        this.activity = activity;
        this.viewModel = viewModel;

        this.dragged = false;
        this.listener = listener;
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        switch (actionState) {
            case ItemTouchHelper.ACTION_STATE_IDLE:
                if (dragged) {
                    dragged = false;
                    viewModel.moveLocationFinish(activity);
                    listener.onLocationSequenceChanged(viewModel.getLocationList());
                }
                break;

            case ItemTouchHelper.ACTION_STATE_DRAG:
                dragged = true;
                break;
        }
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        viewModel.moveLocation(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        Location location = viewModel.getLocation(position);

        switch (direction) {
            case ItemTouchHelper.START: {
                if (viewModel.getLocation(position).isCurrentPosition()) {
                    viewModel.forceUpdateLocation(activity, location, position);
                    listener.onSelectProviderActivityStarted();
                } else {
                    location = new Location(
                            location, location.isCurrentPosition(), !location.isResidentPosition());
                    viewModel.forceUpdateLocation(activity, location, position);
                    listener.onLocationChanged(viewModel.getLocationList(), location);

                    if (location.isResidentPosition()) {
                        SnackbarUtils.showSnackbar(
                                activity,
                                activity.getString(R.string.feedback_resident_location),
                                activity.getString(R.string.learn_more),
                                v -> new LearnMoreAboutResidentLocationDialog().show(
                                        activity.getSupportFragmentManager(), null)
                        );
                    }
                }
                break;
            }
            case ItemTouchHelper.END:
                if (viewModel.getLocationCount() <= 1) {
                    viewModel.forceUpdateLocation(activity, location, position);
                    SnackbarUtils.showSnackbar(
                            activity,
                            activity.getString(R.string.feedback_location_list_cannot_be_null)
                    );
                } else {
                    location = viewModel.deleteLocation(activity, position);
                    SnackbarUtils.showSnackbar(
                            activity,
                            activity.getString(R.string.feedback_delete_succeed),
                            activity.getString(R.string.cancel),
                            new CancelDeleteListener(location, position)
                    );

                    listener.onLocationRemoved(viewModel.getLocationList(), location);
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
            viewHolder.itemView.setElevation(DisplayUtils.dpToPx(activity, dY == 0 ? 0 : 10));
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

        private final Location location;
        private final int index;

        CancelDeleteListener(Location location, int index) {
            this.location = location;
            this.index = index;
        }

        @Override
        public void onClick(View view) {
            viewModel.addLocation(activity, location, index);
            listener.onLocationInserted(viewModel.getLocationList(), location);
        }
    }
}