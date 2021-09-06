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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.GeoFragment;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.common.basic.models.resources.Resource;
import wangdaye.com.geometricweather.common.ui.widgets.SwipeSwitchLayout;
import wangdaye.com.geometricweather.common.basic.insets.FitHorizontalSystemBarRootLayout;
import wangdaye.com.geometricweather.common.ui.widgets.weatherView.WeatherView;
import wangdaye.com.geometricweather.common.ui.widgets.weatherView.WeatherViewController;
import wangdaye.com.geometricweather.common.ui.widgets.weatherView.circularSkyView.CircularSkyWeatherView;
import wangdaye.com.geometricweather.common.ui.widgets.weatherView.materialWeatherView.MaterialWeatherView;
import wangdaye.com.geometricweather.common.utils.helpers.SnackbarHelper;
import wangdaye.com.geometricweather.databinding.FragmentMainBinding;
import wangdaye.com.geometricweather.main.MainActivityViewModel;
import wangdaye.com.geometricweather.main.adapters.main.MainAdapter;
import wangdaye.com.geometricweather.main.dialogs.LocationHelpDialog;
import wangdaye.com.geometricweather.main.layouts.MainLayoutManager;
import wangdaye.com.geometricweather.main.models.LocationResource;
import wangdaye.com.geometricweather.main.utils.MainModuleUtils;
import wangdaye.com.geometricweather.main.utils.MainPalette;
import wangdaye.com.geometricweather.main.utils.MainThemeManager;
import wangdaye.com.geometricweather.resource.ResourcesProviderFactory;
import wangdaye.com.geometricweather.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsManager;

public class MainFragment extends GeoFragment {

    private FragmentMainBinding mBinding;
    private MainActivityViewModel mViewModel;

    private WeatherView mWeatherView;
    private MainAdapter mAdapter;
    private OnScrollListener mScrollListener;
    private @Nullable Animator mRecyclerViewAnimator;

    private ResourceProvider mResourceProvider;

    private @Nullable String mCurrentLocationFormattedId;
    private @Nullable WeatherSource mCurrentWeatherSource;
    private @Nullable Boolean mCurrentLightTheme;
    private long mCurrentWeatherTimeStamp;

    private @Nullable Callback mCallback;

    private static final long INVALID_CURRENT_WEATHER_TIME_STAMP = -1;

    public interface Callback {
        void onManageIconClicked();
        void onSettingsIconClicked();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = FragmentMainBinding.inflate(getLayoutInflater(), container, false);

        initModel();

        // attach weather view.
        switch (SettingsManager.getInstance(requireContext()).getUiStyle()) {
            case MATERIAL:
                mWeatherView = new MaterialWeatherView(requireContext());
                break;

            case CIRCULAR:
                mWeatherView = new CircularSkyWeatherView(requireContext(),
                        mViewModel.getThemeManager().isDaytime());
                break;
        }
        ((CoordinatorLayout) mBinding.switchLayout.getParent()).addView(
                (View) mWeatherView,
                0,
                new CoordinatorLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );
        setSystemBarStyle();
        mViewModel.getThemeManager().registerWeatherView(mWeatherView);

        resetUIUpdateFlag();
        ensureResourceProvider();

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
        mViewModel.getThemeManager().unregisterWeatherView();
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

    @SuppressLint({"ClickableViewAccessibility", "NonConstantResourceId"})
    private void initView() {
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

        mBinding.refreshLayout.setOnRefreshListener(() ->
                mViewModel.updateWeather(true, true));

        boolean listAnimationEnabled = SettingsManager.getInstance(requireContext()).isListAnimationEnabled();
        boolean itemAnimationEnabled = SettingsManager.getInstance(requireContext()).isItemAnimationEnabled();
        mAdapter = new MainAdapter((GeoActivity) requireActivity(), mBinding.recyclerView, mWeatherView,
                null, mResourceProvider, mViewModel.getThemeManager(), listAnimationEnabled, itemAnimationEnabled);

        mBinding.recyclerView.setAdapter(mAdapter);
        mBinding.recyclerView.setLayoutManager(new MainLayoutManager());
        mBinding.recyclerView.addOnScrollListener(mScrollListener = new OnScrollListener());
        mBinding.recyclerView.setOnTouchListener(indicatorStateListener);

        mBinding.indicator.setSwitchView(mBinding.switchLayout);

        mViewModel.getCurrentLocation().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) {
                return;
            }

