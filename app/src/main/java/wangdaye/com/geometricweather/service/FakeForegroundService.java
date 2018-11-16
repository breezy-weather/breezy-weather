package wangdaye.com.geometricweather.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.service.polling.PollingService;

public class FakeForegroundService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationManager manager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
        if (manager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    GeometricWeather.NOTIFICATION_CHANNEL_ID_BACKGROUND,
                    GeometricWeather.getNotificationChannelName(this, GeometricWeather.NOTIFICATION_CHANNEL_ID_BACKGROUND),
                    NotificationManager.IMPORTANCE_LOW);
            channel.setShowBadge(false);
            channel.setLightColor(ContextCompat.getColor(this, R.color.colorPrimary));
            manager.createNotificationChannel(channel);
        }
        startForeground(
                GeometricWeather.NOTIFICATION_ID_RUNNING_IN_BACKGROUND,
                PollingService.getForecastNotification(this));

        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
