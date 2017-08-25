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

    private static boolean fromAlarm;

    private static float pollingRate;
    private static long lastUpdateNormalViewTime;

    private static boolean openTodayForecast;
    private static String todayForecastTime;
    private static boolean openTomorrowForecast;
    private static String tomorrowForecastTime;

    private static final String PREFERENCE_NAME = "polling_service_preference";
    public static final String KEY_POLLING_RATE = "polling_rate";
    public static final String KEY_OPEN_TODAY_FORECAST = "open_today_forecast";
    public static final String KEY_TODAY_FORECAST_TIME = "today_forecast_time";
    public static final String KEY_OPEN_TOMORROW_FORECAST = "open_tomorrow_forecast";
    public static final String KEY_TOMORROW_FORECAST_TIME = "tomorrow_forecast_time";

    public static final String KEY_IS_REFRESH = "is_refresh";
    public static final String KEY_WORKING = "working";
    public static final String KEY_BACKGROUND_FREE = "background_free";
    public static final String KEY_FORCE_REFRESH_TYPE = "refresh_type";

    private static final String KEY_FROM_ALARM = "from_alarm";

    public static final String KEY_POLLING_UPDATE_FAILED = "polling_update_failed";

    private static final int FORCE_REFRESH_TYPE_NONE = 0;
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
                        doRefreshWork(true);
                        break;

                    case Intent.ACTION_TIME_CHANGED:
                    case Intent.ACTION_TIMEZONE_CHANGED:
                        lastUpdateNormalViewTime = -1;
                        startService(getPollingServiceIntent(FORCE_REFRESH_TYPE_ALL, false));
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
        registerReceiver();
        initData();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        readData(intent);
        doProtectionWork();
        doAlarmWork();
        doRefreshWork(false);
        if (!running && (!working || backgroundFree)) {
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
        fromAlarm = false;
        forceRefreshType = FORCE_REFRESH_TYPE_NONE;

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);

        pollingRate = sharedPreferences.getFloat(KEY_POLLING_RATE, 1.5f);
        lastUpdateNormalViewTime = -1;

        openTodayForecast = sharedPreferences.getBoolean(KEY_OPEN_TODAY_FORECAST, false);
        todayForecastTime = sharedPreferences.getString(KEY_TODAY_FORECAST_TIME, null);
        openTomorrowForecast = sharedPreferences.getBoolean(KEY_OPEN_TOMORROW_FORECAST, false);
        tomorrowForecastTime = sharedPreferences.getString(KEY_TOMORROW_FORECAST_TIME, null);
    }

    private void readData(Intent intent) {
        if (intent != null && intent.getBooleanExtra(KEY_IS_REFRESH, false)) {
            working = intent.getBooleanExtra(KEY_WORKING, true);
            backgroundFree = intent.getBooleanExtra(KEY_BACKGROUND_FREE, false);
            fromAlarm = intent.getBooleanExtra(KEY_FROM_ALARM, false);
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
        }
        if (intent != null && intent.getBooleanExtra(KEY_POLLING_UPDATE_FAILED, false)) {
            lastUpdateNormalViewTime = System.currentTimeMillis() - getPollingDelay() + 15 * 60 * 1000;
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
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
        if (forceRefreshType != FORCE_REFRESH_TYPE_NONE) {
            if (working) {
                if (backgroundFree) {
                    AlarmHelper.setAlarmForNormalView(
                            this, getPollingServiceIntent(FORCE_REFRESH_TYPE_NORMAL_VIEW, true), pollingRate);
                }
                if (openTodayForecast) {
                    AlarmHelper.setAlarmForTodayForecast(
                            this, getPollingServiceIntent(FORCE_REFRESH_TYPE_FORECAST_TODAY, true), todayForecastTime);
                }
                if (openTomorrowForecast) {
                    AlarmHelper.setAlarmForTomorrowForecast(
                            this, getPollingServiceIntent(FORCE_REFRESH_TYPE_FORECAST_TOMORROW, true), tomorrowForecastTime);
                }
            } else {
                if (backgroundFree) {
                    AlarmHelper.cancelNormalViewAlarm(
                            this, getPollingServiceIntent(FORCE_REFRESH_TYPE_NORMAL_VIEW, true));
                }
                AlarmHelper.cancelTodayForecastAlarm(
                        this, getPollingServiceIntent(FORCE_REFRESH_TYPE_FORECAST_TODAY, true));
                AlarmHelper.cancelTomorrowForecastAlarm(
                        this, getPollingServiceIntent(FORCE_REFRESH_TYPE_FORECAST_TOMORROW, true));
            }
        }
    }

    private void doRefreshWork(boolean fromBroadcast) {
        // polling service.
        if (lastUpdateNormalViewTime == -1
                || forceRefreshType == FORCE_REFRESH_TYPE_NORMAL_VIEW
                || forceRefreshType == FORCE_REFRESH_TYPE_ALL
                || System.currentTimeMillis() - lastUpdateNormalViewTime > getPollingDelay()) {
            lastUpdateNormalViewTime = System.currentTimeMillis();
            Intent intent = new Intent(this, NormalUpdateService.class);
            intent.putExtra(NormalUpdateService.KEY_NEED_FAILED_CALLBACK, true);
            startService(intent);
        }

        // forecast.
        if (!fromBroadcast
                && forceRefreshType != FORCE_REFRESH_TYPE_NONE
                && forceRefreshType != FORCE_REFRESH_TYPE_NORMAL_VIEW) {
            int realTimes[] = new int[] {
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    Calendar.getInstance().get(Calendar.MINUTE)};

            // today.
            if ((openTodayForecast && !TextUtils.isEmpty(todayForecastTime))) {
                int setTimes[] = new int[]{
                        Integer.parseInt(todayForecastTime.split(":")[0]),
                        Integer.parseInt(todayForecastTime.split(":")[1])};
                if ((realTimes[0] == setTimes[0] && realTimes[1] == setTimes[1])
                        || (fromAlarm && forceRefreshType != FORCE_REFRESH_TYPE_FORECAST_TOMORROW)) {
                    Intent intent = new Intent(this, TodayForecastUpdateService.class);
                    startService(intent);
                }
            }

            // tomorrow.
            if ((openTomorrowForecast && !TextUtils.isEmpty(tomorrowForecastTime))) {
                int setTimes[] = new int[]{
                        Integer.parseInt(tomorrowForecastTime.split(":")[0]),
                        Integer.parseInt(tomorrowForecastTime.split(":")[1])};
                if ((realTimes[0] == setTimes[0] && realTimes[1] == setTimes[1])
                        || (fromAlarm && forceRefreshType != FORCE_REFRESH_TYPE_FORECAST_TODAY)) {
                    Intent intent = new Intent(this, TomorrowForecastUpdateService.class);
                    startService(intent);
                }
            }
        }

        fromAlarm = false;
        forceRefreshType = FORCE_REFRESH_TYPE_NONE;
    }

    private Intent getPollingServiceIntent(int forceRefreshType, boolean fromAlarm) {
        Intent intent = new Intent(this, PollingService.class);
        intent.putExtra(PollingService.KEY_IS_REFRESH, true);
        intent.putExtra(PollingService.KEY_WORKING, working);
        intent.putExtra(PollingService.KEY_BACKGROUND_FREE, backgroundFree);
        intent.putExtra(PollingService.KEY_FROM_ALARM, fromAlarm);
        intent.putExtra(PollingService.KEY_FORCE_REFRESH_TYPE, forceRefreshType);
        intent.putExtra(PollingService.KEY_POLLING_RATE, pollingRate);
        intent.putExtra(PollingService.KEY_OPEN_TODAY_FORECAST, openTodayForecast);
        intent.putExtra(PollingService.KEY_TODAY_FORECAST_TIME, todayForecastTime);
        intent.putExtra(PollingService.KEY_OPEN_TOMORROW_FORECAST, openTomorrowForecast);
        intent.putExtra(PollingService.KEY_TOMORROW_FORECAST_TIME, tomorrowForecastTime);
        return intent;
    }

    private long getPollingDelay() {
        return (long) (pollingRate * 1000 * 60 * 60);
    }
}