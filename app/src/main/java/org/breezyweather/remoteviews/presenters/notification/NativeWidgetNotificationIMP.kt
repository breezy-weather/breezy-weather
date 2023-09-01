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
import notificationBuilder
import notify
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.setLanguage
import org.breezyweather.common.extensions.toBitmap
import org.breezyweather.common.utils.helpers.LunarHelper
import org.breezyweather.remoteviews.Notifications
import org.breezyweather.remoteviews.presenters.AbstractRemoteViewsPresenter
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.ResourcesProviderFactory
import java.util.*
import kotlin.math.roundToInt

object NativeWidgetNotificationIMP : AbstractRemoteViewsPresenter() {
    fun buildNotificationAndSendIt(
        context: Context,
        location: Location,
        temperatureUnit: TemperatureUnit,
        daytime: Boolean,
        tempIcon: Boolean,
        persistent: Boolean
    ) {
        val current = location.weather?.current ?: return
        val provider = ResourcesProviderFactory.newInstance
        context.setLanguage(SettingsManager.getInstance(context).language.locale)

        val tempFeelsLikeOrAir = if (SettingsManager.getInstance(context).isWidgetNotificationUsingFeelsLike) {
            current.temperature?.feelsLikeTemperature ?: current.temperature?.temperature
        } else current.temperature?.temperature
        val temperature = if (tempIcon) tempFeelsLikeOrAir else null

        val subtitle = StringBuilder()
        subtitle.append(location.getPlace(context))
        if (SettingsManager.getInstance(context).language.isChinese) {
            subtitle.append(", ").append(LunarHelper.getLunarDate(Date()))
        } else if (location.weather.base.refreshTime != null) {
            subtitle.append(", ")
                .append(context.getString(R.string.notification_refreshed_at))
                .append(" ")
                .append(location.weather.base.refreshTime.getFormattedTime(location.timeZone, context.is12Hour))
        }

        val contentTitle = StringBuilder()
        if (!tempIcon) {
            contentTitle.append(tempFeelsLikeOrAir)
        }
        if (!current.weatherText.isNullOrEmpty()) {
            if (contentTitle.toString().isNotEmpty()) contentTitle.append(" ")
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
            if (current.weatherCode != null) setLargeIcon(
                ResourceHelper.getWidgetNotificationIcon(
                    provider, current.weatherCode,
                    daytime, false, false
                ).toBitmap()
            )
            setSubText(subtitle.toString())
            setContentTitle(contentTitle.toString())
            if (current.airQuality != null && current.airQuality.isIndexValid) {
                setContentText(context.getString(R.string.air_quality) + " - " + current.airQuality.getName(context))
            } else if (current.wind?.getStrength(context) != null) {
                setContentText(context.getString(R.string.wind) + " - " + current.wind.getStrength(context))
            }
            setOngoing(persistent)
            setOnlyAlertOnce(true)
            setContentIntent(getWeatherPendingIntent(context, null, Notifications.ID_WIDGET))
        }.build()

        if (!tempIcon && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && current.weatherCode != null) {
            try {
                notification.javaClass
                    .getMethod("setSmallIcon", Icon::class.java)
                    .invoke(
                        notification,
                        ResourceHelper.getMinimalIcon(
                            provider, current.weatherCode, daytime
                        )
                    )
            } catch (ignore: Exception) {
                // do nothing.
            }
        }

        context.notify(Notifications.ID_WIDGET, notification)
    }
}
