package wangdaye.com.geometricweather.remoteviews.presenter.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.basic.model.weather.WeatherCode;
import wangdaye.com.geometricweather.remoteviews.presenter.AbstractRemoteViewsPresenter;
import wangdaye.com.geometricweather.resource.ResourceHelper;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherViewController;
import wangdaye.com.geometricweather.utils.LanguageUtils;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

/**
 * Forecast notification utils.
 * */

public class ForecastNotificationIMP extends AbstractRemoteViewsPresenter {

    public static void buildForecastAndSendIt(Context context, Location location, boolean today) {
        Weather weather = location.getWeather();
        if (weather == null) {
            return;
        }

        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        LanguageUtils.setLanguage(
                context,
                SettingsOptionManager.getInstance(context).getLanguage().getLocale()
        );
        
        // create channel.
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    GeometricWeather.NOTIFICATION_CHANNEL_ID_FORECAST,
                    GeometricWeather.getNotificationChannelName(
                            context, GeometricWeather.NOTIFICATION_CHANNEL_ID_FORECAST),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(channel);
        }

        // get builder.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, GeometricWeather.NOTIFICATION_CHANNEL_ID_FORECAST);

        // set notification level.
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        // set notification visibility.
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        WeatherCode weatherCode;
        boolean daytime;
        if (today) {
            daytime = TimeManager.isDaylight(location);
            weatherCode = daytime 
                    ? weather.getDailyForecast().get(0).day().getWeatherCode() 
                    : weather.getDailyForecast().get(0).night().getWeatherCode();
        } else {
            daytime = true;
            weatherCode = weather.getDailyForecast().get(1).day().getWeatherCode() ;
        }

        // set small icon.
        builder.setSmallIcon(
                ResourceHelper.getDefaultMinimalXmlIconId(weatherCode, daytime));

        // large icon.
        builder.setLargeIcon(
                drawableToBitmap(
                        ResourceHelper.getWeatherIcon(provider, weatherCode, daytime)
                )
        );

        // sub text.
        if (today) {
            builder.setSubText(context.getString(R.string.today));
        } else {
            builder.setSubText(context.getString(R.string.tomorrow));
        }

        TemperatureUnit temperatureUnit = SettingsOptionManager.getInstance(context).getTemperatureUnit();

        // title and content.
        if (today) {
            builder.setContentTitle(context.getString(R.string.day)
                    + " " + weather.getDailyForecast().get(0).day().getWeatherText()
                    + " " + weather.getDailyForecast().get(0).day().getTemperature().getTemperature(temperatureUnit)
            ).setContentText(context.getString(R.string.night)
                    + " " + weather.getDailyForecast().get(0).night().getWeatherText()
                    + " " + weather.getDailyForecast().get(0).night().getTemperature().getTemperature(temperatureUnit)
            );
        } else {
            builder.setContentTitle(context.getString(R.string.day)
                    + " " + weather.getDailyForecast().get(1).day().getWeatherText()
                    + " " + weather.getDailyForecast().get(0).day().getTemperature().getTemperature(temperatureUnit)
            ).setContentText(context.getString(R.string.night)
                    + " " + weather.getDailyForecast().get(1).night().getWeatherText()
                    + " " + weather.getDailyForecast().get(1).night().getTemperature().getTemperature(temperatureUnit)
            );
        }

        builder.setColor(WeatherViewController.getThemeColors(context, weather, daytime)[0]);

        // set intent.
        builder.setContentIntent(
                getWeatherPendingIntent(
                        context,
                        null,
                        today
                                ? GeometricWeather.NOTIFICATION_ID_TODAY_FORECAST
                                : GeometricWeather.NOTIFICATION_ID_TOMORROW_FORECAST
                )
        );

        // set sound & vibrate.
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setAutoCancel(true);

        // set badge.
        builder.setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL);

        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
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
        manager.notify(
                today
                        ? GeometricWeather.NOTIFICATION_ID_TODAY_FORECAST
                        : GeometricWeather.NOTIFICATION_ID_TOMORROW_FORECAST,
                notification
        );
    }

    public static boolean isEnable(Context context, boolean today) {
        if (today) {
            return SettingsOptionManager.getInstance(context).isTodayForecastEnabled();
        } else {
            return SettingsOptionManager.getInstance(context).isTomorrowForecastEnabled();
        }
    }
}
