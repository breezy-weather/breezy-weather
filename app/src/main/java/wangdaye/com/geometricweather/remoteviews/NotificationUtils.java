package wangdaye.com.geometricweather.remoteviews;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.weather.Alert;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
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
/*
    private static final String PREFERENCE_PRECIPITATION_ALERT = "PRECIPITATION_ALERT_PREFERENCE";
    private static final String KEY_PRECIPITATION_LOCATION_KEY = "PRECIPITATION_LOCATION_KEY";
    private static final String KEY_PRECIPITATION_START_TIME = "PRECIPITATION_START_TIME";
    private static final String KEY_PRECIPITATION_END_TIME = "PRECIPITATION_END_TIME";
*/
    // notification.

    public static void updateNotificationIfNecessary(Context context, Location location) {
        if (NormalNotificationIMP.isEnable(context)) {
            NormalNotificationIMP.buildNotificationAndSendIt(context, location);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static NotificationChannel getAlertNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                GeometricWeather.NOTIFICATION_CHANNEL_ID_ALERT,
                GeometricWeather.getNotificationChannelName(
                        context, GeometricWeather.NOTIFICATION_CHANNEL_ID_ALERT),
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setShowBadge(true);
        channel.setLightColor(
                ContextCompat.getColor(
                        context,
                        TimeManager.getInstance(context).isDayTime()
                                ? R.color.lightPrimary_5
                                : R.color.darkPrimary_5
                )
        );
        return channel;
    }

    private static NotificationCompat.Builder getAlertNotificationBuilder(Context context, @DrawableRes int iconId,
                                                                          String subtitle, String content,
                                                                          Location location) {
        return new NotificationCompat.Builder(context, GeometricWeather.NOTIFICATION_CHANNEL_ID_ALERT)
                .setSmallIcon(iconId)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
                .setContentTitle(context.getString(R.string.action_alert))
                .setSubText(subtitle)
                .setContentText(content)
                .setColor(
                        ContextCompat.getColor(
                                context,
                                TimeManager.getInstance(context).isDayTime()
                                        ? R.color.lightPrimary_5
                                        : R.color.darkPrimary_5
                        )
                ).setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setContentIntent(getAlertNotificationPendingIntent(context, location));
    }

    private static PendingIntent getAlertNotificationPendingIntent(Context context, Location location) {
        return PendingIntent.getActivity(context, 0,
                IntentHelper.buildMainActivityIntent(location), 0);
    }

    // alert.

    public static void checkAndSendAlert(Context context,
                                         Location location, @Nullable Weather oldResult) {
        Weather weather = location.getWeather();
        if (weather == null
                || !SettingsOptionManager.getInstance(context).isAlertPushEnabled()) {
            return;
        }

        List<Alert> alertList = new ArrayList<>();
        if (oldResult != null) {
            for (int i = 0; i < weather.getAlertList().size(); i ++) {
                boolean newAlert = true;
                for (int j = 0; j < oldResult.getAlertList().size(); j ++) {
                    if (weather.getAlertList().get(i).getAlertId()
                            == oldResult.getAlertList().get(j).getAlertId()) {
                        newAlert = false;
                        break;
                    }
                }
                if (newAlert) {
                    alertList.add(weather.getAlertList().get(i));
                }
            }
        } else {
            alertList.addAll(weather.getAlertList());
        }

        for (int i = 0; i < alertList.size(); i ++) {
            sendAlertNotification(
                    context, location, alertList.get(i), alertList.size() > 1);
        }
    }

    private static void sendAlertNotification(Context context,
                                              Location location, Alert alert, boolean inGroup) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(getAlertNotificationChannel(context));
        }

        manager.notify(
                getAlertNotificationId(context),
                buildSingleAlertNotification(context, location, alert, inGroup)
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && inGroup) {
            manager.notify(
                    GeometricWeather.NOTIFICATION_ID_ALERT_GROUP,
                    buildAlertGroupSummaryNotification(context, location, alert)
            );
        }
    }

    private static Notification buildSingleAlertNotification(Context context, Location location,
                                                             Alert alert, boolean inGroup) {
        NotificationCompat.Builder builder = getAlertNotificationBuilder(
                context,
                R.drawable.ic_alert,
                DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.DEFAULT).format(alert.getDate()),
                alert.getContent(),
                location
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && inGroup) {
            builder.setGroup(NOTIFICATION_GROUP_KEY);
        }
        return builder.build();
    }

    private static Notification buildAlertGroupSummaryNotification(Context context,
                                                                   Location location, Alert alert) {
        return new NotificationCompat.Builder(context, GeometricWeather.NOTIFICATION_CHANNEL_ID_ALERT)
                .setSmallIcon(R.drawable.ic_alert)
                .setContentTitle(alert.getContent())
                .setGroup(NOTIFICATION_GROUP_KEY)
                .setColor(
                        ContextCompat.getColor(
                                context,
                                TimeManager.getInstance(context).isDayTime()
                                        ? R.color.lightPrimary_5
                                        : R.color.darkPrimary_5
                        )
                ).setGroupSummary(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(getAlertNotificationPendingIntent(context, location))
                .build();
    }

    private static int getAlertNotificationId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                PREFERENCE_NOTIFICATION, Context.MODE_PRIVATE);

        int id = sharedPreferences.getInt(
                KEY_NOTIFICATION_ID, GeometricWeather.NOTIFICATION_ID_ALERT_MIN) + 1;
        if (id > GeometricWeather.NOTIFICATION_ID_ALERT_MAX) {
            id = GeometricWeather.NOTIFICATION_ID_ALERT_MIN;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_NOTIFICATION_ID, id);
        editor.apply();

        return id;
    }

    // precipitation.

    public static void checkAndSendPrecipitationForecast(Context context,
                                                         Location location, @Nullable Weather oldResult) {
        // TODO: 2019/8/26 finish this !!!!
        /*
        if (!SettingsOptionManager.getInstance(context).isPrecipitationPushEnabled()) {
            return;
        }

        String precipitationAlert = getPrecipitationAlert(context, weather, oldResult);
        if (TextUtils.isEmpty(precipitationAlert)) {
            return;
        }

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(getAlertNotificationChannel(context));
        }

        manager.notify(
                GeometricWeather.NOTIFICATION_ID_PRECIPITATION,
                getAlertNotificationBuilder(
                        context,
                        R.drawable.ic_precipitation,
                        weather.base.time,
                        precipitationAlert,
                        location
                ).build()
        );*/
    }
