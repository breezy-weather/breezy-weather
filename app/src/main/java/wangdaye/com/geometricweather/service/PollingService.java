package wangdaye.com.geometricweather.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Calendar;

import wangdaye.com.geometricweather.service.alarm.PollingAlarmService;
import wangdaye.com.geometricweather.service.alarm.TodayForecastAlarmService;
import wangdaye.com.geometricweather.service.alarm.TomorrowForecastAlarmService;

/**
 * Polling service.
 * */

public class PollingService extends Service {
    // widget
    private static TimeTickReceiver receiver;

    // data
    private static boolean sPower;
    private static boolean running;
    private static boolean working;
    private static boolean forceRefresh;
    private static boolean onlyRefreshNormalView;

    private String lastPollingTime;
    private boolean openTodayForecast;
    private String todayForecastTime;
    private boolean openTomorrowForecast;
    private String tomorrowForecastTime;

    private static final String PREFERENCE_NAME = "polling_service_preference";
    private static final String KEY_LAST_POLLING_TIME = "last_polling_time";
    private static final String KEY_OPEN_TODAY_FORECAST = "open_today_forecast";
    private static final String KEY_TODAY_FORECAST_TIME = "today_forecast_time";
    private static final String KEY_OPEN_TOMORROW_FORECAST = "open_tomorrow_forecast";
    private static final String KEY_TOMORROW_FORECAST_TIME = "tomorrow_forecast_time";

    /** <br> life cycle. */

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
        registerReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        readData(intent);
        doProtectionWork();
        doRefreshWork(this, true);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** <br> data. */

    private void initData() {
        sPower = true;
        running = false;
        working = true;
        forceRefresh = false;
        onlyRefreshNormalView = false;

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        this.lastPollingTime = sharedPreferences.getString(KEY_LAST_POLLING_TIME, null);
        this.openTodayForecast = sharedPreferences.getBoolean(KEY_OPEN_TODAY_FORECAST, false);
        this.todayForecastTime = sharedPreferences.getString(KEY_TODAY_FORECAST_TIME, null);
        this.openTomorrowForecast = sharedPreferences.getBoolean(KEY_OPEN_TOMORROW_FORECAST, false);
        this.tomorrowForecastTime = sharedPreferences.getString(KEY_TOMORROW_FORECAST_TIME, null);
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        receiver = new TimeTickReceiver();
        registerReceiver(receiver, filter);
    }

    private void readData(Intent intent) {
        if (intent != null && intent.getBooleanExtra("is_refresh", false)) {
            working = intent.getBooleanExtra("working", true);
            forceRefresh = intent.getBooleanExtra("force_refresh", false);
            onlyRefreshNormalView = intent.getBooleanExtra("only_refresh_normal_view", false);

            openTodayForecast = intent.getBooleanExtra("today_forecast", false);
            todayForecastTime = intent.getStringExtra("today_forecast_time");
            openTomorrowForecast = intent.getBooleanExtra("tomorrow_forecast", false);
            tomorrowForecastTime = intent.getStringExtra("tomorrow_forecast_time");

            SharedPreferences.Editor editor = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE).edit();
            editor.putBoolean(KEY_OPEN_TODAY_FORECAST, openTodayForecast);
            editor.putString(KEY_TODAY_FORECAST_TIME, todayForecastTime);
            editor.putBoolean(KEY_OPEN_TOMORROW_FORECAST, openTomorrowForecast);
            editor.putString(KEY_TOMORROW_FORECAST_TIME, tomorrowForecastTime);
            editor.apply();
        }
    }

    private void doProtectionWork() {
        if (!running && working) {
            running = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (sPower && working) {
                        if (System.currentTimeMillis() >= 123456789000000L) {
                            sPower = false;
                        }
                        startService(new Intent(PollingService.this, ProtectService.class));
                        SystemClock.sleep(3000);
                    }
                    if (!working) {
                        stopSelf();
                    }
                }
            }).start();
        }
    }

    private void doRefreshWork(Context context, boolean init) {
        // polling service.
        if (forceRefresh || TextUtils.isEmpty(lastPollingTime)) {
            startServiceAndRefresh(context);
        } else {
            int realTimes[] = new int[] {
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    Calendar.getInstance().get(Calendar.MINUTE)};
            int lastTimes[] = new int[]{
                    Integer.parseInt(lastPollingTime.split(":")[0]),
                    Integer.parseInt(lastPollingTime.split(":")[1])};
            int deltaTime = (realTimes[0] * 60 + realTimes[1]) - (lastTimes[0] * 60 + lastTimes[1]);
            if ((realTimes[0] == 0 && realTimes[1] == 10)
                    || Math.abs(deltaTime) >= 60 * 1.5) {
                startServiceAndRefresh(context);
            }
        }

        // forecast.
        if ((!init || forceRefresh) && !onlyRefreshNormalView) {
            int realTimes[] = new int[] {
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    Calendar.getInstance().get(Calendar.MINUTE)};

            // today.
            if (openTodayForecast && !TextUtils.isEmpty(todayForecastTime)) {
                int setTimes[] = new int[]{
                        Integer.parseInt(todayForecastTime.split(":")[0]),
                        Integer.parseInt(todayForecastTime.split(":")[1])};
                if (realTimes[0] == setTimes[0] && realTimes[1] == setTimes[1]) {
                    Intent intent = new Intent(context, TodayForecastAlarmService.class);
                    startService(intent);
                }
            }

            // tomorrow.
            if (openTomorrowForecast && !TextUtils.isEmpty(tomorrowForecastTime)) {
                int setTimes[] = new int[]{
                        Integer.parseInt(tomorrowForecastTime.split(":")[0]),
                        Integer.parseInt(tomorrowForecastTime.split(":")[1])};
                if (realTimes[0] == setTimes[0] && realTimes[1] == setTimes[1]) {
                    Intent intent = new Intent(context, TomorrowForecastAlarmService.class);
                    startService(intent);
                }
            }
        }

        forceRefresh = false;
        onlyRefreshNormalView = false;
    }

    private void startServiceAndRefresh(Context context) {
        lastPollingTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                + ":" + Calendar.getInstance().get(Calendar.MINUTE);
        Intent intent = new Intent(context, PollingAlarmService.class);
        startService(intent);
        savePollingTime();
    }

    private void savePollingTime() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE).edit();
        editor.putString(KEY_LAST_POLLING_TIME, lastPollingTime);
        editor.apply();
    }

    /** <br> inner class. */

    private class TimeTickReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_TIME_TICK:
                case Intent.ACTION_TIME_CHANGED:
                    doRefreshWork(context, false);
                    break;
            }
        }
    }
}
