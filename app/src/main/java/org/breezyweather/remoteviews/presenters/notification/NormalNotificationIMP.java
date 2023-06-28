package org.breezyweather.remoteviews.presenters.notification;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Date;
import java.util.List;

import org.breezyweather.BreezyWeather;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.options.NotificationStyle;
import org.breezyweather.common.basic.models.options.NotificationTextColor;
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit;
import org.breezyweather.common.basic.models.weather.Hourly;
import org.breezyweather.common.basic.models.weather.Temperature;
import org.breezyweather.common.basic.models.weather.Weather;
import org.breezyweather.theme.resource.ResourceHelper;
import org.breezyweather.theme.resource.ResourcesProviderFactory;
import org.breezyweather.theme.resource.providers.ResourceProvider;
import org.breezyweather.R;
import org.breezyweather.common.utils.LanguageUtils;
import org.breezyweather.common.utils.ObjectUtils;
import org.breezyweather.common.utils.helpers.LunarHelper;
import org.breezyweather.remoteviews.presenters.AbstractRemoteViewsPresenter;
import org.breezyweather.settings.SettingsManager;

public class NormalNotificationIMP extends AbstractRemoteViewsPresenter {

    public static void buildNotificationAndSendIt(
            Context context,
            @NonNull List<Location> locationList
    ) {
        Location location = locationList.get(0);
        Weather weather = location.getWeather();
        if (weather == null) {
            return;
        }

        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        LanguageUtils.setLanguage(
                context,
                SettingsManager.getInstance(context).getLanguage().getLocale()
        );

        // get sp & realTimeWeather.
        SettingsManager settings = SettingsManager.getInstance(context);

        TemperatureUnit temperatureUnit = settings.getTemperatureUnit();

        boolean dayTime = location.isDaylight();

        boolean tempIcon = settings.isWidgetNotificationTemperatureIconEnabled();

        boolean persistent = settings.isWidgetNotificationPersistent();

        if (settings.getWidgetNotificationStyle() == NotificationStyle.NATIVE) {
            NativeNormalNotificationIMP.buildNotificationAndSendIt(
                    context,
                    location,
                    temperatureUnit,
                    dayTime,
                    tempIcon,
                    persistent
            );
            return;
        } else if (settings.getWidgetNotificationStyle() == NotificationStyle.CITIES) {
            MultiCityNotificationIMP.buildNotificationAndSendIt(
                    context,
                    locationList,
                    temperatureUnit,
                    dayTime,
                    tempIcon,
                    persistent
            );
            return;
        }

        // build channel.
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    BreezyWeather.NOTIFICATION_CHANNEL_ID_NORMALLY,
                    BreezyWeather.getNotificationChannelName(
                            context,
                            BreezyWeather.NOTIFICATION_CHANNEL_ID_NORMALLY
                    ),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setShowBadge(false);
            channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(channel);
        }

