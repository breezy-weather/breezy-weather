package wangdaye.com.geometricweather.main;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.polling.PollingManager;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.GeoDialog;
import wangdaye.com.geometricweather.basic.models.Location;
import wangdaye.com.geometricweather.basic.models.options.DarkMode;
import wangdaye.com.geometricweather.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.basic.models.resources.Resource;
import wangdaye.com.geometricweather.basic.models.weather.Weather;
import wangdaye.com.geometricweather.databinding.ActivityMainBinding;
import wangdaye.com.geometricweather.main.adapters.main.MainAdapter;
import wangdaye.com.geometricweather.main.dialogs.BackgroundLocationDialog;
import wangdaye.com.geometricweather.main.dialogs.LocationHelpDialog;
import wangdaye.com.geometricweather.main.dialogs.LocationPermissionStatementDialog;
import wangdaye.com.geometricweather.main.layouts.MainLayoutManager;
import wangdaye.com.geometricweather.main.models.PermissionsRequest;
import wangdaye.com.geometricweather.main.utils.MainModuleUtils;
import wangdaye.com.geometricweather.main.utils.StatementManager;
import wangdaye.com.geometricweather.management.ManagementFragment;
import wangdaye.com.geometricweather.remoteviews.NotificationUtils;
import wangdaye.com.geometricweather.remoteviews.WidgetUtils;
import wangdaye.com.geometricweather.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.resource.providers.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widgets.SwipeSwitchLayout;
import wangdaye.com.geometricweather.ui.widgets.weatherView.WeatherView;
import wangdaye.com.geometricweather.ui.widgets.weatherView.WeatherViewController;
import wangdaye.com.geometricweather.ui.widgets.weatherView.circularSkyView.CircularSkyWeatherView;
import wangdaye.com.geometricweather.ui.widgets.weatherView.materialWeatherView.MaterialWeatherView;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.helpters.AsyncHelper;
import wangdaye.com.geometricweather.utils.helpters.IntentHelper;
import wangdaye.com.geometricweather.utils.helpters.SnackbarHelper;
import wangdaye.com.geometricweather.utils.managers.ShortcutsManager;
import wangdaye.com.geometricweather.utils.managers.ThemeManager;
import wangdaye.com.geometricweather.utils.managers.ThreadManager;
import wangdaye.com.geometricweather.utils.managers.TimeManager;

/**
 * Main activity.
 * */

