package wangdaye.com.geometricweather.view.activity;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.NotificationUtils;
import wangdaye.com.geometricweather.utils.PermissionUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.helpter.ServiceHelper;
import wangdaye.com.geometricweather.utils.widget.SafeHandler;
import wangdaye.com.geometricweather.utils.WidgetUtils;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.helpter.LocationHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.view.widget.InkPageIndicator;
import wangdaye.com.geometricweather.view.widget.StatusBarView;
import wangdaye.com.geometricweather.view.widget.SwipeSwitchLayout;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.TimeUtils;
import wangdaye.com.geometricweather.view.widget.VerticalNestedScrollView;
import wangdaye.com.geometricweather.view.widget.VerticalSwipeRefreshView;
import wangdaye.com.geometricweather.view.widget.weatherView.details.IndexListView;
import wangdaye.com.geometricweather.view.widget.weatherView.sky.SkyView;
import wangdaye.com.geometricweather.view.widget.weatherView.trend.TrendItemView;
import wangdaye.com.geometricweather.view.widget.weatherView.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.view.widget.weatherView.trend.TrendView;

/**
 * Main activity.
 * */

public class MainActivity extends GeoActivity
        implements View.OnClickListener, Toolbar.OnMenuItemClickListener, SwipeSwitchLayout.OnSwitchListener,
        SwipeRefreshLayout.OnRefreshListener, NestedScrollView.OnScrollChangeListener,
        LocationHelper.OnRequestLocationListener, WeatherHelper.OnRequestWeatherListener,
        SafeHandler.HandlerContainer {
    // widget
    private SafeHandler<MainActivity> handler;

    private StatusBarView statusBar;
    private SkyView skyView;
    private Toolbar toolbar;

    private SwipeSwitchLayout swipeSwitchLayout;
    private InkPageIndicator indicator;
    private VerticalSwipeRefreshView swipeRefreshLayout;
    private VerticalNestedScrollView nestedScrollView;
    private LinearLayout weatherContainer;

    private TextView[] titleTexts;

    private TextView refreshTime;
    private FrameLayout locationIconBtn;
    private ImageView locationIcon;
    private TextView locationText;

    private TextView overviewTitle;
    private TrendView trendView;
    private TextView lifeInfoTitle;
    private IndexListView indexListView;

    // data
    private List<Location> locationList;
    public Location locationNow;

    private int scrollTrigger;

    private WeatherHelper weatherHelper;
    private LocationHelper locationHelper;

    // animation
    private AnimatorSet viewShowAnimator;

    private final int LOCATION_PERMISSIONS_REQUEST_CODE = 1;

    public static final int SETTINGS_ACTIVITY = 1;
    public static final int MANAGE_ACTIVITY = 2;

    public static final int MESSAGE_WHAT_STARTUP_SERVICE = 1;

    public static final String KEY_MAIN_ACTIVITY_LOCATION = "MAIN_ACTIVITY_LOCATION";

    /** <br> life cycle. */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isStarted()) {
            setStarted();
            initData();
            initWidget();
            reset();
        } else if (!swipeRefreshLayout.isRefreshing()) {
            Weather memory = DatabaseHelper.getInstance(this).readWeather(locationNow);
            if (locationNow.weather != null && memory != null
                    && !memory.base.time.equals(locationNow.weather.base.time)) {
                locationNow.weather = memory;
                locationNow.history = DatabaseHelper.getInstance(this).readHistory(memory);
                reset();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public View getSnackbarContainer() {
        return swipeSwitchLayout;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SETTINGS_ACTIVITY:
                DisplayUtils.setNavigationBarColor(this, TimeUtils.getInstance(this).isDayTime());
                NotificationUtils.refreshNotificationInNewThread(this, locationList.get(0));
                break;

            case MANAGE_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    readLocationList();
                    readIntentData(data);
                    swipeSwitchLayout.setData(locationList, locationNow);
                    indicator.setSwitchView(swipeSwitchLayout);
                    reset();
                } else {
                    readLocationList();
                    for (int i = 0; i < locationList.size(); i ++) {
                        if (locationNow.equals(locationList.get(i))) {
                            swipeSwitchLayout.setData(locationList, locationNow);
                            indicator.setSwitchView(swipeSwitchLayout);
                            return;
                        }
                    }
                    locationNow = locationList.get(0);
                    swipeSwitchLayout.setData(locationList, locationNow);
                    indicator.setSwitchView(swipeSwitchLayout);
                    reset();
                }
                break;
        }
    }

    /** <br> UI. */

    // init.

    private void initWidget() {
        this.handler = new SafeHandler<>(this);

        this.statusBar = (StatusBarView) findViewById(R.id.activity_main_statusBar);
        this.setStatusBarColor();

        this.skyView = (SkyView) findViewById(R.id.activity_main_skyView);
        initScrollViewPart();

        this.toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
        toolbar.inflateMenu(R.menu.activity_main);
        toolbar.setNavigationOnClickListener(this);
        toolbar.setOnClickListener(this);
        toolbar.setOnMenuItemClickListener(this);
    }

    public void setStatusBarColor() {
        if (TimeUtils.getInstance(this).isDayTime()) {
            statusBar.setBackgroundColor(ContextCompat.getColor(this, R.color.lightPrimary_5));
        } else {
            statusBar.setBackgroundColor(ContextCompat.getColor(this, R.color.darkPrimary_5));
        }
    }
    
    private void initScrollViewPart() {

        // get swipe switch layout.
        this.swipeSwitchLayout = (SwipeSwitchLayout) findViewById(R.id.activity_main_switchView);
        swipeSwitchLayout.setData(locationList, locationNow);
        swipeSwitchLayout.setOnSwitchListener(this);

        // get indicator.
        this.indicator = (InkPageIndicator) findViewById(R.id.activity_main_indicator);
        indicator.setSwitchView(swipeSwitchLayout);

        // get swipe refresh layout & set color.
        this.swipeRefreshLayout = (VerticalSwipeRefreshView) findViewById(R.id.activity_main_refreshView);
        if (TimeUtils.getInstance(this).isDayTime()) {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.lightPrimary_3));
        } else {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.darkPrimary_1));
        }
        swipeRefreshLayout.setOnRefreshListener(this);

        // get nested scroll view & set listener.
        this.nestedScrollView = (VerticalNestedScrollView) findViewById(R.id.activity_main_scrollView);
        nestedScrollView.setOnScrollChangeListener(this);

        swipeSwitchLayout.setIndicator(indicator);
        swipeRefreshLayout.setIndicator(indicator);
        nestedScrollView.setIndicator(indicator);

        // get realTimeWeather container.
        this.weatherContainer = (LinearLayout) findViewById(R.id.container_weather);
        viewShowAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_in);
        viewShowAnimator.setTarget(weatherContainer);

        // get touch layout, set height & get realTime texts.
        RelativeLayout touchLayout = (RelativeLayout) findViewById(R.id.container_weather_touchLayout);
        LinearLayout.LayoutParams touchParams = (LinearLayout.LayoutParams) touchLayout.getLayoutParams();
        touchParams.height = scrollTrigger;
        touchLayout.setLayoutParams(touchParams);
        touchLayout.setOnClickListener(this);

        this.titleTexts = new TextView[] {
                (TextView) findViewById(R.id.container_weather_realtime_tempTxt),
                (TextView) findViewById(R.id.container_weather_realtime_weatherTxt),
                (TextView) findViewById(R.id.container_weather_realtime_aqiTxt)};

        // realTimeWeather card.
        this.initWeatherCard();

        //get life info 
        this.indexListView = (IndexListView) findViewById(R.id.container_weather_lifeInfoView);
    }

    private void initWeatherCard() {
        this.refreshTime = (TextView) findViewById(R.id.container_weather_time_text_live);

        findViewById(R.id.container_weather_locationContainer).setOnClickListener(this);

        this.locationIconBtn = (FrameLayout) findViewById(R.id.container_weather_location_iconButton);
        locationIconBtn.setOnClickListener(this);
        this.locationIcon = (ImageView) findViewById(R.id.container_weather_location_icon);
        this.locationText = (TextView) findViewById(R.id.container_weather_location_text_live);

        this.overviewTitle = (TextView) findViewById(R.id.container_weather_overviewTitle);

        this.trendView = (TrendView) findViewById(R.id.container_weather_trendView);
        ((TrendRecyclerView) findViewById(R.id.container_trend_view_recyclerView)).setSwitchLayout(swipeSwitchLayout);

        this.lifeInfoTitle = (TextView) findViewById(R.id.container_weather_lifeInfoTitle);
    }

    // reset.

    public void reset() {
        skyView.reset();
        this.resetScrollViewPart();
    }

    private void resetScrollViewPart() {
        weatherContainer.setVisibility(View.GONE);
        nestedScrollView.scrollTo(0, 0);

        swipeSwitchLayout.reset();
        swipeSwitchLayout.setEnabled(true);

        if (locationNow.weather == null) {
            setRefreshing(true);
            onRefresh();
        } else {
            setRefreshing(false);
            buildUI();
        }
    }

    // build UI.

    private void setRefreshing(final boolean b) {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(b);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void buildUI() {
        Weather weather = locationNow.weather;
        if (weather == null) {
            return;
        } else {
            TimeUtils.getInstance(this).getDayTime(this, weather, true);
        }

        setStatusBarColor();
        DisplayUtils.setWindowTopColor(this);
        DisplayUtils.setNavigationBarColor(this, TimeUtils.getInstance(this).isDayTime());

        skyView.setWeather(weather);

        titleTexts[0].setText(
                ValueUtils.buildAbbreviatedCurrentTemp(
                        weather.realTime.temp,
                        GeometricWeather.getInstance().isFahrenheit()));
        titleTexts[1].setText(weather.realTime.weather);
        titleTexts[2].setText(weather.aqi.quality);

        refreshTime.setText(weather.base.time);
        locationText.setText(weather.base.city);
        if (weather.alertList.size() == 0) {
            locationIconBtn.setEnabled(false);
            locationIcon.setImageResource(R.drawable.ic_location);
        } else {
            locationIconBtn.setEnabled(true);
            locationIcon.setImageResource(R.drawable.ic_alert);
        }

        if (TimeUtils.getInstance(this).isDayTime()) {
            overviewTitle.setTextColor(ContextCompat.getColor(this, R.color.lightPrimary_3));
            lifeInfoTitle.setTextColor(ContextCompat.getColor(this, R.color.lightPrimary_3));
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.lightPrimary_3));
        } else {
            overviewTitle.setTextColor(ContextCompat.getColor(this, R.color.darkPrimary_1));
            lifeInfoTitle.setTextColor(ContextCompat.getColor(this, R.color.darkPrimary_1));
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.darkPrimary_1));
        }

        trendView.setData(locationNow.weather, locationNow.history);
        trendView.setState(TrendItemView.DATA_TYPE_DAILY, false);
        indexListView.setData(locationNow.weather);

        weatherContainer.setVisibility(View.VISIBLE);
        viewShowAnimator.start();
    }

    /** <br> data. */

    // init.

    private void initData() {
        readLocationList();
        readIntentData(getIntent());

        this.weatherHelper = new WeatherHelper();
        this.locationHelper = new LocationHelper(this);

        this.scrollTrigger = (int) (getResources().getDisplayMetrics().widthPixels / 6.8 * 4
                + DisplayUtils.dpToPx(this, 60)
                - DisplayUtils.dpToPx(this, 300 - 256));
    }

    private void readLocationList() {
        this.locationList = DatabaseHelper.getInstance(this).readLocationList();
    }

    private void readIntentData(Intent intent) {
        String locationName = intent.getStringExtra(KEY_MAIN_ACTIVITY_LOCATION);
        if (TextUtils.isEmpty(locationName)) {
            locationNow = locationList.get(0);
            return;
        }
        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).isLocal() && locationName.equals(getString(R.string.local))) {
                locationNow = locationList.get(i);
                return;
            } else if (locationList.get(i).city.equals(locationName)) {
                locationNow = locationList.get(i);
                return;
            }
        }
        if (locationNow == null) {
            locationNow = locationList.get(0);
        }
    }

    // buildWeather.

    private void setLocationAndReset(Location location) {
        this.locationNow = location;
        reset();
    }

    private void refreshLocation(Location location) {
        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).equals(location)) {
                locationList.set(i, location);
                return;
            }
        }
    }

    private void startupService() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler.obtainMessage(MESSAGE_WHAT_STARTUP_SERVICE).sendToTarget();
            }
        }, 1500);
    }

    /** <br> permission. */

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestLocationPermission() {
        PermissionUtils.requestLocationPermission(this, LOCATION_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResult) {
        super.onRequestPermissionsResult(requestCode, permission, grantResult);
        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST_CODE:
                if (grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    SnackbarUtils.showSnackbar(getString(R.string.feedback_request_location_permission_success));
                    if (locationNow.isLocal()) {
                        setRefreshing(true);
                        onRefresh();
                    }
                } else {
                    SnackbarUtils.showSnackbar(getString(R.string.feedback_request_location_permission_failed));
                }
                break;
        }
    }

    /** <br> interface. */

    // on click listener.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case -1:
                finish();
                break;

            case R.id.container_weather_touchLayout:
            case R.id.activity_main_toolbar:
                skyView.onClickSky();
                break;

            case R.id.container_weather_location_iconButton:
                IntentHelper.startAlertActivity(this, locationNow.weather);
                break;

            case R.id.container_weather_locationContainer:
                IntentHelper.startManageActivityForResult(this);
                break;
        }
    }

    private View.OnClickListener snackbarAction = new View.OnClickListener() {
        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onClick(View view) {
            requestLocationPermission();
        }
    };

    // on menu item click listener.

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_manage:
                IntentHelper.startManageActivityForResult(this);
                break;

            case R.id.action_settings:
                IntentHelper.startSettingsActivityForResult(this);
                break;

            case R.id.action_about:
                IntentHelper.startAboutActivity(this);
                break;
        }
        return true;
    }

    // on swipe listener(swipe switch layout).

    @Override
    public void swipeTakeEffect(int direction) {
        swipeSwitchLayout.setEnabled(false);
        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).equals(locationNow)) {
                int position = direction == SwipeSwitchLayout.DIRECTION_LEFT ?
                        i + 1 : i - 1;
                if (position < 0) {
                    position = locationList.size() - 1;
                } else if (position > locationList.size() - 1) {
                    position = 0;
                }
                setLocationAndReset(locationList.get(position));
                return;
            }
        }
        setLocationAndReset(locationList.get(0));
    }

    // on refresh listener.

    @Override
    public void onRefresh() {
        locationHelper.cancel();
        weatherHelper.cancel();

        if (locationNow.isLocal()) {
            locationHelper.requestLocation(this, locationNow, this);
        } else {
            weatherHelper.requestWeather(this, locationNow, this);
        }
    }

    // on scroll changed listener.

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        skyView.setTranslationY((float) (-Math.min(1, 1.0 * scrollY / scrollTrigger) * skyView.getMeasuredHeight()));
        toolbar.setTranslationY((float) (-Math.min(1, 1.0 * scrollY / scrollTrigger) * toolbar.getMeasuredHeight()));
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

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                SnackbarUtils.showSnackbar(getString(R.string.feedback_location_failed));
            } else {
                SnackbarUtils.showSnackbar(
                        getString(R.string.feedback_location_failed),
                        getString(R.string.feedback_request_permission),
                        snackbarAction);
            }
        }
    }

    // on request weather listener.

    @Override
    public void requestWeatherSuccess(Weather weather, Location requestLocation) {
        if (locationNow.equals(requestLocation)) {
            if (weather == null) {
                requestWeatherFailed(requestLocation);
            } else if (locationNow.weather == null
                    || !locationNow.weather.base.time.equals(weather.base.time)) {
                locationNow.weather = weather;
                locationNow.history = DatabaseHelper.getInstance(this).readHistory(weather);
                refreshLocation(locationNow);
                DatabaseHelper.getInstance(this).writeWeather(locationNow, weather);
                DatabaseHelper.getInstance(this).writeHistory(weather);

                setRefreshing(false);
                buildUI();

                if (locationNow.equals(locationList.get(0))) {
                    startupService();
                }
            } else {
                setRefreshing(false);
            }
        }
    }

    @Override
    public void requestWeatherFailed(Location requestLocation) {
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

                if (locationNow.equals(locationList.get(0))) {
                    startupService();
                }
            } else {
                SnackbarUtils.showSnackbar(getString(R.string.feedback_get_weather_failed));
                setRefreshing(false);
            }
        }
    }

    // handler container.

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case MESSAGE_WHAT_STARTUP_SERVICE:
                WidgetUtils.refreshWidgetInNewThread(this, locationList.get(0));
                NotificationUtils.refreshNotificationInNewThread(this, locationList.get(0));
                ServiceHelper.startupAllService(this, true);
                break;
        }
    }
}