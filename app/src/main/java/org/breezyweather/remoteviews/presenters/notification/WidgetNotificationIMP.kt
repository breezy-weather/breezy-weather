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
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.NotificationStyle
import org.breezyweather.common.basic.models.options.NotificationTextColor
import org.breezyweather.common.basic.models.options.appearance.CalendarHelper
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.extensions.getFormattedMediumDayAndMonthInAdditionalCalendar
import org.breezyweather.common.extensions.getHour
import org.breezyweather.common.extensions.notificationBuilder
import org.breezyweather.common.extensions.notify
import org.breezyweather.domain.location.model.getPlace
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.getName
import org.breezyweather.domain.weather.model.getStrength
import org.breezyweather.domain.weather.model.getTrendTemperature
import org.breezyweather.domain.weather.model.getWeek
import org.breezyweather.domain.weather.model.isIndexValid
import org.breezyweather.domain.weather.model.isToday
import org.breezyweather.remoteviews.Notifications
import org.breezyweather.remoteviews.presenters.AbstractRemoteViewsPresenter
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import java.util.Date

object WidgetNotificationIMP : AbstractRemoteViewsPresenter() {

    fun buildNotificationAndSendIt(
        context: Context,
        locationList: List<Location>,
    ) {
        val location = locationList.getOrNull(0)
        val current = location?.weather?.current ?: return
        val provider = ResourcesProviderFactory.newInstance

        // get sp & realTimeWeather.
        val settings = SettingsManager.getInstance(context)
        val temperatureUnit = settings.temperatureUnit
        val dayTime = location.isDaylight
        val tempIcon = settings.isWidgetNotificationTemperatureIconEnabled
        val persistent = settings.isWidgetNotificationPersistent
        if (settings.widgetNotificationStyle === NotificationStyle.NATIVE) {
            NativeWidgetNotificationIMP.buildNotificationAndSendIt(
                context,
                location,
                dayTime,
                tempIcon,
                persistent
            )
            return
        } else if (settings.widgetNotificationStyle === NotificationStyle.CITIES) {
            MultiCityWidgetNotificationIMP.buildNotificationAndSendIt(
                context,
                locationList,
                temperatureUnit,
                dayTime,
                tempIcon,
                persistent
            )
            return
        }

        val temperature = if (tempIcon) {
            if (SettingsManager.getInstance(context).isWidgetNotificationUsingFeelsLike) {
                current.temperature?.feelsLikeTemperature ?: current.temperature?.temperature
            } else {
                current.temperature?.temperature
            }
        } else {
            null
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
                    ResourceHelper.getDefaultMinimalXmlIconId(current.weatherCode, dayTime)
                )
            }
            setContent(
                buildBaseView(
                    context,
                    RemoteViews(context.packageName, R.layout.notification_base),
                    provider,
                    location,
                    temperatureUnit,
                    dayTime
                )
            )
            setContentIntent(getWeatherPendingIntent(context, null, Notifications.ID_WIDGET))
            setCustomBigContentView(
                buildBigView(
                    context,
                    RemoteViews(context.packageName, R.layout.notification_big),
                    settings.widgetNotificationStyle === NotificationStyle.DAILY,
                    provider,
                    location,
                    temperatureUnit,
                    dayTime
                )
            )
            setOngoing(persistent)
            setOnlyAlertOnce(true)
        }.build()

        if (!tempIcon && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            current.weatherCode?.let { weatherCode ->
                try {
                    notification.javaClass
                        .getMethod("setSmallIcon", Icon::class.java)
                        .invoke(
                            notification,
                            ResourceHelper.getMinimalIcon(
                                provider,
                                weatherCode,
                                dayTime
                            )
                        )
                } catch (ignore: Exception) {
                    // do nothing.
                }
            }
        }

        context.notify(Notifications.ID_WIDGET, notification)
    }

    // TODO: Identical to MultiCityWidgetNotificationIMP.buildBaseView
    private fun buildBaseView(
        context: Context,
        views: RemoteViews,
        provider: ResourceProvider,
        location: Location,
        temperatureUnit: TemperatureUnit,
        dayTime: Boolean,
    ): RemoteViews {
        val current = location.weather?.current ?: return views

        val temperature = if (SettingsManager.getInstance(context).isWidgetNotificationUsingFeelsLike) {
            current.temperature?.feelsLikeTemperature ?: current.temperature?.temperature
        } else {
            current.temperature?.temperature
        }
        val timeStr = StringBuilder()
        timeStr.append(location.getPlace(context))
        if (CalendarHelper.getAlternateCalendarSetting(context) != null) {
            timeStr.append(context.getString(R.string.comma_separator))
                .append(Date().getFormattedMediumDayAndMonthInAdditionalCalendar(location, context))
        }

        views.apply {
            current.weatherCode?.let { weatherCode ->
                setImageViewUri(
                    R.id.notification_base_icon,
                    ResourceHelper.getWidgetNotificationIconUri(
                        provider,
                        weatherCode,
                        dayTime,
                        false,
                        NotificationTextColor.GREY
                    )
                )
            }
            temperature?.let {
                setTextViewText(
                    R.id.notification_base_realtimeTemp,
                    temperatureUnit.getShortValueText(context, it)
                )
            }
            if (current.airQuality?.isIndexValid == true) {
                setTextViewText(
                    R.id.notification_base_aqiAndWind,
                    context.getString(R.string.air_quality) + " - " + current.airQuality!!.getName(context)
                )
            } else {
                current.wind?.getStrength(context)?.let { strength ->
                    setTextViewText(
                        R.id.notification_base_aqiAndWind,
                        context.getString(R.string.wind) + " - " + strength
                    )
                }
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
        daily: Boolean,
        provider: ResourceProvider,
        location: Location,
        temperatureUnit: TemperatureUnit,
        dayTime: Boolean,
    ): RemoteViews {
        val weather = location.weather ?: return viewsP

        // today
        val views = buildBaseView(context, viewsP, provider, location, temperatureUnit, dayTime)
        val viewIds = arrayOf(
            Triple(R.id.notification_big_week_1, R.id.notification_big_temp_1, R.id.notification_big_icon_1),
            Triple(R.id.notification_big_week_2, R.id.notification_big_temp_2, R.id.notification_big_icon_2),
            Triple(R.id.notification_big_week_3, R.id.notification_big_temp_3, R.id.notification_big_icon_3),
            Triple(R.id.notification_big_week_4, R.id.notification_big_temp_4, R.id.notification_big_icon_4),
            Triple(R.id.notification_big_week_5, R.id.notification_big_temp_5, R.id.notification_big_icon_5)
        )

        if (daily) {
            val weekIconDaytime = isWeekIconDaytime(SettingsManager.getInstance(context).widgetWeekIconMode, dayTime)

            // Loop through 5 first days
            viewIds.forEachIndexed { i, viewId ->
                weather.dailyForecastStartingToday.getOrNull(i)?.let { daily ->
                    val weatherCode = if (weekIconDaytime) daily.day?.weatherCode else daily.night?.weatherCode
                    views.apply {
                        setTextViewText(
                            viewId.first,
                            if (daily.isToday(location)) {
                                context.getString(R.string.short_today)
                            } else {
                                daily.getWeek(location, context)
                            }
                        )
                        setTextViewText(
                            viewId.second,
                            daily.getTrendTemperature(context, temperatureUnit)
                        )
                        if (weatherCode != null) {
                            setImageViewUri(
                                viewId.third,
                                ResourceHelper.getWidgetNotificationIconUri(
                                    provider,
                                    weatherCode,
                                    weekIconDaytime,
                                    false,
                                    NotificationTextColor.GREY
                                )
                            )
                        }
                    }
                }
            }
        } else {
            // Loop through 5 next hours
            viewIds.forEachIndexed { i, viewId ->
                weather.nextHourlyForecast.getOrNull(i)?.let { hourly ->
                    views.apply {
                        setTextViewText(viewId.first, hourly.date.getHour(location, context))
                        hourly.temperature?.temperature?.let {
                            setTextViewText(
                                viewId.second,
                                temperatureUnit.getShortValueText(context, it)
                            )
                        }
                        hourly.weatherCode?.let { weatherCode ->
                            setImageViewUri(
                                viewId.third,
                                ResourceHelper.getWidgetNotificationIconUri(
                                    provider,
                                    weatherCode,
                                    hourly.isDaylight,
                                    false,
                                    NotificationTextColor.GREY
                                )
                            )
                        }
                    }
                }
            }
        }
        return views
    }

    fun cancelNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(Notifications.ID_WIDGET)
    }

    fun isEnabled(context: Context): Boolean {
        return SettingsManager.getInstance(context).isWidgetNotificationEnabled
    }
}
