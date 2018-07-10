package wangdaye.com.geometricweather.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Alert;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.manager.ThreadManager;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.remoteView.NormalNotificationUtils;

/**
 * Notification utils.
 * */

public class NotificationUtils {

    private static final String NOTIFICATION_GROUP_KEY = "geometric_weather_alert_notification_group";
    private static final String PREFERENCE_NOTIFICATION = "NOTIFICATION_PREFERENCE";
    private static final String KEY_NOTIFICATION_ID = "NOTIFICATION_ID";
    private static final int NOTIFICATION_GROUP_SUMMARY_ID = 10001;
    private static final String CHANNEL_ID_ALERT = "alert";

    public static void refreshNotificationInNewThread(final Context c, final Location location) {
        ThreadManager.getInstance()
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        if (NormalNotificationUtils.isEnable(c)) {
                            NormalNotificationUtils.buildNotificationAndSendIt(c, location.weather);
                        }
                    }
        });
    }

    public static void checkAndSendAlert(Context c, Weather weather, Weather oldResult) {
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
                    c, weather.base.city, alertList.get(i), alertList.size() > 1);
        }
    }

    private static void sendAlertNotification(Context c,
                                              String cityName, Alert alert, boolean inGroup) {
        NotificationManager manager = ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE));
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID_ALERT,
                        c.getString(R.string.app_name) + " " + c.getString(R.string.action_alert),
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.setShowBadge(true);
                channel.setLightColor(ContextCompat.getColor(
                        c,
                        TimeManager.getInstance(c).isDayTime() ? R.color.lightPrimary_5 : R.color.darkPrimary_5));
                manager.createNotificationChannel(channel);
            }
            manager.notify(
                    getNotificationId(c),
                    buildSingleNotification(c, cityName, alert, inGroup));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && inGroup) {
                manager.notify(NOTIFICATION_GROUP_SUMMARY_ID, buildGroupSummaryNotification(c, cityName, alert));
            }
        }
    }

    private static Notification buildSingleNotification(Context c,
                                                        String cityName, Alert alert, boolean inGroup) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(c, CHANNEL_ID_ALERT)
                .setSmallIcon(R.drawable.ic_alert)
                .setLargeIcon(BitmapFactory.decodeResource(c.getResources(), R.drawable.ic_launcher))
                .setContentTitle(c.getString(R.string.action_alert))
                .setSubText(alert.publishTime)
                .setContentText(alert.description)
                .setColor(ContextCompat.getColor(
                        c,
                        TimeManager.getInstance(c).isDayTime() ? R.color.lightPrimary_5 : R.color.darkPrimary_5))
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setContentIntent(buildIntent(c, cityName));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && inGroup) {
            builder.setGroup(NOTIFICATION_GROUP_KEY);
        }
        return builder.build();
    }

    private static Notification buildGroupSummaryNotification(Context c, String cityName, Alert alert) {
        return new NotificationCompat.Builder(c, CHANNEL_ID_ALERT)
                .setSmallIcon(R.drawable.ic_alert)
                .setContentTitle(alert.description)
                .setGroup(NOTIFICATION_GROUP_KEY)
                .setColor(ContextCompat.getColor(
                        c,
                        TimeManager.getInstance(c).isDayTime() ? R.color.lightPrimary_5 : R.color.darkPrimary_5))
                .setGroupSummary(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(buildIntent(c, cityName))
                .build();
    }

    private static int getNotificationId(Context c) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(
                PREFERENCE_NOTIFICATION,
                Context.MODE_PRIVATE);
        int id = sharedPreferences.getInt(KEY_NOTIFICATION_ID, 1000) + 1;
        if (id > NOTIFICATION_GROUP_SUMMARY_ID - 1) {
            id = 1001;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_NOTIFICATION_ID, id);
        editor.apply();

        return id;
    }

    private static PendingIntent buildIntent(Context c, String cityName) {
        return PendingIntent.getActivity(
                c, 0, IntentHelper.buildMainActivityIntent(cityName), 0);
    }
}
