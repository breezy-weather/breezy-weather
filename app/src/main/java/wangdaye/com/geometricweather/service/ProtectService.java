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

    private static boolean sPower;
    private static boolean running;
    private static boolean working;
    private static boolean backgroundFree;

    public static final String KEY_IS_REFRESH = "is_refresh";
    public static final String KEY_WORKING = "working";
    public static final String KEY_BACKGROUND_FREE = "background_free";

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
        if (!running && (!working || backgroundFree)) {
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
        sPower = true;
        running = false;
        working = true;
        backgroundFree = false;
    }

    private void readData(Intent intent) {
        if (intent != null && intent.getBooleanExtra(KEY_IS_REFRESH, false)) {
            working = intent.getBooleanExtra(KEY_WORKING, true);
            backgroundFree = intent.getBooleanExtra(KEY_BACKGROUND_FREE, false);
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
                        SystemClock.sleep(1500);
                        startService(new Intent(ProtectService.this, PollingService.class));
                    }
                    if (!working || backgroundFree) {
                        stopSelf();
                    }
                }
            }).start();
        }
    }
}
