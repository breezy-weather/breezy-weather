package wangdaye.com.geometricweather.main.fragment;

import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.ui.adapter.LocationAdapter;
import wangdaye.com.geometricweather.ui.decotarion.ListDecoration;
import wangdaye.com.geometricweather.ui.dialog.LearnMoreAboutResidentLocationDialog;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.manager.ShortcutsManager;

public class LocationManageFragment extends Fragment
        implements LocationAdapter.OnLocationItemClickListener {

    private CardView cardView;
    private AppCompatImageButton currentLocationButton;
    private RecyclerView recyclerView;

    private LocationAdapter adapter;
    private int searchRequestCode;
    private int providerSettingsRequestCode;
    private String currentFormattedId;

    private @Nullable LocationManageCallback locationListChangedListener;

    private class LocationSwipeCallback extends ItemTouchHelper.SimpleCallback {

        LocationSwipeCallback(int dragDirs, int swipeDirs) {
            super(dragDirs, swipeDirs);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            notifyMainActivityToUpdate(currentFormattedId);

            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            adapter.moveData(fromPosition, toPosition);
            DatabaseHelper.getInstance(requireActivity()).writeLocationList(adapter.itemList);
            onLocationListChanged(true);

            ((LocationAdapter.ViewHolder) viewHolder).drawDrag(requireActivity(), false);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            deleteLocation(viewHolder.getAdapterPosition());
        }

        @Override
        public void onChildDraw(@NonNull Canvas c,
                                @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            switch (actionState) {
                case ItemTouchHelper.ACTION_STATE_SWIPE:
                    ((LocationAdapter.ViewHolder) viewHolder).drawSwipe(dX);
                    break;

                case ItemTouchHelper.ACTION_STATE_DRAG:
                    ((LocationAdapter.ViewHolder) viewHolder).drawDrag(requireActivity(), dY != 0);
                    break;

                case ItemTouchHelper.ACTION_STATE_IDLE:
                    ((LocationAdapter.ViewHolder) viewHolder)
                            .drawSwipe(0)
                            .drawDrag(requireActivity(), false);
                    break;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_manage, container, false);
        initWidget(view);
        return view;
    }

    private void initWidget(View view) {
        this.cardView = view.findViewById(R.id.fragment_location_manage_searchBar);
        cardView.setOnClickListener(v ->
                IntentHelper.startSearchActivityForResult(requireActivity(), cardView, searchRequestCode));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cardView.setTransitionName(getString(R.string.transition_activity_search_bar));
        }

        this.currentLocationButton = view.findViewById(R.id.fragment_location_manage_currentLocationButton);
        currentLocationButton.setOnClickListener(v -> {
            Location local = Location.buildLocal();
            adapter.insertData(local);
            DatabaseHelper.getInstance(requireActivity()).writeLocation(local);
        });

        this.recyclerView = view.findViewById(R.id.fragment_location_manage_recyclerView);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        );
        recyclerView.addItemDecoration(new ListDecoration(requireActivity()));

        new ItemTouchHelper(
                new LocationSwipeCallback(
                        ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
                )
        ).attachToRecyclerView(recyclerView);

        resetLocationAdapter();
        onLocationListChanged(false);
    }

    public void setData(int searchRequestCode, int providerSettingsRequestCode, String formattedId) {
        this.searchRequestCode = searchRequestCode;
        this.providerSettingsRequestCode = providerSettingsRequestCode;
        this.currentFormattedId = formattedId;
    }

    public void addLocation() {
        notifyMainActivityToUpdate(currentFormattedId);

        adapter.itemList = DatabaseHelper.getInstance(requireActivity()).readLocationList();
        adapter.notifyItemInserted(adapter.getItemCount() - 1);
        onLocationListChanged(true);

        SnackbarUtils.showSnackbar((GeoActivity) requireActivity(), getString(R.string.feedback_collect_succeed));
    }

    public void resetLocationList() {
        resetLocationAdapter();
        onLocationListChanged(false);
    }

    private void notifyMainActivityToUpdate(@Nullable String formattedId) {
        if (locationListChangedListener != null) {
            locationListChangedListener.onLocationListChanged(formattedId);
        }
    }

    private void resetLocationAdapter() {
        adapter = new LocationAdapter(
                (GeoActivity) requireActivity(),
                providerSettingsRequestCode,
                DatabaseHelper.getInstance(requireActivity()).readLocationList(),
                true,
                this
        );
        recyclerView.setAdapter(adapter);
    }

    private void onLocationListChanged(boolean updateShortcuts) {
        setCurrentLocationButtonEnabled();
        if (updateShortcuts && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutsManager.refreshShortcutsInNewThread(requireActivity(), adapter.itemList);
        }
    }

    private void setCurrentLocationButtonEnabled() {
        boolean hasCurrentLocation = false;
        for (int i = 0; i < adapter.itemList.size(); i ++) {
            if (adapter.itemList.get(i).isCurrentPosition()) {
                hasCurrentLocation = true;
                break;
            }
        }
        currentLocationButton.setEnabled(!hasCurrentLocation);
        currentLocationButton.setAlpha(hasCurrentLocation ? 0.5f : 1);
    }

    private void deleteLocation(int position) {
        if (0 <= position && position < adapter.itemList.size()) {
            Location location = adapter.itemList.get(position);
            if (adapter.itemList.size() <= 1) {
                adapter.removeData(position);
                adapter.insertData(location);
                SnackbarUtils.showSnackbar(
                        (GeoActivity) requireActivity(), getString(R.string.feedback_location_list_cannot_be_null));
            } else {
                String formattedId = adapter.itemList.get(position).getFormattedId();
                notifyMainActivityToUpdate(
                        formattedId.equals(currentFormattedId) ? null : currentFormattedId);

                adapter.removeData(position);
                onLocationListChanged(true);

                location.setWeather(DatabaseHelper.getInstance(requireActivity()).readWeather(location));

                DatabaseHelper.getInstance(requireActivity()).deleteLocation(location);
                DatabaseHelper.getInstance(requireActivity()).deleteWeather(location);
                DatabaseHelper.getInstance(requireActivity()).writeLocationList(adapter.itemList);

                SnackbarUtils.showSnackbar(
                        (GeoActivity) requireActivity(),
                        getString(R.string.feedback_delete_succeed),
                        getString(R.string.cancel),
                        new CancelDeleteListener(location)
                );
            }
        }
    }

    // interface.

    public interface LocationManageCallback {
        void onSelectedLocation(@NonNull String formattedId);
        void onLocationListChanged(@Nullable String formattedId);
    }

    public void setOnLocationListChangedListener(LocationManageCallback l) {
        this.locationListChangedListener = l;
    }

    // on click listener.

    private class CancelDeleteListener
            implements View.OnClickListener {

        private Location location;

        CancelDeleteListener(Location l) {
            this.location = l;
        }

        @Override
        public void onClick(View view) {
            adapter.insertData(location);
            onLocationListChanged(true);

            DatabaseHelper.getInstance(requireActivity()).writeLocation(location);
            if (location.getWeather() != null) {
                DatabaseHelper.getInstance(requireActivity())
                        .writeWeather(location, location.getWeather());
            }
        }
    }

    // on location item click listener.

    @Override
    public void onClick(View view, int position) {
        String formattedId = adapter.itemList.get(position).getFormattedId();
        notifyMainActivityToUpdate(formattedId);
        if (locationListChangedListener != null) {
            locationListChangedListener.onSelectedLocation(formattedId);
        }
    }

    @Override
    public void onDelete(View view, int position) {
        deleteLocation(position);
    }

    @Override
    public void onResidentSwitch(View view, int position, boolean resident) {
        adapter.itemList.get(position).setResidentPosition(resident);
        onLocationListChanged(true);

        DatabaseHelper.getInstance(requireActivity()).writeLocation(adapter.itemList.get(position));

        if (resident) {
            SnackbarUtils.showSnackbar(
                    (GeoActivity) requireActivity(),
                    getString(R.string.feedback_resident_location),
                    getString(R.string.learn_more),
                    v -> new LearnMoreAboutResidentLocationDialog().show(requireFragmentManager(), null)
            );
        }
    }
}
