package wangdaye.com.geometricweather.main.fragments;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.ColorUtils;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.GeoFragment;
import wangdaye.com.geometricweather.common.basic.insets.FitHorizontalSystemBarRootLayout;
import wangdaye.com.geometricweather.common.basic.livedata.EqualtableLiveData;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.ui.widgets.SwipeSwitchLayout;
import wangdaye.com.geometricweather.databinding.FragmentMainBinding;
import wangdaye.com.geometricweather.main.MainActivityViewModel;
import wangdaye.com.geometricweather.main.adapters.main.MainAdapter;
import wangdaye.com.geometricweather.main.layouts.MainLayoutManager;
import wangdaye.com.geometricweather.main.utils.DayNightColorWrapper;
import wangdaye.com.geometricweather.main.utils.MainModuleUtils;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.theme.ThemeManager;
import wangdaye.com.geometricweather.theme.resource.ResourcesProviderFactory;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.theme.weatherView.WeatherView;
import wangdaye.com.geometricweather.theme.weatherView.WeatherViewController;

public class MainFragment extends GeoFragment {

    private FragmentMainBinding mBinding;
    private MainActivityViewModel mViewModel;

    private WeatherView mWeatherView;
    private MainAdapter mAdapter;
    private OnScrollListener mScrollListener;
    private @Nullable Animator mRecyclerViewAnimator;
    private ResourceProvider mResourceProvider;

    private final EqualtableLiveData<Integer> mPreviewOffset = new EqualtableLiveData<>(0);

    private @Nullable Callback mCallback;
    public interface Callback {
        void onManageIconClicked();
        void onSettingsIconClicked();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        mBinding = FragmentMainBinding.inflate(getLayoutInflater(), container, false);

        initModel();

        // attach weather view.
        mWeatherView = ThemeManager
                .getInstance(requireContext())
                .getWeatherThemeDelegate()
                .getWeatherView(requireContext());
        ((CoordinatorLayout) mBinding.switchLayout.getParent()).addView(
                (View) mWeatherView,
                0,
                new CoordinatorLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );
        setSystemBarStyle();

        initView();
        setCallback((Callback) requireActivity());

        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        mWeatherView.setDrawable(!isHidden());
    }

    @Override
    public void onPause() {
        super.onPause();
        mWeatherView.setDrawable(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter = null;
        mBinding.recyclerView.clearOnScrollListeners();
        mScrollListener = null;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        mWeatherView.setDrawable(!hidden);

        if (!hidden) {
            setSystemBarStyle();
        }
    }

    // init.

    private void initModel() {
        mViewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);
    }

