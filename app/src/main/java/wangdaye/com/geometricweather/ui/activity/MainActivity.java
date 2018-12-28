package wangdaye.com.geometricweather.ui.activity;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.data.entity.model.History;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.ui.adapter.DetailsAdapter;
import wangdaye.com.geometricweather.ui.widget.AlertDisplayView;
import wangdaye.com.geometricweather.ui.widget.StatusBarView;
import wangdaye.com.geometricweather.ui.widget.trendView.TrendRecyclerView;
import wangdaye.com.geometricweather.ui.widget.trendView.TrendViewController;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherViewController;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.MaterialWeatherView;
import wangdaye.com.geometricweather.utils.NotificationUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.WidgetUtils;
import wangdaye.com.geometricweather.utils.SafeHandler;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.helpter.LocationHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.ui.widget.InkPageIndicator;
import wangdaye.com.geometricweather.ui.widget.verticalScrollView.SwipeSwitchLayout;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.manager.BackgroundManager;
import wangdaye.com.geometricweather.utils.manager.ThreadManager;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.ui.widget.verticalScrollView.VerticalNestedScrollView;
import wangdaye.com.geometricweather.ui.widget.verticalScrollView.VerticalSwipeRefreshLayout;
import wangdaye.com.geometricweather.utils.manager.ShortcutsManager;

/**
 * Main activity.
 * */

