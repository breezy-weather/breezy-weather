package wangdaye.com.geometricweather.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;

/**
 * Protect service.
 * */

public class ProtectService extends Service {

    private static boolean working;
    private static boolean threadRunning;

    public static final String KEY_IS_REFRESH = "is_refresh";
    public static final String KEY_WORKING = "working";

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
        if (!threadRunning && !working) {
            stopSelf();
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initData() {
        working = true;
        threadRunning = false;
    }

    private void readData(Intent intent) {
        if (intent != null && intent.getBooleanExtra(KEY_IS_REFRESH, false)) {
            working = intent.getBooleanExtra(KEY_WORKING, true);
        }
    }

    private void doProtectionWork() {
        if (!threadRunning && working) {
            threadRunning = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (working) {
                        startService(new Intent(ProtectService.this, PollingService.class));
                        SystemClock.sleep(1500);
                    }
                    if (!working) {
                        stopSelf();
                    }
                }
            }).start();
        }
    }
}
