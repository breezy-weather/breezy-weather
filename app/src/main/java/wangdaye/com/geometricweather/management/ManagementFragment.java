package wangdaye.com.geometricweather.management;

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
import wangdaye.com.geometricweather.basic.models.Location;
import wangdaye.com.geometricweather.databinding.FragmentManagementBinding;
import wangdaye.com.geometricweather.management.adapter.LocationAdapter;
import wangdaye.com.geometricweather.management.adapter.LocationItemTouchCallback;
import wangdaye.com.geometricweather.management.models.SelectableLocationListResource;
import wangdaye.com.geometricweather.ui.decotarions.ListDecoration;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.helpters.SnackbarHelper;
import wangdaye.com.geometricweather.utils.helpters.IntentHelper;
import wangdaye.com.geometricweather.utils.managers.ShortcutsManager;
import wangdaye.com.geometricweather.utils.managers.ThemeManager;

public class ManagementFragment extends Fragment
        implements LocationItemTouchCallback.OnLocationListChangedListener {

    private FragmentManagementBinding mBinding;
    private ManagementFragmentViewModel mViewModel;

    private @Nullable LocationAdapter mAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private ListDecoration mItemDecoration;
    private int mSearchRequestCode;
    private int mProviderSettingsRequestCode;

    private ValueAnimator mColorAnimator;

    private boolean mDrawerMode = false;

    private @Nullable LocationManageCallback mLocationListChangedListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentManagementBinding.inflate(getLayoutInflater(), container, false);
        initModel();
        initView();
        return mBinding.getRoot();
    }

    private void initModel() {
        mViewModel = new ViewModelProvider(requireActivity()).get(ManagementFragmentViewModel.class);
        if (mViewModel.checkIsNewInstance()) {
            mViewModel.resetLocationList(null);
        }
    }

    private void initView() {
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.appBar, (v, insets) -> new DisplayUtils.RelativeInsets(
                insets, false, false, mDrawerMode, true).setPaddingRelative(v));

        mBinding.searchBar.setOnClickListener(v -> IntentHelper.startSearchActivityForResult(
                requireActivity(), mBinding.searchBar, mSearchRequestCode));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBinding.searchBar.setTransitionName(getString(R.string.transition_activity_search_bar));
        }

        mBinding.currentLocationButton.setOnClickListener(v -> {
            mViewModel.addLocation(Location.buildLocal());
            SnackbarHelper.showSnackbar(getString(R.string.feedback_collect_succeed));
        });

        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(
                requireActivity(), RecyclerView.VERTICAL, false));

        mItemTouchHelper = new ItemTouchHelper(new LocationItemTouchCallback(
                (GeoActivity) requireActivity(), mViewModel, this));
        mItemTouchHelper.attachToRecyclerView(mBinding.recyclerView);

        ViewCompat.setOnApplyWindowInsetsListener(mBinding.recyclerView, (v, insets) -> new DisplayUtils.RelativeInsets(
                insets, false, true, mDrawerMode, false).setPaddingRelative(v));

        mViewModel.getListResource().observe(getViewLifecycleOwner(), resource -> {

            if (mAdapter == null) {
                mAdapter = new LocationAdapter(
                        requireActivity(),
                        resource.dataList,
                        resource.selectedId,
                        (v, formattedId) -> { // on click.
                            String selectedId = mViewModel.getSelectedId();
                            if (selectedId != null) {
                                mAdapter.update(selectedId);
                                setThemeStyle();
                            }
                            if (mLocationListChangedListener != null) {
                                mLocationListChangedListener.onSelectedLocation(formattedId);
                            }
                        },
                        holder -> mItemTouchHelper.startDrag(holder) // on drag.
                );
                mBinding.recyclerView.setAdapter(mAdapter);
            } else if (resource.source instanceof SelectableLocationListResource.ItemMoved) {
                SelectableLocationListResource.ItemMoved source
                        = (SelectableLocationListResource.ItemMoved) resource.source;
                mAdapter.update(source.from, source.to);
            } else {
                mAdapter.update(resource.dataList, resource.selectedId, resource.forceUpdateId);
            }

            setThemeStyle();
            onLocationListChanged(resource.dataList, false, false);
        });
    }

    private void setThemeStyle() {
        ThemeManager themeManager = ThemeManager.getInstance(requireActivity());

        ImageViewCompat.setImageTintList(
                mBinding.searchIcon,
                ColorStateList.valueOf(themeManager.getTextContentColor(requireActivity()))
        );
        ImageViewCompat.setImageTintList(
                mBinding.currentLocationButton,
                ColorStateList.valueOf(themeManager.getTextContentColor(requireActivity()))
        );
        mBinding.title.setTextColor(
                ColorStateList.valueOf(themeManager.getTextSubtitleColor(requireActivity())));

        // background.
        if (mColorAnimator != null) {
            mColorAnimator.cancel();
            mColorAnimator = null;
        }

        int oldColor = Color.TRANSPARENT;
        Drawable background = mBinding.recyclerView.getBackground();
        if (background instanceof ColorDrawable) {
            oldColor = ((ColorDrawable) background).getColor();
        }
        int newColor = themeManager.getRootColor(requireActivity());

        if (newColor != oldColor) {
            mColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), oldColor, newColor);
            mColorAnimator.addUpdateListener(animation -> {
                mBinding.searchBar.setCardBackgroundColor((Integer) animation.getAnimatedValue());
                mBinding.recyclerView.setBackgroundColor((Integer) animation.getAnimatedValue());
            });
            mColorAnimator.setDuration(450);
            mColorAnimator.start();
        } else {
            mBinding.searchBar.setCardBackgroundColor(newColor);
            mBinding.recyclerView.setBackgroundColor(newColor);
        }

        if (mItemDecoration != null) {
            mBinding.recyclerView.removeItemDecoration(mItemDecoration);
            mItemDecoration = null;
        }
        mItemDecoration = new ListDecoration(
                requireActivity(),
                ThemeManager.getInstance(requireActivity()).getLineColor(requireActivity())
        );
        mBinding.recyclerView.addItemDecoration(mItemDecoration);
    }

    public void setRequestCodes(int searchRequestCode, int providerSettingsRequestCode) {
        mSearchRequestCode = searchRequestCode;
        mProviderSettingsRequestCode = providerSettingsRequestCode;
    }

    public void updateView(List<Location> newList, @Nullable String selectedId) {
        mViewModel.updateLocation(newList, selectedId);
    }

    public void readAppendLocation() {
        mViewModel.readAppendCache();
        SnackbarHelper.showSnackbar(getString(R.string.feedback_collect_succeed));
    }

    public void resetLocationList(@Nullable String selectedId) {
        mViewModel.resetLocationList(selectedId);
    }

    private void onLocationListChanged(List<Location> list, boolean updateShortcuts, boolean notifyOutside) {
        setCurrentLocationButtonEnabled(list);
        if (updateShortcuts && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutsManager.refreshShortcutsInNewThread(requireActivity(), list);
        }
        if (notifyOutside && mLocationListChangedListener != null) {
            mLocationListChangedListener.onLocationListChanged(list);
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
        mBinding.currentLocationButton.setEnabled(enabled);
        mBinding.currentLocationButton.setAlpha(enabled ? 1 : .5f);
    }

    public void setDrawerMode(boolean drawerMode) {
        mDrawerMode = drawerMode;
    }

    // interface.

    public interface LocationManageCallback {
        void onSelectedLocation(@NonNull String formattedId);
        void onLocationListChanged(List<Location> locationList);
    }

    public void setOnLocationListChangedListener(LocationManageCallback l) {
        mLocationListChangedListener = l;
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
                requireActivity(), mProviderSettingsRequestCode);
    }
}
