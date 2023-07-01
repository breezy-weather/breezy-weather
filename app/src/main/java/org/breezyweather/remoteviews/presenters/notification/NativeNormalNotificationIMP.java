package org.breezyweather.remoteviews.presenters.notification;

import android.Manifest;
import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Date;

import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit;
import org.breezyweather.common.basic.models.weather.Weather;
import org.breezyweather.remoteviews.Notifications;
import org.breezyweather.theme.resource.ResourceHelper;
import org.breezyweather.theme.resource.ResourcesProviderFactory;
import org.breezyweather.theme.resource.providers.ResourceProvider;
import org.breezyweather.R;
import org.breezyweather.common.utils.DisplayUtils;
import org.breezyweather.common.utils.LanguageUtils;
import org.breezyweather.common.utils.helpers.LunarHelper;
import org.breezyweather.remoteviews.presenters.AbstractRemoteViewsPresenter;
import org.breezyweather.settings.SettingsManager;

class NativeNormalNotificationIMP extends AbstractRemoteViewsPresenter {

    static void buildNotificationAndSendIt(
            Context context,
            Location location,
            TemperatureUnit temperatureUnit,
            boolean daytime,
            boolean tempIcon,
            boolean persistent
    ) {
        Weather weather = location.getWeather();
        if (weather == null) {
            return;
        }

        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        LanguageUtils.setLanguage(
                context,
                SettingsManager.getInstance(context).getLanguage().getLocale()
        );

        // create channel.
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);

        // get manager & builder.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context,
                Notifications.CHANNEL_WIDGET
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
                                SettingsManager.getInstance(context).isWidgetNotificationUsingFeelsLike() ?
                                weather.getCurrent().getTemperature().getFeelsLikeTemperature() :
                                weather.getCurrent().getTemperature().getTemperature()
                        )
                ) : ResourceHelper.getDefaultMinimalXmlIconId(
                        weather.getCurrent().getWeatherCode(),
                        daytime
                )
        );

        // large icon.
        builder.setLargeIcon(
                drawableToBitmap(
                        ResourceHelper.getWidgetNotificationIcon(
                                provider, weather.getCurrent().getWeatherCode(),
                                daytime, false, false
                        )
                )
        );

        StringBuilder subtitle = new StringBuilder();
        subtitle.append(location.getCityName(context));
        if (SettingsManager.getInstance(context).getLanguage().isChinese()) {
            subtitle.append(", ").append(LunarHelper.getLunarDate(new Date()));
        } else {
            subtitle.append(", ")
                    .append(context.getString(R.string.notification_refreshed_at))
                    .append(" ")
                    .append(DisplayUtils.getTime(context, weather.getBase().getUpdateDate(), location.getTimeZone()));
        }
        builder.setSubText(subtitle.toString());

        StringBuilder content = new StringBuilder();
        if (!tempIcon) {
            content.append(
                    SettingsManager.getInstance(context).isWidgetNotificationUsingFeelsLike() ?
                    weather.getCurrent().getTemperature().getFeelsLikeTemperature(context, temperatureUnit) :
                    weather.getCurrent().getTemperature().getTemperature(context, temperatureUnit))
                    .append(" ");
        }
        content.append(weather.getCurrent().getWeatherText());
        builder.setContentTitle(content.toString());

        StringBuilder contentText = new StringBuilder();
        if (weather.getCurrent().getAirQuality().isValid()) {
            contentText.append(context.getString(R.string.air_quality))
                    .append(" - ")
                    .append(weather.getCurrent().getAirQuality().getName(context, null));
        } else {
            contentText.append(context.getString(R.string.wind))
                    .append(" - ")
                    .append(weather.getCurrent().getWind().getLevel());
        }
        builder.setContentText(contentText.toString());

        // set clear flag
        builder.setOngoing(persistent);

        // set only alert once.
        builder.setOnlyAlertOnce(true);

        builder.setContentIntent(
                getWeatherPendingIntent(context, null, Notifications.ID_WIDGET)
        );

        Notification notification = builder.build();
        if (!tempIcon && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                notification.getClass()
                        .getMethod("setSmallIcon", Icon.class)
                        .invoke(
                                notification,
                                ResourceHelper.getMinimalIcon(
                                        provider, weather.getCurrent().getWeatherCode(), daytime)
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
            manager.notify(Notifications.ID_WIDGET, notification);
        }
    }
}
