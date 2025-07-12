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
import androidx.core.app.NotificationCompat
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.WeatherCode
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.extensions.cancelNotification
import org.breezyweather.common.extensions.notificationBuilder
import org.breezyweather.common.extensions.notify
import org.breezyweather.common.extensions.toBitmap
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.remoteviews.Notifications
import org.breezyweather.remoteviews.presenters.AbstractRemoteViewsPresenter
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory

class ForecastNotificationNotifier(
    private val context: Context,
) {

    private val progressNotificationBuilder = context
        .notificationBuilder(Notifications.CHANNEL_FORECAST) {
            setSmallIcon(R.drawable.ic_running_in_background)
            setAutoCancel(false)
            setOngoing(true)
            setOnlyAlertOnce(true)
        }

    private val completeNotificationBuilder = context
        .notificationBuilder(Notifications.CHANNEL_FORECAST) {
            setAutoCancel(false)
        }

    fun showProgress(): Notification {
        return progressNotificationBuilder
            // prevent Android from muting notifications ('muting recently noisy')
            // and only play a sound for the actual forecast notification
            .setSilent(true)
            .setContentTitle(context.getString(R.string.notification_running_in_background))
            .build()
    }

    fun showComplete(location: Location, today: Boolean) {
        context.cancelNotification(
            if (today) {
                Notifications.ID_UPDATING_TODAY_FORECAST
            } else {
                Notifications.ID_UPDATING_TOMORROW_FORECAST
            }
        )

        val weather = location.weather ?: return
        val daily = (if (today) weather.today else weather.tomorrow) ?: return

        val provider = ResourcesProviderFactory.newInstance

        val daytime: Boolean = if (today) location.isDaylight else true
        val weatherCode: WeatherCode? = if (today) {
            if (daytime) daily.day?.weatherCode else daily.night?.weatherCode
        } else {
            daily.day?.weatherCode
        }
        val temperatureUnit = SettingsManager.getInstance(context).getTemperatureUnit(context)

        val notification: Notification = with(completeNotificationBuilder) {
            priority = NotificationCompat.PRIORITY_MAX
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setSubText(
                if (today) {
                    context.getString(R.string.daily_today_short)
                } else {
                    context.getString(R.string.daily_tomorrow_short)
                }
            )
            setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
            setAutoCancel(true)
            setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            setSmallIcon(ResourceHelper.getDefaultMinimalXmlIconId(weatherCode, daytime))
            weatherCode?.let {
                setLargeIcon(ResourceHelper.getWeatherIcon(provider, it, daytime).toBitmap())
            }

            setContentTitle(getDayString(daily, temperatureUnit))
            setContentText(getNightString(daily, temperatureUnit))
            setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        getDayString(daily, temperatureUnit) + "\n\n" +
                            getNightString(daily, temperatureUnit)
                    )
                    // do not show any title when expanding the notification
                    .setBigContentTitle("")
            )
            setContentIntent(
                AbstractRemoteViewsPresenter.getWeatherPendingIntent(
                    context,
                    null,
                    if (today) {
                        Notifications.ID_TODAY_FORECAST
                    } else {
                        Notifications.ID_TOMORROW_FORECAST
                    }
                )
            )
        }.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            weather.current?.weatherCode != null
        ) {
            try {
                notification.javaClass
                    .getMethod("setSmallIcon", Icon::class.java)
                    .invoke(
                        notification,
                        ResourceHelper.getMinimalIcon(
                            provider,
                            weather.current!!.weatherCode!!,
                            daytime
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

    private fun getDayString(daily: Daily, temperatureUnit: TemperatureUnit) =
        context.getString(R.string.daytime) + context.getString(R.string.colon_separator) +
            daily.day?.temperature?.temperature?.let {
                temperatureUnit.formatMeasure(context, it, 0)
            } + context.getString(R.string.dot_separator) + daily.day?.weatherText

    private fun getNightString(daily: Daily, temperatureUnit: TemperatureUnit) =
        context.getString(R.string.nighttime) + context.getString(R.string.colon_separator) +
            daily.night?.temperature?.temperature?.let {
                temperatureUnit.formatMeasure(context, it, 0)
            } + context.getString(R.string.dot_separator) + daily.night?.weatherText
}
