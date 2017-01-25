package wangdaye.com.geometricweather.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

    private String lastPollingTime;
    private boolean openTodayForecast;
    private String todayForecastTime;
    private boolean openTomorrowForecast;
    private String tomorrowForecastTime;

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
        doRefreshWork(this);
        return START_NOT_STICKY;
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

        this.lastPollingTime = null;
        this.openTodayForecast = false;
        this.todayForecastTime = null;
        this.openTomorrowForecast = false;
        this.tomorrowForecastTime = null;
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        receiver = new TimeTickReceiver();
        registerReceiver(receiver, filter);
    }

    private void readData(Intent intent) {
        if (intent.getBooleanExtra("from_main", false)) {
            working = intent.getBooleanExtra("working", true);
            forceRefresh = intent.getBooleanExtra("force_refresh", false);

            openTodayForecast = intent.getBooleanExtra("today_forecast", false);
            todayForecastTime = intent.getStringExtra("today_forecast_time");
            openTomorrowForecast = intent.getBooleanExtra("tomorrow_forecast", false);
            tomorrowForecastTime = intent.getStringExtra("tomorrow_forecast_time");
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

    private void doRefreshWork(Context context) {
        // polling service.
        if (forceRefresh || TextUtils.isEmpty(lastPollingTime)) {
            forceRefresh = false;
            lastPollingTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    + ":" + Calendar.getInstance().get(Calendar.MINUTE);
            Intent intent = new Intent(context, PollingAlarmService.class);
            startService(intent);
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
                Intent intent = new Intent(context, PollingAlarmService.class);
                startService(intent);
            }
        }

        // forecast.
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

    /** <br> inner class. */

    private class TimeTickReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_TIME_TICK:
                case Intent.ACTION_TIME_CHANGED:
                    doRefreshWork(context);
                    break;
            }
        }
    }
}
