package wangdaye.com.geometricweather.main.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.models.Location;
import wangdaye.com.geometricweather.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.basic.models.resources.Resource;
import wangdaye.com.geometricweather.databinding.FragmentMainBinding;
import wangdaye.com.geometricweather.main.MainActivityViewModel;
import wangdaye.com.geometricweather.main.adapters.main.MainAdapter;
import wangdaye.com.geometricweather.main.dialogs.LocationHelpDialog;
import wangdaye.com.geometricweather.main.layouts.MainLayoutManager;
import wangdaye.com.geometricweather.main.models.LocationResource;
import wangdaye.com.geometricweather.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.resource.providers.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.snackbar.SnackbarHelper;
import wangdaye.com.geometricweather.ui.widgets.SwipeSwitchLayout;
import wangdaye.com.geometricweather.ui.widgets.insets.FitHorizontalSystemBarRootLayout;
import wangdaye.com.geometricweather.ui.widgets.weatherView.WeatherView;
import wangdaye.com.geometricweather.ui.widgets.weatherView.WeatherViewController;
import wangdaye.com.geometricweather.ui.widgets.weatherView.circularSkyView.CircularSkyWeatherView;
import wangdaye.com.geometricweather.ui.widgets.weatherView.materialWeatherView.MaterialWeatherView;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.managers.ThemeManager;
import wangdaye.com.geometricweather.utils.managers.TimeManager;

public class MainFragment extends Fragment {

    private FragmentMainBinding mBinding;
    private MainActivityViewModel mViewModel;

    private WeatherView mWeatherView;
    private MainAdapter mAdapter;
    private @Nullable AnimatorSet mRecyclerViewAnimator;

    private ResourceProvider mResourceProvider;
    private ThemeManager mThemeManager;

    private @Nullable String mCurrentLocationFormattedId;
    private @Nullable WeatherSource mCurrentWeatherSource;
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

        // attach weather view.
        switch (SettingsOptionManager.getInstance(requireContext()).getUiStyle()) {
            case MATERIAL:
                mWeatherView = new MaterialWeatherView(requireContext());
                break;

            case CIRCULAR:
                mWeatherView = new CircularSkyWeatherView(requireContext());
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
        mWeatherView.setSystemBarStyle(requireContext(), requireActivity().getWindow(),
                false, false, true, false);

        resetUIUpdateFlag();
        ensureResourceProvider();
        updateThemeManager();

        initModel();
        initView();
        setCallback((Callback) requireActivity());

        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        mWeatherView.setDrawable(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        mWeatherView.setDrawable(false);
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

        ViewCompat.setOnApplyWindowInsetsListener(mBinding.refreshLayout, (v, insets) -> {
            int startPosition = insets.getSystemWindowInsetTop()
                    + getResources().getDimensionPixelSize(R.dimen.normal_margin);
            mBinding.refreshLayout.setProgressViewOffset(
                    false,
                    startPosition,
                    (int) (startPosition + 64 * getResources().getDisplayMetrics().density)
            );
            return insets;
        });
        mBinding.refreshLayout.setOnRefreshListener(() ->
                mViewModel.updateWeather(true, true));

        boolean listAnimationEnabled = SettingsOptionManager.getInstance(requireContext()).isListAnimationEnabled();
        boolean itemAnimationEnabled = SettingsOptionManager.getInstance(requireContext()).isItemAnimationEnabled();
        mAdapter = new MainAdapter((GeoActivity) requireActivity(), mWeatherView, null, mResourceProvider,
                listAnimationEnabled, itemAnimationEnabled);

        mBinding.recyclerView.setAdapter(mAdapter);
        mBinding.recyclerView.setLayoutManager(new MainLayoutManager());
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
                        v -> new LocationHelpDialog().show(getParentFragmentManager(), null)
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
        if (location.equals(mCurrentLocationFormattedId)
                && location.getWeatherSource() == mCurrentWeatherSource
                && location.getWeather() != null
                && location.getWeather().getBase().getTimeStamp() == mCurrentWeatherTimeStamp) {
            return;
        }

        boolean needToResetUI = !location.equals(mCurrentLocationFormattedId)
                || mCurrentWeatherSource != location.getWeatherSource()
                || mCurrentWeatherTimeStamp != INVALID_CURRENT_WEATHER_TIME_STAMP;

        mCurrentLocationFormattedId = location.getFormattedId();
        mCurrentWeatherSource = location.getWeatherSource();
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
            resetUIUpdateFlag();
            ensureResourceProvider();
        }
        updateThemeManager();

        FitHorizontalSystemBarRootLayout rootLayout = ((GeoActivity) requireActivity())
                .getFitHorizontalSystemBarRootLayout();
        rootLayout.setRootColor(mThemeManager.getRootColor(requireContext()));
        rootLayout.setLineColor(mThemeManager.getLineColor(requireContext()));

        WeatherViewController.setWeatherCode(
                mWeatherView,
                location.getWeather(),
                TimeManager.getInstance(requireContext())
                        .update(requireContext(), location)
                        .isDayTime(),
                mResourceProvider
        );

        mBinding.refreshLayout.setColorSchemeColors(mWeatherView.getThemeColors(mThemeManager.isLightTheme())[0]);
        mBinding.refreshLayout.setProgressBackgroundColorSchemeColor(mThemeManager.getRootColor(requireContext()));

        boolean listAnimationEnabled = SettingsOptionManager.getInstance(requireContext()).isListAnimationEnabled();
        boolean itemAnimationEnabled = SettingsOptionManager.getInstance(requireContext()).isItemAnimationEnabled();
        mAdapter.update((GeoActivity) requireActivity(), mWeatherView, location, mResourceProvider,
                listAnimationEnabled, itemAnimationEnabled);
        mAdapter.notifyDataSetChanged();

        OnScrollListener l = new OnScrollListener();
        mBinding.recyclerView.clearOnScrollListeners();
        mBinding.recyclerView.addOnScrollListener(l);
        mBinding.recyclerView.post(() -> l.onScrolled(mBinding.recyclerView, 0, 0));

        mBinding.indicator.setCurrentIndicatorColor(mThemeManager.getAccentColor(requireContext()));
        mBinding.indicator.setIndicatorColor(mThemeManager.getTextSubtitleColor(requireContext()));

        if (!listAnimationEnabled) {
            mBinding.recyclerView.setAlpha(0f);
            mRecyclerViewAnimator = new AnimatorSet();
            mRecyclerViewAnimator.playTogether(
                    ObjectAnimator.ofFloat(mBinding.recyclerView, "alpha", 0f, 1f),
                    ObjectAnimator.ofFloat(
                            mBinding.recyclerView,
                            "translationY",
                            DisplayUtils.dpToPx(requireContext(), 40), 0f
                    )
            );
            mRecyclerViewAnimator.setDuration(450);
            mRecyclerViewAnimator.setInterpolator(new DecelerateInterpolator(2f));
            mRecyclerViewAnimator.setStartDelay(150);
            mRecyclerViewAnimator.start();
        }
    }

