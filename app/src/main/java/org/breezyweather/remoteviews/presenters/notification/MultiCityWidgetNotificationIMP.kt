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
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import notificationBuilder
import notify
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.NotificationTextColor
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.extensions.setLanguage
import org.breezyweather.common.utils.helpers.LunarHelper
import org.breezyweather.remoteviews.Notifications
import org.breezyweather.remoteviews.presenters.AbstractRemoteViewsPresenter
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.ResourcesProviderFactory
import org.breezyweather.theme.resource.providers.ResourceProvider
import java.util.*
import kotlin.math.roundToInt

object MultiCityWidgetNotificationIMP : AbstractRemoteViewsPresenter() {
    fun buildNotificationAndSendIt(
        context: Context,
        locationList: List<Location>,
        temperatureUnit: TemperatureUnit,
        dayTime: Boolean,
        tempIcon: Boolean,
        persistent: Boolean
    ) {
        val current = locationList.getOrNull(0)?.weather?.current ?: return
        val provider = ResourcesProviderFactory.newInstance
        context.setLanguage(SettingsManager.getInstance(context).language.locale)

        val temperature = if (tempIcon) {
            if (SettingsManager.getInstance(context).isWidgetNotificationUsingFeelsLike) {
                current.temperature?.feelsLikeTemperature ?: current.temperature?.temperature
            } else current.temperature?.temperature
        } else null
        val notification = context.notificationBuilder(Notifications.CHANNEL_WIDGET).apply {
            priority = NotificationCompat.PRIORITY_MAX
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setSmallIcon(
                if (temperature != null) {
                    ResourceHelper.getTempIconId(context, temperatureUnit.getValueWithoutUnit(temperature).roundToInt())
                } else ResourceHelper.getDefaultMinimalXmlIconId(current.weatherCode, dayTime)
            )
            setContent(
                buildBaseView(
                    context,
                    RemoteViews(context.packageName, R.layout.notification_base),
                    provider,
                    locationList[0],
                    temperatureUnit,
                    dayTime
                )
            )
            setContentIntent(getWeatherPendingIntent(context, null, Notifications.ID_WIDGET))
            setCustomBigContentView(
                buildBigView(
                    context,
                    RemoteViews(context.packageName, R.layout.notification_multi_city),
                    provider,
                    locationList,
                    temperatureUnit,
                    dayTime
                )
            )
            setOngoing(persistent)
            setOnlyAlertOnce(true)
        }.build()

        if (!tempIcon && current.weatherCode != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                notification.javaClass
                    .getMethod("setSmallIcon", Icon::class.java)
                    .invoke(
                        notification,
                        ResourceHelper.getMinimalIcon(
                            provider, current.weatherCode, dayTime
                        )
                    )
            } catch (ignore: Exception) {
                // do nothing.
            }
        }

        context.notify(Notifications.ID_WIDGET, notification)
    }

    private fun buildBaseView(
        context: Context, views: RemoteViews,
        provider: ResourceProvider, location: Location,
        temperatureUnit: TemperatureUnit, dayTime: Boolean
    ): RemoteViews {
        val current = location.weather?.current ?: return views

        val temperature = if (SettingsManager.getInstance(context).isWidgetNotificationUsingFeelsLike) {
            current.temperature?.feelsLikeTemperature ?: current.temperature?.temperature
        } else current.temperature?.temperature
        val timeStr = StringBuilder()
        timeStr.append(location.getPlace(context))
        if (SettingsManager.getInstance(context).language.isChinese) {
            timeStr.append(", ").append(LunarHelper.getLunarDate(Date()))
        }

        views.apply {
            if (current.weatherCode != null) {
                setImageViewUri(
                    R.id.notification_base_icon,
                    ResourceHelper.getWidgetNotificationIconUri(
                        provider,
                        current.weatherCode,
                        dayTime,
                        false,
                        NotificationTextColor.GREY
                    )
                )
            }
            if (temperature != null) {
                setTextViewText(
                    R.id.notification_base_realtimeTemp,
                    Temperature.getShortTemperature(
                        context,
                        temperature,
                        temperatureUnit
                    )
                )
            }
            if (current.airQuality != null && current.airQuality.isIndexValid) {
                setTextViewText(
                    R.id.notification_base_aqiAndWind,
                    context.getString(R.string.air_quality) + " - " + current.airQuality.getName(context)
                )
            } else if (current.wind?.getStrength(context) != null) {
                setTextViewText(
                    R.id.notification_base_aqiAndWind,
                    context.getString(R.string.wind) + " - " + current.wind.getStrength(context)
                )
            }
            if (!current.weatherText.isNullOrEmpty()) {
                setTextViewText(
                    R.id.notification_base_weather,
                    current.weatherText
                )
            }
            setTextViewText(R.id.notification_base_time, timeStr.toString())
        }

        return views
    }

    private fun buildBigView(
        context: Context,
        viewsP: RemoteViews,
        provider: ResourceProvider,
        locationList: List<Location>,
        temperatureUnit: TemperatureUnit,
        dayTime: Boolean
    ): RemoteViews {
        if (locationList.getOrNull(0)?.weather == null) return viewsP

        // today
        val views = buildBaseView(context, viewsP, provider, locationList[0], temperatureUnit, dayTime)
        val viewIds = arrayOf(
            Triple(R.id.notification_multi_city_1, R.id.notification_multi_city_icon_1, R.id.notification_multi_city_text_1),
            Triple(R.id.notification_multi_city_2, R.id.notification_multi_city_icon_2, R.id.notification_multi_city_text_2),
            Triple(R.id.notification_multi_city_3, R.id.notification_multi_city_icon_3, R.id.notification_multi_city_text_3)
        )

        // Loop through locations 1 to 3
        viewIds.forEachIndexed { i, viewId ->
            locationList.getOrNull(i + 1)?.weather?.let { weather ->
                val location = locationList[i + 1]
                val cityDayTime = location.isDaylight
                val weatherCode = if (cityDayTime) {
                    weather.today?.day?.weatherCode
                } else weather.today?.night?.weatherCode
                views.apply {
                    setViewVisibility(viewId.first, View.VISIBLE)
                    if (weatherCode != null) {
                        setImageViewUri(
                            viewId.second,
                            ResourceHelper.getWidgetNotificationIconUri(
                                provider, weatherCode, cityDayTime, false, NotificationTextColor.GREY
                            )
                        )
                    }
                    setTextViewText(viewId.third, getCityTitle(context, location, temperatureUnit))
                }
            } ?: views.setViewVisibility(viewId.first, View.GONE)
        }

        return views
    }

    private fun getCityTitle(context: Context, location: Location, unit: TemperatureUnit): String {
        val builder = StringBuilder(
            location.getPlace(context, true)
        )
        location.weather?.today?.let {
            builder.append(", ").append(
                Temperature.getTrendTemperature(
                    context,
                    it.night?.temperature?.temperature,
                    it.day?.temperature?.temperature,
                    unit
                )
            )
        }
        return builder.toString()
    }

    fun isEnabled(context: Context): Boolean {
        return SettingsManager.getInstance(context).isWidgetNotificationEnabled
    }
}
