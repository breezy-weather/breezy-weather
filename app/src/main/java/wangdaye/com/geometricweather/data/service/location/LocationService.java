package wangdaye.com.geometricweather.data.service.location;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;

/**
 * Location service.
 * */

public abstract class LocationService {

    public class Result {
        public String district;
        public String city;
        public String province;
        public String country;
        public String latitude;
        public String longitude;
        public boolean inChina;
    }

    public abstract void requestLocation(Context context, @NonNull LocationCallback callback);

    public abstract void cancel();

    @RequiresApi(api = Build.VERSION_CODES.O)
    NotificationChannel getLocationNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                GeometricWeather.NOTIFICATION_CHANNEL_ID_LOCATION,
                GeometricWeather.getNotificationChannelName(context, GeometricWeather.NOTIFICATION_CHANNEL_ID_LOCATION),
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

    // interface.

    public interface LocationCallback {
        void onCompleted(@Nullable Result result);
    }
}