    private void resetUI(Location location) {
        if (mWeatherView.getWeatherKind() == WeatherView.WEATHER_KING_NULL
                && location.getWeather() == null) {
            WeatherViewController.setWeatherCode(
                    mWeatherView, null, mThemeManager.isLightTheme(), mResourceProvider);
            mBinding.refreshLayout.setColorSchemeColors(
                    mWeatherView.getThemeColors(mThemeManager.isLightTheme())[0]);
            mBinding.refreshLayout.setProgressBackgroundColorSchemeColor(
                    mThemeManager.getRootColor(requireContext()));
        }
        mWeatherView.setGravitySensorEnabled(
                SettingsOptionManager.getInstance(requireContext()).isGravitySensorEnabled());

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
        String iconProvider = SettingsOptionManager.getInstance(requireContext()).getIconProvider();
        if (mResourceProvider == null
                || !mResourceProvider.getPackageName().equals(iconProvider)) {
            mResourceProvider = ResourcesProviderFactory.getNewInstance();
        }
    }

    public void updateThemeManager() {
        if (mThemeManager == null) {
            mThemeManager = ThemeManager.getInstance(requireContext());
        }
        mThemeManager.update(requireContext(), mWeatherView);
    }

    private void setRefreshing(final boolean b) {
        mBinding.refreshLayout.post(() -> mBinding.refreshLayout.setRefreshing(b));
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
                            TimeManager.isDaylight(mLocation),
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
        private boolean mTopOverlap;

        private int mFirstCardMarginTop;

        private int mScrollY;
        private float mLastAppBarTranslationY;

        OnScrollListener() {
            super();

            mTopChanged = null;
            mTopOverlap = false;

            mFirstCardMarginTop = 0;

            mScrollY = 0;
            mLastAppBarTranslationY = 0;
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
                mAdapter.onScroll(recyclerView);
            }

            // set translation y of toolbar.
            if (mAdapter != null && mFirstCardMarginTop > 0) {
                if (mFirstCardMarginTop
                        >= mBinding.appBar.getMeasuredHeight() + mAdapter.getCurrentTemperatureTextHeight(recyclerView)) {
                    if (mScrollY < mFirstCardMarginTop
                            - mBinding.appBar.getMeasuredHeight()
                            - mAdapter.getCurrentTemperatureTextHeight(recyclerView)) {
                        mBinding.appBar.setTranslationY(0);
                    } else if (mScrollY > mFirstCardMarginTop - mBinding.appBar.getY()) {
                        mBinding.appBar.setTranslationY(-mBinding.appBar.getMeasuredHeight());
                    } else {
                        mBinding.appBar.setTranslationY(
                                mFirstCardMarginTop
                                        - mAdapter.getCurrentTemperatureTextHeight(recyclerView)
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
                mTopOverlap = false;
            } else {
                mTopChanged = (mBinding.appBar.getTranslationY() != 0) != (mLastAppBarTranslationY != 0);
                mTopOverlap = mBinding.appBar.getTranslationY() != 0;
            }

            if (mTopChanged) {
                mWeatherView.setSystemBarColor(requireContext(), requireActivity().getWindow(),
                        mTopOverlap, false, true, false);
            }
        }
    }
}
