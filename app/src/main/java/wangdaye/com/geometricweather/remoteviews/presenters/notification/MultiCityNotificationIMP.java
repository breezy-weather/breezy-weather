package wangdaye.com.geometricweather.remoteviews.presenters.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Date;
import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.NotificationTextColor;
import wangdaye.com.geometricweather.common.basic.models.options.unit.TemperatureUnit;
import wangdaye.com.geometricweather.common.basic.models.weather.Temperature;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.utils.LanguageUtils;
import wangdaye.com.geometricweather.common.utils.helpers.LunarHelper;
import wangdaye.com.geometricweather.remoteviews.presenters.AbstractRemoteViewsPresenter;
import wangdaye.com.geometricweather.theme.resource.ResourceHelper;
import wangdaye.com.geometricweather.theme.resource.ResourcesProviderFactory;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsManager;

public class MultiCityNotificationIMP extends AbstractRemoteViewsPresenter {

    public static void buildNotificationAndSendIt(
            Context context,
            @NonNull List<Location> locationList,
            TemperatureUnit temperatureUnit,
            boolean dayTime,
            boolean tempIcon,
            boolean canBeCleared
    ) {
        Weather weather = locationList.get(0).getWeather();
        if (weather == null) {
            return;
        }

        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        LanguageUtils.setLanguage(
                context,
                SettingsManager.getInstance(context).getLanguage().getLocale()
        );

        // build channel.
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    GeometricWeather.NOTIFICATION_CHANNEL_ID_NORMALLY,
                    GeometricWeather.getNotificationChannelName(
                            context,
                            GeometricWeather.NOTIFICATION_CHANNEL_ID_NORMALLY
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
                GeometricWeather.NOTIFICATION_CHANNEL_ID_NORMALLY
        );

        // set notification level.
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        // set notification visibility.
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // set small icon.
        builder.setSmallIcon(
                tempIcon ? ResourceHelper.getTempIconId(
                        context,
                        temperatureUnit.getTemperature(
                                weather.getCurrent().getTemperature().getTemperature()

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
                        locationList.get(0),
                        temperatureUnit,
                        dayTime
                )
        );

        builder.setContentIntent(
                getWeatherPendingIntent(context, null, GeometricWeather.NOTIFICATION_ID_NORMALLY)
        );

        // build big view.
        builder.setCustomBigContentView(
                buildBigView(
                        context,
                        new RemoteViews(context.getPackageName(), R.layout.notification_multi_city),
                        provider,
                        locationList,
                        temperatureUnit,
                        dayTime
                )
        );

        // set clear flag
        builder.setOngoing(!canBeCleared);

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
                                        provider, weather.getCurrent().getWeatherCode(), dayTime)
                        );
            } catch (Exception ignore) {
                // do nothing.
            }
        }

        // commit.
        manager.notify(GeometricWeather.NOTIFICATION_ID_NORMALLY, notification);
    }

    private static RemoteViews buildBaseView(
            Context context,
            RemoteViews views,
            ResourceProvider provider,
            Location location,
            TemperatureUnit temperatureUnit,
            boolean dayTime
    ) {
        Weather weather = location.getWeather();
        if (weather == null) {
            return views;
        }

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
                        weather.getCurrent().getTemperature().getTemperature(),
                        temperatureUnit
                )
        );

        if (weather.getCurrent().getAirQuality().isValid()) {
            views.setTextViewText(
                    R.id.notification_base_aqiAndWind,
                    context.getString(R.string.air_quality)
                            + " - "
                            + weather.getCurrent().getAirQuality().getAqiText()
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

    private static RemoteViews buildBigView(
            Context context,
            RemoteViews views,
            ResourceProvider provider,
            List<Location> locationList,
            TemperatureUnit temperatureUnit,
            boolean dayTime
    ) {
        Weather weather = locationList.get(0).getWeather();
        if (weather == null) {
            return views;
        }

        // today
        views = buildBaseView(
                context,
                views,
                provider,
                locationList.get(0),
                temperatureUnit,
                dayTime
        );

        // city 1.
        views.setViewVisibility(R.id.notification_multi_city_1, View.GONE);
        if (locationList.size() > 1 && locationList.get(1).getWeather() != null) {
            Location location = locationList.get(1);
            weather = location.getWeather();
            boolean cityDayTime = location.isDaylight();

            views.setViewVisibility(R.id.notification_multi_city_1, View.VISIBLE);

            assert weather != null;
            views.setImageViewUri( // set icon 1.
                    R.id.notification_multi_city_icon_1,
                    ResourceHelper.getWidgetNotificationIconUri(
                            provider,
                            cityDayTime
                                    ? weather.getDailyForecast().get(0).day().getWeatherCode()
                                    : weather.getDailyForecast().get(0).night().getWeatherCode(),
                            cityDayTime,
                            false,
                            NotificationTextColor.GREY
                    )
            );
            views.setTextViewText(
                    R.id.notification_multi_city_text_1,
                    getCityTitle(context, location, temperatureUnit)
            );
        }

        // city 2.
        views.setViewVisibility(R.id.notification_multi_city_2, View.GONE);
        if (locationList.size() > 2 && locationList.get(2).getWeather() != null) {
            Location location = locationList.get(2);
            weather = location.getWeather();
            boolean cityDayTime = location.isDaylight();

            views.setViewVisibility(R.id.notification_multi_city_2, View.VISIBLE);

            assert weather != null;
            views.setImageViewUri( // set icon 2.
                    R.id.notification_multi_city_icon_2,
                    ResourceHelper.getWidgetNotificationIconUri(
                            provider,
                            cityDayTime
                                    ? weather.getDailyForecast().get(0).day().getWeatherCode()
                                    : weather.getDailyForecast().get(0).night().getWeatherCode(),
                            cityDayTime,
                            false,
                            NotificationTextColor.GREY
                    )
            );
            views.setTextViewText(
                    R.id.notification_multi_city_text_2,
                    getCityTitle(context, location, temperatureUnit)
            );
        }

        // city 3.
        views.setViewVisibility(R.id.notification_multi_city_3, View.GONE);
        if (locationList.size() > 3 && locationList.get(3).getWeather() != null) {
            Location location = locationList.get(3);
            weather = location.getWeather();
            boolean cityDayTime = location.isDaylight();

            views.setViewVisibility(R.id.notification_multi_city_3, View.VISIBLE);

            assert weather != null;
            views.setImageViewUri( // set icon 3.
                    R.id.notification_multi_city_icon_3,
                    ResourceHelper.getWidgetNotificationIconUri(
                            provider,
                            cityDayTime
                                    ? weather.getDailyForecast().get(0).day().getWeatherCode()
                                    : weather.getDailyForecast().get(0).night().getWeatherCode(),
                            cityDayTime,
                            false,
                            NotificationTextColor.GREY
                    )
            );
            views.setTextViewText(
                    R.id.notification_multi_city_text_3,
                    getCityTitle(context, location, temperatureUnit)
            );
        }

        return views;
    }

    private static String getCityTitle(Context context, Location location, TemperatureUnit unit) {
        StringBuilder builder = new StringBuilder(location.isCurrentPosition()
                ? context.getString(R.string.current_location)
                : location.getCityName(context));
        if (location.getWeather() != null) {
            builder.append(", ").append(
                    Temperature.getTrendTemperature(
                            context,
                            location.getWeather().getDailyForecast().get(0).night().getTemperature().getTemperature(),
                            location.getWeather().getDailyForecast().get(0).day().getTemperature().getTemperature(),
                            unit
                    )
            );
        }
        return builder.toString();
    }

    public static boolean isEnable(Context context) {
        return SettingsManager.getInstance(context).isNotificationEnabled();
    }
}
