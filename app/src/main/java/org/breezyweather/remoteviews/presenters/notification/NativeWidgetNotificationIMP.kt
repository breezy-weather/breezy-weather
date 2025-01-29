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

package org.breezyweather.remoteviews.presenters.notification

import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.appearance.CalendarHelper
import org.breezyweather.common.extensions.getFormattedMediumDayAndMonthInAdditionalCalendar
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.notificationBuilder
import org.breezyweather.common.extensions.notify
import org.breezyweather.common.extensions.toBitmap
import org.breezyweather.domain.location.model.getPlace
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.getName
import org.breezyweather.domain.weather.model.getStrength
import org.breezyweather.domain.weather.model.isIndexValid
import org.breezyweather.remoteviews.Notifications
import org.breezyweather.remoteviews.presenters.AbstractRemoteViewsPresenter
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory
import java.util.Date

object NativeWidgetNotificationIMP : AbstractRemoteViewsPresenter() {
    fun buildNotificationAndSendIt(
        context: Context,
        location: Location,
        daytime: Boolean,
        tempIcon: Boolean,
        persistent: Boolean,
    ) {
        val current = location.weather?.current ?: return
        val provider = ResourcesProviderFactory.newInstance

        val tempFeelsLikeOrAir = if (SettingsManager.getInstance(context).isWidgetNotificationUsingFeelsLike) {
            current.temperature?.feelsLikeTemperature ?: current.temperature?.temperature
        } else {
            current.temperature?.temperature
        }
        val temperature = if (tempIcon) tempFeelsLikeOrAir else null

        val subtitle = StringBuilder()
        subtitle.append(location.getPlace(context))
        if (CalendarHelper.getAlternateCalendarSetting(context) != null) {
            subtitle.append(context.getString(R.string.comma_separator))
                .append(Date().getFormattedMediumDayAndMonthInAdditionalCalendar(location, context))
        } else {
            location.weather!!.base.refreshTime?.let {
                subtitle.append(context.getString(R.string.comma_separator))
                    .append(context.getString(R.string.notification_refreshed_at))
                    .append(" ")
                    .append(it.getFormattedTime(location, context, context.is12Hour))
            }
        }

        val contentTitle = StringBuilder()
        if (!tempIcon && tempFeelsLikeOrAir != null) {
            val temperatureUnit = SettingsManager.getInstance(context).temperatureUnit
            contentTitle.append(temperatureUnit.getValueText(context, tempFeelsLikeOrAir))
        }
        if (!current.weatherText.isNullOrEmpty()) {
            if (contentTitle.toString().isNotEmpty()) contentTitle.append(" - ")
            contentTitle.append(current.weatherText)
        }

        val notification = context.notificationBuilder(Notifications.CHANNEL_WIDGET).apply {
            priority = NotificationCompat.PRIORITY_MAX
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            if (temperature != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setSmallIcon(
                    IconCompat.createWithBitmap(
                        ResourceHelper.createTempBitmap(context, temperature)
                    )
                )
            } else {
                setSmallIcon(
                    ResourceHelper.getDefaultMinimalXmlIconId(current.weatherCode, daytime)
                )
            }
            current.weatherCode?.let { weatherCode ->
                setLargeIcon(
                    ResourceHelper.getWidgetNotificationIcon(
                        provider,
                        weatherCode,
                        daytime,
                        minimal = false,
                        darkText = false
                    ).toBitmap()
                )
            }
            setSubText(subtitle.toString())
            setContentTitle(contentTitle.toString())
            if (current.airQuality?.isIndexValid == true) {
                setContentText(context.getString(R.string.air_quality) + " - " + current.airQuality!!.getName(context))
            } else {
                current.wind?.getStrength(context)?.let { strength ->
                    setContentText(context.getString(R.string.wind) + " - " + strength)
                }
            }
            setOngoing(persistent)
            setOnlyAlertOnce(true)
            setContentIntent(getWeatherPendingIntent(context, null, Notifications.ID_WIDGET))
        }.build()

        if (!tempIcon && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            current.weatherCode?.let { weatherCode ->
                try {
                    notification.javaClass
                        .getMethod("setSmallIcon", Icon::class.java)
                        .invoke(
                            notification,
                            ResourceHelper.getMinimalIcon(provider, weatherCode, daytime)
                        )
                } catch (ignore: Exception) {
                    // do nothing.
                }
            }
        }

        context.notify(Notifications.ID_WIDGET, notification)
    }
}
