package wangdaye.com.geometricweather.main.fragments;

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
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.models.Location;
import wangdaye.com.geometricweather.databinding.FragmentManagementBinding;
import wangdaye.com.geometricweather.main.MainActivityViewModel;
import wangdaye.com.geometricweather.main.adapters.LocationAdapterAnimWrapper;
import wangdaye.com.geometricweather.main.models.SelectableLocationListResource;
import wangdaye.com.geometricweather.main.ui.LocationItemTouchCallback;
import wangdaye.com.geometricweather.ui.adapters.location.LocationAdapter;
import wangdaye.com.geometricweather.ui.decotarions.ListDecoration;
import wangdaye.com.geometricweather.utils.helpters.SnackbarHelper;
import wangdaye.com.geometricweather.utils.managers.ThemeManager;

public class ManagementFragment extends Fragment
        implements LocationItemTouchCallback.OnSelectProviderActivityStartedCallback {

    private FragmentManagementBinding mBinding;
    private MainActivityViewModel mViewModel;

    private LocationAdapter mAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private @Nullable RecyclerView.ItemDecoration mItemDecoration;

    private ValueAnimator mColorAnimator;

    private @Nullable Callback mCallback;

    public interface Callback {
        void onSearchBarClicked(View searchBar);
        void onSelectProviderActivityStarted();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentManagementBinding.inflate(getLayoutInflater(), container, false);
        initModel();
        initView();
        setCallback((Callback) requireActivity());
        return mBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mItemDecoration != null) {
            mBinding.recyclerView.removeItemDecoration(mItemDecoration);
        }
    }

    private void initModel() {
        mViewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);
    }

    private void initView() {
        mBinding.searchBar.setOnClickListener(v -> {
            if (mCallback != null) {
                mCallback.onSearchBarClicked(mBinding.searchBar);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBinding.searchBar.setTransitionName(getString(R.string.transition_activity_search_bar));
        }

        mBinding.currentLocationButton.setOnClickListener(v -> {
            mViewModel.addLocation(Location.buildLocal());
            SnackbarHelper.showSnackbar(getString(R.string.feedback_collect_succeed));
        });

        mAdapter = new LocationAdapter(
                requireActivity(),
                new ArrayList<>(),
                null,
                (v, formattedId) -> { // on click.
                    mViewModel.setLocation(formattedId);
                    getParentFragmentManager().popBackStack();
                },
                holder -> mItemTouchHelper.startDrag(holder) // on drag.
        );
        mBinding.recyclerView.setAdapter(new LocationAdapterAnimWrapper(requireContext(), mAdapter));
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(
                requireActivity(), RecyclerView.VERTICAL, false));

        mItemTouchHelper = new ItemTouchHelper(new LocationItemTouchCallback(
                (GeoActivity) requireActivity(), mViewModel, this));
        mItemTouchHelper.attachToRecyclerView(mBinding.recyclerView);

        mViewModel.getListResource().observe(getViewLifecycleOwner(), resource -> {

            if (resource.source instanceof SelectableLocationListResource.ItemMoved) {
                SelectableLocationListResource.ItemMoved source
                        = (SelectableLocationListResource.ItemMoved) resource.source;
                mAdapter.update(source.from, source.to);
            } else {
                mAdapter.update(resource.dataList, resource.selectedId, resource.forceUpdateId);
            }

            setThemeStyle();
            setCurrentLocationButtonEnabled(resource.dataList);
        });
    }

    private void setThemeStyle() {
        ThemeManager themeManager = ThemeManager.getInstance(requireContext());
        themeManager.update(requireContext());

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
        }
        mItemDecoration = new ListDecoration(
                requireActivity(),
                ThemeManager.getInstance(requireActivity()).getLineColor(requireActivity())
        );
        mBinding.recyclerView.addItemDecoration(mItemDecoration);
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

    // interface.

    public void setCallback(Callback l) {
        mCallback = l;
    }

    // on location list changed listener.

    @Override
    public void onSelectProviderActivityStarted() {
        if (mCallback != null) {
            mCallback.onSelectProviderActivityStarted();
        }
    }
}
