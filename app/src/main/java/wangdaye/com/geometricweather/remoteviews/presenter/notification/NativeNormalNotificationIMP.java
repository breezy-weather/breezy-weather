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
import androidx.core.app.NotificationManagerCompat;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.remoteviews.presenter.AbstractRemoteViewsPresenter;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherViewController;
import wangdaye.com.geometricweather.utils.LanguageUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * Forecast notification utils.
 * */

class NativeNormalNotificationIMP extends AbstractRemoteViewsPresenter {

    static void buildNotificationAndSendIt(Context context, @NonNull Weather weather,
                                           boolean daytime, boolean fahrenheit, boolean tempIcon,
                                           boolean hideNotificationIcon, boolean hideNotificationInLockScreen,
                                           boolean canBeCleared) {
        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        LanguageUtils.setLanguage(
                context,
                SettingsOptionManager.getInstance(context).getLanguage()
        );

        // create channel.
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
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
            channel.setImportance(hideNotificationIcon
                    ? NotificationManager.IMPORTANCE_UNSPECIFIED : NotificationManager.IMPORTANCE_HIGH);
            channel.setLockscreenVisibility(hideNotificationInLockScreen
                    ? NotificationCompat.VISIBILITY_SECRET : NotificationCompat.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(channel);
        }

        // get manager & builder.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, GeometricWeather.NOTIFICATION_CHANNEL_ID_NORMALLY);

        // set notification level.
        builder.setPriority(hideNotificationIcon
                ? NotificationCompat.PRIORITY_MIN : NotificationCompat.PRIORITY_MAX);

        // set notification visibility.
        builder.setVisibility(hideNotificationInLockScreen
                ? NotificationCompat.VISIBILITY_SECRET : NotificationCompat.VISIBILITY_PUBLIC);

        // set small icon.
        builder.setSmallIcon(tempIcon
                ? WeatherHelper.getTempIconId(
                        context,
                        fahrenheit
                                ? ValueUtils.calcFahrenheit(weather.realTime.temp)
                                : weather.realTime.temp
                ) : WeatherHelper.getDefaultMinimalXmlIconId(
                        weather.realTime.weatherKind,
                        daytime
                )
        );

        // large icon.
        builder.setLargeIcon(
                drawableToBitmap(
                        WeatherHelper.getWidgetNotificationIcon(
                                provider, weather.realTime.weatherKind, daytime, false, false)
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

        builder.setColor(WeatherViewController.getThemeColors(context, weather, daytime)[0]);

        // set clear flag
        builder.setOngoing(!canBeCleared);

        builder.setContentIntent(
                getWeatherPendingIntent(context, null, GeometricWeather.NOTIFICATION_ID_NORMALLY)
        );

        Notification notification = builder.build();
        if (!tempIcon && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                notification.getClass()
                        .getMethod("setSmallIcon", Icon.class)
                        .invoke(
                                notification,
                                WeatherHelper.getMinimalIcon(
                                        provider, weather.realTime.weatherKind, daytime)
                        );
            } catch (Exception ignore) {
                // do nothing.
            }
        }

        // commit.
        manager.notify(GeometricWeather.NOTIFICATION_ID_NORMALLY, notification);
    }
}
