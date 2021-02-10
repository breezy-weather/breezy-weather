package wangdaye.com.geometricweather.main;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import java.util.HashMap;
import java.util.List;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.DarkMode;
import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.basic.model.resource.Resource;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.databinding.ActivityMainBinding;
import wangdaye.com.geometricweather.main.adapter.main.MainAdapter;
import wangdaye.com.geometricweather.main.dialog.LocationHelpDialog;
import wangdaye.com.geometricweather.main.model.LocationResource;
import wangdaye.com.geometricweather.manage.ManageFragment;
import wangdaye.com.geometricweather.main.layout.MainLayoutManager;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherViewController;
import wangdaye.com.geometricweather.ui.widget.weatherView.circularSkyView.CircularSkyWeatherView;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.MaterialWeatherView;
import wangdaye.com.geometricweather.remoteviews.NotificationUtils;
import wangdaye.com.geometricweather.remoteviews.WidgetUtils;
import wangdaye.com.geometricweather.utils.helpter.AsyncHelper;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.ui.widget.SwipeSwitchLayout;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.background.polling.PollingManager;
import wangdaye.com.geometricweather.utils.manager.ThemeManager;
import wangdaye.com.geometricweather.utils.manager.ThreadManager;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.manager.ShortcutsManager;

/**
 * Main activity.
 * */

