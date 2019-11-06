package wangdaye.com.geometricweather.background.polling.basic;

import android.app.NotificationChannel;
import android.app.NotificationManager;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.db.DatabaseHelper;

/**
 * Foreground update service.
 * */

public abstract class ForegroundUpdateService extends UpdateService {

    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    GeometricWeather.NOTIFICATION_CHANNEL_ID_BACKGROUND,
                    GeometricWeather.getNotificationChannelName(
                            this, GeometricWeather.NOTIFICATION_CHANNEL_ID_BACKGROUND),
                    NotificationManager.IMPORTANCE_MIN
            );
            channel.setShowBadge(false);
            channel.setLightColor(ContextCompat.getColor(this, R.color.colorPrimary));

            NotificationManagerCompat.from(this).createNotificationChannel(channel);
            startForeground(
                    getForegroundNotificationId(),
                    getForegroundNotification(
                            1,
                            DatabaseHelper.getInstance(this).countLocation()
                    ).build()
            );
        }

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
            NotificationManagerCompat.from(this).cancel(getForegroundNotificationId());
        }
    }

    @Override
    public void stopService(boolean updateFailed) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
            NotificationManagerCompat.from(this).cancel(getForegroundNotificationId());
        }
        super.stopService(updateFailed);
    }

    public NotificationCompat.Builder getForegroundNotification(int index, int total) {
        return new NotificationCompat.Builder(this, GeometricWeather.NOTIFICATION_CHANNEL_ID_BACKGROUND)
                .setSmallIcon(R.drawable.ic_running_in_background)
                .setContentTitle(getString(R.string.geometric_weather))
                .setContentText(getString(R.string.feedback_updating_weather_data) + " (" + index + "/" + total + ")")
                .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setProgress(0, 0, true)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setAutoCancel(false)
                .setOngoing(false);
    }

    public abstract int getForegroundNotificationId();

    @Override
    public void onUpdateCompleted(@NonNull Location location, @Nullable Weather old,
                                  boolean succeed, int index, int total) {
        super.onUpdateCompleted(location, old, succeed, index, total);
        if (index + 1 != total) {
            NotificationManagerCompat.from(this).notify(
                    getForegroundNotificationId(),
                    getForegroundNotification(index + 2, total).build()
            );
        }
    }
}