public class MainActivity extends GeoActivity
        implements View.OnClickListener, Toolbar.OnMenuItemClickListener,
        SwipeSwitchLayout.OnSwitchListener, SwipeRefreshLayout.OnRefreshListener,
        LocationHelper.OnRequestLocationListener, WeatherHelper.OnRequestWeatherListener,
        SafeHandler.HandlerContainer {

    private SafeHandler<MainActivity> handler;

    private FrameLayout background;
    private StatusBarView statusBar;

    private WeatherView weatherView;

    private LinearLayout appBar;
    private Toolbar toolbar;

    private InkPageIndicator indicator;

    private SwipeSwitchLayout switchLayout;
    private VerticalSwipeRefreshLayout refreshLayout;
    private VerticalNestedScrollView scrollView;

    private LinearLayout cardContainer;

    private CardView firstCard;
    private CardView secondCard;
    private CardView detailsCard;

    private TextView realtimeTemp;
    private TextView realtimeWeather;
    private TextView realtimeSendibleTemp;
    private TextView aqiOrWind;

    private AppCompatImageView timeIcon;
    private TextView refreshTime;
    
    private AlertDisplayView alertView;

    private TextView firstTitle;
    private TrendRecyclerView firstTrendRecyclerView;

    private TextView secondTitle;
    private TrendRecyclerView secondTrendRecyclerView;

    private TextView detailsTitle;
    private RecyclerView detailRecyclerView;

    private TextView footerText;

    private AnimatorSet initAnimator;

    private List<Location> locationList;
    public Location locationNow;

    private WeatherHelper weatherHelper;
    private LocationHelper locationHelper;

    private String uiStyle;

    private final int LOCATION_PERMISSIONS_REQUEST_CODE = 1;

    public static final int SETTINGS_ACTIVITY = 1;
    public static final int MANAGE_ACTIVITY = 2;

    public static final int MESSAGE_WHAT_STARTUP_SERVICE = 1;

    public static final String KEY_MAIN_ACTIVITY_LOCATION = "MAIN_ACTIVITY_LOCATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.key_ui_style), "material")
                .equals("material")) {
            uiStyle = "material";
            setContentView(R.layout.activity_main_material);
        } else {
            uiStyle = "circular";
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
    @SuppressLint("SimpleDateFormat")
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
            } else if (locationNow.weather != null) {
                alertView.display(locationNow.weather.alertList);
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
    protected void onStop() {
        super.onStop();
        alertView.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationHelper.cancel();
        weatherHelper.cancel();
        handler.removeCallbacksAndMessages(null);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
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
                locationList.get(i).history = DatabaseHelper.getInstance(this)
                        .readHistory(locationList.get(i).weather);
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
        this.handler = new SafeHandler<>(this);

        this.background = findViewById(R.id.activity_main_background);
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

        background.setBackgroundColor(weatherView.getBackgroundColor());

        this.appBar = findViewById(R.id.activity_main_appBar);

        this.toolbar = findViewById(R.id.activity_main_toolbar);
        toolbar.inflateMenu(R.menu.activity_main);
        toolbar.setNavigationOnClickListener(this);
        toolbar.setOnClickListener(this);
        toolbar.setOnMenuItemClickListener(this);

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
        scrollView.setOnScrollChangeListener(new OnScrollListener(weatherView.getFirstCardMarginTop()));
        scrollView.setOnTouchListener(indicatorStateListener);

        this.cardContainer = findViewById(R.id.activity_main_cardContainer);

        this.firstCard = findViewById(R.id.container_main_trend_first_card);
        this.secondCard = findViewById(R.id.container_main_trend_second_card);
        this.detailsCard = findViewById(R.id.container_main_details_card);

        RelativeLayout baseView = findViewById(R.id.container_main_base_view);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) baseView.getLayoutParams();
        params.height = weatherView.getFirstCardMarginTop();
        baseView.setLayoutParams(params);
        baseView.setOnClickListener(this);

        this.realtimeTemp = findViewById(R.id.container_main_base_view_tempTxt);
        this.realtimeWeather = findViewById(R.id.container_main_base_view_weatherTxt);
        this.realtimeSendibleTemp = findViewById(R.id.container_main_base_view_sendibleTempTxt);
        this.aqiOrWind = findViewById(R.id.container_main_base_view_aqiOrWindTxt);

        findViewById(R.id.container_main_trend_first_card_timeContainer).setOnClickListener(this);

        this.timeIcon = findViewById(R.id.container_main_trend_first_card_timeIcon);
        timeIcon.setOnClickListener(this);

        this.refreshTime = findViewById(R.id.container_main_trend_first_card_timeText);
        
        this.alertView = findViewById(R.id.container_main_trend_first_card_alert);
        alertView.setOnClickListener(this);

        this.firstTitle = findViewById(R.id.container_main_trend_first_card_title);
        this.firstTrendRecyclerView = findViewById(R.id.container_main_trend_first_card_trendRecyclerView);

        this.secondTitle = findViewById(R.id.container_main_trend_second_card_title);
        this.secondTrendRecyclerView = findViewById(R.id.container_main_trend_second_card_trendRecyclerView);

        this.detailsTitle = findViewById(R.id.container_main_details_card_title);
        this.detailRecyclerView = findViewById(R.id.container_main_details_card_recyclerView);

        this.footerText = findViewById(R.id.activity_main_footer_text);

        this.indicator = findViewById(R.id.activity_main_indicator);
    }

    // control.

    public void reset() {
        DisplayUtils.setWindowTopColor(this, weatherView.getThemeColors()[0]);
        DisplayUtils.setNavigationBarColor(this, weatherView.getThemeColors()[0]);

        if (locationNow.weather == null) {
            toolbar.setTitle(locationNow.getCityName(this));
        } else {
            toolbar.setTitle(locationNow.weather.base.city);
        }

        cardContainer.setVisibility(View.GONE);
        scrollView.scrollTo(0, 0);

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
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(b);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void buildUI() {
        Weather weather = locationNow.weather;
        History history = locationNow.history;

        if (weather == null) {
            return;
        } else {
            TimeManager.getInstance(this).getDayTime(this, weather, true);
        }

        WeatherViewController.setWeatherViewWeatherKind(
                weatherView, weather, TimeManager.getInstance(this).isDayTime());
        setDarkMode(TimeManager.getInstance(this).isDayTime());

        DisplayUtils.setWindowTopColor(this, weatherView.getThemeColors()[0]);
        DisplayUtils.setNavigationBarColor(this, weatherView.getThemeColors()[0]);

        toolbar.setTitle(weather.base.city);
        refreshLayout.setColorSchemeColors(weatherView.getThemeColors()[0]);

        realtimeTemp.setText(
                ValueUtils.buildAbbreviatedCurrentTemp(
                        weather.realTime.temp,
                        GeometricWeather.getInstance().isFahrenheit()));
        realtimeWeather.setText(weather.realTime.weather);
        realtimeSendibleTemp.setText(
                getString(R.string.feels_like) + " "
                        + ValueUtils.buildAbbreviatedCurrentTemp(
                        weather.realTime.sensibleTemp, GeometricWeather.getInstance().isFahrenheit()));

        if (weather.aqi == null) {
            aqiOrWind.setText(weather.realTime.windLevel);
        } else {
            aqiOrWind.setText(weather.aqi.quality);
        }

        if (weather.alertList.size() == 0) {
            timeIcon.setEnabled(false);
            timeIcon.setImageResource(R.drawable.ic_time);
        } else {
            timeIcon.setEnabled(true);
            timeIcon.setImageResource(R.drawable.ic_alert);
        }
        refreshTime.setText(weather.base.time);

        if (weather.alertList.size() == 0) {
            alertView.stop();
            alertView.setVisibility(View.GONE);
        } else {
            alertView.setVisibility(View.VISIBLE);
            alertView.display(weather.alertList);
        }

        firstTitle.setTextColor(weatherView.getThemeColors()[0]);
        secondTitle.setTextColor(weatherView.getThemeColors()[0]);
        detailsTitle.setTextColor(weatherView.getThemeColors()[0]);

        if (GeometricWeather.getInstance().getCardOrder().equals("daily_first")) {
            TrendViewController.setDailyTrend(
                    this, firstTitle, firstTrendRecyclerView,
                    weather, history, weatherView.getThemeColors());
            TrendViewController.setHourlyTrend(
                    this, secondTitle, secondTrendRecyclerView,
                    weather, history, weatherView.getThemeColors());
        } else {
            TrendViewController.setHourlyTrend(
                    this, firstTitle, firstTrendRecyclerView,
                    weather, history, weatherView.getThemeColors());
            TrendViewController.setDailyTrend(
                    this, secondTitle, secondTrendRecyclerView,
                    weather, history, weatherView.getThemeColors());
        }

        detailRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        detailRecyclerView.setAdapter(new DetailsAdapter(this, weather));

        footerText.setText("Powered by " + ValueUtils.getWeatherSource(this, locationNow.source));

        cardContainer.setVisibility(View.VISIBLE);
        if (initAnimator != null) {
            initAnimator.cancel();
        }
        initAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_in);
        initAnimator.setTarget(cardContainer);
        initAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        initAnimator.start();

        if (locationNow.equals(locationList.get(0))) {
            startupService();
        }
    }

    @SuppressLint("RestrictedApi")
    private void setDarkMode(boolean dayTime) {
        if (GeometricWeather.getInstance().getDarkMode().equals("auto")) {
            int currentNightMode = getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && dayTime)
                    || (currentNightMode != Configuration.UI_MODE_NIGHT_YES && !dayTime)) {
                // need switch theme.
                int mode = dayTime ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
                getDelegate().setLocalNightMode(mode);
                AppCompatDelegate.setDefaultNightMode(mode);

                int rootColor = ContextCompat.getColor(this, R.color.colorRoot);
                refreshLayout.setProgressBackgroundColorSchemeColor(rootColor);
                firstCard.setCardBackgroundColor(rootColor);
                secondCard.setCardBackgroundColor(rootColor);
                detailsCard.setCardBackgroundColor(rootColor);

                if (uiStyle.equals("circular")) {
                    Drawable drawable = background.getBackground();
                    if (drawable instanceof ColorDrawable) {
                        ValueAnimator colorAnimator = ValueAnimator.ofObject(
                                new ArgbEvaluator(),
                                ((ColorDrawable) drawable).getColor(),
                                weatherView.getBackgroundColor());
                        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                background.setBackgroundColor((Integer) animation.getAnimatedValue());
                            }
                        });
                        colorAnimator.setDuration(300);
                        colorAnimator.start();
                    } else {
                        background.setBackgroundColor(rootColor);
                    }
                }

                timeIcon.setSupportImageTintList(getResources().getColorStateList(R.color.colorTextContent));
                refreshTime.setTextColor(ContextCompat.getColor(this, R.color.colorTextContent));

                indicator.setCurrentIndicatorColor(ContextCompat.getColor(this, R.color.colorAccent));
                indicator.setIndicatorColor(ContextCompat.getColor(this, R.color.colorTextSubtitle));
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

    private void startupService() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler.obtainMessage(MESSAGE_WHAT_STARTUP_SERVICE).sendToTarget();
            }
        }, 1500);
    }

    // permission.

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestLocationPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(
                    new String[] {
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSIONS_REQUEST_CODE);
        } else {
            locationHelper.requestLocation(this, locationNow, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permission, @NonNull int[] grantResult) {
        super.onRequestPermissionsResult(requestCode, permission, grantResult);
        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST_CODE:
                if (grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    SnackbarUtils.showSnackbar(getString(R.string.feedback_request_location_permission_success));
                    if (locationNow.isLocal()) {
                        locationHelper.requestLocation(this, locationNow, this);
                    }
                } else {
                    SnackbarUtils.showSnackbar(
                            getString(R.string.feedback_request_location_permission_failed),
                            getString(R.string.feedback_request_permission),
                            applicationDetalsListener);
                    setRefreshing(false);
                }
                break;
        }
    }

    // interface.

    // on click listener.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.container_main_base_view:
                weatherView.onClick();
                break;

            case R.id.container_main_trend_first_card_timeIcon:
            case R.id.container_main_trend_first_card_alert:
                IntentHelper.startAlertActivity(this, locationNow.weather);
                break;

            case R.id.container_main_trend_first_card_timeContainer:
                IntentHelper.startManageActivityForResult(this);
                break;
        }
    }

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
        }
        return true;
    }

    private View.OnClickListener applicationDetalsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            IntentHelper.startApplicationDetailsActivity(MainActivity.this);
        }
    };

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
            this.firstCardMarginTop = firstCardMarginTop;
            this.overlapTriggerDistance = firstCardMarginTop
                    - DisplayUtils.getStatusBarHeight(getResources());
        }

        @Override
        public void onScrollChange(NestedScrollView v,
                                   int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            weatherView.onScroll(scrollY);

            // set translation y of toolbar.
            if (scrollY < firstCardMarginTop - appBar.getMeasuredHeight() - realtimeTemp.getMeasuredHeight()) {
                appBar.setTranslationY(0);
            } else if (scrollY > firstCardMarginTop - appBar.getY()) {
                appBar.setTranslationY(-appBar.getMeasuredHeight());
            } else {
                appBar.setTranslationY(
                        firstCardMarginTop - realtimeTemp.getMeasuredHeight() - scrollY
                                - appBar.getMeasuredHeight());
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
    public void requestLocationSuccess(Location requestLocation, boolean locationChanged) {
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)) {
                SnackbarUtils.showSnackbar(
                        getString(R.string.feedback_location_failed),
                        getString(R.string.feedback_request_permission),
                        applicationDetalsListener);
            } else {
                SnackbarUtils.showSnackbar(getString(R.string.feedback_location_failed));
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
                    || !locationNow.weather.base.date.equals(weather.base.date)
                    || !locationNow.weather.base.time.equals(weather.base.time)) {
                locationNow.weather = weather;
                if (requestLocation.history == null) {
                    locationNow.history = DatabaseHelper.getInstance(this).readHistory(weather);
                } else {
                    locationNow.history = requestLocation.history;
                    DatabaseHelper.getInstance(this).writeHistory(locationNow.history);
                }
                refreshLocation(locationNow);
                DatabaseHelper.getInstance(this).writeWeather(locationNow, weather);
                DatabaseHelper.getInstance(this).writeHistory(weather);

                setRefreshing(false);
                buildUI();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    ShortcutsManager.refreshShortcuts(this, locationList);
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
                ThreadManager.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        BackgroundManager.resetAllBackgroundTask(MainActivity.this, false);
                    }
                });
                break;
        }
    }
}