public class MainActivity extends GeoActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    private MainActivityViewModel mViewModel;
    private ActivityMainBinding mBinding;

    private @Nullable String mPendingAction;
    private @Nullable HashMap<String, Object> mPendingExtraMap;

    private @Nullable ManageFragment mManageFragment;
    private WeatherView mWeatherView;
    private @Nullable MainAdapter mAdapter;
    private @Nullable AnimatorSet mRecyclerViewAnimator;

    private ResourceProvider mResourceProvider;
    private ThemeManager mThemeManager;

    private @Nullable String mCurrentLocationFormattedId;
    private @Nullable WeatherSource mCurrentWeatherSource;
    private long mCurrentWeatherTimeStamp;

    public static final int SETTINGS_ACTIVITY = 1;
    public static final int MANAGE_ACTIVITY = 2;
    public static final int CARD_MANAGE_ACTIVITY = 3;
    public static final int SEARCH_ACTIVITY = 4;
    public static final int SELECT_PROVIDER_ACTIVITY = 5;

    private static final long INVALID_CURRENT_WEATHER_TIME_STAMP = -1;

    public static final String ACTION_MAIN = "com.wangdaye.geometricweather.Main";
    public static final String KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID
            = "MAIN_ACTIVITY_LOCATION_FORMATTED_ID";

    public static final String ACTION_UPDATE_WEATHER_IN_BACKGROUND
            = "com.wangdaye.geomtricweather.ACTION_UPDATE_WEATHER_IN_BACKGROUND";
    public static final String KEY_LOCATION_FORMATTED_ID = "LOCATION_FORMATTED_ID";

    public static final String ACTION_SHOW_ALERTS
            = "com.wangdaye.geomtricweather.ACTION_SHOW_ALERTS";

    public static final String ACTION_SHOW_DAILY_FORECAST
            = "com.wangdaye.geomtricweather.ACTION_SHOW_DAILY_FORECAST";
    public static final String KEY_DAILY_INDEX = "DAILY_INDEX";

    private final BroadcastReceiver backgroundUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String formattedId = intent.getStringExtra(KEY_LOCATION_FORMATTED_ID);
            mViewModel.updateLocationFromBackground(MainActivity.this, formattedId);
            if (isForeground()) {
                getSnackbarContainer().postDelayed(() -> {
                    if (isForeground()
                            && formattedId != null
                            && formattedId.equals(mViewModel.getCurrentFormattedId())) {
                        SnackbarUtils.showSnackbar(
                                MainActivity.this, getString(R.string.feedback_updated_in_background));
                    }
                }, 1200);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pendingIntentAction(getIntent());

        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        // attach weather view.
        switch (SettingsOptionManager.getInstance(this).getUiStyle()) {
            case MATERIAL:
                mWeatherView = new MaterialWeatherView(this);
                break;

            case CIRCULAR:
                mWeatherView = new CircularSkyWeatherView(this);
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
        mWeatherView.setSystemBarStyle(this, getWindow(),
                false, false, true, false);

        resetUIUpdateFlag();
        ensureResourceProvider();
        updateThemeManager();

        initModel();
        initView();

        registerReceiver(
                backgroundUpdateReceiver,
                new IntentFilter(ACTION_UPDATE_WEATHER_IN_BACKGROUND)
        );
        refreshBackgroundViews(true, mViewModel.getLocationList(),
                false, false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        pendingIntentAction(intent);
        resetUIUpdateFlag();
        mViewModel.init(this, getLocationId(intent));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SETTINGS_ACTIVITY:
                ensureResourceProvider();
                updateThemeManager();

                ThreadManager.getInstance().execute(() ->
                        NotificationUtils.updateNotificationIfNecessary(
                                this, mViewModel.getLocationList()
                        )
                );
                resetUIUpdateFlag();
                mViewModel.reset(this);

                refreshBackgroundViews(true, mViewModel.getLocationList(),
                        true, true);
                break;

            case MANAGE_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    String formattedId = getLocationId(data);
                    if (TextUtils.isEmpty(formattedId)) {
                        formattedId = mViewModel.getCurrentFormattedId();
                    }
                    mViewModel.init(this, formattedId);
                }
                break;

            case CARD_MANAGE_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    resetUIUpdateFlag();
                    mViewModel.reset(this);
                }
                break;

            case SEARCH_ACTIVITY:
                if (resultCode == RESULT_OK && mManageFragment != null) {
                    mManageFragment.readAppendLocation();
                }
                break;

            case SELECT_PROVIDER_ACTIVITY:
                if (mManageFragment != null) {
                    mManageFragment.resetLocationList(mViewModel.getCurrentFormattedId());
                }
                break;
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (SettingsOptionManager.getInstance(this).getDarkMode() == DarkMode.SYSTEM) {
            updateThemeManager();
            resetUIUpdateFlag();
            mViewModel.reset(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mWeatherView.setDrawable(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mWeatherView.setDrawable(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(backgroundUpdateReceiver);
    }

    @Override
    public View getSnackbarContainer() {
        return mBinding.background;
    }

    // init.

    private void initModel() {
        mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        if (mViewModel.isNewInstance()) {
            mViewModel.init(this, getLocationId(getIntent()));
        }
    }

    @Nullable
    private String getLocationId(@Nullable Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.getStringExtra(KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID);
    }

    @SuppressLint({"ClickableViewAccessibility", "NonConstantResourceId"})
    private void initView() {
        mManageFragment = (ManageFragment) getSupportFragmentManager().findFragmentById(
                R.id.fragment_drawer);
        if (mManageFragment != null) {
            mManageFragment.setDrawerMode(true);
            mManageFragment.setRequestCodes(SEARCH_ACTIVITY, SELECT_PROVIDER_ACTIVITY);
            mManageFragment.setOnLocationListChangedListener(new ManageFragment.LocationManageCallback() {
                @Override
                public void onSelectedLocation(@NonNull String formattedId) {
                    mViewModel.init(MainActivity.this, formattedId);
                }

                @Override
                public void onLocationListChanged() {
                    mViewModel.init(MainActivity.this, mViewModel.getCurrentFormattedId());
                }
            });
        }

        ViewCompat.setOnApplyWindowInsetsListener(mBinding.appBar, (v, insets) -> new DisplayUtils.RelativeInsets(
                insets, mManageFragment != null, false, false, true).setPaddingRelative(v));

        mBinding.toolbar.inflateMenu(R.menu.activity_main);
        mBinding.toolbar.getMenu().getItem(0).setVisible(
                !MainModuleUtils.isMultiFragmentEnabled(this));
        mBinding.toolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.action_manage:
                    IntentHelper.startManageActivityForResult(this, MANAGE_ACTIVITY);
                    break;

                case R.id.action_settings:
                    IntentHelper.startSettingsActivityForResult(this, SETTINGS_ACTIVITY);
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
        mBinding.refreshLayout.setOnRefreshListener(this);

        mBinding.recyclerView.setLayoutManager(new MainLayoutManager());
        mBinding.recyclerView.setOnTouchListener(indicatorStateListener);
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.recyclerView, (v, insets) -> new DisplayUtils.RelativeInsets(
                insets, mManageFragment != null, true, false, false).setPaddingRelative(v));

        mBinding.indicator.setSwitchView(mBinding.switchLayout);

        mViewModel.getCurrentLocation().observe(this, resource -> {
            if (resource == null) {
                return;
            }

            setRefreshing(resource.status == Resource.Status.LOADING);
            drawUI(resource.data, resource.defaultLocation, resource.source);

            if (mManageFragment != null) {
                mManageFragment.updateView(mViewModel.getLocationList(), resource.data.getFormattedId());
            }

            if (resource.locateFailed) {
                SnackbarUtils.showSnackbar(
                        this,
                        getString(R.string.feedback_location_failed),
                        getString(R.string.help),
                        v -> {
                            if (isForeground()) {
                                new LocationHelpDialog().show(getSupportFragmentManager(), null);
                            }
                        }
                );
            } else if (resource.status == Resource.Status.ERROR) {
                SnackbarUtils.showSnackbar(this, getString(R.string.feedback_get_weather_failed));
            }

            consumeIntentAction();
        });

        mViewModel.getIndicator().observe(this, resource -> {
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
    private void drawUI(Location location, boolean defaultLocation, LocationResource.Source source) {
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
                    mViewModel.updateWeather(this, true);
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

        boolean oldDaytime = TimeManager.getInstance(this).isDayTime();
        boolean daytime = TimeManager.getInstance(this)
                .update(this, location)
                .isDayTime();

        setDarkMode(daytime);
        if (oldDaytime != daytime) {
            updateThemeManager();
        }

        WeatherViewController.setWeatherCode(
                mWeatherView, location.getWeather(), daytime, mResourceProvider);

        mBinding.refreshLayout.setColorSchemeColors(mWeatherView.getThemeColors(mThemeManager.isLightTheme())[0]);
        mBinding.refreshLayout.setProgressBackgroundColorSchemeColor(mThemeManager.getRootColor(this));

        boolean listAnimationEnabled = SettingsOptionManager.getInstance(this).isListAnimationEnabled();
        boolean itemAnimationEnabled = SettingsOptionManager.getInstance(this).isItemAnimationEnabled();

        if (mAdapter == null) {
            mAdapter = new MainAdapter(this, mWeatherView, location, mResourceProvider,
                    listAnimationEnabled, itemAnimationEnabled);
            mBinding.recyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.update(this, mWeatherView, location, mResourceProvider,
                    listAnimationEnabled, itemAnimationEnabled);
            mAdapter.notifyDataSetChanged();
        }

        OnScrollListener l = new OnScrollListener();
        mBinding.recyclerView.clearOnScrollListeners();
        mBinding.recyclerView.addOnScrollListener(l);
        mBinding.recyclerView.post(() -> l.onScrolled(mBinding.recyclerView, 0, 0));

        mBinding.indicator.setCurrentIndicatorColor(mThemeManager.getAccentColor(this));
        mBinding.indicator.setIndicatorColor(mThemeManager.getTextSubtitleColor(this));

        if (!listAnimationEnabled) {
            mBinding.recyclerView.setAlpha(0f);
            mRecyclerViewAnimator = new AnimatorSet();
            mRecyclerViewAnimator.playTogether(
                    ObjectAnimator.ofFloat(mBinding.recyclerView, "alpha", 0f, 1f),
                    ObjectAnimator.ofFloat(
                            mBinding.recyclerView,
                            "translationY",
                            DisplayUtils.dpToPx(this, 40), 0f
                    )
            );
            mRecyclerViewAnimator.setDuration(450);
            mRecyclerViewAnimator.setInterpolator(new DecelerateInterpolator(2f));
            mRecyclerViewAnimator.setStartDelay(150);
            mRecyclerViewAnimator.start();
        }

        refreshBackgroundViews(false, mViewModel.getLocationList(),
                defaultLocation, source != LocationResource.Source.BACKGROUND);
    }

    private void resetUI(Location location) {
        if (mWeatherView.getWeatherKind() == WeatherView.WEATHER_KING_NULL
                && location.getWeather() == null) {
            WeatherViewController.setWeatherCode(
                    mWeatherView, null, mThemeManager.isLightTheme(), mResourceProvider);
            mBinding.refreshLayout.setColorSchemeColors(
                    mWeatherView.getThemeColors(mThemeManager.isLightTheme())[0]);
            mBinding.refreshLayout.setProgressBackgroundColorSchemeColor(
                    mThemeManager.getRootColor(this));
        }
        mWeatherView.setGravitySensorEnabled(
                SettingsOptionManager.getInstance(this).isGravitySensorEnabled());

        mBinding.toolbar.setTitle(location.getCityName(this));

        mBinding.switchLayout.reset();

        if (mRecyclerViewAnimator != null) {
            mRecyclerViewAnimator.cancel();
            mRecyclerViewAnimator = null;
        }
        if (mAdapter != null) {
            mAdapter.setNullWeather();
            mAdapter.notifyDataSetChanged();
        }
    }

    private void resetUIUpdateFlag() {
        mCurrentLocationFormattedId = null;
        mCurrentWeatherSource = null;
        mCurrentWeatherTimeStamp = INVALID_CURRENT_WEATHER_TIME_STAMP;
    }

    private void ensureResourceProvider() {
        String iconProvider = SettingsOptionManager.getInstance(this).getIconProvider();
        if (mResourceProvider == null
                || !mResourceProvider.getPackageName().equals(iconProvider)) {
            mResourceProvider = ResourcesProviderFactory.getNewInstance();
        }
    }

    private void updateThemeManager() {
        if (mThemeManager == null) {
            mThemeManager = ThemeManager.getInstance(this);
        }
        mThemeManager.update(this, mWeatherView);
    }

    @SuppressLint("RestrictedApi")
    private void setDarkMode(boolean dayTime) {
        if (SettingsOptionManager.getInstance(this).getDarkMode() == DarkMode.AUTO) {
            int mode = dayTime ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
            getDelegate().setLocalNightMode(mode);
            AppCompatDelegate.setDefaultNightMode(mode);
        } else if (SettingsOptionManager.getInstance(this).getDarkMode() == DarkMode.SYSTEM) {
            int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            getDelegate().setLocalNightMode(mode);
            AppCompatDelegate.setDefaultNightMode(mode);
        }
    }

    private void setRefreshing(final boolean b) {
        mBinding.refreshLayout.post(() -> mBinding.refreshLayout.setRefreshing(b));
    }

    private void refreshBackgroundViews(boolean resetBackground, List<Location> locationList,
                                        boolean defaultLocationChanged, boolean updateRemoteViews) {
        if (resetBackground) {
            AsyncHelper.delayRunOnIO(() -> PollingManager.resetAllBackgroundTask(
                    this, false
            ), 1000);
        }

        if (updateRemoteViews) {
            AsyncHelper.delayRunOnIO(() -> {
                if (defaultLocationChanged) {
                    WidgetUtils.updateWidgetIfNecessary(this, locationList.get(0));
                    NotificationUtils.updateNotificationIfNecessary(this, locationList);
                }
                WidgetUtils.updateWidgetIfNecessary(this, locationList);
            }, 1000);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                ShortcutsManager.refreshShortcutsInNewThread(this, locationList);
            }
        }
    }

    private void pendingIntentAction(Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            mPendingAction = null;
            mPendingExtraMap = null;
            return;
        }

        if (action.equals(ACTION_SHOW_ALERTS)) {
            mPendingAction = ACTION_SHOW_ALERTS;
            mPendingExtraMap = new HashMap<>();
        } else if (action.equals(ACTION_SHOW_DAILY_FORECAST)) {
            mPendingAction = ACTION_SHOW_DAILY_FORECAST;

            mPendingExtraMap = new HashMap<>();
            mPendingExtraMap.put(KEY_DAILY_INDEX, intent.getIntExtra(KEY_DAILY_INDEX, 0));
        }
    }

    private void consumeIntentAction() {
        String action = mPendingAction;
        HashMap<String, Object> extraMap = mPendingExtraMap;
        mPendingAction = null;
        mPendingExtraMap = null;
        if (TextUtils.isEmpty(action) || extraMap == null) {
            return;
        }

        if (action.equals(ACTION_SHOW_ALERTS)) {
            Location location = mViewModel.getCurrentLocationValue();
            if (location != null) {
                Weather weather = location.getWeather();
                if (weather != null) {
                    IntentHelper.startAlertActivity(this, weather);
                }
            }
        } else if (action.equals(ACTION_SHOW_DAILY_FORECAST)) {
            String formattedId = mViewModel.getCurrentFormattedId();
            Integer index = (Integer) extraMap.get(KEY_DAILY_INDEX);
            if (formattedId != null && index != null) {
                IntentHelper.startDailyWeatherActivity(
                        this, mViewModel.getCurrentFormattedId(), index);
            }
        }
    }

    // interface.

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

        private @Nullable Location location;
        private boolean indexSwitched;

        private float lastProgress = 0;

        @Override
        public void onSwipeProgressChanged(int swipeDirection, float progress) {
            mBinding.indicator.setDisplayState(progress != 0);

            indexSwitched = false;

            if (progress >= 1 && lastProgress < 0.5) {
                indexSwitched = true;
                location = mViewModel.getLocationFromList(
                        swipeDirection == SwipeSwitchLayout.SWIPE_DIRECTION_LEFT ? 1 : -1);
                lastProgress = 1;
            } else if (progress < 0.5 && lastProgress >= 1) {
                indexSwitched = true;
                location = mViewModel.getLocationFromList(0);
                lastProgress = 0;
            }

            if (indexSwitched && location != null) {
                mBinding.toolbar.setTitle(location.getCityName(MainActivity.this));
                if (location.getWeather() != null) {
                    WeatherViewController.setWeatherCode(
                            mWeatherView,
                            location.getWeather(),
                            TimeManager.isDaylight(location),
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
                mViewModel.setLocation(
                        MainActivity.this,
                        swipeDirection == SwipeSwitchLayout.SWIPE_DIRECTION_LEFT ? 1 : -1
                );
            }
        }
    };

    // on refresh listener.

    @Override
    public void onRefresh() {
        mViewModel.updateWeather(this, true);
    }

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
                mWeatherView.setSystemBarColor(MainActivity.this, getWindow(),
                        mTopOverlap, false, true, false);
            }
        }
    }
}