package wangdaye.com.geometricweather.manage;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.databinding.FragmentManageBinding;
import wangdaye.com.geometricweather.manage.adapter.LocationAdapter;
import wangdaye.com.geometricweather.manage.adapter.LocationItemTouchCallback;
import wangdaye.com.geometricweather.manage.model.SelectableLocationListResource;
import wangdaye.com.geometricweather.ui.decotarion.ListDecoration;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.manager.ShortcutsManager;
import wangdaye.com.geometricweather.utils.manager.ThemeManager;

public class ManageFragment extends Fragment
        implements LocationItemTouchCallback.OnLocationListChangedListener {

    private FragmentManageBinding binding;
    private ManageFragmentViewModel viewModel;

    private @Nullable LocationAdapter adapter;
    private ItemTouchHelper itemTouchHelper;
    private ListDecoration decoration;
    private int searchRequestCode;
    private int providerSettingsRequestCode;

    private ValueAnimator colorAnimator;

    private boolean drawerMode = false;

    private @Nullable LocationManageCallback locationListChangedListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageBinding.inflate(getLayoutInflater(), container, false);
        initModel();
        initView();
        return binding.getRoot();
    }

    private void initModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(ManageFragmentViewModel.class);
        if (viewModel.isNewInstance()) {
            viewModel.init(requireActivity(), null);
        }
    }

    private void initView() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBar, (v, insets) -> new DisplayUtils.RelativeInsets(
                insets, false, false, drawerMode, true).setPaddingRelative(v));

        binding.searchBar.setOnClickListener(v -> IntentHelper.startSearchActivityForResult(
                requireActivity(), binding.searchBar, searchRequestCode));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.searchBar.setTransitionName(getString(R.string.transition_activity_search_bar));
        }

        binding.currentLocationButton.setOnClickListener(v -> {
            viewModel.addLocation(requireActivity(), Location.buildLocal());
            SnackbarUtils.showSnackbar(
                    (GeoActivity) requireActivity(), getString(R.string.feedback_collect_succeed));
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(
                requireActivity(), RecyclerView.VERTICAL, false));

        this.itemTouchHelper = new ItemTouchHelper(new LocationItemTouchCallback(
                (GeoActivity) requireActivity(), viewModel, this));
        itemTouchHelper.attachToRecyclerView(binding.recyclerView);

        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerView, (v, insets) -> new DisplayUtils.RelativeInsets(
                insets, false, true, drawerMode, false).setPaddingRelative(v));

        viewModel.getListResource().observe(getViewLifecycleOwner(), resource -> {
            if (adapter == null) {
                adapter = new LocationAdapter(
                        requireActivity(),
                        resource.dataList,
                        resource.selectedId,
                        (v, formattedId) -> { // on click.
                            String selectedId = viewModel.getSelectedId();
                            if (selectedId != null) {
                                adapter.update(selectedId);
                                setThemeStyle();
                            }
                            if (locationListChangedListener != null) {
                                locationListChangedListener.onSelectedLocation(formattedId);
                            }
                        },
                        holder -> itemTouchHelper.startDrag(holder) // on drag.
                );
                binding.recyclerView.setAdapter(adapter);
            } else if (resource.source instanceof SelectableLocationListResource.ItemMoved) {
                SelectableLocationListResource.ItemMoved source
                        = (SelectableLocationListResource.ItemMoved) resource.source;
                adapter.notifyItemMoved(source.from, source.to);
            } else {
                adapter.update(resource.dataList, resource.selectedId, resource.forceUpdateId);
            }

            setThemeStyle();
            onLocationListChanged(resource.dataList, false, false);
        });
    }

    private void setThemeStyle() {
        ThemeManager themeManager = ThemeManager.getInstance(requireActivity());

        ImageViewCompat.setImageTintList(
                binding.searchIcon,
                ColorStateList.valueOf(themeManager.getTextContentColor(requireActivity()))
        );
        ImageViewCompat.setImageTintList(
                binding.currentLocationButton,
                ColorStateList.valueOf(themeManager.getTextContentColor(requireActivity()))
        );
        binding.title.setTextColor(
                ColorStateList.valueOf(themeManager.getTextSubtitleColor(requireActivity())));

        // background.
        if (colorAnimator != null) {
            colorAnimator.cancel();
            colorAnimator = null;
        }

        int oldColor = Color.TRANSPARENT;
        Drawable background = binding.recyclerView.getBackground();
        if (background instanceof ColorDrawable) {
            oldColor = ((ColorDrawable) background).getColor();
        }
        int newColor = themeManager.getRootColor(requireActivity());

        if (newColor != oldColor) {
            colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), oldColor, newColor);
            colorAnimator.addUpdateListener(animation -> {
                binding.searchBar.setCardBackgroundColor((Integer) animation.getAnimatedValue());
                binding.recyclerView.setBackgroundColor((Integer) animation.getAnimatedValue());
            });
            colorAnimator.setDuration(450);
            colorAnimator.start();
        } else {
            binding.searchBar.setCardBackgroundColor(newColor);
            binding.recyclerView.setBackgroundColor(newColor);
        }

        if (decoration != null) {
            binding.recyclerView.removeItemDecoration(decoration);
            decoration = null;
        }
        decoration = new ListDecoration(
                requireActivity(),
                ThemeManager.getInstance(requireActivity()).getLineColor(requireActivity())
        );
        binding.recyclerView.addItemDecoration(decoration);
    }

    public void setRequestCodes(int searchRequestCode, int providerSettingsRequestCode) {
        this.searchRequestCode = searchRequestCode;
        this.providerSettingsRequestCode = providerSettingsRequestCode;
    }

    public void updateView(List<Location> newList, @Nullable String selectedId) {
        viewModel.updateLocation(requireActivity(), newList, selectedId);
    }

    public void readAppendLocation() {
        viewModel.readAppendCache(requireActivity());
        SnackbarUtils.showSnackbar(
                (GeoActivity) requireActivity(), getString(R.string.feedback_collect_succeed));
    }

    public void resetLocationList(@Nullable String selectedId) {
        viewModel.init(requireActivity(), selectedId);
    }

    private void onLocationListChanged(List<Location> list, boolean updateShortcuts, boolean notifyOutside) {
        setCurrentLocationButtonEnabled(list);
        if (updateShortcuts && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutsManager.refreshShortcutsInNewThread(requireActivity(), list);
        }
        if (notifyOutside && locationListChangedListener != null) {
            locationListChangedListener.onLocationListChanged();
        }
    }

    private void setCurrentLocationButtonEnabled(List<Location> list) {
        boolean enabled = list.size() != 0;
        for (int i = 0; i < list.size(); i ++) {
            if (list.get(i).isCurrentPosition()) {
                enabled = false;
                break;
            }
        }
        binding.currentLocationButton.setEnabled(enabled);
        binding.currentLocationButton.setAlpha(enabled ? 1 : .5f);
    }

    public void setDrawerMode(boolean drawerMode) {
        this.drawerMode = drawerMode;
    }

    // interface.

    public interface LocationManageCallback {
        void onSelectedLocation(@NonNull String formattedId);
        void onLocationListChanged();
    }

    public void setOnLocationListChangedListener(LocationManageCallback l) {
        this.locationListChangedListener = l;
    }

    // on location list changed listener.

    @Override
    public void onLocationSequenceChanged(List<Location> locationList) {
        onLocationListChanged(locationList, true, true);
    }

    @Override
    public void onLocationInserted(List<Location> locationList, Location location) {
        onLocationListChanged(locationList, true, true);
    }

    @Override
    public void onLocationRemoved(List<Location> locationList, Location location) {
        onLocationListChanged(locationList, true, true);
    }

    @Override
    public void onLocationChanged(List<Location> locationList, Location location) {
        onLocationListChanged(locationList, true, true);
    }

    @Override
    public void onSelectProviderActivityStarted() {
        IntentHelper.startSelectProviderActivityForResult(
                requireActivity(), providerSettingsRequestCode);
    }
}
