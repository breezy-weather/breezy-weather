package wangdaye.com.geometricweather.main.fragment;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.main.MainListDecoration;
import wangdaye.com.geometricweather.main.MainThemePicker;
import wangdaye.com.geometricweather.main.adapter.LocationAdapter;
import wangdaye.com.geometricweather.ui.dialog.LearnMoreAboutResidentLocationDialog;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.manager.ShortcutsManager;

public class LocationManageFragment extends Fragment
        implements LocationAdapter.OnLocationItemClickListener {

    private CardView cardView;
    private AppCompatImageView searchIcon;
    private TextView searchTitle;
    private AppCompatImageButton currentLocationButton;
    private RecyclerView recyclerView;

    private LocationAdapter adapter;
    private MainListDecoration decoration;
    private int searchRequestCode;
    private int providerSettingsRequestCode;

    private @Nullable MainThemePicker themePicker;
    private ValueAnimator colorAnimator;

    private @Nullable LocationManageCallback locationListChangedListener;

    private class LocationSwipeCallback extends ItemTouchHelper.SimpleCallback {

        LocationSwipeCallback(int dragDirs, int swipeDirs) {
            super(dragDirs, swipeDirs);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            ((LocationAdapter.ViewHolder) viewHolder).drawDrag(requireActivity(), false);

            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            adapter.moveData(fromPosition, toPosition);

            DatabaseHelper.getInstance(requireActivity()).writeLocationList(adapter.itemList);
            onLocationListChanged(true, true);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            Location location = adapter.itemList.get(position);
            switch (direction) {
                case ItemTouchHelper.START:
                    if (location.isCurrentPosition()) {
                        IntentHelper.startSelectProviderActivityForResult(
                                requireActivity(), providerSettingsRequestCode);
                    } else {
                        location.setResidentPosition(!location.isResidentPosition());
                        DatabaseHelper.getInstance(requireActivity()).writeLocation(location);

                        if (location.isResidentPosition()) {
                            SnackbarUtils.showSnackbar(
                                    (GeoActivity) requireActivity(),
                                    getString(R.string.feedback_resident_location),
                                    getString(R.string.learn_more),
                                    v -> new LearnMoreAboutResidentLocationDialog().show(requireFragmentManager(), null)
                            );
                        }

                        onLocationListChanged(true, true);
                    }
                    adapter.notifyItemChanged(position);
                    break;

                case ItemTouchHelper.END:
                    if (adapter.itemList.size() <= 1) {
                        adapter.removeData(position);
                        adapter.insertData(location);
                        SnackbarUtils.showSnackbar(
                                (GeoActivity) requireActivity(), getString(R.string.feedback_location_list_cannot_be_null));
                    } else {
                        location.setWeather(DatabaseHelper.getInstance(requireActivity()).readWeather(location));

                        DatabaseHelper.getInstance(requireActivity()).deleteLocation(location);
                        DatabaseHelper.getInstance(requireActivity()).deleteWeather(location);

                        adapter.removeData(position);
                        SnackbarUtils.showSnackbar(
                                (GeoActivity) requireActivity(),
                                getString(R.string.feedback_delete_succeed),
                                getString(R.string.cancel),
                                new CancelDeleteListener(location)
                        );

                        onLocationListChanged(true, true);
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

        this.searchIcon = view.findViewById(R.id.fragment_location_manage_searchIcon);
        this.searchTitle = view.findViewById(R.id.fragment_location_manage_title);

        this.currentLocationButton = view.findViewById(R.id.fragment_location_manage_currentLocationButton);
        currentLocationButton.setOnClickListener(v -> {
            DatabaseHelper.getInstance(requireActivity()).writeLocation(Location.buildLocal());
            addLocation(true);
        });

        this.recyclerView = view.findViewById(R.id.fragment_location_manage_recyclerView);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        );

        new ItemTouchHelper(
                new LocationSwipeCallback(
                        ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.START | ItemTouchHelper.END
                )
        ).attachToRecyclerView(recyclerView);

        adapter = null;
        setThemeStyle();
        resetLocationAdapter(readLocationList());
        onLocationListChanged(false, false);
    }

    private void setThemeStyle() {
        if (themePicker != null) {
            // search bar elements.
            searchIcon.setSupportImageTintList(
                    ColorStateList.valueOf(themePicker.getTextContentColor(requireActivity())));
            searchTitle.setTextColor(
                    ColorStateList.valueOf(themePicker.getTextSubtitleColor(requireActivity())));
            currentLocationButton.setSupportImageTintList(
                    ColorStateList.valueOf(themePicker.getTextContentColor(requireActivity())));

            // background.
            if (colorAnimator != null) {
                colorAnimator.cancel();
                colorAnimator = null;
            }

            int oldColor = Color.TRANSPARENT;
            Drawable background = recyclerView.getBackground();
            if (background instanceof ColorDrawable) {
                oldColor = ((ColorDrawable) background).getColor();
            }
            int newColor = themePicker.getRootColor(requireActivity());

            if (newColor != oldColor) {
                colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), oldColor, newColor);
                colorAnimator.addUpdateListener(animation -> {
                    cardView.setCardBackgroundColor((Integer) animation.getAnimatedValue());
                    recyclerView.setBackgroundColor((Integer) animation.getAnimatedValue());
                });
                colorAnimator.setDuration(450);
                colorAnimator.start();
            } else {
                cardView.setCardBackgroundColor(newColor);
                recyclerView.setBackgroundColor(newColor);
            }
        }

        if (decoration != null) {
            recyclerView.removeItemDecoration(decoration);
            decoration = null;
        }
        decoration = new MainListDecoration(requireActivity(), themePicker);
        recyclerView.addItemDecoration(decoration);
    }

    public void setRequestCodes(int searchRequestCode, int providerSettingsRequestCode) {
        this.searchRequestCode = searchRequestCode;
        this.providerSettingsRequestCode = providerSettingsRequestCode;
    }

    public void setThemePicker(@Nullable MainThemePicker picker) {
        this.themePicker = picker;
    }

    private List<Location> readLocationList() {
        List<Location> locationList = DatabaseHelper.getInstance(requireActivity()).readLocationList();
        for (Location l : locationList) {
            l.setWeather(DatabaseHelper.getInstance(requireActivity()).readWeather(l));
        }
        return locationList;
    }

    public void updateView(List<Location> newList, @NonNull MainThemePicker picker) {
        boolean themeChanged = themePicker == null || themePicker.isLightTheme() != picker.isLightTheme();
        if (themeChanged) {
            setThemePicker(picker);
            setThemeStyle();
        }
        resetLocationAdapter(newList);
    }

    public void addLocation() {
        addLocation(true);
    }

    private void addLocation(boolean notify) {
        resetLocationList();
        if (notify) {
            SnackbarUtils.showSnackbar((GeoActivity) requireActivity(), getString(R.string.feedback_collect_succeed));
        }
    }

    public void resetLocationList() {
        resetLocationAdapter(readLocationList());
        onLocationListChanged(false, true);
    }

    private void resetLocationAdapter(List<Location> list) {
        if (adapter == null) {
            adapter = new LocationAdapter(
                    (GeoActivity) requireActivity(),
                    list,
                    themePicker,
                    this
            );
            recyclerView.setAdapter(adapter);
        } else {
            adapter.changeData(list, themePicker);
        }
    }

    private void onLocationListChanged(boolean updateShortcuts, boolean notifyOutside) {
        setCurrentLocationButtonEnabled();
        if (updateShortcuts && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutsManager.refreshShortcutsInNewThread(requireActivity(), adapter.itemList);
        }
        if (notifyOutside && locationListChangedListener != null) {
            locationListChangedListener.onLocationListChanged();
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

    // interface.

    public interface LocationManageCallback {
        void onSelectedLocation(@NonNull String formattedId);
        void onLocationListChanged();
    }

    public void setOnLocationListChangedListener(LocationManageCallback l) {
        this.locationListChangedListener = l;
    }

    // on click listener.

    private class CancelDeleteListener implements View.OnClickListener {

        private Location location;

        CancelDeleteListener(Location l) {
            this.location = l;
        }

        @Override
        public void onClick(View view) {
            DatabaseHelper.getInstance(requireActivity()).writeLocation(location);
            if (location.getWeather() != null) {
                DatabaseHelper.getInstance(requireActivity()).writeWeather(location, location.getWeather());
            }

            addLocation(false);
        }
    }

    // on location item click listener.

    @Override
    public void onClick(View view, int position) {
        String formattedId = adapter.itemList.get(position).getFormattedId();
        if (locationListChangedListener != null) {
            locationListChangedListener.onSelectedLocation(formattedId);
        }
    }
}
