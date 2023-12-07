/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.background.forecast

import android.app.Notification
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import cancelNotification
import notificationBuilder
import notify
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.common.extensions.setLanguage
import org.breezyweather.common.extensions.toBitmap
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
        val daily = (if (today) weather.today else weather.tomorrow) ?: return

        val provider = ResourcesProviderFactory.newInstance
        context.setLanguage(SettingsManager.getInstance(context).language.locale)

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
                    ResourceHelper.getWeatherIcon(
                        provider,
                        it,
                        daytime
                    ).toBitmap()
                )
            }

            val remoteViews = getBigView(daily, temperatureUnit)
            setCustomBigContentView(remoteViews)

            setContentTitle(getDayString(daily, temperatureUnit))
            setContentText(getNightString(daily, temperatureUnit))
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

    private fun getBigView(daily: Daily, temperatureUnit: TemperatureUnit): RemoteViews {
        val view = RemoteViews(context.packageName, R.layout.notification_forecast)
        view.setTextViewText(
            R.id.notification_forecast_day, getDayString(daily, temperatureUnit)
        )
        view.setTextViewText(
            R.id.notification_forecast_night, getNightString(daily, temperatureUnit)
        )
        return view
    }

    private fun getDayString(daily: Daily, temperatureUnit: TemperatureUnit) =
        context.getString(R.string.daytime) +
                " " + daily.day?.weatherText +
                " " + daily.day?.temperature?.getTemperature(context, temperatureUnit, 0)

    private fun getNightString(daily: Daily, temperatureUnit: TemperatureUnit) =
        context.getString(R.string.nighttime) +
                " " + daily.night?.weatherText +
                " " + daily.night?.temperature?.getTemperature(context, temperatureUnit, 0)
}
