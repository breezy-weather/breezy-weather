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
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.ui.dialog.LearnMoreAboutResidentLocationDialog;
import wangdaye.com.geometricweather.ui.widget.slidingItem.SlidingItemTouchCallback;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.SnackbarUtils;

public class LocationItemTouchCallback extends SlidingItemTouchCallback {

    private final GeoActivity activity;
    private final LocationAdapter adapter;

    @NonNull
    private final OnLocationListChangedListener listener;
    @NonNull
    private final SelectedIdGetter getter;

    public LocationItemTouchCallback(GeoActivity activity, LocationAdapter adapter,
                                     @NonNull OnLocationListChangedListener listener,
                                     @NonNull SelectedIdGetter getter) {
        super();
        this.activity = activity;
        this.adapter = adapter;
        this.listener = listener;
        this.getter = getter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        List<Location> list = adapter.moveItem(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        listener.onLocationSequenceChanged(list);
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        switch (direction) {
            case ItemTouchHelper.START: {
                int position = viewHolder.getAdapterPosition();
                Location location = adapter.getLocation(position);

                if (location.isCurrentPosition()) {
                    adapter.update(
                            adapter.getLocationList(),
                            getter.getSelectedId(),
                            location.getFormattedId()
                    );
                    listener.onSelectProviderActivityStarted();
                } else {
                    List<Location> list = adapter.getLocationList();
                    location = list.get(viewHolder.getAdapterPosition());
                    location.setResidentPosition(!location.isResidentPosition());
                    adapter.update(list, getter.getSelectedId(), location.getFormattedId());
                    if (location.isResidentPosition()) {
                        SnackbarUtils.showSnackbar(
                                activity,
                                activity.getString(R.string.feedback_resident_location),
                                activity.getString(R.string.learn_more),
                                v -> new LearnMoreAboutResidentLocationDialog().show(
                                        activity.getSupportFragmentManager(), null)
                        );
                    }

                    listener.onLocationChanged(list, location);
                }
                break;
            }
            case ItemTouchHelper.END:
                if (adapter.getItemCount() <= 1) {
                    List<Location> list = adapter.getLocationList();
                    Location location = list.get(viewHolder.getAdapterPosition());
                    adapter.update(list, getter.getSelectedId(), location.getFormattedId());
                    SnackbarUtils.showSnackbar(
                            activity,
                            activity.getString(R.string.feedback_location_list_cannot_be_null)
                    );
                } else {
                    List<Location> list = adapter.getLocationList();
                    Location location = list.remove(viewHolder.getAdapterPosition());
                    location.setWeather(DatabaseHelper.getInstance(activity).readWeather(location));

                    String selectedId = getter.getSelectedId();
                    if (selectedId != null && location.getFormattedId().equals(selectedId)) {
                        selectedId = list.get(0).getFormattedId();
                    }
                    adapter.update(list, selectedId, location.getFormattedId());
                    SnackbarUtils.showSnackbar(
                            activity,
                            activity.getString(R.string.feedback_delete_succeed),
                            activity.getString(R.string.cancel),
                            new CancelDeleteListener(location)
                    );

                    listener.onLocationRemoved(list, location);
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

    public interface SelectedIdGetter {
        @Nullable String getSelectedId();
    }

    // on click listener.

    private class CancelDeleteListener implements View.OnClickListener {

        private final Location location;

        CancelDeleteListener(Location location) {
            this.location = location;
        }

        @Override
        public void onClick(View view) {
            List<Location> list = adapter.getLocationList();
            list.add(location);
            adapter.update(list, getter.getSelectedId(), null);

            listener.onLocationInserted(list, location);
        }
    }
}