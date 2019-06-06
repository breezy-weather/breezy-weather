package wangdaye.com.geometricweather;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.tencent.bugly.crashreport.CrashReport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.weather.TLSCompactHelper;
import wangdaye.com.geometricweather.utils.LanguageUtils;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

/**
 * Geometric weather application class.
 * */

public class GeometricWeather extends Application {

    private static GeometricWeather instance;
    public static GeometricWeather getInstance() {
        return instance;
    }

    private List<GeoActivity> activityList;

    private OkHttpClient okHttpClient;
    private GsonConverterFactory gsonConverterFactory;
    private RxJava2CallAdapterFactory rxJava2CallAdapterFactory;

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

    // day.
    public static final int WIDGET_DAY_PENDING_INTENT_CODE_WEATHER = 11;
    public static final int WIDGET_DAY_PENDING_INTENT_CODE_REFRESH = 12;
    public static final int WIDGET_DAY_PENDING_INTENT_CODE_CALENDAR = 13;
    // week.
    public static final int WIDGET_WEEK_PENDING_INTENT_CODE_WEATHER = 21;
    public static final int WIDGET_WEEK_PENDING_INTENT_CODE_REFRESH = 22;
    // day + week.
    public static final int WIDGET_DAY_WEEK_PENDING_INTENT_CODE_WEATHER = 31;
    public static final int WIDGET_DAY_WEEK_PENDING_INTENT_CODE_REFRESH = 32;
    public static final int WIDGET_DAY_WEEK_PENDING_INTENT_CODE_CALENDAR = 33;
    // clock + day (vertical).
    public static final int WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_WEATHER = 41;
    public static final int WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_REFRESH = 42;
    public static final int WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_LIGHT = 43;
    public static final int WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_NORMAL = 44;
    public static final int WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_BLACK = 45;
    public static final int WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_LIGHT = 46;
    public static final int WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_LIGHT = 47;
    public static final int WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_NORMAL = 48;
    public static final int WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_NORMAL = 49;
    public static final int WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_BLACK = 50;
    public static final int WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_BLACK = 51;
    // clock + day (horizontal).
    public static final int WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_WEATHER = 61;
    public static final int WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_REFRESH = 62;
    public static final int WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CALENDAR = 63;
    public static final int WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CLOCK_LIGHT = 64;
    public static final int WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CLOCK_NORMAL = 65;
    public static final int WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CLOCK_BLACK = 66;
    // clock + day + details.
    public static final int WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_WEATHER = 71;
    public static final int WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_REFRESH = 72;
    public static final int WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CALENDAR = 73;
    public static final int WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CLOCK_LIGHT = 74;
    public static final int WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CLOCK_NORMAL = 75;
    public static final int WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CLOCK_BLACK = 76;
    // clock + day + week.
    public static final int WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_WEATHER = 81;
    public static final int WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_REFRESH = 82;
    public static final int WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CALENDAR = 83;
    public static final int WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CLOCK_LIGHT = 84;
    public static final int WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CLOCK_NORMAL = 85;
    public static final int WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CLOCK_BLACK = 86;
    // text.
    public static final int WIDGET_TEXT_PENDING_INTENT_CODE_WEATHER = 91;
    public static final int WIDGET_TEXT_PENDING_INTENT_CODE_REFRESH = 92;
    public static final int WIDGET_TEXT_PENDING_INTENT_CODE_CALENDAR = 93;
    // trend daily.
    public static final int WIDGET_TREND_DAILY_PENDING_INTENT_CODE_WEATHER = 101;
    public static final int WIDGET_TREND_DAILY_PENDING_INTENT_CODE_REFRESH = 102;
    // trend hourly.
    public static final int WIDGET_TREND_HOURLY_PENDING_INTENT_CODE_WEATHER = 111;
    public static final int WIDGET_TREND_HOURLY_PENDING_INTENT_CODE_REFRESH = 112;

    @Override
    public void onCreate() {
        super.onCreate();
        initialize();

        String processName = getProcessName();
        if (processName != null && processName.equals(getPackageName())) {
            resetDayNightMode();
        }
    }

    private void initialize() {
        instance = this;
        activityList = new ArrayList<>();

        okHttpClient = TLSCompactHelper.getClientBuilder().build();
        gsonConverterFactory = GsonConverterFactory.create();
        rxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create();

        LanguageUtils.setLanguage(this, SettingsOptionManager.getInstance(this).getLanguage());

        CrashReport.initCrashReport(getApplicationContext(), "148f1437d5", false);
    }

    public void addActivity(GeoActivity a) {
        activityList.add(a);
    }

    public void removeActivity(GeoActivity a) {
        activityList.remove(a);
    }

    @Nullable
    public GeoActivity getTopActivity() {
        if (activityList.size() == 0) {
            return null;
        }
        return activityList.get(activityList.size() - 1);
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public GsonConverterFactory getGsonConverterFactory() {
        return gsonConverterFactory;
    }

    public RxJava2CallAdapterFactory getRxJava2CallAdapterFactory() {
        return rxJava2CallAdapterFactory;
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

    public void resetDayNightMode() {
        switch (SettingsOptionManager.getInstance(this).getDarkMode()) {
            case "auto":
                AppCompatDelegate.setDefaultNightMode(
                        TimeManager.getInstance(this).isDayTime()
                                ? AppCompatDelegate.MODE_NIGHT_NO
                                : AppCompatDelegate.MODE_NIGHT_YES
                );
                break;

            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;

            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
    }

    public void recreateAllActivities() {
        for (Activity a : activityList) {
            a.recreate();
        }
    }
}
