package wangdaye.com.geometricweather.remoteviews;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
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
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.weather.Alert;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper;
import wangdaye.com.geometricweather.remoteviews.presenters.notification.NormalNotificationIMP;
import wangdaye.com.geometricweather.settings.ConfigStore;
import wangdaye.com.geometricweather.settings.SettingsManager;

/**
 * Notification helper.
 * */

public class NotificationHelper {

    private static final String NOTIFICATION_GROUP_KEY = "geometric_weather_alert_notification_group";
    private static final String PREFERENCE_NOTIFICATION = "NOTIFICATION_PREFERENCE";
    private static final String KEY_NOTIFICATION_ID = "NOTIFICATION_ID";

    private static final String PREFERENCE_SHORT_TERM_PRECIPITATION_ALERT = "SHORT_TERM_PRECIPITATION_ALERT_PREFERENCE";
    private static final String KEY_PRECIPITATION_LOCATION_KEY = "PRECIPITATION_LOCATION_KEY";
    private static final String KEY_PRECIPITATION_DATE = "PRECIPITATION_DATE";

    // notification.

    public static void updateNotificationIfNecessary(Context context, @NonNull List<Location> locationList) {
        if (NormalNotificationIMP.isEnable(context)) {
            NormalNotificationIMP.buildNotificationAndSendIt(context, locationList);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static NotificationChannel getAlertNotificationChannel(Context context, @ColorInt int color) {
        NotificationChannel channel = new NotificationChannel(
                GeometricWeather.NOTIFICATION_CHANNEL_ID_ALERT,
                GeometricWeather.getNotificationChannelName(
                        context, GeometricWeather.NOTIFICATION_CHANNEL_ID_ALERT),
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setShowBadge(true);
        channel.setLightColor(color);
        return channel;
    }

    private static NotificationCompat.Builder getNotificationBuilder(Context context, @DrawableRes int iconId,
                                                                     String title, String subtitle, String content,
                                                                     @ColorInt int color, PendingIntent intent) {
        return new NotificationCompat.Builder(context, GeometricWeather.NOTIFICATION_CHANNEL_ID_ALERT)
                .setSmallIcon(iconId)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
                .setContentTitle(title)
                .setSubText(subtitle)
                .setContentText(content)
                .setColor(color)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setContentIntent(intent);
    }

    // alert.

    public static void checkAndSendAlert(Context context,
                                         Location location, @Nullable Weather oldResult) {
        Weather weather = location.getWeather();
        if (weather == null
                || !SettingsManager.getInstance(context).isAlertPushEnabled()) {
            return;
        }

        List<Alert> alertList = new ArrayList<>();
        if (oldResult != null) {
            if (weather.getBase().getTimeStamp() == oldResult.getBase().getTimeStamp()) {
                return;
            }

            boolean existingFlag;
            for (Alert newAlert : weather.getAlertList()) {
                existingFlag = false;

                for (Alert oldAlert : oldResult.getAlertList()) {
                    if (newAlert.getAlertId() == oldAlert.getAlertId()
                            || TextUtils.equals(newAlert.getDescription(), oldAlert.getDescription())) {
                        existingFlag = true;
                        break;
                    }
                }
                if (!existingFlag) {
                    alertList.add(newAlert);
                }
            }
        } else {
            // oldResult == null.
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
            manager.createNotificationChannel(
                    getAlertNotificationChannel(context, getColor(context, location))
            );
        }

        int notificationId = getAlertNotificationId(context);
        manager.notify(
                notificationId,
                buildSingleAlertNotification(context, location, alert, inGroup, notificationId)
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && inGroup) {
            manager.notify(
                    GeometricWeather.NOTIFICATION_ID_ALERT_GROUP,
                    buildAlertGroupSummaryNotification(context, location, alert, notificationId)
            );
        }
    }

    @SuppressLint("InlinedApi")
    private static Notification buildSingleAlertNotification(Context context,
                                                             Location location,
                                                             Alert alert,
                                                             boolean inGroup,
                                                             int notificationId) {
        String time = DateFormat.getDateTimeInstance(
                DateFormat.LONG, DateFormat.DEFAULT).format(alert.getDate());

        NotificationCompat.Builder builder = getNotificationBuilder(
                context,
                R.drawable.ic_alert,
                alert.getDescription(),
                time,
                alert.getContent(),
                getColor(context, location),
                PendingIntent.getActivity(
                        context,
                        notificationId,
                        IntentHelper.buildMainActivityShowAlertsIntent(location),
                        PendingIntent.FLAG_UPDATE_CURRENT
                        // PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                )
        ).setStyle(
                new NotificationCompat.BigTextStyle()
                        .setBigContentTitle(alert.getDescription())
                        .setSummaryText(time)
                        .bigText(alert.getContent())
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && inGroup) {
            builder.setGroup(NOTIFICATION_GROUP_KEY);
        }
        return builder.build();
    }

    @SuppressLint("InlinedApi")
    private static Notification buildAlertGroupSummaryNotification(Context context,
                                                                   Location location,
                                                                   Alert alert,
                                                                   int notificationId) {
        return new NotificationCompat.Builder(context, GeometricWeather.NOTIFICATION_CHANNEL_ID_ALERT)
                .setSmallIcon(R.drawable.ic_alert)
                .setContentTitle(alert.getDescription())
                .setGroup(NOTIFICATION_GROUP_KEY)
                .setColor(getColor(context, location))
                .setGroupSummary(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                notificationId,
                                IntentHelper.buildMainActivityShowAlertsIntent(location),
                                PendingIntent.FLAG_UPDATE_CURRENT
                                // PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                        )
                ).build();
    }

    private static int getAlertNotificationId(Context context) {
        ConfigStore config = ConfigStore.getInstance(context, PREFERENCE_NOTIFICATION);

        int id = config.getInt(
                KEY_NOTIFICATION_ID, GeometricWeather.NOTIFICATION_ID_ALERT_MIN) + 1;
        if (id > GeometricWeather.NOTIFICATION_ID_ALERT_MAX) {
            id = GeometricWeather.NOTIFICATION_ID_ALERT_MIN;
        }

        config.edit()
                .putInt(KEY_NOTIFICATION_ID, id)
                .apply();

        return id;
    }

    // precipitation.

    @SuppressLint("InlinedApi")
    public static void checkAndSendPrecipitationForecast(Context context,
                                                         Location location, @Nullable Weather oldResult) {
        if (!SettingsManager.getInstance(context).isPrecipitationPushEnabled()
                || location.getWeather() == null) {
            return;
        }
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                    getAlertNotificationChannel(context, getColor(context, location))
            );
        }

        Weather weather = location.getWeather();

        ConfigStore config = ConfigStore.getInstance(
                context, PREFERENCE_SHORT_TERM_PRECIPITATION_ALERT);
        String locationKey = config.getString(KEY_PRECIPITATION_LOCATION_KEY, null);
        long date = config.getLong(KEY_PRECIPITATION_DATE, 0);

        if ((!location.getFormattedId().equals(locationKey)
                || isDifferentDays(date, weather.getBase().getPublishTime()))
                && isShortTermLiquid(weather)) {
            manager.notify(
                    GeometricWeather.NOTIFICATION_ID_PRECIPITATION,
                    getNotificationBuilder(
                            context,
                            R.drawable.ic_precipitation,
                            context.getString(R.string.precipitation_overview),
                            weather.getDailyForecast().get(0).getDate(
                                    context.getString(R.string.date_format_widget_long)),
                            context.getString(R.string.feedback_short_term_precipitation_alert),
                            getColor(context, location),
                            PendingIntent.getActivity(
                                    context,
                                    GeometricWeather.NOTIFICATION_ID_PRECIPITATION,
                                    IntentHelper.buildMainActivityIntent(location),
                                    PendingIntent.FLAG_UPDATE_CURRENT
                                    // PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                            )
                    ).build()
            );
            config.edit()
                    .putString(KEY_PRECIPITATION_LOCATION_KEY, location.getFormattedId())
                    .putLong(KEY_PRECIPITATION_DATE, weather.getBase().getPublishTime())
                    .apply();
            return;
        }

        if ((oldResult == null
                || isDifferentDays(oldResult.getBase().getPublishTime(), weather.getBase().getPublishTime()))
                && isLiquidDay(weather)) {
            manager.notify(
                    GeometricWeather.NOTIFICATION_ID_PRECIPITATION,
                    getNotificationBuilder(
                            context,
                            R.drawable.ic_precipitation,
                            context.getString(R.string.precipitation_overview),
                            weather.getDailyForecast().get(0).getDate(
                                    context.getString(R.string.date_format_widget_long)),
                            context.getString(R.string.feedback_today_precipitation_alert),
                            getColor(context, location),
                            PendingIntent.getActivity(
                                    context,
                                    GeometricWeather.NOTIFICATION_ID_PRECIPITATION,
                                    IntentHelper.buildMainActivityIntent(location),
                                    PendingIntent.FLAG_UPDATE_CURRENT
                                    // PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                            )
                    ).build()
            );
        }
    }

    private static boolean isShortTermLiquid(Weather weather) {
        for (int i = 0; i < 4; i ++) {
            if (weather.getHourlyForecast().get(i).getWeatherCode().isPrecipitation()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDifferentDays(long time1, long time2) {
        long day1 = time1 / 1000 / 60 / 60 / 24;
        long day2 = time2 / 1000 / 60 / 60 / 24;
        return day1 != day2;
    }

    private static boolean isLiquidDay(Weather weather) {
        return weather.getDailyForecast().get(0).day().getWeatherCode().isPrecipitation()
                || weather.getDailyForecast().get(0).night().getWeatherCode().isPrecipitation();
    }

    @ColorInt
    private static int getColor(Context context, Location location) {
        return ContextCompat.getColor(context,
                location.isDaylight() ? R.color.lightPrimary_5 : R.color.darkPrimary_5);

    }
}
