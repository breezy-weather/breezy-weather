package wangdaye.com.geometricweather.remoteviews;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Alert;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.remoteviews.presenter.notification.NormalNotificationIMP;

/**
 * Notification utils.
 * */

public class NotificationUtils {

    private static final String NOTIFICATION_GROUP_KEY = "geometric_weather_alert_notification_group";
    private static final String PREFERENCE_NOTIFICATION = "NOTIFICATION_PREFERENCE";
    private static final String KEY_NOTIFICATION_ID = "NOTIFICATION_ID";

    public static void refreshNotificationIfNecessary(Context c, @Nullable Weather weather) {
        if (NormalNotificationIMP.isEnable(c)) {
            NormalNotificationIMP.buildNotificationAndSendIt(c, weather);
        }
    }

    public static void checkAndSendAlert(Context c, Location location, Weather weather, @Nullable Weather oldResult) {
        if (!PreferenceManager.getDefaultSharedPreferences(c)
                .getBoolean(c.getString(R.string.key_alert_notification_switch), true)) {
            return;
        }

        List<Alert> alertList = new ArrayList<>();
        if (oldResult != null) {
            for (int i = 0; i < weather.alertList.size(); i ++) {
                boolean newAlert = true;
                for (int j = 0; j < oldResult.alertList.size(); j ++) {
                    if (weather.alertList.get(i).id == oldResult.alertList.get(j).id) {
                        newAlert = false;
                        break;
                    }
                }
                if (newAlert) {
                    alertList.add(weather.alertList.get(i));
                }
            }
        } else {
            alertList.addAll(weather.alertList);
        }

        for (int i = 0; i < alertList.size(); i ++) {
            sendAlertNotification(
                    c, location, alertList.get(i), alertList.size() > 1);
        }
    }

    private static void sendAlertNotification(Context c,
                                              Location location, Alert alert, boolean inGroup) {
        NotificationManager manager = ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE));
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        GeometricWeather.NOTIFICATION_CHANNEL_ID_ALERT,
                        GeometricWeather.getNotificationChannelName(c, GeometricWeather.NOTIFICATION_CHANNEL_ID_ALERT),
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.setShowBadge(true);
                channel.setLightColor(ContextCompat.getColor(
                        c,
                        TimeManager.getInstance(c).isDayTime() ? R.color.lightPrimary_5 : R.color.darkPrimary_5));
                manager.createNotificationChannel(channel);
            }
            manager.notify(
                    getNotificationId(c),
                    buildSingleNotification(c, location, alert, inGroup)
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && inGroup) {
                manager.notify(
                        GeometricWeather.NOTIFICATION_ID_ALERT_GROUP,
                        buildGroupSummaryNotification(c, location, alert)
                );
            }
        }
    }

    private static Notification buildSingleNotification(Context c,
                                                        Location location, Alert alert, boolean inGroup) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                c, GeometricWeather.NOTIFICATION_CHANNEL_ID_ALERT
        ).setSmallIcon(R.drawable.ic_alert)
                .setLargeIcon(BitmapFactory.decodeResource(c.getResources(), R.drawable.ic_launcher))
                .setContentTitle(c.getString(R.string.action_alert))
                .setSubText(alert.publishTime)
                .setContentText(alert.description)
                .setColor(ContextCompat.getColor(
                        c, TimeManager.getInstance(c).isDayTime()
                                ? R.color.lightPrimary_5
                                : R.color.darkPrimary_5
                )).setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setContentIntent(buildIntent(c, location));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && inGroup) {
            builder.setGroup(NOTIFICATION_GROUP_KEY);
        }
        return builder.build();
    }

    private static Notification buildGroupSummaryNotification(Context c, Location location, Alert alert) {
        return new NotificationCompat.Builder(c, GeometricWeather.NOTIFICATION_CHANNEL_ID_ALERT)
                .setSmallIcon(R.drawable.ic_alert)
                .setContentTitle(alert.description)
                .setGroup(NOTIFICATION_GROUP_KEY)
                .setColor(ContextCompat.getColor(
                        c, TimeManager.getInstance(c).isDayTime()
                                ? R.color.lightPrimary_5
                                : R.color.darkPrimary_5
                )).setGroupSummary(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(buildIntent(c, location))
                .build();
    }

    private static int getNotificationId(Context c) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(
                PREFERENCE_NOTIFICATION,
                Context.MODE_PRIVATE
        );
        int id = sharedPreferences.getInt(KEY_NOTIFICATION_ID, GeometricWeather.NOTIFICATION_ID_ALERT_MIN) + 1;
        if (id > GeometricWeather.NOTIFICATION_ID_ALERT_MAX) {
            id = GeometricWeather.NOTIFICATION_ID_ALERT_MIN;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_NOTIFICATION_ID, id);
        editor.apply();

        return id;
    }

    private static PendingIntent buildIntent(Context c, Location location) {
        return PendingIntent.getActivity(
                c, 0, IntentHelper.buildMainActivityIntent(location), 0);
    }
}
