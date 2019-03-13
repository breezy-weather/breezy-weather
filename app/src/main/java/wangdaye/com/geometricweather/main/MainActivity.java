package wangdaye.com.geometricweather.main;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.nekocode.rxlifecycle.LifecycleEvent;
import cn.nekocode.rxlifecycle.compact.RxLifecycleCompact;
import io.reactivex.Observable;
import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.History;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.ui.dialog.LocationHelpDialog;
import wangdaye.com.geometricweather.ui.widget.StatusBarView;
import wangdaye.com.geometricweather.ui.widget.verticalScrollView.VerticalNestedScrollView;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherViewController;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.MaterialWeatherView;
import wangdaye.com.geometricweather.remote.NotificationUtils;
import wangdaye.com.geometricweather.remote.WidgetUtils;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.location.LocationHelper;
import wangdaye.com.geometricweather.weather.WeatherHelper;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.ui.widget.InkPageIndicator;
import wangdaye.com.geometricweather.ui.widget.verticalScrollView.SwipeSwitchLayout;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.background.BackgroundManager;
import wangdaye.com.geometricweather.utils.manager.ThreadManager;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.ui.widget.verticalScrollView.VerticalSwipeRefreshLayout;
import wangdaye.com.geometricweather.utils.manager.ShortcutsManager;

/**
 * Main activity.
 * */

