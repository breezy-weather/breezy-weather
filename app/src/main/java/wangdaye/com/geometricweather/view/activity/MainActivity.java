package wangdaye.com.geometricweather.view.activity;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.utils.NotificationUtils;
import wangdaye.com.geometricweather.utils.PermissionUtils;
import wangdaye.com.geometricweather.utils.WidgetUtils;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpter.LocationHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;
import wangdaye.com.geometricweather.utils.manager.ShortcutsManager;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.view.dialog.ManageDialog;
import wangdaye.com.geometricweather.view.dialog.WeatherDialog;
import wangdaye.com.geometricweather.view.widget.StatusBarView;
import wangdaye.com.geometricweather.view.widget.SwipeSwitchLayout;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.TimeUtils;
import wangdaye.com.geometricweather.view.widget.weatherView.LifeInfoView;
import wangdaye.com.geometricweather.view.widget.weatherView.SkyView;
import wangdaye.com.geometricweather.view.widget.weatherView.TrendView;
import wangdaye.com.geometricweather.view.widget.weatherView.WeekWeatherView;

/**
 * Main activity.
 * */

public class MainActivity extends GeoActivity
        implements ManageDialog.OnLocationChangedListener,
        View.OnClickListener, Toolbar.OnMenuItemClickListener, SwipeRefreshLayout.OnRefreshListener,
        NestedScrollView.OnScrollChangeListener, SwipeSwitchLayout.OnSwipeListener,
        WeekWeatherView.OnClickWeekContainerListener, WeatherHelper.OnRequestWeatherListener,
        LocationHelper.OnRequestLocationListener {
    // widget    
    private StatusBarView statusBar;
    private SkyView skyView;
    private Toolbar toolbar;

    private SwipeSwitchLayout swipeSwitchLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NestedScrollView nestedScrollView;
    private LinearLayout weatherContainer;

    private TextView[] titleTexts;

    private TextView refreshTime;
    private TextView locationText;
    private ImageButton collectionIcon;

    private TextView overviewTitle;
    private WeekWeatherView weekWeatherView;
    private TrendView trendView;
    private TextView lifeInfoTitle;
    private LifeInfoView lifeInfoView;

    // data
    private List<Location> locationList;
    public Location locationNow;
    public boolean collected;

    private int scrollTrigger;

    private WeatherHelper weatherHelper;
    private LocationHelper locationHelper;

    // animation
    private AnimatorSet viewShowAnimator;

    private final int LOCATION_PERMISSIONS_REQUEST_CODE = 1;

    private static final int SETTINGS_ACTIVITY = 1;
    public static final String KEY_CITY = "city";

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

            String locationName = getIntent().getStringExtra(KEY_CITY);
            if (!TextUtils.isEmpty(locationName)) {
                for (int i = 0; i < locationList.size(); i ++) {
                    if (locationList.get(i).name.equals(locationName)) {
                        setLocationAndReset(locationList.get(i), true);
                        break;
                    }
                }
            }
            if (locationNow == null) {
                setLocationAndReset(locationList.get(0), true);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutsManager.checkAndPublishShortcuts(this, locationList);
        }
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
                NotificationUtils.refreshNotification(this, locationList.get(0));
                break;
        }
    }

    /** <br> UI. */

    // init.

    private void initWidget() {
        this.statusBar = (StatusBarView) findViewById(R.id.activity_main_statusBar);
        this.initStatusBarColor();

        this.skyView = (SkyView) findViewById(R.id.activity_main_skyView);
        initScrollViewPart();

        this.toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
        toolbar.inflateMenu(R.menu.activity_main);
        toolbar.setNavigationOnClickListener(this);
        toolbar.setOnClickListener(this);
        toolbar.setOnMenuItemClickListener(this);
    }

    public void initStatusBarColor() {
        if (TimeUtils.getInstance(this).isDayTime()) {
            statusBar.setBackgroundColor(ContextCompat.getColor(this, R.color.lightPrimary_5));
        } else {
            statusBar.setBackgroundColor(ContextCompat.getColor(this, R.color.darkPrimary_5));
        }
    }
    
    private void initScrollViewPart() {
        // get swipe switch layout.
        this.swipeSwitchLayout = (SwipeSwitchLayout) findViewById(R.id.activity_main_switchView);
        swipeSwitchLayout.setOnSwipeListener(this);

        // get swipe refresh layout & set color.
        this.swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_refreshView);
        if (TimeUtils.getInstance(this).isDayTime()) {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.lightPrimary_3));
        } else {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.darkPrimary_1));
        }
        swipeRefreshLayout.setOnRefreshListener(this);

        // get nested scroll view & set listener.
        this.nestedScrollView = (NestedScrollView) findViewById(R.id.activity_main_scrollView);
        nestedScrollView.setOnScrollChangeListener(this);

        // get weather container.
        this.weatherContainer = (LinearLayout) findViewById(R.id.container_weather);
        viewShowAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_in);
        viewShowAnimator.setTarget(weatherContainer);

        // get touch layout, set height & get live texts.
        RelativeLayout touchLayout = (RelativeLayout) findViewById(R.id.container_weather_touchLayout);
        LinearLayout.LayoutParams touchParams = (LinearLayout.LayoutParams) touchLayout.getLayoutParams();
        touchParams.height = scrollTrigger;
        touchLayout.setLayoutParams(touchParams);
        touchLayout.setOnClickListener(this);

        this.titleTexts = new TextView[] {
                (TextView) findViewById(R.id.container_weather_aqi_text_live),
                (TextView) findViewById(R.id.container_weather_weather_text_live)};

        // weather card.
        this.initWeatherCard();

        //get life info 
        this.lifeInfoView = (LifeInfoView) findViewById(R.id.container_weather_lifeInfoView);
    }

    private void initWeatherCard() {
        this.refreshTime = (TextView) findViewById(R.id.container_weather_time_text_live);

        findViewById(R.id.container_weather_locationContainer).setOnClickListener(this);

        this.locationText = (TextView) findViewById(R.id.container_weather_location_text_live);

        this.collectionIcon = (ImageButton) findViewById(R.id.container_weather_location_collect_icon);
        collectionIcon.setOnClickListener(this);
        collectionIcon.setImageResource(collected ? R.drawable.ic_collected : R.drawable.ic_uncollected);

        this.overviewTitle = (TextView) findViewById(R.id.container_weather_overviewTitle);

        this.weekWeatherView = (WeekWeatherView) findViewById(R.id.container_weather_weekWeatherView);
        weekWeatherView.setOnClickWeekContainerListener(this);

        this.trendView = (TrendView) findViewById(R.id.container_weather_trendView);

        this.lifeInfoTitle = (TextView) findViewById(R.id.container_weather_lifeInfoTitle);
    }

    // reset.

    public void reset() {
        skyView.reset();
        this.resetScrollViewPart();
    }

    private void resetScrollViewPart() {
        // set weather container gone.
        weatherContainer.setVisibility(View.GONE);
        // set swipe switch layout reset.
        swipeSwitchLayout.reset();
        swipeSwitchLayout.setEnabled(true);
        // set nested scroll view scroll to top.
        nestedScrollView.scrollTo(0, 0);
        // set swipe refresh layout refreshing.
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
        
        initStatusBarColor();
        DisplayUtils.setWindowTopColor(this);
        DisplayUtils.setNavigationBarColor(this, TimeUtils.getInstance(this).isDayTime());

        skyView.setWeather(weather);

        titleTexts[0].setText(weather.live.air);
        titleTexts[1].setText(weather.live.weather + " " + weather.live.temp + "℃");
        refreshTime.setText(weather.base.refreshTime);
        locationText.setText(weather.base.location);
        collectionIcon.setImageResource(collected ? R.drawable.ic_collected : R.drawable.ic_uncollected);

        if (TimeUtils.getInstance(this).isDayTime()) {
            overviewTitle.setTextColor(ContextCompat.getColor(this, R.color.lightPrimary_3));
            lifeInfoTitle.setTextColor(ContextCompat.getColor(this, R.color.lightPrimary_3));
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.lightPrimary_3));
        } else {
            overviewTitle.setTextColor(ContextCompat.getColor(this, R.color.darkPrimary_1));
            lifeInfoTitle.setTextColor(ContextCompat.getColor(this, R.color.darkPrimary_1));
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.darkPrimary_1));
        }

        weekWeatherView.setData(weather);
        trendView.setData(locationNow.weather, locationNow.history);
        trendView.setState(TrendView.DAILY_STATE);
        lifeInfoView.setData(locationNow.weather);

        weatherContainer.setVisibility(View.VISIBLE);
        viewShowAnimator.start();
    }

    /** <br> data. */

    // init.

    private void initData() {
        this.locationList = DatabaseHelper.getInstance(this).readLocationList();

        this.weatherHelper = new WeatherHelper();
        this.locationHelper = new LocationHelper(this);

        this.scrollTrigger = (int) (getResources().getDisplayMetrics().widthPixels / 6.8 * 4
                + DisplayUtils.dpToPx(this, 60)
                - DisplayUtils.dpToPx(this, 300 - 256));
    }

    // build.

    private void setLocationAndReset(Location location, boolean collected) {
        this.locationNow = location;
        this.collected = collected;
        reset();
    }

    public void switchCity(String name, int swipeDir) {
        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).name.equals(name)) {
                int position = swipeDir == SwipeSwitchLayout.DIRECTION_LEFT ?
                        i + 1 : i - 1;
                if (position < 0) {
                    position = locationList.size() - 1;
                } else if (position > locationList.size() - 1) {
                    position = 0;
                }

                setLocationAndReset(locationList.get(position), true);
                return;
            }
        }
        setLocationAndReset(locationList.get(0), true);
    }

    private void addLocation(Location location) {
        DatabaseHelper.getInstance(this).insertLocation(location);
        locationList.add(location);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutsManager.refreshShortcuts(this, locationList);
        }
    }

    private boolean deleteLocation(Location location) {
        if (locationList.size() <= 1) {
            SnackbarUtils.showSnackbar(getString(R.string.feedback_location_list_cannot_be_null));
            return false;
        } else {
            DatabaseHelper.getInstance(this).deleteLocation(location);
            for (int i = 0; i < locationList.size(); i ++) {
                if (locationList.get(i).name.equals(location.name)) {
                    locationList.remove(i);
                    break;
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                ShortcutsManager.refreshShortcuts(this, locationList);
            }
            return true;
        }
    }

    private void refreshLocation(Location location) {
        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).name.equals(location.name)) {
                locationList.remove(i);
                locationList.add(i, location);
                break;
            }
        }
    }

    /** <br> permission. */

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestLocationPermission() {
        PermissionUtils.requestLocationPermission(
                this, LOCATION_PERMISSIONS_REQUEST_CODE,
                new PermissionUtils.OnRequestPermissionCallback() {
            @Override
            public void onRequestSuccess() {
                SnackbarUtils.showSnackbar(getString(R.string.feedback_request_location_permission_success));
                locationHelper.requestLocation(MainActivity.this, MainActivity.this);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResult) {
        super.onRequestPermissionsResult(requestCode, permission, grantResult);
        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST_CODE:
                if (grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    SnackbarUtils.showSnackbar(getString(R.string.feedback_request_location_permission_success));
                    locationHelper.requestLocation(this, this);
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

            case R.id.container_weather_locationContainer:
                ManageDialog manageDialog = new ManageDialog();
                manageDialog.setOnLocationChangedListener(this);
                manageDialog.show(getFragmentManager(), null);
                break;

            case R.id.container_weather_location_collect_icon:
                if (collected) {
                    if (deleteLocation(locationNow)) {
                        collected = false;
                        collectionIcon.setImageResource(R.drawable.ic_uncollected);
                    }
                } else {
                    collected = true;
                    if (!locationNow.name.equals(getString(R.string.local))) {
                        locationNow.realName = locationNow.name;
                    }
                    addLocation(locationNow);
                    collectionIcon.setImageResource(R.drawable.ic_collected);
                }
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
                ManageDialog manageDialog = new ManageDialog();
                manageDialog.setOnLocationChangedListener(this);
                manageDialog.show(getFragmentManager(), null);
                break;

            case R.id.action_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_ACTIVITY);
                break;

            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        return true;
    }

    // on refresh listener.

    @Override
    public void onRefresh() {
        locationHelper.cancel();
        weatherHelper.cancel();

        if (locationNow.name.equals(getString(R.string.local))) {
            locationHelper.requestLocation(this, this);
        } else {
            if (!locationNow.name.equals(getString(R.string.local))) {
                locationNow.realName = locationNow.name;
                if (collected) {
                    DatabaseHelper.getInstance(this).insertLocation(locationNow);
                }
            }
            weatherHelper.requestWeather(this, locationNow, this);
        }
    }

    // on scroll changed listener.

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        skyView.setTranslationY((float) (-Math.min(1, 1.0 * scrollY / scrollTrigger) * skyView.getMeasuredHeight()));
        toolbar.setTranslationY((float) (-Math.min(1, 1.0 * scrollY / scrollTrigger) * toolbar.getMeasuredHeight()));
    }

    // on swipe listener(swipe switch layout).

    @Override
    public void swipeTakeEffect(int direction) {
        swipeSwitchLayout.setEnabled(false);
        switchCity(locationNow.name, direction);
    }

    // on click week listener.

    @Override
    public void onClickWeekContainer(int position) {
        WeatherDialog weatherDialog = new WeatherDialog();
        weatherDialog.setData(locationNow.weather, position);
        weatherDialog.show(getFragmentManager(), null);
    }

    // on request name listener.

    @Override
    public void requestLocationSuccess(String locationName) {
        if (locationNow.name.equals(getString(R.string.local))) {
            locationNow.realName = locationName;
            weatherHelper.requestWeather(this, locationNow, this);
            refreshLocation(locationNow);
            DatabaseHelper.getInstance(this).insertLocation(locationNow);
        }
    }

    @Override
    public void requestLocationFailed() {
        if (locationNow.name.equals(getString(R.string.local))) {
            if (locationNow.weather == null && !TextUtils.isEmpty(locationNow.realName)) {
                locationNow.weather = DatabaseHelper.getInstance(this).searchWeather(locationNow.realName);
                locationNow.history = DatabaseHelper.getInstance(this).searchYesterdayHistory(locationNow.weather);
                refreshLocation(locationNow);
                setRefreshing(false);
                buildUI();
            } else {
                setRefreshing(false);
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                SnackbarUtils.showSnackbar(getString(R.string.feedback__location_failed));
            } else {
                SnackbarUtils.showSnackbar(
                        getString(R.string.feedback__location_failed),
                        getString(R.string.feedback_request_permission),
                        snackbarAction);
            }
        }
    }

    // on request weather listener.

    @Override
    public void requestWeatherSuccess(Weather weather, String locationName) {
        if ((locationNow.name.equals(getString(R.string.local)) && locationNow.realName.equals(locationName))
                || locationNow.name.equals(locationName)) {
            if (weather == null) {
                SnackbarUtils.showSnackbar(getString(R.string.feedback_get_weather_failed));
                weather = DatabaseHelper.getInstance(this).searchWeather(locationNow.realName);
            }
            if (locationNow.weather == null
                    || !locationNow.weather.base.refreshTime.equals(weather.base.refreshTime)) {
                locationNow.weather = weather;

                locationNow.history = DatabaseHelper.getInstance(this).searchYesterdayHistory(weather);
                refreshLocation(locationNow);
                DatabaseHelper.getInstance(this).insertWeather(weather);
                DatabaseHelper.getInstance(this).insertHistory(weather);

                setRefreshing(false);
                buildUI();
            } else {
                setRefreshing(false);
            }
        }

        WidgetUtils.refreshWidgetView(this, locationNow);
        if (locationList.get(0).name.equals(locationName)) {
            NotificationUtils.refreshNotification(this, locationNow);
            NotificationUtils.startupAllOfNotificationService(this);
        }
    }

    @Override
    public void requestWeatherFailed(String name) {
        if (locationNow.name.equals(name)) {
            if (locationNow.weather == null) {
                locationNow.weather = DatabaseHelper.getInstance(this).searchWeather(locationNow.realName);

                if (locationNow.weather != null) {
                    locationNow.history = DatabaseHelper.getInstance(this)
                            .searchYesterdayHistory(locationNow.weather);
                }

                refreshLocation(locationNow);
                SnackbarUtils.showSnackbar(getString(R.string.feedback_get_weather_failed));

                setRefreshing(false);
                buildUI();
            } else {
                setRefreshing(false);
            }
        }
    }

    // on name changed listener.

    @Override
    public void selectLocation(String result) {
        Location location = null;
        boolean collected = false;
        for (Location l : locationList) {
            if (l.name.equals(result)) {
                location = l;
                collected = true;
                break;
            }
        }
        if (location == null) {
            location = new Location(
                    result,
                    result.equals(getString(R.string.local)) ?
                            null : result);
        }

        setLocationAndReset(location, collected);
    }

    @Override
    public void changeLocationList(List<String> nameList) {
        List<Location> newList = new ArrayList<>();
        for (int i = 0; i < nameList.size(); i ++) {
            for (Location l : locationList) {
                if (l.name.equals(nameList.get(i))) {
                    newList.add(l);
                    break;
                }
            }
            if (newList.size() - 1 < i) {
                newList.add(new Location(nameList.get(i), null));
            }
        }

        DatabaseHelper.getInstance(this).clearLocation();
        DatabaseHelper.getInstance(this).writeLocationList(newList);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutsManager.refreshShortcuts(this, locationList);
        }

        collected = false;
        collectionIcon.setImageResource(R.drawable.ic_uncollected);

        locationList.clear();
        for (int i = 0; i < newList.size(); i ++) {
            locationList.add(newList.get(i));
            if (locationList.get(i).name.equals(locationNow.name)) {
                collected = true;
                collectionIcon.setImageResource(R.drawable.ic_collected);
            }
        }
    }
}
