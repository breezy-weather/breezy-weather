package wangdaye.com.geometricweather.ui.adapter.location;

import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.ui.dialog.LearnMoreAboutResidentLocationDialog;
import wangdaye.com.geometricweather.utils.SnackbarUtils;

public class LocationTouchCallback extends ItemTouchHelper.SimpleCallback {

    private GeoActivity activity;
    private LocationAdapter adapter;
    private @Nullable OnLocationListChangedListener listener;

    public LocationTouchCallback(GeoActivity activity, LocationAdapter adapter, int dragDirs, int swipeDirs,
                                 @Nullable OnLocationListChangedListener l) {
        super(dragDirs, swipeDirs);
        this.activity = activity;
        this.adapter = adapter;
        this.listener = l;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        List<Location> list = adapter.moveItem(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        if (listener != null) {
            listener.onLocationSequenceChanged(list);
        }
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        switch (direction) {
            case ItemTouchHelper.START: {
                Location location = ((LocationHolder) viewHolder).model.location;
                if (location.isCurrentPosition()) {
                    adapter.update(
                            adapter.getLocationList(),
                            location.getFormattedId()
                    );
                    if (listener != null) {
                        listener.onSelectProviderActivityStarted();
                    }
                } else {
                    List<Location> list = adapter.getLocationList();
                    location = list.get(viewHolder.getAdapterPosition());
                    location.setResidentPosition(!location.isResidentPosition());
                    adapter.update(list, location.getFormattedId());
                    if (location.isResidentPosition()) {
                        SnackbarUtils.showSnackbar(
                                activity,
                                activity.getString(R.string.feedback_resident_location),
                                activity.getString(R.string.learn_more),
                                v -> new LearnMoreAboutResidentLocationDialog().show(
                                        activity.getSupportFragmentManager(), null)
                        );
                    }

                    if (listener != null) {
                        listener.onLocationChanged(list, location);
                    }
                }
                break;
            }
            case ItemTouchHelper.END:
                if (adapter.getItemCount() <= 1) {
                    List<Location> list = adapter.getLocationList();
                    Location location = list.get(viewHolder.getAdapterPosition());
                    adapter.update(list, location.getFormattedId());
                    SnackbarUtils.showSnackbar(
                            activity, activity.getString(R.string.feedback_location_list_cannot_be_null));
                } else {
                    List<Location> list = adapter.getLocationList();
                    Location location = list.remove(viewHolder.getAdapterPosition());
                    location.setWeather(DatabaseHelper.getInstance(activity).readWeather(location));

                    adapter.update(list, location.getFormattedId());
                    SnackbarUtils.showSnackbar(
                            activity,
                            activity.getString(R.string.feedback_delete_succeed),
                            activity.getString(R.string.cancel),
                            new CancelDeleteListener(location)
                    );

                    if (listener != null) {
                        listener.onLocationRemoved(list, location);
                    }
                }
                break;
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        switch (actionState) {
            case ItemTouchHelper.ACTION_STATE_SWIPE:
                ((LocationHolder) viewHolder).drawSwipe(activity, dX);
                break;

            case ItemTouchHelper.ACTION_STATE_DRAG:
                ((LocationHolder) viewHolder).drawDrag(activity, dY != 0);
                break;

            case ItemTouchHelper.ACTION_STATE_IDLE:
                break;
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

        private Location location;

        CancelDeleteListener(Location l) {
            this.location = l;
        }

        @Override
        public void onClick(View view) {
            List<Location> list = adapter.getLocationList();
            list.add(location);
            adapter.update(list);

            if (listener != null) {
                listener.onLocationInserted(list, location);
            }
        }
    }
}