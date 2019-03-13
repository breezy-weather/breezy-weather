package wangdaye.com.geometricweather.background.service.polling;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import android.os.Build;
import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.service.alarm.UpdateService;

/**
 * Polling update service.
 * */

public abstract class PollingUpdateService extends UpdateService {

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
            if (manager != null) {
                NotificationChannel channel = new NotificationChannel(
                        GeometricWeather.NOTIFICATION_CHANNEL_ID_BACKGROUND,
                        GeometricWeather.getNotificationChannelName(this, GeometricWeather.NOTIFICATION_CHANNEL_ID_BACKGROUND),
                        NotificationManager.IMPORTANCE_MIN);
                channel.setShowBadge(false);
                channel.setLightColor(ContextCompat.getColor(this, R.color.colorPrimary));
                manager.createNotificationChannel(channel);
            }
            startForeground(getForegroundNotificationId(), getForegroundNotification());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
    }

    public abstract Notification getForegroundNotification();

    public abstract int getForegroundNotificationId();
}