public class MainActivity extends GeoActivity
        implements SwipeRefreshLayout.OnRefreshListener, LocationPermissionStatementDialog.Callback,
        BackgroundLocationDialog.Callback {

    private MainActivityViewModel mViewModel;
    private ActivityMainBinding mBinding;

    private @Nullable String mPendingAction;
    private @Nullable HashMap<String, Object> mPendingExtraMap;

    private @Nullable ManagementFragment mManagementFragment;
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
    public static final String KEY_LOCATION = "LOCATION";

    public static final String ACTION_SHOW_ALERTS
            = "com.wangdaye.geomtricweather.ACTION_SHOW_ALERTS";

    public static final String ACTION_SHOW_DAILY_FORECAST
            = "com.wangdaye.geomtricweather.ACTION_SHOW_DAILY_FORECAST";
    public static final String KEY_DAILY_INDEX = "DAILY_INDEX";

    private final BroadcastReceiver backgroundUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = (Location) intent.getSerializableExtra(KEY_LOCATION);
            if (location == null) {
                return;
            }

            mViewModel.updateLocationFromBackground(location);
            if (isForeground()
                    && location.getFormattedId().equals(mViewModel.getCurrentFormattedId())) {
                SnackbarHelper.showSnackbar(getString(R.string.feedback_updated_in_background));
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

        initModel(savedInstanceState == null);
        initView();

        LocalBroadcastManager.getInstance(this).registerReceiver(
                backgroundUpdateReceiver,
                new IntentFilter(ACTION_UPDATE_WEATHER_IN_BACKGROUND)
        );
        refreshBackgroundViews(true, mViewModel.getValidLocationList(),
                false, false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        pendingIntentAction(intent);
        resetUIUpdateFlag();
        mViewModel.init(getLocationId(intent));
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
                                this, mViewModel.getValidLocationList()
                        )
                );
                resetUIUpdateFlag();
                mViewModel.init();

                refreshBackgroundViews(true, mViewModel.getValidLocationList(),
                        true, true);
                break;

            case MANAGE_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    String formattedId = getLocationId(data);
                    if (TextUtils.isEmpty(formattedId)) {
                        formattedId = mViewModel.getCurrentFormattedId();
                    }
                    mViewModel.init(formattedId);
                }
                break;

            case CARD_MANAGE_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    resetUIUpdateFlag();
                    mViewModel.init();
                }
                break;

            case SEARCH_ACTIVITY:
                if (resultCode == RESULT_OK && mManagementFragment != null) {
                    mManagementFragment.readAppendLocation();
                }
                break;

            case SELECT_PROVIDER_ACTIVITY:
                if (mManagementFragment != null) {
                    mManagementFragment.resetLocationList(mViewModel.getCurrentFormattedId());
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
            mViewModel.init();
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(backgroundUpdateReceiver);
    }

    @Override
    public View getSnackbarContainer() {
        return mBinding.background;
    }

    // init.

    private void initModel(boolean newActivity) {
        mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        if (!mViewModel.checkIsNewInstance()) {
            return;
        }

        if (newActivity) {
            mViewModel.init(getLocationId(getIntent()));
        } else {
            mViewModel.init();
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
        mManagementFragment = (ManagementFragment) getSupportFragmentManager().findFragmentById(
                R.id.fragment_drawer);
        if (mManagementFragment != null) {
            mManagementFragment.setDrawerMode(true);
            mManagementFragment.setRequestCodes(SEARCH_ACTIVITY, SELECT_PROVIDER_ACTIVITY);
            mManagementFragment.setOnLocationListChangedListener(new ManagementFragment.LocationManageCallback() {
                @Override
                public void onSelectedLocation(@NonNull String formattedId) {
                    mViewModel.setLocation(formattedId);
                }

                @Override
                public void onLocationListChanged(List<Location> locationList) {
                    mViewModel.updateLocationList(locationList);
                }
            });
        }

        ViewCompat.setOnApplyWindowInsetsListener(mBinding.appBar, (v, insets) -> new DisplayUtils.RelativeInsets(
                insets, mManagementFragment != null, false, false, true).setPaddingRelative(v));

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
                insets, mManagementFragment != null, true, false, false).setPaddingRelative(v));

        mBinding.indicator.setSwitchView(mBinding.switchLayout);

        mViewModel.getCurrentLocation().observe(this, resource -> {
            if (resource == null) {
                return;
            }

            setRefreshing(resource.status == Resource.Status.LOADING);
            drawUI(resource.data, resource.defaultLocation, resource.fromBackgroundUpdate);

            if (mManagementFragment != null) {
                mManagementFragment.updateView(
                        mViewModel.getTotalLocationList(), mViewModel.getCurrentFormattedId());
            }

            if (resource.locateFailed) {
                SnackbarHelper.showSnackbar(
                        getString(R.string.feedback_location_failed),
                        getString(R.string.help),
                        v -> {
                            if (isForeground()) {
                                new LocationHelpDialog().show(getSupportFragmentManager(), null);
                            }
                        }
                );
            } else if (resource.status == Resource.Status.ERROR) {
                SnackbarHelper.showSnackbar(getString(R.string.feedback_get_weather_failed));
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

        mViewModel.getPermissionsRequest().observe(this, request -> {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                    || request.permissionList.size() == 0
                    || !request.consume()) {
                return;
            }

            // only show dialog if we need request basic location permissions.
            boolean needShowDialog = false;
            for (String permission : request.permissionList) {
                if (isLocationPermission(permission)) {
                    needShowDialog = true;
                    break;
                }
            }
            if (needShowDialog
                    && !StatementManager.getInstance(this).isLocationPermissionDeclared()) {
                // only show dialog once.
                LocationPermissionStatementDialog dialog = new LocationPermissionStatementDialog();
                dialog.setCancelable(false);
                dialog.show(getSupportFragmentManager(), null);
            } else {
                requestPermissions(request.permissionList.toArray(new String[0]), 0);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PermissionsRequest request = mViewModel.getPermissionsRequestValue();
        if (request.permissionList.size() == 0 || request.target == null) {
            return;
        }

        for (int i = 0; i < permissions.length && i < grantResults.length; i++) {
            if (isForegroundLocationPermission(permissions[i])
                    && grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                // denied basic location permissions.
                if (request.target.isUsable()) {
                    mViewModel.updateWeather(request.triggeredByUser, false);
                } else {
                    mViewModel.requestPermissionsFailed(request.target);
                }
                return;
            }
        }

        // check background location permissions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && !StatementManager.getInstance(this).isBackgroundLocationDeclared()
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            new BackgroundLocationDialog().show(getSupportFragmentManager(), null);
        }

        mViewModel.updateWeather(request.triggeredByUser, false);
    }

    private boolean isLocationPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                    || permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)
                    || permission.equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        } else {
            return isForegroundLocationPermission(permission);
        }
    }

    private boolean isForegroundLocationPermission(String permission) {
        return permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                || permission.equals(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    // control.

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    private void drawUI(Location location, boolean defaultLocation, boolean fromBackgroundUpdate) {
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

        refreshBackgroundViews(false, mViewModel.getValidLocationList(),
                defaultLocation, !fromBackgroundUpdate);
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

        if (ACTION_SHOW_ALERTS.equals(action)) {
            Location location = mViewModel.getCurrentLocationValue();
            if (location != null) {
                Weather weather = location.getWeather();
                if (weather != null) {
                    IntentHelper.startAlertActivity(this, weather);
                }
            }
        } else if (ACTION_SHOW_DAILY_FORECAST.equals(action)) {
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
                mBinding.toolbar.setTitle(mLocation.getCityName(MainActivity.this));
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

    // on refresh listener.

    @Override
    public void onRefresh() {
        mViewModel.updateWeather(true, true);
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

    // location permissions statement callback.

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void requestLocationPermissions() {
        StatementManager.getInstance(this).setLocationPermissionDeclared(this);

        PermissionsRequest request = mViewModel.getPermissionsRequestValue();
        if (request.permissionList.size() != 0 && request.target != null) {
            requestPermissions(request.permissionList.toArray(new String[0]), 0);
        }

        for (GeoDialog dialog : getDialogSet()) {
            if (dialog instanceof LocationPermissionStatementDialog) {
                dialog.dismiss();
            }
        }
    }

    // background location permissions callback.

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void requestBackgroundLocationPermission() {
        StatementManager.getInstance(this).setBackgroundLocationDeclared(this);

        List<String> permissionList = new ArrayList<>();
        permissionList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);

        requestPermissions(permissionList.toArray(new String[0]), 0);

        for (GeoDialog dialog : getDialogSet()) {
            if (dialog instanceof BackgroundLocationDialog) {
                dialog.dismiss();
            }
        }
    }
}