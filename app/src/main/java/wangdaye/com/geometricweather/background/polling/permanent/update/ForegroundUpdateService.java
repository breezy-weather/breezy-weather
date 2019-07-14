package wangdaye.com.geometricweather.background.polling.permanent.update;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import android.os.Build;
import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.polling.basic.UpdateService;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.db.DatabaseHelper;

/**
 * Foreground update service.
 * */

public abstract class ForegroundUpdateService extends UpdateService {

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
            if (manager != null) {
                NotificationChannel channel = new NotificationChannel(
                        GeometricWeather.NOTIFICATION_CHANNEL_ID_BACKGROUND,
                        GeometricWeather.getNotificationChannelName(
                                this, GeometricWeather.NOTIFICATION_CHANNEL_ID_BACKGROUND),
                        NotificationManager.IMPORTANCE_MIN
                );
                channel.setShowBadge(false);
                channel.setLightColor(ContextCompat.getColor(this, R.color.colorPrimary));
                manager.createNotificationChannel(channel);
            }
            startForeground(
                    getForegroundNotificationId(),
                    getForegroundNotification(
                            1,
                            DatabaseHelper.getInstance(this).countLocation()
                    )
            );
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
    }

    public abstract Notification getForegroundNotification(int index, int total);

    public abstract int getForegroundNotificationId();

    @Override
    public void onUpdateCompleted(Location location, Weather weather, Weather old,
                                  boolean succeed, int index, int total) {
        super.onUpdateCompleted(location, weather, old, succeed, index, total);
        if (index + 1 != total) {
            NotificationManager manager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
            if (manager != null) {
                manager.notify(
                        getForegroundNotificationId(),
                        getForegroundNotification(index + 2, total)
                );
            }
        }
    }
}
