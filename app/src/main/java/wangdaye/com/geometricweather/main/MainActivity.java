package wangdaye.com.geometricweather.main;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.google.android.material.appbar.AppBarLayout;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.DarkMode;
import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.basic.model.resource.Resource;
import wangdaye.com.geometricweather.main.adapter.main.MainAdapter;
import wangdaye.com.geometricweather.main.dialog.LocationHelpDialog;
import wangdaye.com.geometricweather.ui.fragment.LocationManageFragment;
import wangdaye.com.geometricweather.main.layout.MainLayoutManager;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.verticalScrollView.VerticalRecyclerView;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherViewController;
import wangdaye.com.geometricweather.ui.widget.weatherView.circularSkyView.CircularSkyWeatherView;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.MaterialWeatherView;
import wangdaye.com.geometricweather.remoteviews.NotificationUtils;
import wangdaye.com.geometricweather.remoteviews.WidgetUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.ui.widget.InkPageIndicator;
import wangdaye.com.geometricweather.ui.widget.SwipeSwitchLayout;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.background.polling.PollingManager;
import wangdaye.com.geometricweather.utils.manager.ThemeManager;
import wangdaye.com.geometricweather.utils.manager.ThreadManager;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.ui.widget.verticalScrollView.VerticalSwipeRefreshLayout;
import wangdaye.com.geometricweather.utils.manager.ShortcutsManager;

/**
 * Main activity.
 * */

