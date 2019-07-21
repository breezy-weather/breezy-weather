package wangdaye.com.geometricweather.remoteviews.presenter.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.remoteviews.presenter.AbstractRemoteViewsPresenter;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.LanguageUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * Forecast notification utils.
 * */

class NativeNormalNotificationIMP extends AbstractRemoteViewsPresenter {

    static void buildNotificationAndSendIt(Context context, @NonNull Weather weather,
                                           boolean dayTime, boolean fahrenheit, boolean tempIcon,
                                           boolean hideNotificationIcon, boolean hideNotificationInLockScreen,
                                           boolean canBeCleared) {
        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        LanguageUtils.setLanguage(
                context,
                SettingsOptionManager.getInstance(context).getLanguage()
        );

        // create channel.
        NotificationManager manager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
        if (manager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    GeometricWeather.NOTIFICATION_CHANNEL_ID_NORMALLY,
                    GeometricWeather.getNotificationChannelName(
                            context,
                            GeometricWeather.NOTIFICATION_CHANNEL_ID_NORMALLY
                    ),
                    hideNotificationIcon
                            ? NotificationManager.IMPORTANCE_MIN
                            : NotificationManager.IMPORTANCE_LOW
            );
            channel.setShowBadge(false);
            manager.createNotificationChannel(channel);
        }

        // get manager & builder.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, GeometricWeather.NOTIFICATION_CHANNEL_ID_NORMALLY);

        // set notification level.
        if (hideNotificationIcon) {
            builder.setPriority(NotificationCompat.PRIORITY_MIN);
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_MAX);
        }

        // set notification visibility.
        if (hideNotificationInLockScreen) {
            builder.setVisibility(NotificationCompat.VISIBILITY_SECRET);
        } else {
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        }

        // set small icon.
        builder.setSmallIcon(tempIcon
                ? WeatherHelper.getTempIconId(
                        context,
                        fahrenheit
                                ? ValueUtils.calcFahrenheit(weather.realTime.temp)
                                : weather.realTime.temp
                ) : WeatherHelper.getDefaultMinimalXmlIconId(
                        weather.realTime.weatherKind,
                        dayTime
                )
        );

        // large icon.
        builder.setLargeIcon(
                drawableToBitmap(
                        WeatherHelper.getWidgetNotificationIcon(
                                provider, weather.realTime.weatherKind, dayTime, false, false)
                )
        );

        builder.setSubText(weather.base.city + " " + weather.base.time);
        builder.setContentTitle(
                ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit) + " "
                        + weather.realTime.weather
        );

        StringBuilder contentText = new StringBuilder();
        contentText.append(context.getString(R.string.feels_like))
                .append(" ")
                .append(ValueUtils.buildAbbreviatedCurrentTemp(weather.realTime.sensibleTemp, fahrenheit));
        if (weather.aqi == null) {
            contentText.append(", ").append(weather.realTime.windLevel);
        } else if (!TextUtils.isEmpty(weather.aqi.quality)) {
            contentText.append(", ").append(weather.aqi.quality);
        }
        builder.setContentText(contentText.toString());

        builder.setColor(getWeatherColors(context, weather, dayTime, provider)[0]);

        // set clear flag
        if (canBeCleared) {
            // the notification can be cleared
            builder.setAutoCancel(true);
        } else {
            // the notification can not be cleared
            builder.setOngoing(true);
        }

        builder.setContentIntent(
                getWeatherPendingIntent(context, null, GeometricWeather.NOTIFICATION_ID_NORMALLY)
        );

        Notification notification = builder.build();
        if (!tempIcon && Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                notification.getClass()
                        .getMethod("setSmallIcon", Icon.class)
                        .invoke(
                                notification,
                                WeatherHelper.getMinimalIcon(
                                        provider, weather.realTime.weatherKind, dayTime)
                        );
            } catch (Exception ignore) {
                // do nothing.
            }
        }

        // commit.
        manager.notify(GeometricWeather.NOTIFICATION_ID_NORMALLY, notification);
    }
}
