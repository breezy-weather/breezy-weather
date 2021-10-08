package wangdaye.com.geometricweather.location.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;

/**
 * Location service.
 * */

public abstract class LocationService {

    public static class Result {

        public float latitude;
        public float longitude;

        public Result(float lat, float lon) {
            latitude = lat;
            longitude = lon;
        }
    }

    public interface LocationCallback {
        void onCompleted(@Nullable Result result);
    }

    public abstract void requestLocation(Context context, @NonNull LocationCallback callback);

    public abstract void cancel();

    public boolean hasPermissions(Context context) {
        String[] permissions = getPermissions();
        for (String p : permissions) {
            if (p.equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                    || p.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                continue;
            }

            if (ActivityCompat.checkSelfPermission(
                    context,
                    p
            ) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    public abstract String[] getPermissions();

    @RequiresApi(api = Build.VERSION_CODES.O)
    NotificationChannel getLocationNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                GeometricWeather.NOTIFICATION_CHANNEL_ID_LOCATION,
                GeometricWeather.getNotificationChannelName(
                        context, GeometricWeather.NOTIFICATION_CHANNEL_ID_LOCATION),
                NotificationManager.IMPORTANCE_MIN);
        channel.setShowBadge(false);
        channel.setLightColor(ContextCompat.getColor(context, R.color.colorPrimary));
        return channel;
    }

    Notification getLocationNotification(Context context) {
        return new NotificationCompat.Builder(context, GeometricWeather.NOTIFICATION_CHANNEL_ID_LOCATION)
                .setSmallIcon(R.drawable.ic_location)
                .setContentTitle(context.getString(R.string.feedback_request_location))
                .setContentText(context.getString(R.string.feedback_request_location_in_background))
                .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setAutoCancel(true)
                .setProgress(0, 0, true)
                .build();
    }
}
