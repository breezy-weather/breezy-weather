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

/**
 * Polling service.
 * */

public class PollingService extends Service {

    private static TimeTickReceiver receiver;

    private static boolean sPower;
    private static boolean running;
    private static boolean working;
    private static boolean forceRefresh;
    private static boolean onlyRefreshNormalView;

    private static float pollingRate;
    private static String lastPollingTime;

    private static boolean openTodayForecast;
    private static String todayForecastTime;
    private static boolean openTomorrowForecast;
    private static String tomorrowForecastTime;

    private static final String PREFERENCE_NAME = "polling_service_preference";
    public static final String KEY_POLLING_RATE = "polling_rate";
    public static final String KEY_LAST_POLLING_TIME = "last_polling_time";
    public static final String KEY_OPEN_TODAY_FORECAST = "open_today_forecast";
    public static final String KEY_TODAY_FORECAST_TIME = "today_forecast_time";
    public static final String KEY_OPEN_TOMORROW_FORECAST = "open_tomorrow_forecast";
    public static final String KEY_TOMORROW_FORECAST_TIME = "tomorrow_forecast_time";

    public static final String KEY_IS_REFRESH = "is_refresh";
    public static final String KEY_WORKING = "working";
    public static final String KEY_FORCE_REFRESH = "force_refresh";
    public static final String KEY_REFRESH_NORMAL_VIEW = "only_refresh_normal_view";

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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
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
        unregisterReceiver();
    }

    private void initData() {
        sPower = true;
        running = false;
        working = true;
        forceRefresh = false;
        onlyRefreshNormalView = false;

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);

        pollingRate = sharedPreferences.getFloat(KEY_POLLING_RATE, 1.5f);
        lastPollingTime = sharedPreferences.getString(KEY_LAST_POLLING_TIME, null);

        openTodayForecast = sharedPreferences.getBoolean(KEY_OPEN_TODAY_FORECAST, false);
        todayForecastTime = sharedPreferences.getString(KEY_TODAY_FORECAST_TIME, null);
        openTomorrowForecast = sharedPreferences.getBoolean(KEY_OPEN_TOMORROW_FORECAST, false);
        tomorrowForecastTime = sharedPreferences.getString(KEY_TOMORROW_FORECAST_TIME, null);
    }

    private void readData(Intent intent) {
        if (intent != null && intent.getBooleanExtra(KEY_IS_REFRESH, false)) {
            working = intent.getBooleanExtra(KEY_WORKING, true);
            forceRefresh = intent.getBooleanExtra(KEY_FORCE_REFRESH, false);
            onlyRefreshNormalView = intent.getBooleanExtra(KEY_REFRESH_NORMAL_VIEW, false);

            pollingRate = intent.getFloatExtra(KEY_POLLING_RATE, 1.5f);

            openTodayForecast = intent.getBooleanExtra(KEY_OPEN_TODAY_FORECAST, false);
            todayForecastTime = intent.getStringExtra(KEY_TODAY_FORECAST_TIME);
            openTomorrowForecast = intent.getBooleanExtra(KEY_OPEN_TOMORROW_FORECAST, false);
            tomorrowForecastTime = intent.getStringExtra(KEY_TOMORROW_FORECAST_TIME);

            SharedPreferences.Editor editor = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE).edit();
            editor.putFloat(KEY_POLLING_RATE, pollingRate);
            editor.putBoolean(KEY_OPEN_TODAY_FORECAST, openTodayForecast);
            editor.putString(KEY_TODAY_FORECAST_TIME, todayForecastTime);
            editor.putBoolean(KEY_OPEN_TOMORROW_FORECAST, openTomorrowForecast);
            editor.putString(KEY_TOMORROW_FORECAST_TIME, tomorrowForecastTime);
            editor.apply();

            registerReceiver();
        } else if (intent == null || receiver == null) {
            registerReceiver();
        }
    }

    private void registerReceiver() {
        unregisterReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        receiver = new TimeTickReceiver();
        registerReceiver(receiver, filter);
    }

    private void unregisterReceiver() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
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
            if ((realTimes[0] == 0 && realTimes[1] == 15)
                    || Math.abs(deltaTime) >= 60 * pollingRate) {
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
                    Intent intent = new Intent(context, TodayForecastUpdateService.class);
                    startService(intent);
                }
            }

            // tomorrow.
            if (openTomorrowForecast && !TextUtils.isEmpty(tomorrowForecastTime)) {
                int setTimes[] = new int[]{
                        Integer.parseInt(tomorrowForecastTime.split(":")[0]),
                        Integer.parseInt(tomorrowForecastTime.split(":")[1])};
                if (realTimes[0] == setTimes[0] && realTimes[1] == setTimes[1]) {
                    Intent intent = new Intent(context, TomorrowForecastUpdateService.class);
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
        Intent intent = new Intent(context, NormalUpdateService.class);
        startService(intent);
        savePollingTime();
    }

    private void savePollingTime() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE).edit();
        editor.putString(KEY_LAST_POLLING_TIME, lastPollingTime);
        editor.apply();
    }
}
