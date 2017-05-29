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

import wangdaye.com.geometricweather.utils.helpter.AlarmHelper;

/**
 * Polling service.
 * */

public class PollingService extends Service {

    private static TimeTickReceiver receiver;

    private static boolean sPower;
    private static boolean running;
    private static boolean working;
    private static boolean backgroundFree;
    private static int forceRefreshType;

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
    public static final String KEY_BACKGROUND_FREE = "background_free";
    public static final String KEY_FORCE_REFRESH_TYPE = "refresh_type";

    public static final int FORCE_REFRESH_TYPE_NONE = 0;
    public static final int FORCE_REFRESH_TYPE_NORMAL_VIEW = 1;
    public static final int FORCE_REFRESH_TYPE_FORECAST_TODAY = 2;
    public static final int FORCE_REFRESH_TYPE_FORECAST_TOMORROW = 3;
    public static final int FORCE_REFRESH_TYPE_FORECAST = 4;
    public static final int FORCE_REFRESH_TYPE_ALL = 5;

    private class TimeTickReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!backgroundFree) {
                switch (intent.getAction()) {
                    case Intent.ACTION_TIME_TICK:
                    case Intent.ACTION_TIME_CHANGED:
                        doRefreshWork(context, false);
                        break;
                }
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
        doAlarmWork();
        doRefreshWork(this, true);
        if (!working || backgroundFree) {
            stopSelf();
        }
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
        backgroundFree = false;
        forceRefreshType = FORCE_REFRESH_TYPE_NONE;

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
            backgroundFree = intent.getBooleanExtra(KEY_BACKGROUND_FREE, false);
            forceRefreshType = intent.getIntExtra(KEY_FORCE_REFRESH_TYPE, FORCE_REFRESH_TYPE_NONE);

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
        if (!running && working && !backgroundFree) {
            running = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (sPower && working && !backgroundFree) {
                        if (System.currentTimeMillis() >= 123456789000000L) {
                            sPower = false;
                        }
                        startService(new Intent(PollingService.this, ProtectService.class));
                        SystemClock.sleep(3000);
                    }
                    if (!working || backgroundFree) {
                        stopSelf();
                    }
                }
            }).start();
        }
    }

    private void doAlarmWork() {
        if (working && backgroundFree) {
            AlarmHelper.setAlarmForNormalView(
                    this, getBaseIntent(FORCE_REFRESH_TYPE_NORMAL_VIEW), pollingRate);
            if (openTodayForecast) {
                AlarmHelper.setAlarmForTodayForecast(
                        this, getBaseIntent(FORCE_REFRESH_TYPE_FORECAST_TODAY), todayForecastTime);
            }
            if (openTomorrowForecast) {
                AlarmHelper.setAlarmForTomorrowForecast(
                        this, getBaseIntent(FORCE_REFRESH_TYPE_FORECAST_TOMORROW), tomorrowForecastTime);
            }
        } else {
            AlarmHelper.cancelNormalViewAlarm(
                    this, getBaseIntent(FORCE_REFRESH_TYPE_NORMAL_VIEW));
            AlarmHelper.cancelTodayForecastAlarm(
                    this, getBaseIntent(FORCE_REFRESH_TYPE_FORECAST_TODAY));
            AlarmHelper.cancelTomorrowForecastAlarm(
                    this, getBaseIntent(FORCE_REFRESH_TYPE_FORECAST_TOMORROW));
        }
    }

    private void doRefreshWork(Context context, boolean init) {
        // polling service.
        if (TextUtils.isEmpty(lastPollingTime)
                || forceRefreshType == FORCE_REFRESH_TYPE_NORMAL_VIEW
                || forceRefreshType == FORCE_REFRESH_TYPE_ALL) {
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
        if (!init
                || (forceRefreshType != FORCE_REFRESH_TYPE_NONE
                && forceRefreshType != FORCE_REFRESH_TYPE_NORMAL_VIEW)) {
            int realTimes[] = new int[] {
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    Calendar.getInstance().get(Calendar.MINUTE)};

            // today.
            if ((openTodayForecast && !TextUtils.isEmpty(todayForecastTime))) {
                int setTimes[] = new int[]{
                        Integer.parseInt(todayForecastTime.split(":")[0]),
                        Integer.parseInt(todayForecastTime.split(":")[1])};
                if ((realTimes[0] == setTimes[0] && realTimes[1] == setTimes[1])
                        || (!init && backgroundFree && forceRefreshType != FORCE_REFRESH_TYPE_FORECAST_TOMORROW)) {
                    Intent intent = new Intent(context, TodayForecastUpdateService.class);
                    startService(intent);
                }
            }

            // tomorrow.
            if ((openTomorrowForecast && !TextUtils.isEmpty(tomorrowForecastTime))) {
                int setTimes[] = new int[]{
                        Integer.parseInt(tomorrowForecastTime.split(":")[0]),
                        Integer.parseInt(tomorrowForecastTime.split(":")[1])};
                if ((realTimes[0] == setTimes[0] && realTimes[1] == setTimes[1])
                        || (!init && backgroundFree && forceRefreshType != FORCE_REFRESH_TYPE_FORECAST_TODAY)) {
                    Intent intent = new Intent(context, TomorrowForecastUpdateService.class);
                    startService(intent);
                }
            }
        }

        forceRefreshType = FORCE_REFRESH_TYPE_NONE;
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

    private Intent getBaseIntent(int forceRefreshType) {
        Intent intent = new Intent(this, PollingService.class);
        intent.putExtra(PollingService.KEY_IS_REFRESH, true);
        intent.putExtra(PollingService.KEY_WORKING, working);
        intent.putExtra(PollingService.KEY_BACKGROUND_FREE, backgroundFree);
        intent.putExtra(PollingService.KEY_FORCE_REFRESH_TYPE, forceRefreshType);
        intent.putExtra(PollingService.KEY_POLLING_RATE, pollingRate);
        intent.putExtra(PollingService.KEY_OPEN_TODAY_FORECAST, openTodayForecast);
        intent.putExtra(PollingService.KEY_TODAY_FORECAST_TIME, todayForecastTime);
        intent.putExtra(PollingService.KEY_OPEN_TOMORROW_FORECAST, openTomorrowForecast);
        intent.putExtra(PollingService.KEY_TOMORROW_FORECAST_TIME, tomorrowForecastTime);
        return intent;
    }
}
