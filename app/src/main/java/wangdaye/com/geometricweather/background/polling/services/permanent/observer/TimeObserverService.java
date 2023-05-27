package wangdaye.com.geometricweather.background.polling.services.permanent.observer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.polling.services.permanent.update.ForegroundNormalUpdateService;
import wangdaye.com.geometricweather.background.polling.services.permanent.update.ForegroundTodayForecastUpdateService;
import wangdaye.com.geometricweather.background.polling.services.permanent.update.ForegroundTomorrowForecastUpdateService;
import wangdaye.com.geometricweather.settings.SettingsManager;

/**
 * Time observer service.
 * */

public class TimeObserverService extends Service {

    private static TimeTickReceiver sReceiver;

    private static Float sPollingRate;
    private static long sLastUpdateNormalViewTime;
    private static long sLastTodayForecastTime;
    private static long sLastTomorrowForecastTime;
    private static String sTodayForecastTime;
    private static String sTomorrowForecastTime;

    public static final String KEY_CONFIG_CHANGED = "config_changed";
    public static final String KEY_POLLING_FAILED = "polling_failed";
    public static final String KEY_POLLING_RATE = "polling_rate";
    public static final String KEY_TODAY_FORECAST_TIME = "today_forecast_time";
    public static final String KEY_TOMORROW_FORECAST_TIME = "tomorrow_forecast_time";

    private class TimeTickReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            switch (action) {
                case Intent.ACTION_TIME_TICK:
                    doRefreshWork();
                    break;

                case Intent.ACTION_TIME_CHANGED:
                case Intent.ACTION_TIMEZONE_CHANGED:
                    sLastUpdateNormalViewTime = System.currentTimeMillis();
                    sLastTodayForecastTime = System.currentTimeMillis();
                    sLastTomorrowForecastTime = System.currentTimeMillis();
                    doRefreshWork();
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
        startForeground();
        initData();
        registerReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        readData(intent);
        doRefreshWork();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
        stopForeground(true);
    }

    private void initData() {
        sPollingRate = 1.5f;
        sTodayForecastTime = SettingsManager.DEFAULT_TODAY_FORECAST_TIME;
        sTomorrowForecastTime = SettingsManager.DEFAULT_TOMORROW_FORECAST_TIME;
        sLastUpdateNormalViewTime = System.currentTimeMillis();
        sLastTodayForecastTime = System.currentTimeMillis();
        sLastTomorrowForecastTime = System.currentTimeMillis();
    }

    private void readData(Intent intent) {
        if (intent != null) {
            if (intent.getBooleanExtra(KEY_CONFIG_CHANGED, false)) {
                sPollingRate = intent.getFloatExtra(KEY_POLLING_RATE, 1.5f);
                sLastTodayForecastTime = System.currentTimeMillis();
                sLastTomorrowForecastTime = System.currentTimeMillis();
                sTodayForecastTime = intent.getStringExtra(KEY_TODAY_FORECAST_TIME);
                sTomorrowForecastTime = intent.getStringExtra(KEY_TOMORROW_FORECAST_TIME);
            }
            if (intent.getBooleanExtra(KEY_POLLING_FAILED, false)) {
                if (getPollingInterval() != null) {
                    sLastUpdateNormalViewTime = System.currentTimeMillis() - getPollingInterval() + 15 * 60 * 1000;
                } else {
                    sLastUpdateNormalViewTime = 0;
                }
            }
        }
    }

    private void doRefreshWork() {
        if (getPollingInterval() != null && System.currentTimeMillis() - sLastUpdateNormalViewTime > getPollingInterval()) {

            sLastUpdateNormalViewTime = System.currentTimeMillis();

            Intent intent = new Intent(this, ForegroundNormalUpdateService.class);
            ContextCompat.startForegroundService(this, intent);
        }
        if (!TextUtils.isEmpty(sTodayForecastTime)
                && isForecastTime(sTodayForecastTime, sLastTodayForecastTime)) {

            sLastTodayForecastTime = System.currentTimeMillis();

            Intent intent = new Intent(this, ForegroundTodayForecastUpdateService.class);
            ContextCompat.startForegroundService(this, intent);
        }
        if (!TextUtils.isEmpty(sTomorrowForecastTime)
                && isForecastTime(sTomorrowForecastTime, sLastTomorrowForecastTime)) {

            sLastTomorrowForecastTime = System.currentTimeMillis();

            Intent intent = new Intent(this, ForegroundTomorrowForecastUpdateService.class);
            ContextCompat.startForegroundService(this, intent);
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        sReceiver = new TimeTickReceiver();
        registerReceiver(sReceiver, filter);
    }

    private void unregisterReceiver() {
        if (sReceiver != null) {
            unregisterReceiver(sReceiver);
            sReceiver = null;
        }
    }

    private void startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    GeometricWeather.NOTIFICATION_CHANNEL_ID_BACKGROUND,
                    GeometricWeather.getNotificationChannelName(
                            this, GeometricWeather.NOTIFICATION_CHANNEL_ID_BACKGROUND),
                    NotificationManager.IMPORTANCE_MIN
            );
            channel.setShowBadge(false);
            channel.setLightColor(ContextCompat.getColor(this, R.color.md_theme_primary));

            NotificationManagerCompat.from(this).createNotificationChannel(channel);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(
                    GeometricWeather.NOTIFICATION_ID_RUNNING_IN_BACKGROUND,
                    getForegroundNotification(this, true)
            );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            startForeground(
                    GeometricWeather.NOTIFICATION_ID_RUNNING_IN_BACKGROUND,
                    getForegroundNotification(this, false)
            );
            startService(new Intent(this, FakeForegroundService.class));
        } else {
            startForeground(
                    GeometricWeather.NOTIFICATION_ID_RUNNING_IN_BACKGROUND,
                    getForegroundNotification(this, true)
            );
            startService(new Intent(this, FakeForegroundService.class));
        }
    }

    public static Notification getForegroundNotification(Context context, boolean setIcon) {
        return new NotificationCompat.Builder(context, GeometricWeather.NOTIFICATION_CHANNEL_ID_BACKGROUND)
                .setSmallIcon(setIcon ? R.drawable.ic_running_in_background : 0)
                .setContentTitle(context.getString(R.string.geometric_weather))
                .setContentText(context.getString(R.string.feedback_running_in_background))
                .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setAutoCancel(true)
                .build();
    }

    private Long getPollingInterval() {
        return sPollingRate != null ? (long) (sPollingRate * 1000 * 60 * 60) : null;
    }

    private static boolean isForecastTime(String time, long lastForecastTime) {
        final long currentTime = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.split(":")[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(time.split(":")[1]));
        final long configTime = calendar.getTimeInMillis();

        return currentTime >= configTime && configTime > lastForecastTime;
    }
}