package org.breezyweather.remoteviews.presenters.notification

import android.app.Notification
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.app.NotificationCompat
import notificationBuilder
import notify
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.common.utils.LanguageUtils
import org.breezyweather.remoteviews.Notifications
import org.breezyweather.remoteviews.presenters.AbstractRemoteViewsPresenter
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.ResourcesProviderFactory

/**
 * Forecast notification utils.
 */
object ForecastNotificationIMP : AbstractRemoteViewsPresenter() {

    fun buildForecastAndSendIt(context: Context, location: Location, today: Boolean) {
        val weather = location.weather ?: return
        val daily = weather.dailyForecast.getOrNull(if (today) 0 else 1) ?: return

        val provider = ResourcesProviderFactory.newInstance
        LanguageUtils.setLanguage(context, SettingsManager.getInstance(context).language.locale)

        val daytime: Boolean = if (today) location.isDaylight else true
        val weatherCode: WeatherCode? = if (today) {
            if (daytime) daily.day?.weatherCode else daily.night?.weatherCode
        } else {
            daily.day?.weatherCode
        }
        val temperatureUnit = SettingsManager.getInstance(context).temperatureUnit

        val notification: Notification = context.notificationBuilder(Notifications.CHANNEL_FORECAST).apply {
            priority = NotificationCompat.PRIORITY_MAX
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setSubText(if (today) context.getString(R.string.short_today) else context.getString(R.string.short_tomorrow))
            setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
            setAutoCancel(true)
            setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            setSmallIcon(ResourceHelper.getDefaultMinimalXmlIconId(weatherCode, daytime))
            weatherCode?.let {
                setLargeIcon(drawableToBitmap(ResourceHelper.getWeatherIcon(provider, it, daytime)))
            }
            setContentTitle(
                context.getString(R.string.daytime)
                        + " " + daily.day?.weatherText
                        + " " + daily.day?.temperature?.getTemperature(context, temperatureUnit)
            )
            setContentText(
                context.getString(R.string.nighttime)
                        + " " + daily.night?.weatherText
                        + " " + daily.night?.temperature?.getTemperature(context, temperatureUnit)
            )
            setContentIntent(
                getWeatherPendingIntent(
                    context,
                    null,
                    if (today) Notifications.ID_TODAY_FORECAST else Notifications.ID_TOMORROW_FORECAST
                )
            )
        }.build()

        // TODO: Why?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && weather.current?.weatherCode != null) {
            try {
                notification.javaClass
                    .getMethod("setSmallIcon", Icon::class.java)
                    .invoke(
                        notification,
                        ResourceHelper.getMinimalIcon(
                            provider, weather.current.weatherCode, daytime
                        )
                    )
            } catch (ignore: Exception) {
                // do nothing.
            }
        }

        context.notify(
            if (today) Notifications.ID_TODAY_FORECAST else Notifications.ID_TOMORROW_FORECAST,
            notification
        )
    }

    fun isEnabled(context: Context, today: Boolean): Boolean {
        return if (today) {
            SettingsManager.getInstance(context).isTodayForecastEnabled
        } else {
            SettingsManager.getInstance(context).isTomorrowForecastEnabled
        }
    }
}