    @SuppressLint({"ClickableViewAccessibility", "NonConstantResourceId", "NotifyDataSetChanged"})
    private void initView() {
        ensureResourceProvider();

        FitHorizontalSystemBarRootLayout rootLayout = ((GeoActivity) requireActivity())
                .getFitHorizontalSystemBarRootLayout();
        DayNightColorWrapper.bind(
                rootLayout,
                new Integer[]{
                        android.R.attr.colorBackground,
                        R.attr.colorOutline
                },
                (colors, animated) -> {
                    rootLayout.setRootColor(colors[0]);
                    rootLayout.setLineColor(colors[1]);
                    return null;
                }
        );

        mWeatherView.setGravitySensorEnabled(
                SettingsManager.getInstance(requireContext()).isGravitySensorEnabled()
        );

        mBinding.toolbar.setNavigationOnClickListener(v -> {
            if (mCallback != null) {
                mCallback.onManageIconClicked();
            }
        });
        mBinding.toolbar.inflateMenu(R.menu.activity_main);
        mBinding.toolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.action_manage:
                    if (mCallback != null) {
                        mCallback.onManageIconClicked();
                    }
                    break;

                case R.id.action_settings:
                    if (mCallback != null) {
                        mCallback.onSettingsIconClicked();
                    }
                    break;
            }
            return true;
        });

        mBinding.switchLayout.setOnSwitchListener(switchListener);
        mBinding.switchLayout.reset();

        mBinding.indicator.setSwitchView(mBinding.switchLayout);
        DayNightColorWrapper.bind(
                mBinding.indicator,
                new Integer[]{
                        android.R.attr.colorBackground,
                        R.attr.colorOnSurface
                },
                (colors, animated) -> {
                    mBinding.indicator.setCurrentIndicatorColor(colors[0]);
                    mBinding.indicator.setIndicatorColor(
                            ColorUtils.setAlphaComponent(colors[1], (int) (0.5 * 255))
                    );
                    return null;
                }
        );

        DayNightColorWrapper.bind(mBinding.refreshLayout, R.attr.colorSurface, (color, animated) -> {
            mBinding.refreshLayout.setProgressBackgroundColorSchemeColor(color);
            return null;
        });
        mBinding.refreshLayout.setOnRefreshListener(() ->
                mViewModel.updateWithUpdatingChecking(true, true)
        );

        boolean listAnimationEnabled = SettingsManager
                .getInstance(requireContext())
                .isListAnimationEnabled();
        boolean itemAnimationEnabled = SettingsManager
                .getInstance(requireContext())
                .isItemAnimationEnabled();
        mAdapter = new MainAdapter(
                (GeoActivity) requireActivity(),
                mBinding.recyclerView,
                mWeatherView,
                null,
                mResourceProvider,
                listAnimationEnabled,
                itemAnimationEnabled
        );

        mBinding.recyclerView.setAdapter(mAdapter);
        mBinding.recyclerView.setLayoutManager(new MainLayoutManager());
        mBinding.recyclerView.addOnScrollListener(mScrollListener = new OnScrollListener());
        mBinding.recyclerView.setOnTouchListener(indicatorStateListener);

        mViewModel.currentLocation.observe(getViewLifecycleOwner(), location -> {
            ensureResourceProvider();
            updatePreviewSubviews();
            updateContentViews(location);
        });

        mViewModel.loading.observe(getViewLifecycleOwner(), this::setRefreshing);

        mViewModel.indicator.observe(getViewLifecycleOwner(), indicator -> {
            mBinding.switchLayout.setEnabled(indicator.getTotal() > 1);

            if (mBinding.switchLayout.getTotalCount() != indicator.getTotal()
                    || mBinding.switchLayout.getPosition() != indicator.getIndex()) {
                mBinding.switchLayout.setData(indicator.getIndex(), indicator.getTotal());
                mBinding.indicator.setSwitchView(mBinding.switchLayout);
            }

            if (indicator.getTotal() > 1) {
                mBinding.indicator.setVisibility(View.VISIBLE);
            } else {
                mBinding.indicator.setVisibility(View.GONE);
            }
        });

        mPreviewOffset.observe(getViewLifecycleOwner(), offset -> updatePreviewSubviews());
    }

    // control.

    public void updateViews() {
        ensureResourceProvider();
        updatePreviewSubviews();
        updateContentViews(mViewModel.currentLocation.getValue());
    }

    @SuppressLint({"ClickableViewAccessibility", "NotifyDataSetChanged"})
    private void updateContentViews(Location location) {
        if (mRecyclerViewAnimator != null) {
            mRecyclerViewAnimator.cancel();
            mRecyclerViewAnimator = null;
        }

        mBinding.switchLayout.reset();

        if (location.getWeather() == null) {
            mAdapter.setNullWeather();
            mAdapter.notifyDataSetChanged();

            mBinding.recyclerView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN
                        && !mBinding.refreshLayout.isRefreshing()) {
                    mViewModel.updateWithUpdatingChecking(true, true);
                }
                return false;
            });

            return;
        }

        mBinding.recyclerView.setOnTouchListener(null);

        boolean listAnimationEnabled = SettingsManager.getInstance(
                requireContext()
        ).isListAnimationEnabled();
        boolean itemAnimationEnabled = SettingsManager.getInstance(
                requireContext()
        ).isItemAnimationEnabled();
        mAdapter.update(
                (GeoActivity) requireActivity(),
                mBinding.recyclerView,
                mWeatherView,
                location,
                mResourceProvider,
                listAnimationEnabled,
                itemAnimationEnabled
        );
        mAdapter.notifyDataSetChanged();

        mScrollListener.postReset(mBinding.recyclerView);

        if (!listAnimationEnabled) {
            mBinding.recyclerView.setAlpha(0f);
            mRecyclerViewAnimator = MainModuleUtils.getEnterAnimator(mBinding.recyclerView, 0);
            mRecyclerViewAnimator.setStartDelay(150);
            mRecyclerViewAnimator.start();
        }
    }

    public void ensureResourceProvider() {
        String iconProvider = SettingsManager
                .getInstance(requireContext())
                .getIconProvider(requireContext());
        if (mResourceProvider == null
                || !mResourceProvider.getPackageName().equals(iconProvider)) {
            mResourceProvider = ResourcesProviderFactory.getNewInstance();
        }
    }

    private void updatePreviewSubviews() {
        mBinding.getRoot().post(() -> {
            Location location = mViewModel.getValidLocation(mPreviewOffset.getValue());
            boolean daylight = location.isDaylight();

            mBinding.toolbar.setTitle(location.getCityName(requireContext()));
            WeatherViewController.setWeatherCode(
                    mWeatherView,
                    location.getWeather(),
                    daylight,
                    mResourceProvider
            );

            mBinding.refreshLayout.setColorSchemeColors(
                    ThemeManager
                            .getInstance(requireContext())
                            .getWeatherThemeDelegate()
                            .getThemeColors(
                                    requireContext(),
                                    WeatherViewController.getWeatherKind(location.getWeather()),
                                    daylight
                            )[0]
            );
        });
    }

    private void setRefreshing(final boolean b) {
        mBinding.refreshLayout.post(() -> mBinding.refreshLayout.setRefreshing(b));
    }

    private void setSystemBarStyle() {
        ThemeManager
                .getInstance(requireContext())
                .getWeatherThemeDelegate()
                .setSystemBarStyle(
                        requireContext(),
                        requireActivity().getWindow(),
                        mScrollListener != null && mScrollListener.topOverlap,
                        false,
                        true,
                        false
                );
    }

    // interface.

    public void setCallback(@Nullable Callback callback) {
        mCallback = callback;
    }

    // on touch listener.

    private final View.OnTouchListener indicatorStateListener = new View.OnTouchListener() {

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    mBinding.indicator.setDisplayState(true);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mBinding.indicator.setDisplayState(false);
                    break;
            }
            return false;
        }
    };

    // on swipe listener(swipe switch layout).

    private final SwipeSwitchLayout.OnSwitchListener switchListener
            = new SwipeSwitchLayout.OnSwitchListener() {

        @Override
        public void onSwiped(int swipeDirection, float progress) {
            mBinding.indicator.setDisplayState(progress != 0);

            if (progress >= 1) {
                mPreviewOffset.setValue(
                        swipeDirection == SwipeSwitchLayout.SWIPE_DIRECTION_LEFT ? 1 : -1
                );
            } else {
                mPreviewOffset.setValue(0);
            }
        }

        @Override
        public void onSwitched(int swipeDirection) {
            mBinding.indicator.setDisplayState(false);
            mViewModel.offsetLocation(
                    swipeDirection == SwipeSwitchLayout.SWIPE_DIRECTION_LEFT ? 1 : -1
            );
            mPreviewOffset.setValue(0);
        }
    };

    // on scroll changed listener.

    private class OnScrollListener extends RecyclerView.OnScrollListener {

        private @Nullable Boolean mTopChanged;
        boolean topOverlap;

        private int mFirstCardMarginTop;

        private int mScrollY;
        private float mLastAppBarTranslationY;

        OnScrollListener() {
            super();

            mTopChanged = null;
            topOverlap = false;

            mFirstCardMarginTop = 0;

            mScrollY = 0;
            mLastAppBarTranslationY = 0;
        }

        void postReset(@NonNull RecyclerView recyclerView) {
            recyclerView.post(() -> {
                mTopChanged = null;
                topOverlap = false;

                mFirstCardMarginTop = 0;

                mScrollY = 0;
                mLastAppBarTranslationY = 0;

                onScrolled(recyclerView, 0, 0);
            });
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            if (recyclerView.getChildCount() > 0) {
                mFirstCardMarginTop = recyclerView.getChildAt(0).getMeasuredHeight();
            } else {
                mFirstCardMarginTop = -1;
            }

            mScrollY = recyclerView.computeVerticalScrollOffset();
            mLastAppBarTranslationY = mBinding.appBar.getTranslationY();

            mWeatherView.onScroll(mScrollY);
            if (mAdapter != null) {
                mAdapter.onScroll();
            }

            // set translation y of toolbar.
            if (mAdapter != null && mFirstCardMarginTop > 0) {
                if (mFirstCardMarginTop
                        >= mBinding.appBar.getMeasuredHeight() + mAdapter.getCurrentTemperatureTextHeight()) {
                    if (mScrollY < mFirstCardMarginTop
                            - mBinding.appBar.getMeasuredHeight()
                            - mAdapter.getCurrentTemperatureTextHeight()) {
                        mBinding.appBar.setTranslationY(0);
                    } else if (mScrollY > mFirstCardMarginTop - mBinding.appBar.getY()) {
                        mBinding.appBar.setTranslationY(-mBinding.appBar.getMeasuredHeight());
                    } else {
                        mBinding.appBar.setTranslationY(
                                mFirstCardMarginTop
                                        - mAdapter.getCurrentTemperatureTextHeight()
                                        - mScrollY
                                        - mBinding.appBar.getMeasuredHeight()
                        );
                    }
                } else {
                    mBinding.appBar.setTranslationY(-mScrollY);
                }
            }

            // set system bar style.
            if (mFirstCardMarginTop <= 0) {
                mTopChanged = true;
                topOverlap = false;
            } else {
                mTopChanged = (mBinding.appBar.getTranslationY() != 0) != (mLastAppBarTranslationY != 0);
                topOverlap = mBinding.appBar.getTranslationY() != 0;
            }

            if (mTopChanged) {
                ThemeManager
                        .getInstance(requireContext())
                        .getWeatherThemeDelegate()
                        .setSystemBarStyle(
                                requireContext(),
                                requireActivity().getWindow(),
                                topOverlap,
                                false,
                                true,
                                false
                        );
            }
        }
    }
}