            setRefreshing(resource.status == Resource.Status.LOADING);
            drawUI(resource.data, resource.event == LocationResource.Event.INITIALIZE);

            if (resource.locateFailed) {
                SnackbarHelper.showSnackbar(
                        getString(R.string.feedback_location_failed),
                        getString(R.string.help),
                        v -> LocationHelpDialog.getInstance(
                                new MainPalette(requireContext(), mViewModel.getThemeManager())
                        ).show(getParentFragmentManager(), null)
                );
            } else if (resource.status == Resource.Status.ERROR) {
                SnackbarHelper.showSnackbar(getString(R.string.feedback_get_weather_failed));
            }
        });

        mViewModel.getIndicator().observe(getViewLifecycleOwner(), resource -> {
            mBinding.switchLayout.setEnabled(resource.total > 1);

            if (mBinding.switchLayout.getTotalCount() != resource.total
                    || mBinding.switchLayout.getPosition() != resource.index) {
                mBinding.switchLayout.setData(resource.index, resource.total);
                mBinding.indicator.setSwitchView(mBinding.switchLayout);
            }

            if (resource.total > 1) {
                mBinding.indicator.setVisibility(View.VISIBLE);
            } else {
                mBinding.indicator.setVisibility(View.GONE);
            }
        });
    }

    // control.

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    private void drawUI(Location location, boolean initialize) {
        if (location.getFormattedId().equals(mCurrentLocationFormattedId)
                && location.getWeatherSource() == mCurrentWeatherSource
                && location.getWeather() != null
                && location.getWeather().getBase().getTimeStamp() == mCurrentWeatherTimeStamp
                && mCurrentLightTheme != null
                && mCurrentLightTheme == mViewModel.getThemeManager().isLightTheme()) {
            return;
        }

        boolean needToResetUI = !location.getFormattedId().equals(mCurrentLocationFormattedId)
                || mCurrentWeatherSource != location.getWeatherSource()
                || mCurrentWeatherTimeStamp != INVALID_CURRENT_WEATHER_TIME_STAMP
                || mCurrentLightTheme == null
                || mCurrentLightTheme != mViewModel.getThemeManager().isLightTheme();

        mCurrentLocationFormattedId = location.getFormattedId();
        mCurrentWeatherSource = location.getWeatherSource();
        mCurrentLightTheme = mViewModel.getThemeManager().isLightTheme();
        mCurrentWeatherTimeStamp = location.getWeather() != null
                ? location.getWeather().getBase().getTimeStamp()
                : INVALID_CURRENT_WEATHER_TIME_STAMP;

        if (location.getWeather() == null) {
            resetUI(location);

            mBinding.recyclerView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN
                        && !mBinding.refreshLayout.isRefreshing()) {
                    mViewModel.updateWeather(true, true);
                }
                return false;
            });

            return;
        } else {
            mBinding.recyclerView.setOnTouchListener(null);
        }

        if (needToResetUI) {
            resetUI(location);
        }

        if (initialize) {
            ensureResourceProvider();
        }

        MainThemeManager themeManager = mViewModel.getThemeManager();

        FitHorizontalSystemBarRootLayout rootLayout = ((GeoActivity) requireActivity())
                .getFitHorizontalSystemBarRootLayout();
        rootLayout.setRootColor(themeManager.getRootColor(requireContext()));
        rootLayout.setLineColor(themeManager.getLineColor(requireContext()));

        WeatherViewController.setWeatherCode(
                mWeatherView,
                location.getWeather(),
                location.isDaylight(),
                mResourceProvider
        );

        mBinding.refreshLayout.setColorSchemeColors(mWeatherView.getThemeColors(themeManager.isLightTheme())[0]);
        mBinding.refreshLayout.setProgressBackgroundColorSchemeColor(themeManager.getRootColor(requireContext()));

        boolean listAnimationEnabled = SettingsManager.getInstance(requireContext()).isListAnimationEnabled();
        boolean itemAnimationEnabled = SettingsManager.getInstance(requireContext()).isItemAnimationEnabled();
        mAdapter.update((GeoActivity) requireActivity(), mBinding.recyclerView, mWeatherView, location,
                mResourceProvider, mViewModel.getThemeManager(), listAnimationEnabled, itemAnimationEnabled);
        mAdapter.notifyDataSetChanged();

        mScrollListener.postReset(mBinding.recyclerView);

        mBinding.indicator.setCurrentIndicatorColor(themeManager.getAccentColor(requireContext()));
        mBinding.indicator.setIndicatorColor(themeManager.getTextSubtitleColor(requireContext()));

        if (!listAnimationEnabled) {
            mBinding.recyclerView.setAlpha(0f);
            mRecyclerViewAnimator = MainModuleUtils.getEnterAnimator(mBinding.recyclerView, 0);
            mRecyclerViewAnimator.setStartDelay(150);
            mRecyclerViewAnimator.start();
        }
    }

    private void resetUI(Location location) {
        MainThemeManager themeManager = mViewModel.getThemeManager();

        if (mWeatherView.getWeatherKind() == WeatherView.WEATHER_KING_NULL
                && location.getWeather() == null) {
            WeatherViewController.setWeatherCode(
                    mWeatherView, null, themeManager.isLightTheme(), mResourceProvider);
            mBinding.refreshLayout.setColorSchemeColors(
                    mWeatherView.getThemeColors(themeManager.isLightTheme())[0]);
            mBinding.refreshLayout.setProgressBackgroundColorSchemeColor(
                    themeManager.getRootColor(requireContext())
            );
        }
        mWeatherView.setGravitySensorEnabled(
                SettingsManager.getInstance(requireContext()).isGravitySensorEnabled());

        mBinding.toolbar.setTitle(location.getCityName(requireContext()));

        mBinding.switchLayout.reset();

        if (mRecyclerViewAnimator != null) {
            mRecyclerViewAnimator.cancel();
            mRecyclerViewAnimator = null;
        }
        mAdapter.setNullWeather();
        mAdapter.notifyDataSetChanged();
    }

    public void resetUIUpdateFlag() {
        mCurrentLocationFormattedId = null;
        mCurrentWeatherSource = null;
        mCurrentWeatherTimeStamp = INVALID_CURRENT_WEATHER_TIME_STAMP;
    }

    public void ensureResourceProvider() {
        String iconProvider = SettingsManager.getInstance(requireContext()).getIconProvider(
                requireContext());
        if (mResourceProvider == null
                || !mResourceProvider.getPackageName().equals(iconProvider)) {
            mResourceProvider = ResourcesProviderFactory.getNewInstance();
        }
    }

    private void setRefreshing(final boolean b) {
        mBinding.refreshLayout.post(() -> mBinding.refreshLayout.setRefreshing(b));
    }

    private void setSystemBarStyle() {
        boolean statusShader = mScrollListener != null && mScrollListener.topOverlap;
        mWeatherView.setSystemBarStyle(requireContext(), requireActivity().getWindow(),
                statusShader, false, true, false);
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

    private final SwipeSwitchLayout.OnSwitchListener switchListener = new SwipeSwitchLayout.OnSwitchListener() {

        private @Nullable Location mLocation;
        private boolean mIndexSwitched;

        private float mLastProgress = 0;

        @Override
        public void onSwipeProgressChanged(int swipeDirection, float progress) {
            mBinding.indicator.setDisplayState(progress != 0);

            mIndexSwitched = false;

            if (progress >= 1 && mLastProgress < 0.5) {
                mIndexSwitched = true;
                mLocation = mViewModel.getLocationFromList(
                        swipeDirection == SwipeSwitchLayout.SWIPE_DIRECTION_LEFT ? 1 : -1);
                mLastProgress = 1;
            } else if (progress < 0.5 && mLastProgress >= 1) {
                mIndexSwitched = true;
                mLocation = mViewModel.getLocationFromList(0);
                mLastProgress = 0;
            }

            if (mIndexSwitched && mLocation != null) {
                mBinding.toolbar.setTitle(mLocation.getCityName(requireContext()));
                if (mLocation.getWeather() != null) {
                    WeatherViewController.setWeatherCode(
                            mWeatherView,
                            mLocation.getWeather(),
                            mLocation.isDaylight(),
                            mResourceProvider
                    );
                }
            }
        }

        @Override
        public void onSwipeReleased(int swipeDirection, boolean doSwitch) {
            if (doSwitch) {
                resetUIUpdateFlag();

                mBinding.indicator.setDisplayState(false);
                mViewModel.setLocation(swipeDirection == SwipeSwitchLayout.SWIPE_DIRECTION_LEFT ? 1 : -1);
            }
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
                mWeatherView.setSystemBarColor(requireContext(), requireActivity().getWindow(),
                        topOverlap, false, true, false);
            }
        }
    }
}
