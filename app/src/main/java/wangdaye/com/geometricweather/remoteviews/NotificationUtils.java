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

    private static final String PREFERENCE_SHORT_TERM_PRECIPITATION_ALERT = "SHORT_TERM_PRECIPITATION_ALERT_PREFERENCE";
    private static final String KEY_PRECIPITATION_LOCATION_KEY = "PRECIPITATION_LOCATION_KEY";
    private static final String KEY_PRECIPITATION_DATE = "PRECIPITATION_DATE";

    // notification.

    public static void updateNotificationIfNecessary(Context context, @NonNull Location location) {
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

    private static NotificationCompat.Builder getNotificationBuilder(Context context, @DrawableRes int iconId,
                                                                     String title, String subtitle, String content,
                                                                     Location location) {
        return new NotificationCompat.Builder(context, GeometricWeather.NOTIFICATION_CHANNEL_ID_ALERT)
                .setSmallIcon(iconId)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
                .setContentTitle(title)
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
        NotificationCompat.Builder builder = getNotificationBuilder(
                context,
                R.drawable.ic_alert,
                context.getString(R.string.action_alert),
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
        if (!SettingsOptionManager.getInstance(context).isPrecipitationPushEnabled()
                || location.getWeather() == null) {
            return;
        }
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(getAlertNotificationChannel(context));
        }

        Weather weather = location.getWeather();

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                PREFERENCE_SHORT_TERM_PRECIPITATION_ALERT, Context.MODE_PRIVATE);
        String locationKey = sharedPreferences.getString(KEY_PRECIPITATION_LOCATION_KEY, null);
        long date = sharedPreferences.getLong(KEY_PRECIPITATION_DATE, 0);

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
                            location
                    ).build()
            );
            sharedPreferences.edit()
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
                            location
                    ).build()
            );
        }
    }

    private static boolean isShortTermLiquid(Weather weather) {
        for (int i = 0; i < 4; i ++) {
            if (weather.getHourlyForecast().get(i).getWeatherCode().isPercipitation()) {
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
        return weather.getDailyForecast().get(0).day().getWeatherCode().isPercipitation()
                || weather.getDailyForecast().get(0).night().getWeatherCode().isPercipitation();
    }
}