public class MainActivity extends GeoActivity
        implements SwipeSwitchLayout.OnSwitchListener, SwipeRefreshLayout.OnRefreshListener,
        LocationHelper.OnRequestLocationListener, WeatherHelper.OnRequestWeatherListener {

    private StatusBarView statusBar;
    private WeatherView weatherView;
    private LinearLayout appBar;
    private Toolbar toolbar;

    private InkPageIndicator indicator;

    private SwipeSwitchLayout switchLayout;
    private VerticalSwipeRefreshLayout refreshLayout;
    private VerticalNestedScrollView scrollView;

    private MainControllerAdapter adapter;
    private AnimatorSet initAnimator;

    private List<Location> locationList;
    public Location locationNow;

    private WeatherHelper weatherHelper;
    private LocationHelper locationHelper;

    private final int LOCATION_PERMISSIONS_REQUEST_CODE = 1;

    public static final int SETTINGS_ACTIVITY = 1;
    public static final int MANAGE_ACTIVITY = 2;

    public static final String KEY_MAIN_ACTIVITY_LOCATION = "MAIN_ACTIVITY_LOCATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.key_ui_style), "material")
                .equals("material")) {
            setContentView(R.layout.activity_main_material);
        } else {
            setContentView(R.layout.activity_main_circular);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Location old = locationNow;
        readLocationList();
        readLocationNow(intent);
        if (!old.equals(locationNow)) {
            reset();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isStarted()) {
            setStarted();
            initData();
            initWidget();
            reset();
        } else {
            // reread cache and check if there are new data available through background service.
            Weather old = locationNow.weather;
            readLocationList();
            for (int i = 0; i < locationList.size(); i ++) {
                if (locationList.get(i).equals(locationNow)) {
                    locationNow = locationList.get(i);
                    break;
                }
            }

            if (!refreshLayout.isRefreshing()
                    && locationNow.weather != null && old != null
                    && locationNow.weather.base.timeStamp > old.base.timeStamp) {
                reset();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SETTINGS_ACTIVITY:
                DisplayUtils.setNavigationBarColor(this, weatherView.getThemeColors()[0]);
                NotificationUtils.refreshNotificationInNewThread(this, locationList.get(0));

                readLocationList();
                readLocationNow(data);
                switchLayout.setData(locationList, locationNow);
                reset();
                break;

            case MANAGE_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    readLocationList();
                    readLocationNow(data);
                    switchLayout.setData(locationList, locationNow);
                    reset();
                } else {
                    readLocationList();
                    for (int i = 0; i < locationList.size(); i ++) {
                        if (locationNow.equals(locationList.get(i))) {
                            switchLayout.setData(locationList, locationNow);
                            return;
                        }
                    }
                    locationNow = locationList.get(0);
                    switchLayout.setData(locationList, locationNow);
                    reset();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        indicator.setSwitchView(switchLayout);
        if (locationList.size() > 1) {
            indicator.setVisibility(View.VISIBLE);
        } else {
            indicator.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationHelper.cancel();
        weatherHelper.cancel();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        // do nothing.
    }

    @Override
    public View getSnackbarContainer() {
        return switchLayout;
    }

    // init.

    private void initData() {
        readLocationList();
        readLocationNow(getIntent());

        this.weatherHelper = new WeatherHelper();
        this.locationHelper = new LocationHelper(this);
    }

    private void readLocationList() {
        this.locationList = DatabaseHelper.getInstance(this).readLocationList();
        for (int i = 0; i <locationList.size(); i ++) {
            locationList.get(i).weather = DatabaseHelper.getInstance(this)
                    .readWeather(locationList.get(i));
            if (locationList.get(i).weather != null) {
                locationList.get(i).history = DatabaseHelper.getInstance(this).readHistory(
                        locationList.get(i).weather);
            }
        }
    }

    private void readLocationNow(@Nullable Intent intent) {
        if (locationNow != null) {
            boolean exist = false;
            for (int i = 0; i < locationList.size(); i ++) {
                if (locationList.get(i).equals(locationNow)) {
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                locationNow = null;
            }
        }

        if (intent != null) {
            String locationName = intent.getStringExtra(KEY_MAIN_ACTIVITY_LOCATION);
            if (TextUtils.isEmpty(locationName) && locationNow == null) {
                locationNow = locationList.get(0);
                return;
            } else if (!TextUtils.isEmpty(locationName)) {
                for (int i = 0; i < locationList.size(); i ++) {
                    if (locationList.get(i).isLocal() && locationName.equals(getString(R.string.local))) {
                        if (locationNow == null || !locationNow.equals(locationList.get(i))) {
                            locationNow = locationList.get(i);
                            return;
                        }
                    } else if (locationList.get(i).city.equals(locationName)) {
                        if (locationNow == null || !locationNow.city.equals(locationName)) {
                            locationNow = locationList.get(i);
                            return;
                        }
                    }
                }
            }
        }
        if (locationNow == null) {
            locationNow = locationList.get(0);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initWidget() {
        this.statusBar = findViewById(R.id.activity_main_statusBar);

        this.weatherView = findViewById(R.id.activity_main_weatherView);
        if (weatherView instanceof MaterialWeatherView) {
            int kind;
            if (locationNow.weather == null) {
                kind = WeatherView.WEATHER_KIND_CLEAR_DAY;
            } else {
                kind = WeatherViewController.getWeatherViewWeatherKind(
                        locationNow.weather.realTime.weatherKind,
                        TimeManager.getInstance(this).isDayTime());
            }
            weatherView.setWeather(kind);
            ((MaterialWeatherView) weatherView).setOpenGravitySensor(
                    PreferenceManager.getDefaultSharedPreferences(this)
                            .getBoolean(getString(R.string.key_gravity_sensor_switch), true));
        }

        this.appBar = findViewById(R.id.activity_main_appBar);

        this.toolbar = findViewById(R.id.activity_main_toolbar);
        toolbar.inflateMenu(R.menu.activity_main);
        toolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.action_manage:
                    IntentHelper.startManageActivityForResult(this);
                    break;

                case R.id.action_settings:
                    IntentHelper.startSettingsActivityForResult(this);
                    break;
            }
            return true;
        });

        this.switchLayout = findViewById(R.id.activity_main_switchView);
        switchLayout.setData(locationList, locationNow);
        switchLayout.setOnSwitchListener(this);
        switchLayout.setOnTouchListener(indicatorStateListener);

        this.refreshLayout = findViewById(R.id.activity_main_refreshView);
        int startPosition = (int) (DisplayUtils.getStatusBarHeight(getResources())
                + DisplayUtils.dpToPx(this, 16));
        refreshLayout.setProgressViewOffset(
                false, startPosition, startPosition + refreshLayout.getProgressViewEndOffset());
        refreshLayout.setOnRefreshListener(this);
        if (weatherView instanceof MaterialWeatherView) {
            refreshLayout.setColorSchemeColors(weatherView.getThemeColors()[0]);
            refreshLayout.setProgressBackgroundColorSchemeResource(R.color.colorRoot);
        }

        this.scrollView = findViewById(R.id.activity_main_scrollView);
        scrollView.setOnTouchListener(indicatorStateListener);
        scrollView.setOnScrollChangeListener(new OnScrollListener(weatherView.getFirstCardMarginTop()));

        this.indicator = findViewById(R.id.activity_main_indicator);
    }

    // control.

    public void reset() {
        DisplayUtils.setWindowTopColor(this, weatherView.getThemeColors()[0]);
        DisplayUtils.setStatusBarStyleWithScrolling(getWindow(), statusBar, false);
        DisplayUtils.setNavigationBarColor(this, weatherView.getThemeColors()[0]);

        if (locationNow.weather == null) {
            toolbar.setTitle(locationNow.getCityName(this));
        } else {
            toolbar.setTitle(locationNow.weather.base.city);
        }

        scrollView.setVisibility(View.GONE);
        scrollView.scrollTo(0, 0);

        if (adapter != null) {
            adapter.destroy();
        }

        switchLayout.reset();
        switchLayout.setEnabled(true);

        if (locationNow.weather == null) {
            setRefreshing(true);
            onRefresh();
        } else {
            boolean valid = locationNow.weather.isValid(4);
            setRefreshing(!valid);
            buildUI();
            if (!valid) {
                onRefresh();
            }
        }
    }

    private void setRefreshing(final boolean b) {
        refreshLayout.post(() -> refreshLayout.setRefreshing(b));
    }

    @SuppressLint("SetTextI18n")
    private void buildUI() {
        if (locationNow.weather == null) {
            return;
        } else {
            TimeManager.getInstance(this).getDayTime(this, locationNow.weather, true);
        }

        WeatherViewController.setWeatherViewWeatherKind(
                weatherView, locationNow.weather, TimeManager.getInstance(this).isDayTime());
        setDarkMode(TimeManager.getInstance(this).isDayTime());

        DisplayUtils.setWindowTopColor(this, weatherView.getThemeColors()[0]);
        DisplayUtils.setNavigationBarColor(this, weatherView.getThemeColors()[0]);

        toolbar.setTitle(locationNow.weather.base.city);
        refreshLayout.setColorSchemeColors(weatherView.getThemeColors()[0]);

        adapter = new MainControllerAdapter(this, weatherView, locationNow);
        adapter.bindView();
        adapter.onScroll(0, 0);

        scrollView.setVisibility(View.VISIBLE);

        if (initAnimator != null) {
            initAnimator.cancel();
        }
        initAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(
                MainActivity.this, R.animator.card_in);
        initAnimator.setTarget(scrollView);
        initAnimator.setInterpolator(new DecelerateInterpolator());
        initAnimator.start();
    }

    @SuppressLint("RestrictedApi")
    private void setDarkMode(boolean dayTime) {
        if (GeometricWeather.getInstance().getDarkMode().equals("auto")) {
            boolean isDarkMode = DisplayUtils.isDarkMode(this);
            if ((isDarkMode && dayTime) || (!isDarkMode && !dayTime)) {
                // need switch theme.
                int mode = dayTime ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
                getDelegate().setLocalNightMode(mode);
                AppCompatDelegate.setDefaultNightMode(mode);

                int rootColor = ContextCompat.getColor(this, R.color.colorRoot);
                refreshLayout.setProgressBackgroundColorSchemeColor(rootColor);

                indicator.setCurrentIndicatorColor(ContextCompat.getColor(this, R.color.colorAccent));
                indicator.setIndicatorColor(ContextCompat.getColor(this, R.color.colorTextSubtitle));

                refreshBackgroundViews();
            }
        }
    }

    private void refreshLocation(Location location) {
        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).equals(location)) {
                locationList.set(i, location);
                return;
            }
        }
    }

    private void refreshBackgroundViews() {
        Observable.just(1)
                .compose(RxLifecycleCompact.bind(this).disposeObservableWhen(LifecycleEvent.DESTROY))
                .delay(1, TimeUnit.SECONDS)
                .doOnNext(integer -> ThreadManager.getInstance().execute(() ->
                        BackgroundManager.resetAllBackgroundTask(MainActivity.this, false)))
                .subscribe();
        if (locationNow.equals(locationList.get(0))) {
            Observable.just(1)
                    .compose(RxLifecycleCompact.bind(this).disposeObservableWhen(LifecycleEvent.DESTROY))
                    .delay(1, TimeUnit.SECONDS)
                    .doOnNext(integer -> {
                        WidgetUtils.refreshWidgetInNewThread(this, locationList.get(0));
                        NotificationUtils.refreshNotificationInNewThread(this, locationList.get(0));
                    })
                    .subscribe();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutsManager.refreshShortcutsInNewThread(this, locationList);
        }
    }

    // permission.

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestLocationPermission() {
        if (locationHelper.hasPermissions(this)) {
            locationHelper.requestLocation(this, locationNow, this);
        } else {
            this.requestPermissions(locationHelper.getPermissions(), LOCATION_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permission, @NonNull int[] grantResult) {
        super.onRequestPermissionsResult(requestCode, permission, grantResult);
        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST_CODE:
                for (int i = 0; i < permission.length && i < grantResult.length; i ++) {
                    if ((permission[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                            || permission[i].equals(Manifest.permission.ACCESS_FINE_LOCATION))
                            && grantResult[i] != PackageManager.PERMISSION_GRANTED) {
                        SnackbarUtils.showSnackbar(
                                getString(R.string.feedback_request_location_permission_failed),
                                getString(R.string.help),
                                v -> {
                                    if (isForeground()) {
                                        new LocationHelpDialog().show(getSupportFragmentManager(), null);
                                    }
                                });
                        setRefreshing(false);
                        return;
                    }
                }
                if (locationNow.isLocal()) {
                    locationHelper.requestLocation(this, locationNow, this);
                }
                break;
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
                    indicator.setDisplayState(false);
                    break;
            }
            return false;
        }
    };

    // on swipe listener(swipe switch layout).

    @Override
    public void swipeTakeEffect(int direction) {
        switchLayout.setEnabled(false);
        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).equals(locationNow)) {
                int position = direction == SwipeSwitchLayout.DIRECTION_LEFT ?
                        i + 1 : i - 1;
                if (position < 0) {
                    position = locationList.size() - 1;
                } else if (position > locationList.size() - 1) {
                    position = 0;
                }

                locationNow = locationList.get(position);
                reset();

                return;
            }
        }

        locationNow = locationList.get(0);
        reset();
    }

    // on refresh listener.

    @Override
    public void onRefresh() {
        locationHelper.cancel();
        weatherHelper.cancel();

        if (locationNow.isLocal()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                locationHelper.requestLocation(this, locationNow, this);
            } else {
                requestLocationPermission();
            }
        } else {
            weatherHelper.requestWeather(this, locationNow, this);
        }
    }

    // on scroll changed listener.

    private class OnScrollListener implements NestedScrollView.OnScrollChangeListener {

        private int firstCardMarginTop;
        private int overlapTriggerDistance;

        OnScrollListener(int firstCardMarginTop) {
            super();
            this.firstCardMarginTop = firstCardMarginTop;
            this.overlapTriggerDistance = firstCardMarginTop
                    - DisplayUtils.getStatusBarHeight(getResources());
        }

        @Override
        public void onScrollChange(NestedScrollView v,
                                   int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

            weatherView.onScroll(scrollY);
            adapter.onScroll(oldScrollY, scrollY);

            // set translation y of toolbar.
            if (adapter != null) {
                if (scrollY < firstCardMarginTop
                        - appBar.getMeasuredHeight()
                        - adapter.getCurrentTemperatureTextHeight()) {
                    appBar.setTranslationY(0);
                } else if (scrollY > firstCardMarginTop - appBar.getY()) {
                    appBar.setTranslationY(-appBar.getMeasuredHeight());
                } else {
                    appBar.setTranslationY(firstCardMarginTop
                            - adapter.getCurrentTemperatureTextHeight()
                            - scrollY
                            - appBar.getMeasuredHeight());
                }
            }

            // set status bar style.
            if (oldScrollY < overlapTriggerDistance && overlapTriggerDistance <= scrollY) {
                DisplayUtils.setStatusBarStyleWithScrolling(getWindow(), statusBar, true);
            } else if (oldScrollY >= overlapTriggerDistance && overlapTriggerDistance > scrollY) {
                DisplayUtils.setStatusBarStyleWithScrolling(getWindow(), statusBar, false);
            }
        }
    }

    // on request location listener.

    @Override
    public void requestLocationSuccess(Location requestLocation) {
        if (!requestLocation.isUsable()) {
            requestLocationFailed(requestLocation);
        } else if (locationNow.equals(requestLocation)) {
            locationNow = requestLocation;
            refreshLocation(locationNow);
            DatabaseHelper.getInstance(this).writeLocation(locationNow);
            weatherHelper.requestWeather(this, locationNow, this);
        }
    }

    @Override
    public void requestLocationFailed(Location requestLocation) {
        if (locationNow.equals(requestLocation)) {
            if (locationNow.weather == null && locationNow.isUsable()) {
                weatherHelper.requestWeather(this, locationNow, this);
            } else {
                setRefreshing(false);
            }

            SnackbarUtils.showSnackbar(
                    getString(R.string.feedback_location_failed),
                    getString(R.string.help),
                    v -> {
                        if (isForeground()) {
                            new LocationHelpDialog().show(getSupportFragmentManager(), null);
                        }
                    });
        }
    }

    // on request weather listener.

    @Override
    public void requestWeatherSuccess(@Nullable Weather weather, @Nullable History history,
                                      @NonNull Location requestLocation) {
        if (locationNow.equals(requestLocation)) {
            if (weather == null) {
                requestWeatherFailed(requestLocation);
            } else {
                locationNow.weather = weather;
                locationNow.history = history;
                if (locationNow.history == null) {
                    locationNow.history = DatabaseHelper.getInstance(this).readHistory(weather);
                }
                refreshLocation(locationNow);

                refreshBackgroundViews();

                setRefreshing(false);
                buildUI();

                setRefreshing(false);
            }
        }
    }

    @Override
    public void requestWeatherFailed(@NonNull Location requestLocation) {
        if (locationNow.equals(requestLocation)) {
            if (locationNow.weather == null) {
                locationNow.weather = DatabaseHelper.getInstance(this).readWeather(locationNow);
                if (locationNow.weather != null) {
                    locationNow.history = DatabaseHelper.getInstance(this).readHistory(locationNow.weather);
                }

                refreshLocation(locationNow);
                SnackbarUtils.showSnackbar(getString(R.string.feedback_get_weather_failed));

                setRefreshing(false);
                buildUI();
            } else {
                SnackbarUtils.showSnackbar(getString(R.string.feedback_get_weather_failed));
                setRefreshing(false);
            }
        }
    }
}