public class MainActivity extends GeoActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    private MainActivityViewModel viewModel;

    private CoordinatorLayout background;
    @Nullable private LocationManageFragment manageFragment;

    private WeatherView weatherView;
    private AppBarLayout appBar;
    private Toolbar toolbar;

    private InkPageIndicator indicator;

    private SwipeSwitchLayout switchLayout;
    private VerticalSwipeRefreshLayout refreshLayout;
    private VerticalRecyclerView recyclerView;

    @Nullable private MainAdapter adapter;
    @Nullable private AnimatorSet recyclerViewAnimator;

    private ResourceProvider resourceProvider;
    private ThemeManager themeManager;

    @Nullable private String currentLocationFormattedId;
    @Nullable private WeatherSource currentWeatherSource;
    private long currentWeatherTimeStamp;

    public static final int SETTINGS_ACTIVITY = 1;
    public static final int MANAGE_ACTIVITY = 2;
    public static final int CARD_MANAGE_ACTIVITY = 3;
    public static final int SEARCH_ACTIVITY = 4;
    public static final int SELECT_PROVIDER_ACTIVITY = 5;

    private static final long INVALID_CURRENT_WEATHER_TIME_STAMP = -1;

    public static final String KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID
            = "MAIN_ACTIVITY_LOCATION_FORMATTED_ID";

    public static final String ACTION_UPDATE_WEATHER_IN_BACKGROUND
            = "com.wangdaye.geomtricweather.ACTION_UPDATE_WEATHER_IN_BACKGROUND";
    public static final String KEY_LOCATION_FORMATTED_ID = "LOCATION_FORMATTED_ID";

    private BroadcastReceiver backgroundUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String formattedId = intent.getStringExtra(KEY_LOCATION_FORMATTED_ID);
            viewModel.updateLocationFromBackground(MainActivity.this, formattedId);
            if (isForeground()) {
                getSnackbarContainer().postDelayed(() -> {
                    if (isForeground()
                            && formattedId != null
                            && formattedId.equals(viewModel.getCurrentLocationFormattedId())) {
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
        DisplayUtils.setSystemBarStyle(MainActivity.this, getWindow(), true,
                false, false, true, false);

        if (MainDisplayUtils.isMultiFragmentEnabled(this)) {
            setContentView(R.layout.activity_main_tablet);
        } else {
            setContentView(R.layout.activity_main);
        }

        // attach weather view.
        switch (SettingsOptionManager.getInstance(this).getUiStyle()) {
            case MATERIAL:
                weatherView = new MaterialWeatherView(this);
                break;

            case CIRCULAR:
                weatherView = new CircularSkyWeatherView(this);
                break;
        }
        ((CoordinatorLayout) findViewById(R.id.activity_main_switchView).getParent()).addView(
                (View) weatherView,
                0,
                new CoordinatorLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        resetUIUpdateFlag();
        ensureResourceProvider();
        updateThemeManager();

        initModel();
        initView();

        registerReceiver(
                backgroundUpdateReceiver,
                new IntentFilter(ACTION_UPDATE_WEATHER_IN_BACKGROUND)
        );
        refreshBackgroundViews(true, viewModel.getLocationList(),
                false, false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        resetUIUpdateFlag();
        viewModel.init(this, getLocationId(intent));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SETTINGS_ACTIVITY:
                ensureResourceProvider();
                updateThemeManager();

                Location location = viewModel.getCurrentLocationValue();
                if (location != null) {
                    ThreadManager.getInstance().execute(() ->
                            NotificationUtils.updateNotificationIfNecessary(this, location));
                }
                resetUIUpdateFlag();
                viewModel.reset(this);

                refreshBackgroundViews(true, viewModel.getLocationList(),
                        true, true);
                break;

            case MANAGE_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    String formattedId = getLocationId(data);
                    if (TextUtils.isEmpty(formattedId)) {
                        formattedId = viewModel.getCurrentLocationFormattedId();
                    }
                    viewModel.init(this, formattedId);
                }
                break;

            case CARD_MANAGE_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    resetUIUpdateFlag();
                    viewModel.reset(this);
                }
                break;

            case SEARCH_ACTIVITY:
                if (resultCode == RESULT_OK && manageFragment != null) {
                    manageFragment.addLocation();
                }
                break;

            case SELECT_PROVIDER_ACTIVITY:
                if (manageFragment != null) {
                    manageFragment.resetLocationList();
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        weatherView.setDrawable(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        weatherView.setDrawable(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(backgroundUpdateReceiver);
    }

    @Override
    public View getSnackbarContainer() {
        return background;
    }

    // init.

    private void initModel() {
        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        if (viewModel.isNewInstance()) {
            viewModel.init(this, getLocationId(getIntent()));
        }
    }

    @Nullable
    private String getLocationId(@Nullable Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.getStringExtra(KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        this.background = findViewById(R.id.activity_main_background);

        if (MainDisplayUtils.isMultiFragmentEnabled(this)) {
            this.manageFragment = new LocationManageFragment();
            manageFragment.setDrawerMode(true);
            manageFragment.setRequestCodes(SEARCH_ACTIVITY, SELECT_PROVIDER_ACTIVITY);
            manageFragment.setOnLocationListChangedListener(new LocationManageFragment.LocationManageCallback() {
                @Override
                public void onSelectedLocation(@NonNull String formattedId) {
                    viewModel.init(MainActivity.this, formattedId);
                }

                @Override
                public void onLocationListChanged() {
                    viewModel.init(MainActivity.this, viewModel.getCurrentLocationFormattedId());
                }
            });
            getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.activity_main_locationContainer, manageFragment)
                    .commit();
        }

        this.appBar = findViewById(R.id.activity_main_appBar);
        ViewCompat.setOnApplyWindowInsetsListener(appBar, (v, insets) -> {
            v.setPadding(
                    manageFragment != null ? 0 : insets.getSystemWindowInsetLeft(),
                    insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(),
                    0
            );
            return insets;
        });

        this.toolbar = findViewById(R.id.activity_main_toolbar);
        toolbar.inflateMenu(R.menu.activity_main);
        toolbar.getMenu().getItem(0).setVisible(!MainDisplayUtils.isMultiFragmentEnabled(this));
        toolbar.setOnMenuItemClickListener(menuItem -> {
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

        this.switchLayout = findViewById(R.id.activity_main_switchView);
        switchLayout.setOnSwitchListener(switchListener);

        this.refreshLayout = findViewById(R.id.activity_main_refreshView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            refreshLayout.setOnApplyWindowInsetsListener((v, insets) -> {
                int startPosition = insets.getSystemWindowInsetTop()
                        + getResources().getDimensionPixelSize(R.dimen.normal_margin);
                refreshLayout.setProgressViewOffset(
                        false,
                        startPosition,
                        (int) (startPosition + 64 * getResources().getDisplayMetrics().density)
                );
                return insets;
            });
        }

        refreshLayout.setOnRefreshListener(this);

        this.recyclerView = findViewById(R.id.activity_main_recyclerView);
        recyclerView.setLayoutManager(new MainLayoutManager());
        recyclerView.setOnTouchListener(indicatorStateListener);
        ViewCompat.setOnApplyWindowInsetsListener(recyclerView, (v, insets) -> {
            v.setPadding(
                    manageFragment != null ? 0 : insets.getSystemWindowInsetLeft(),
                    0,
                    insets.getSystemWindowInsetRight(),
                    insets.getSystemWindowInsetBottom()
            );
            return insets;
        });

        this.indicator = findViewById(R.id.activity_main_indicator);
        indicator.setSwitchView(switchLayout);

        viewModel.getCurrentLocation().observe(this, resource -> {
            boolean updateInBackground = resource.consumeUpdatedInBackground();

            setRefreshing(resource.status == Resource.Status.LOADING);
            drawUI(resource.data, resource.isDefaultLocation(), updateInBackground);

            if (resource.isLocateFailed()) {
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
        });

        viewModel.getIndicator().observe(this, resource -> {
            switchLayout.setEnabled(resource.total > 1);

            if (switchLayout.getTotalCount() != resource.total
                    || switchLayout.getPosition() != resource.index) {
                switchLayout.setData(resource.index, resource.total);
                indicator.setSwitchView(switchLayout);
            }

            if (resource.total > 1) {
                indicator.setVisibility(View.VISIBLE);
            } else {
                indicator.setVisibility(View.GONE);
            }
        });
    }

    // control.

    @SuppressLint("SetTextI18n")
    private void drawUI(Location location, boolean defaultLocation, boolean updatedInBackground) {
        if (location.equals(currentLocationFormattedId)
                && location.getWeatherSource() == currentWeatherSource
                && location.getWeather() != null
                && location.getWeather().getBase().getTimeStamp() == currentWeatherTimeStamp) {
            return;
        }

        boolean needToResetUI = !location.equals(currentLocationFormattedId)
                || currentWeatherSource != location.getWeatherSource()
                || currentWeatherTimeStamp != INVALID_CURRENT_WEATHER_TIME_STAMP;

        currentLocationFormattedId = location.getFormattedId();
        currentWeatherSource = location.getWeatherSource();
        currentWeatherTimeStamp = location.getWeather() != null
                ? location.getWeather().getBase().getTimeStamp()
                : INVALID_CURRENT_WEATHER_TIME_STAMP;

        if (location.getWeather() == null) {
            resetUI(location);
            return;
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
        if (manageFragment != null) {
            manageFragment.updateView(viewModel.getLocationList());
        }

        WeatherViewController.setWeatherCode(
                weatherView, location.getWeather(), daytime, resourceProvider);

        refreshLayout.setColorSchemeColors(weatherView.getThemeColors(themeManager.isLightTheme())[0]);
        refreshLayout.setProgressBackgroundColorSchemeColor(themeManager.getRootColor(this));

        boolean listAnimationEnabled = SettingsOptionManager.getInstance(this).isListAnimationEnabled();
        boolean itemAnimationEnabled = SettingsOptionManager.getInstance(this).isItemAnimationEnabled();

        if (adapter == null) {
            adapter = new MainAdapter(
                    this, location, resourceProvider, listAnimationEnabled, itemAnimationEnabled);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.reset(
                    this, location, resourceProvider, listAnimationEnabled, itemAnimationEnabled);
            adapter.notifyDataSetChanged();
        }

        OnScrollListener l = new OnScrollListener();
        recyclerView.clearOnScrollListeners();
        recyclerView.addOnScrollListener(l);
        recyclerView.post(() -> l.onScrolled(recyclerView, 0, 0));

        indicator.setCurrentIndicatorColor(themeManager.getAccentColor(this));
        indicator.setIndicatorColor(themeManager.getTextSubtitleColor(this));

        if (!listAnimationEnabled) {
            recyclerView.setAlpha(0f);
            recyclerViewAnimator = new AnimatorSet();
            recyclerViewAnimator.playTogether(
                    ObjectAnimator.ofFloat(recyclerView, "alpha", 0f, 1f),
                    ObjectAnimator.ofFloat(
                            recyclerView,
                            "translationY",
                            DisplayUtils.dpToPx(this, 40), 0f
                    )
            );
            recyclerViewAnimator.setDuration(450);
            recyclerViewAnimator.setInterpolator(new DecelerateInterpolator(2f));
            recyclerViewAnimator.setStartDelay(150);
            recyclerViewAnimator.start();
        }

        refreshBackgroundViews(false, viewModel.getLocationList(),
                defaultLocation, !updatedInBackground);
    }

    private void resetUI(Location location) {
        if (weatherView.getWeatherKind() == WeatherView.WEATHER_KING_NULL
                && location.getWeather() == null) {
            WeatherViewController.setWeatherCode(
                    weatherView, null, themeManager.isLightTheme(), resourceProvider);
            refreshLayout.setColorSchemeColors(weatherView.getThemeColors(themeManager.isLightTheme())[0]);
            refreshLayout.setProgressBackgroundColorSchemeColor(themeManager.getRootColor(this));
        }
        weatherView.setGravitySensorEnabled(
                SettingsOptionManager.getInstance(this).isGravitySensorEnabled());

        toolbar.setTitle(location.getCityName(this));

        switchLayout.reset();

        if (recyclerViewAnimator != null) {
            recyclerViewAnimator.cancel();
            recyclerViewAnimator = null;
        }
        if (adapter != null) {
            adapter.setNullWeather();
            adapter.notifyDataSetChanged();
        }
    }

    private void resetUIUpdateFlag() {
        currentLocationFormattedId = null;
        currentWeatherSource = null;
        currentWeatherTimeStamp = INVALID_CURRENT_WEATHER_TIME_STAMP;
    }

    private void ensureResourceProvider() {
        String iconProvider = SettingsOptionManager.getInstance(this).getIconProvider();
        if (resourceProvider == null
                || !resourceProvider.getPackageName().equals(iconProvider)) {
            resourceProvider = ResourcesProviderFactory.getNewInstance();
        }
    }

    private void updateThemeManager() {
        if (themeManager == null) {
            themeManager = ThemeManager.getInstance(this);
        }
        themeManager.update(this, weatherView);
    }

    @SuppressLint("RestrictedApi")
    private void setDarkMode(boolean dayTime) {
        if (SettingsOptionManager.getInstance(this).getDarkMode() == DarkMode.AUTO) {
            int mode = dayTime ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
            getDelegate().setLocalNightMode(mode);
            AppCompatDelegate.setDefaultNightMode(mode);
        }
    }

    private void setRefreshing(final boolean b) {
        refreshLayout.post(() -> refreshLayout.setRefreshing(b));
    }

    private void refreshBackgroundViews(boolean resetBackground, List<Location> locationList,
                                        boolean defaultLocationChanged, boolean updateRemoteViews) {
        if (resetBackground) {
            Observable.create(emitter -> PollingManager.resetAllBackgroundTask(this, false))
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .delay(1, TimeUnit.SECONDS)
                    .subscribe();
        }

        if (updateRemoteViews) {
            Observable.create(emitter -> {
                if (defaultLocationChanged) {
                    WidgetUtils.updateWidgetIfNecessary(this, locationList.get(0));
                    NotificationUtils.updateNotificationIfNecessary(this, locationList.get(0));
                }
                WidgetUtils.updateWidgetIfNecessary(this, locationList);
            }).subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .delay(1, TimeUnit.SECONDS)
                    .subscribe();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                ShortcutsManager.refreshShortcutsInNewThread(this, locationList);
            }
        }
    }

    // interface.

    // on touch listener.

    private View.OnTouchListener indicatorStateListener = new View.OnTouchListener() {

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    indicator.setDisplayState(true);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    indicator.setDisplayState(false);
                    break;
            }
            return false;
        }
    };

    // on swipe listener(swipe switch layout).

    private SwipeSwitchLayout.OnSwitchListener switchListener = new SwipeSwitchLayout.OnSwitchListener() {

        private Location location;
        private boolean indexSwitched;

        private float lastProgress = 0;

        @Override
        public void onSwipeProgressChanged(int swipeDirection, float progress) {
            indicator.setDisplayState(progress != 0);

            indexSwitched = false;

            if (progress >= 1 && lastProgress < 0.5) {
                indexSwitched = true;
                location = viewModel.getLocationFromList(swipeDirection == SwipeSwitchLayout.SWIPE_DIRECTION_LEFT ? 1 : -1);
                lastProgress = 1;
            } else if (progress < 0.5 && lastProgress >= 1) {
                indexSwitched = true;
                location = viewModel.getLocationFromList(0);
                lastProgress = 0;
            }

            if (indexSwitched) {
                toolbar.setTitle(location.getCityName(MainActivity.this));
                if (location.getWeather() != null) {
                    WeatherViewController.setWeatherCode(
                            weatherView,
                            location.getWeather(),
                            TimeManager.isDaylight(location),
                            resourceProvider
                    );
                }
            }
        }

        @Override
        public void onSwipeReleased(int swipeDirection, boolean doSwitch) {
            if (doSwitch) {
                resetUIUpdateFlag();

                indicator.setDisplayState(false);
                viewModel.setLocation(
                        MainActivity.this,
                        swipeDirection == SwipeSwitchLayout.SWIPE_DIRECTION_LEFT ? 1 : -1
                );
            }
        }
    };

    // on refresh listener.

    @Override
    public void onRefresh() {
        viewModel.updateWeather(this);
    }

    // on scroll changed listener.

    private class OnScrollListener extends RecyclerView.OnScrollListener {

        private @Nullable Boolean topChanged;
        private boolean topOverlap;

        private int firstCardMarginTop;

        private int oldScrollY;
        private int scrollY;

        OnScrollListener() {
            super();

            this.topChanged = null;
            this.topOverlap = false;

            this.firstCardMarginTop = 0;

            this.oldScrollY = 0;
            this.scrollY = 0;
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            if (recyclerView.getChildCount() > 0) {
                firstCardMarginTop = recyclerView.getChildAt(0).getMeasuredHeight();
            } else {
                firstCardMarginTop = -1;
            }

            scrollY = recyclerView.computeVerticalScrollOffset();
            oldScrollY = scrollY - dy;

            weatherView.onScroll(scrollY);
            if (adapter != null) {
                adapter.onScroll(recyclerView);
            }

            // set translation y of toolbar.
            if (adapter != null && firstCardMarginTop > 0) {
                if (firstCardMarginTop
                        >= appBar.getMeasuredHeight() + adapter.getCurrentTemperatureTextHeight(recyclerView)) {
                    if (scrollY < firstCardMarginTop
                            - appBar.getMeasuredHeight()
                            - adapter.getCurrentTemperatureTextHeight(recyclerView)) {
                        appBar.setTranslationY(0);
                    } else if (scrollY > firstCardMarginTop - appBar.getY()) {
                        appBar.setTranslationY(-appBar.getMeasuredHeight());
                    } else {
                        appBar.setTranslationY(
                                firstCardMarginTop
                                        - adapter.getCurrentTemperatureTextHeight(recyclerView)
                                        - scrollY
                                        - appBar.getMeasuredHeight()
                        );
                    }
                } else {
                    appBar.setTranslationY(-scrollY);
                }
            }

            // set system bar style.
            if (firstCardMarginTop <= 0) {
                topChanged = true;
                topOverlap = false;
            } else if (scrollY >= firstCardMarginTop) {
                topChanged = oldScrollY < firstCardMarginTop;
                topOverlap = true;
            } else {
                topChanged = oldScrollY >= firstCardMarginTop;
                topOverlap = false;
            }

            if (topChanged) {
                DisplayUtils.setSystemBarColor(MainActivity.this, getWindow(), true,
                        topOverlap, false, true, false);
            }
        }
    }
}