/*
    @Nullable
    private static String getPrecipitationAlert(Context context,
                                                Weather weather, @Nullable Weather oldResult) {

        if (oldResult != null) {
            if (weather.base.timeStamp == oldResult.base.timeStamp) {
                return null;
            } else if (weather.base.timeStamp < oldResult.base.timeStamp) {
                oldResult = null;
            }
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                PREFERENCE_PRECIPITATION_ALERT, Context.MODE_PRIVATE);

        String lastPricipitationLocationKey = sharedPreferences.getString(
                KEY_PRECIPITATION_LOCATION_KEY, null);
        long lastPricipitationStartTime = sharedPreferences.getLong(
                KEY_PRECIPITATION_START_TIME, -1);
        long lastPricipitationEndTime = sharedPreferences.getLong(
                KEY_PRECIPITATION_END_TIME, -1);

        String currentLocationKey = weather.base.cityId;
        String current

        if (newPrecipitationDuration - oldPrecipitationDuration >= 6) {
            return context.getString(R.string.feedback_precipitation_alert_content)
                    .replace("$l$", weather.base.city)
                    .replace("$d$", String.valueOf(newPrecipitationDuration));
        }

        return null;
    }

    @Size(2)
    private static long[] getUpcomingPrecipitationPeriod(@Nullable Weather weather) {
        long[] precipitationPeriod = new long[] {-1, -1};
        if (weather == null) {
            return precipitationPeriod;
        }

        int interruption = 0;

        for (Hourly hourly : weather.hourlyList) {
            if (hourly.weatherKind.equals(Weather.KIND_RAIN)
                    || hourly.weatherKind.equals(Weather.KIND_SNOW)
                    || hourly.weatherKind.equals(Weather.KIND_SLEET)) {
                if (precipitationPeriod[0] == -1) {
                    precipitationPeriod[0] = getHourlyTimeStamp(weather, hourly);
                }
                precipitationPeriod[1] = getHourlyTimeStamp(weather, hourly);

                interruption = 0;
            } else if (precipitationPeriod[0] != -1) {
                interruption += 1;
                if (interruption >= 4) {

                }
            }
        }

        return duration;
    }

    private static long getHourlyTimeStamp(Weather weather, Hourly hourly) {

    }*/
}
