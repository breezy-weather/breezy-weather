package org.breezyweather.background.forecast

import android.app.Notification
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.app.NotificationCompat
import cancelNotification
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

class ForecastNotificationNotifier(private val context: Context) {

    private val progressNotificationBuilder = context.notificationBuilder(Notifications.CHANNEL_FORECAST) {
        setSmallIcon(R.drawable.ic_running_in_background)
        setAutoCancel(false)
        setOngoing(true)
        setOnlyAlertOnce(true)
    }

    private val completeNotificationBuilder = context.notificationBuilder(Notifications.CHANNEL_FORECAST) {
        setAutoCancel(false)
    }

    private fun NotificationCompat.Builder.show(id: Int) {
        context.notify(id, build())
    }

    fun showProgress(today: Boolean): NotificationCompat.Builder {
        val builder = with(progressNotificationBuilder) {
            setContentTitle(context.getString(R.string.notification_running_in_background))

            setProgress(0, 0, true)
        }

        builder.show(if (today) Notifications.ID_UPDATING_TODAY_FORECAST else Notifications.ID_UPDATING_TOMORROW_FORECAST)

        return builder
    }

    fun showComplete(location: Location, today: Boolean) {
        context.cancelNotification(if (today) Notifications.ID_UPDATING_TODAY_FORECAST else Notifications.ID_UPDATING_TOMORROW_FORECAST)

        val weather = location.weather ?: return
        // TODO: Probably not safe if requested at 00:00 or 23:59, we should filter on date instead
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

        val notification: Notification = with(completeNotificationBuilder) {
            priority = NotificationCompat.PRIORITY_MAX
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setSubText(if (today) context.getString(R.string.short_today) else context.getString(R.string.short_tomorrow))
            setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
            setAutoCancel(true)
            setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            setSmallIcon(ResourceHelper.getDefaultMinimalXmlIconId(weatherCode, daytime))
            weatherCode?.let {
                setLargeIcon(
                    AbstractRemoteViewsPresenter.drawableToBitmap(
                        ResourceHelper.getWeatherIcon(
                            provider,
                            it,
                            daytime
                        )
                    )
                )
            }
            setContentTitle(
                context.getString(R.string.daytime)
                        + " " + daily.day?.weatherText
                        + " " + daily.day?.temperature?.getTemperature(context, temperatureUnit, 0)
            )
            setContentText(
                context.getString(R.string.nighttime)
                        + " " + daily.night?.weatherText
                        + " " + daily.night?.temperature?.getTemperature(context, temperatureUnit, 0)
            )
            setContentIntent(
                AbstractRemoteViewsPresenter.getWeatherPendingIntent(
                    context,
                    null,
                    if (today) Notifications.ID_TODAY_FORECAST else Notifications.ID_TOMORROW_FORECAST
                )
            )
        }.build()

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
}