        // get manager & builder.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context,
                BreezyWeather.NOTIFICATION_CHANNEL_ID_NORMALLY
        );

        // set notification level.
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        // set notification visibility.
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // set small icon.
        builder.setSmallIcon(
                tempIcon ? ResourceHelper.getTempIconId(
                        context,
                        temperatureUnit.getValueWithoutUnit(
                                settings.isWidgetNotificationUsingFeelsLike()
                                        ? ObjectUtils.safeValueOf(weather.getCurrent().getTemperature().getFeelsLikeTemperature())
                                        : weather.getCurrent().getTemperature().getTemperature()
                        )
                ) : ResourceHelper.getDefaultMinimalXmlIconId(
                        weather.getCurrent().getWeatherCode(),
                        dayTime
                )
        );

        // build base view.
        builder.setContent(
                buildBaseView(
                        context,
                        new RemoteViews(context.getPackageName(), R.layout.notification_base),
                        provider,
                        location,
                        temperatureUnit,
                        dayTime
                )
        );

        builder.setContentIntent(
                getWeatherPendingIntent(context, null, BreezyWeather.NOTIFICATION_ID_NORMALLY)
        );

        // build big view.
        builder.setCustomBigContentView(
                buildBigView(
                        context,
                        new RemoteViews(context.getPackageName(), R.layout.notification_big),
                        settings.getWidgetNotificationStyle() == NotificationStyle.DAILY,
                        provider,
                        location,
                        temperatureUnit,
                        dayTime
                )
        );

        // set clear flag
        builder.setOngoing(persistent);

        // set only alert once.
        builder.setOnlyAlertOnce(true);

        Notification notification = builder.build();
        if (!tempIcon && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                notification.getClass()
                        .getMethod("setSmallIcon", Icon.class)
                        .invoke(
                                notification,
                                ResourceHelper.getMinimalIcon(
                                        provider,
                                        weather.getCurrent().getWeatherCode(),
                                        dayTime
                                )
                        );
            } catch (Exception ignore) {
                // do nothing.
            }
        }

        // commit.
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED) {
            manager.notify(BreezyWeather.NOTIFICATION_ID_NORMALLY, notification);
        }
    }

    private static RemoteViews buildBaseView(Context context, RemoteViews views,
                                             ResourceProvider provider, Location location,
                                             TemperatureUnit temperatureUnit,
                                             boolean dayTime) {
        Weather weather = location.getWeather();
        if (weather == null) {
            return views;
        }

        SettingsManager settings = SettingsManager.getInstance(context);

        views.setImageViewUri(
                R.id.notification_base_icon,
                ResourceHelper.getWidgetNotificationIconUri(
                        provider,
                        weather.getCurrent().getWeatherCode(),
                        dayTime,
                        false,
                        NotificationTextColor.GREY
                )
        );

        views.setTextViewText(
                R.id.notification_base_realtimeTemp,
                Temperature.getShortTemperature(
                        context,
                        settings.isWidgetNotificationUsingFeelsLike()
                                ? ObjectUtils.safeValueOf(weather.getCurrent().getTemperature().getFeelsLikeTemperature())
                                : weather.getCurrent().getTemperature().getTemperature(),
                        temperatureUnit
                )
        );

        if (weather.getCurrent().getAirQuality().isValid()) {
            views.setTextViewText(
                    R.id.notification_base_aqiAndWind,
                    context.getString(R.string.air_quality)
                            + " - "
                            + weather.getCurrent().getAirQuality().getName(context, null)
            );
        } else {
            views.setTextViewText(
                    R.id.notification_base_aqiAndWind,
                    context.getString(R.string.wind)
                            + " - "
                            + weather.getCurrent().getWind().getLevel()
            );
        }

        views.setTextViewText(
                R.id.notification_base_weather,
                weather.getCurrent().getWeatherText()
        );

        StringBuilder timeStr = new StringBuilder();
        timeStr.append(location.getCityName(context));
        if (SettingsManager.getInstance(context).getLanguage().isChinese()) {
            timeStr.append(", ")
                    .append(LunarHelper.getLunarDate(new Date()));
        }
        views.setTextViewText(R.id.notification_base_time, timeStr.toString());

        return views;
    }

    private static RemoteViews buildBigView(Context context, RemoteViews views, boolean daily,
                                            ResourceProvider provider, Location location,
                                            TemperatureUnit temperatureUnit,
                                            boolean dayTime) {
        Weather weather = location.getWeather();
        if (weather == null) {
            return views;
        }

        // today
        views = buildBaseView(
                context, views, provider, location,
                temperatureUnit, dayTime
        );

        if (daily) {
            // weekly.
            boolean weekIconDaytime = isWeekIconDaytime(
                    SettingsManager.getInstance(context).getWidgetWeekIconMode(),
                    dayTime
            );
            // 1
            views.setTextViewText( // set week 1.
                    R.id.notification_big_week_1,
                    context.getString(R.string.short_today)
            );
            views.setTextViewText( // set temps 1.
                    R.id.notification_big_temp_1,
                    Temperature.getTrendTemperature(
                            context,
                            weather.getDailyForecast().get(0).getNight().getTemperature().getTemperature(),
                            weather.getDailyForecast().get(0).getDay().getTemperature().getTemperature(),
                            temperatureUnit
                    )
            );
            views.setImageViewUri( // set icon 1.
                    R.id.notification_big_icon_1,
                    ResourceHelper.getWidgetNotificationIconUri(
                            provider,
                            weekIconDaytime
                                    ? weather.getDailyForecast().get(0).getDay().getWeatherCode()
                                    : weather.getDailyForecast().get(0).getNight().getWeatherCode(),
                            weekIconDaytime,
                            false,
                            NotificationTextColor.GREY
                    )
            );
            // 2
            views.setTextViewText( // set week 2.
                    R.id.notification_big_week_2,
                    weather.getDailyForecast().get(1).getWeek(context, location.getTimeZone())
            );
            views.setTextViewText( // set temps 2.
                    R.id.notification_big_temp_2,
                    Temperature.getTrendTemperature(
                            context,
                            weather.getDailyForecast().get(1).getNight().getTemperature().getTemperature(),
                            weather.getDailyForecast().get(1).getDay().getTemperature().getTemperature(),
                            temperatureUnit
                    )
            );
            views.setImageViewUri( // set icon 2.
                    R.id.notification_big_icon_2,
                    ResourceHelper.getWidgetNotificationIconUri( // get icon 2 resource id.
                            provider,
                            weekIconDaytime
                                    ? weather.getDailyForecast().get(1).getDay().getWeatherCode()
                                    : weather.getDailyForecast().get(1).getNight().getWeatherCode(),
                            weekIconDaytime,
                            false,
                            NotificationTextColor.GREY
                    )
            );
            // 3
            views.setTextViewText( // set week 3.
                    R.id.notification_big_week_3,
                    weather.getDailyForecast().get(2).getWeek(context, location.getTimeZone())
            );
            views.setTextViewText( // set temps 3.
                    R.id.notification_big_temp_3,
                    Temperature.getTrendTemperature(
                            context,
                            weather.getDailyForecast().get(2).getNight().getTemperature().getTemperature(),
                            weather.getDailyForecast().get(2).getDay().getTemperature().getTemperature(),
                            temperatureUnit
                    )
            );
            views.setImageViewUri( // set icon 3.
                    R.id.notification_big_icon_3,
                    ResourceHelper.getWidgetNotificationIconUri( // get icon 3 resource id.
                            provider,
                            weekIconDaytime
                                    ? weather.getDailyForecast().get(2).getDay().getWeatherCode()
                                    : weather.getDailyForecast().get(2).getNight().getWeatherCode(),
                            weekIconDaytime,
                            false,
                            NotificationTextColor.GREY
                    )
            );
            // 4
            views.setTextViewText( // set week 4.
                    R.id.notification_big_week_4,
                    weather.getDailyForecast().get(3).getWeek(context, location.getTimeZone())
            );
            views.setTextViewText( // set temps 4.
                    R.id.notification_big_temp_4,
                    Temperature.getTrendTemperature(
                            context,
                            weather.getDailyForecast().get(3).getNight().getTemperature().getTemperature(),
                            weather.getDailyForecast().get(3).getDay().getTemperature().getTemperature(),
                            temperatureUnit
                    )
            );
            views.setImageViewUri( // set icon 4.
                    R.id.notification_big_icon_4,
                    ResourceHelper.getWidgetNotificationIconUri( // get icon 4 resource id.
                            provider,
                            weekIconDaytime
                                    ? weather.getDailyForecast().get(3).getDay().getWeatherCode()
                                    : weather.getDailyForecast().get(3).getNight().getWeatherCode(),
                            weekIconDaytime,
                            false,
                            NotificationTextColor.GREY
                    )
            );
            // 5
            views.setTextViewText( // set week 5.
                    R.id.notification_big_week_5,
                    weather.getDailyForecast().get(4).getWeek(context, location.getTimeZone())
            );
            views.setTextViewText( // set temps 5.
                    R.id.notification_big_temp_5,
                    Temperature.getTrendTemperature(
                            context,
                            weather.getDailyForecast().get(4).getNight().getTemperature().getTemperature(),
                            weather.getDailyForecast().get(4).getDay().getTemperature().getTemperature(),
                            temperatureUnit
                    )
            );
            views.setImageViewUri( // set icon 5.
                    R.id.notification_big_icon_5,
                    ResourceHelper.getWidgetNotificationIconUri( // get icon 5 resource id.
                            provider,
                            weekIconDaytime
                                    ? weather.getDailyForecast().get(4).getDay().getWeatherCode()
                                    : weather.getDailyForecast().get(4).getNight().getWeatherCode(),
                            weekIconDaytime,
                            false,
                            NotificationTextColor.GREY
                    )
            );
        } else {
            // 1
            Hourly hourly = weather.getHourlyForecast().get(0);
            views.setTextViewText( // set hour 1.
                    R.id.notification_big_week_1,
                    hourly.getHour(context, location.getTimeZone())
            );
            views.setTextViewText( // set temps 1.
                    R.id.notification_big_temp_1,
                    hourly.getTemperature().getShortTemperature(context, temperatureUnit)
            );
            views.setImageViewUri( // set icon 1.
                    R.id.notification_big_icon_1,
                    ResourceHelper.getWidgetNotificationIconUri(
                            provider,
                            hourly.getWeatherCode(),
                            hourly.isDaylight(),
                            false,
                            NotificationTextColor.GREY
                    )
            );
            // 2
            hourly = weather.getHourlyForecast().get(1);
            views.setTextViewText( // set hour 2.
                    R.id.notification_big_week_2,
                    hourly.getHour(context, location.getTimeZone())
            );
            views.setTextViewText( // set temps 2.
                    R.id.notification_big_temp_2,
                    hourly.getTemperature().getShortTemperature(context, temperatureUnit)
            );
            views.setImageViewUri( // set icon 2.
                    R.id.notification_big_icon_2,
                    ResourceHelper.getWidgetNotificationIconUri(
                            provider,
                            hourly.getWeatherCode(),
                            hourly.isDaylight(),
                            false,
                            NotificationTextColor.GREY
                    )
            );
            // 3
            hourly = weather.getHourlyForecast().get(2);
            views.setTextViewText( // set hour 3.
                    R.id.notification_big_week_3,
                    hourly.getHour(context, location.getTimeZone())
            );
            views.setTextViewText( // set temps 3.
                    R.id.notification_big_temp_3,
                    hourly.getTemperature().getShortTemperature(context, temperatureUnit)
            );
            views.setImageViewUri( // set icon 3.
                    R.id.notification_big_icon_3,
                    ResourceHelper.getWidgetNotificationIconUri(
                            provider,
                            hourly.getWeatherCode(),
                            hourly.isDaylight(),
                            false,
                            NotificationTextColor.GREY
                    )
            );
            // 4
            hourly = weather.getHourlyForecast().get(3);
            views.setTextViewText( // set hour 4.
                    R.id.notification_big_week_4,
                    hourly.getHour(context, location.getTimeZone())
            );
            views.setTextViewText( // set temps 4.
                    R.id.notification_big_temp_4,
                    hourly.getTemperature().getShortTemperature(context, temperatureUnit)
            );
            views.setImageViewUri( // set icon 4.
                    R.id.notification_big_icon_4,
                    ResourceHelper.getWidgetNotificationIconUri(
                            provider,
                            hourly.getWeatherCode(),
                            hourly.isDaylight(),
                            false,
                            NotificationTextColor.GREY
                    )
            );
            // 5
            hourly = weather.getHourlyForecast().get(4);
            views.setTextViewText( // set hour 5.
                    R.id.notification_big_week_5,
                    hourly.getHour(context, location.getTimeZone())
            );
            views.setTextViewText( // set temps 5.
                    R.id.notification_big_temp_5,
                    hourly.getTemperature().getShortTemperature(context, temperatureUnit)
            );
            views.setImageViewUri( // set icon 5.
                    R.id.notification_big_icon_5,
                    ResourceHelper.getWidgetNotificationIconUri(
                            provider,
                            hourly.getWeatherCode(),
                            hourly.isDaylight(),
                            false,
                            NotificationTextColor.GREY
                    )
            );
        }

        return views;
    }

    public static void cancelNotification(Context context) {
        NotificationManagerCompat.from(context).cancel(BreezyWeather.NOTIFICATION_ID_NORMALLY);
    }

    public static boolean isEnable(Context context) {
        return SettingsManager.getInstance(context).isWidgetNotificationEnabled();
    }
}
