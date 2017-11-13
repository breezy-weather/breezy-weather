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

/**
 * Polling service.
 * */

public class PollingService extends Service {

    private static TimeTickReceiver receiver;

    private static boolean running;
    private static boolean working;
    private static boolean forceRefresh;

    private static float pollingRate;
    private static long lastUpdateNormalViewTime;

    private static final String PREFERENCE_NAME = "polling_service_preference";
    public static final String KEY_POLLING_RATE = "polling_rate";

    public static final String KEY_IS_REFRESH = "is_refresh";
    public static final String KEY_WORKING = "working";
    public static final String KEY_FORCE_REFRESH = "force_refresh";

    public static final String KEY_POLLING_UPDATE_FAILED = "polling_update_failed";

    private class TimeTickReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                switch (intent.getAction()) {
                    case Intent.ACTION_TIME_TICK:
                        doRefreshWork();
                        break;

                    case Intent.ACTION_TIME_CHANGED:
                    case Intent.ACTION_TIMEZONE_CHANGED:
                        lastUpdateNormalViewTime = -1;
                        doRefreshWork();
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
        doRefreshWork();
        if (!running && !working) {
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
        running = false;
        working = true;

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        pollingRate = sharedPreferences.getFloat(KEY_POLLING_RATE, 1.5f);
        lastUpdateNormalViewTime = -1;
    }

    private void readData(Intent intent) {
        if (intent != null && intent.getBooleanExtra(KEY_IS_REFRESH, false)) {
            working = intent.getBooleanExtra(KEY_WORKING, true);
            pollingRate = intent.getFloatExtra(KEY_POLLING_RATE, 1.5f);
            forceRefresh = intent.getBooleanExtra(KEY_FORCE_REFRESH, false);

            SharedPreferences.Editor editor = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE).edit();
            editor.putFloat(KEY_POLLING_RATE, pollingRate);
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
        if (!running && working) {
            running = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (working) {
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

    private void doRefreshWork() {
        if (lastUpdateNormalViewTime == -1
                || forceRefresh
                || System.currentTimeMillis() - lastUpdateNormalViewTime > getPollingDelay()) {
            lastUpdateNormalViewTime = System.currentTimeMillis();
            forceRefresh = false;
            Intent intent = new Intent(this, NormalUpdateService.class);
            intent.putExtra(NormalUpdateService.KEY_NEED_FAILED_CALLBACK, true);
            startService(intent);
        }
    }

    private long getPollingDelay() {
        return (long) (pollingRate * 1000 * 60 * 60);
    }
}