package wangdaye.com.geometricweather;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.utils.LanguageUtils;

/**
 * Geometric realTimeWeather.
 * */

public class GeometricWeather extends Application {

    private static GeometricWeather instance;

    public static GeometricWeather getInstance() {
        return instance;
    }

    private List<GeoActivity> activityList;
    private String chineseSource;
    private String locationService;
    private String cardOrder;
    private boolean colorNavigationBar;
    private boolean fahrenheit;
    private boolean imperial;
    private String language;

    public static final String DEFAULT_TODAY_FORECAST_TIME = "07:00";
    public static final String DEFAULT_TOMORROW_FORECAST_TIME = "21:00";

    public static final String NOTIFICATION_CHANNEL_ID_NORMALLY = "normally";
    public static final String NOTIFICATION_CHANNEL_ID_ALERT = "alert";
    public static final String NOTIFICATION_CHANNEL_ID_FORECAST = "forecast";
    public static final String NOTIFICATION_CHANNEL_ID_LOCATION = "location";
    public static final String NOTIFICATION_CHANNEL_ID_BACKGROUND = "background";

    public static final int NOTIFICATION_ID_NORMALLY = 1;
    public static final int NOTIFICATION_ID_TODAY_FORECAST = 2;
    public static final int NOTIFICATION_ID_TOMORROW_FORECAST = 3;
    public static final int NOTIFICATION_ID_LOCATION = 4;
    public static final int NOTIFICATION_ID_RUNNING_IN_BACKGROUND = 5;
    public static final int NOTIFICATION_ID_UPDATING_NORMALLY = 6;
    public static final int NOTIFICATION_ID_UPDATING_TODAY_FORECAST= 7;
    public static final int NOTIFICATION_ID_UPDATING_TOMORROW_FORECAST= 8;
    public static final int NOTIFICATION_ID_ALERT_MIN = 1000;
    public static final int NOTIFICATION_ID_ALERT_MAX = 1999;
    public static final int NOTIFICATION_ID_ALERT_GROUP = 2000;

    public static final int WIDGET_DAY_PENDING_INTENT_CODE = 11;
    public static final int WIDGET_WEEK_PENDING_INTENT_CODE = 21;
    public static final int WIDGET_DAY_WEEK_PENDING_INTENT_CODE = 31;
    public static final int WIDGET_CLOCK_DAY_VERTICAL_WEATHER_PENDING_INTENT_CODE = 41;
    public static final int WIDGET_CLOCK_DAY_VERTICAL_CLOCK_PENDING_INTENT_CODE = 42;
    public static final int WIDGET_CLOCK_DAY_HORIZONTAL_WEATHER_PENDING_INTENT_CODE = 51;
    public static final int WIDGET_CLOCK_DAY_HORIZONTAL_CLOCK_PENDING_INTENT_CODE = 52;
    public static final int WIDGET_CLOCK_DAY_DETAILS_WEATHER_PENDING_INTENT_CODE = 61;
    public static final int WIDGET_CLOCK_DAY_DETAILS_CLOCK_PENDING_INTENT_CODE = 62;
    public static final int WIDGET_CLOCK_DAY_WEEK_WEATHER_PENDING_INTENT_CODE = 71;
    public static final int WIDGET_CLOCK_DAY_WEEK_CLOCK_PENDING_INTENT_CODE = 72;
    public static final int WIDGET_TEXT_PENDING_INTENT_CODE = 81;

    @Override
    public void onCreate() {
        super.onCreate();
        String processName = getProcessName();
        if (!TextUtils.isEmpty(processName)
                && processName.equals(this.getPackageName())) {
            initialize();
        }
    }

    private void initialize() {
        instance = this;
        activityList = new ArrayList<>();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        chineseSource = sharedPreferences.getString(getString(R.string.key_chinese_source), "cn");
        locationService = sharedPreferences.getString(getString(R.string.key_location_service), "baidu");
        cardOrder = sharedPreferences.getString(getString(R.string.key_card_order), "daily_first");
        colorNavigationBar = sharedPreferences.getBoolean(getString(R.string.key_navigationBar_color), false);
        fahrenheit = sharedPreferences.getBoolean(getString(R.string.key_fahrenheit), false);
        imperial = sharedPreferences.getBoolean(getString(R.string.key_imperial), false);
        language = sharedPreferences.getString(getString(R.string.key_language), "follow_system");

        LanguageUtils.setLanguage(this, language);
    }

    public void addActivity(GeoActivity a) {
        activityList.add(a);
    }

    public void removeActivity() {
        activityList.remove(activityList.size() - 1);
    }

    @Nullable
    public GeoActivity getTopActivity() {
        if (activityList.size() == 0) {
            return null;
        }
        return activityList.get(activityList.size() - 1);
    }

    public String getChineseSource() {
        return chineseSource;
    }

    public void setChineseSource(String chineseSource) {
        this.chineseSource = chineseSource;
    }

    public String getLocationService() {
        return locationService;
    }

    public void setLocationService(String locationService) {
        this.locationService = locationService;
    }

    public String getCardOrder() {
        return cardOrder;
    }

    public void setCardOrder(String cardOrder) {
        this.cardOrder = cardOrder;
    }

    public boolean isColorNavigationBar() {
        return colorNavigationBar;
    }

    public void setColorNavigationBar() {
        this.colorNavigationBar = !colorNavigationBar;
    }

    public boolean isFahrenheit() {
        return fahrenheit;
    }

    public void setFahrenheit(boolean fahrenheit) {
        this.fahrenheit = fahrenheit;
    }

    public boolean isImperial() {
        return imperial;
    }

    public void setImperial(boolean imperial) {
        this.imperial = imperial;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public static String getProcessName() {
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getNotificationChannelName(Context c, String channelId) {
        switch (channelId) {
            case NOTIFICATION_CHANNEL_ID_ALERT:
                return c.getString(R.string.geometric_weather) + " " + c.getString(R.string.action_alert);

            case NOTIFICATION_CHANNEL_ID_FORECAST:
                return c.getString(R.string.geometric_weather) + " " + c.getString(R.string.forecast);

            case NOTIFICATION_CHANNEL_ID_LOCATION:
                return c.getString(R.string.geometric_weather) + " " + c.getString(R.string.feedback_request_location);

            case NOTIFICATION_CHANNEL_ID_BACKGROUND:
                return c.getString(R.string.geometric_weather) + " " + c.getString(R.string.background_information);

            default:
                return c.getString(R.string.geometric_weather);
        }
    }
}
