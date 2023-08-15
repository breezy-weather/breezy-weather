/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.remoteviews.presenters

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import org.breezyweather.R
import org.breezyweather.background.receiver.widget.WidgetDayWeekProvider
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.unit.SpeedUnit
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.Weather
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.utils.helpers.LunarHelper
import org.breezyweather.remoteviews.Widgets
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.ResourcesProviderFactory
import org.breezyweather.theme.resource.providers.ResourceProvider
import java.util.*

object DayWeekWidgetIMP : AbstractRemoteViewsPresenter() {

    fun updateWidgetView(context: Context, location: Location) {
        val config = getWidgetConfig(context, context.getString(R.string.sp_widget_day_week_setting))
        val views = getRemoteViews(
            context, location,
            config.viewStyle, config.cardStyle, config.cardAlpha,
            config.textColor, config.textSize, config.hideSubtitle, config.subtitleData
        )
        AppWidgetManager.getInstance(context).updateAppWidget(
            ComponentName(context, WidgetDayWeekProvider::class.java),
            views
        )
    }

    fun getRemoteViews(
        context: Context, location: Location?,
        viewStyle: String?, cardStyle: String?, cardAlpha: Int,
        textColor: String?, textSize: Int, hideSubtitle: Boolean, subtitleData: String?
    ): RemoteViews {
        val provider = ResourcesProviderFactory.newInstance
        val settings = SettingsManager.getInstance(context)
        val temperatureUnit = settings.temperatureUnit
        val speedUnit = settings.speedUnit
        val weekIconMode = settings.widgetWeekIconMode
        val minimalIcon = settings.isWidgetUsingMonochromeIcons
        val color = WidgetColor(context, cardStyle!!, textColor!!)
        val views = buildWidgetViewDayPart(
            context, provider, location, temperatureUnit, speedUnit,
            color, textSize, minimalIcon, viewStyle, hideSubtitle, subtitleData
        )
        val weather = location?.weather ?: return views

        // set week part.
        val weekIconDaytime = isWeekIconDaytime(weekIconMode, location.isDaylight)
        val dailyIds = arrayOf(
            arrayOf(R.id.widget_day_week_week_1, R.id.widget_day_week_temp_1, R.id.widget_day_week_icon_1),
            arrayOf(R.id.widget_day_week_week_2, R.id.widget_day_week_temp_2, R.id.widget_day_week_icon_2),
            arrayOf(R.id.widget_day_week_week_3, R.id.widget_day_week_temp_3, R.id.widget_day_week_icon_3),
            arrayOf(R.id.widget_day_week_week_4, R.id.widget_day_week_temp_4, R.id.widget_day_week_icon_4),
            arrayOf(R.id.widget_day_week_week_5, R.id.widget_day_week_temp_5, R.id.widget_day_week_icon_5),
        )
        dailyIds.forEachIndexed { i, dailyId ->
            weather.dailyForecastStartingToday.getOrNull(i)?.let {
                views.setTextViewText(
                    dailyId[0],
                    if (it.isToday(location.timeZone)) {
                        context.getString(R.string.short_today)
                    } else it.getWeek(context, location.timeZone)
                )
            } ?: views.setTextViewText(dailyId[0], null)
            views.setTextViewText(
                dailyId[1],
                Temperature.getTrendTemperature(
                    context,
                    weather.dailyForecastStartingToday.getOrNull(i)?.night?.temperature?.temperature,
                    weather.dailyForecastStartingToday.getOrNull(i)?.day?.temperature?.temperature,
                    temperatureUnit
                )
            )
            if (weekIconDaytime) {
                weather.dailyForecastStartingToday.getOrNull(i)?.day?.weatherCode?.let {
                    views.setViewVisibility(dailyId[2], View.VISIBLE)
                    views.setImageViewUri(
                        dailyId[2],
                        ResourceHelper.getWidgetNotificationIconUri(
                            provider, it, weekIconDaytime, minimalIcon, color.minimalIconColor
                        )
                    )
                } ?: views.setViewVisibility(dailyId[2], View.INVISIBLE)
            } else {
                weather.dailyForecastStartingToday.getOrNull(i)?.night?.weatherCode?.let {
                    views.setViewVisibility(dailyId[2], View.VISIBLE)
                    views.setImageViewUri(
                        dailyId[2],
                        ResourceHelper.getWidgetNotificationIconUri(
                            provider, it, weekIconDaytime, minimalIcon, color.minimalIconColor
                        )
                    )
                } ?: views.setViewVisibility(dailyId[2], View.INVISIBLE)
            }
        }

        // set text color.
        if (color.textColor != Color.TRANSPARENT) {
            views.apply {
                setTextColor(R.id.widget_day_week_week_1, color.textColor)
                setTextColor(R.id.widget_day_week_week_2, color.textColor)
                setTextColor(R.id.widget_day_week_week_3, color.textColor)
                setTextColor(R.id.widget_day_week_week_4, color.textColor)
                setTextColor(R.id.widget_day_week_week_5, color.textColor)
                setTextColor(R.id.widget_day_week_temp_1, color.textColor)
                setTextColor(R.id.widget_day_week_temp_2, color.textColor)
                setTextColor(R.id.widget_day_week_temp_3, color.textColor)
                setTextColor(R.id.widget_day_week_temp_4, color.textColor)
                setTextColor(R.id.widget_day_week_temp_5, color.textColor)
            }
        }

        // set text size.
        if (textSize != 100) {
            val contentSize = context.resources.getDimensionPixelSize(R.dimen.widget_content_text_size)
                .toFloat() * textSize / 100f
            views.apply {
                setTextViewTextSize(R.id.widget_day_week_week_1, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_day_week_week_2, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_day_week_week_3, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_day_week_week_4, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_day_week_week_5, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_day_week_temp_1, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_day_week_temp_2, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_day_week_temp_3, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_day_week_temp_4, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_day_week_temp_5, TypedValue.COMPLEX_UNIT_PX, contentSize)
            }
        }

        // set card.
        if (color.showCard) {
            views.setImageViewResource(R.id.widget_day_week_card, getCardBackgroundId(color.cardColor))
            views.setInt(R.id.widget_day_week_card, "setImageAlpha", (cardAlpha / 100.0 * 255).toInt())
        }

        // set intent.
        setOnClickPendingIntent(context, views, location, subtitleData)
        return views
    }

    private fun buildWidgetViewDayPart(
        context: Context, helper: ResourceProvider, location: Location?,
        temperatureUnit: TemperatureUnit, speedUnit: SpeedUnit,
        color: WidgetColor, textSize: Int, minimalIcon: Boolean,
        viewStyle: String?, hideSubtitle: Boolean, subtitleData: String?
    ): RemoteViews {
        val views = RemoteViews(
            context.packageName,
            when (viewStyle) {
                "rectangle" -> if (!color.showCard) R.layout.widget_day_week_rectangle else R.layout.widget_day_week_rectangle_card
                "tile" -> if (!color.showCard) R.layout.widget_day_week_tile else R.layout.widget_day_week_tile_card
                else -> if (!color.showCard) R.layout.widget_day_week_symmetry else R.layout.widget_day_week_symmetry_card
            }
        )
        val weather = location?.weather ?: return views
        weather.current?.weatherCode?.let {
            views.setViewVisibility(R.id.widget_day_week_icon, View.VISIBLE)
            views.setImageViewUri(
                R.id.widget_day_week_icon,
                ResourceHelper.getWidgetNotificationIconUri(
                    helper, it, location.isDaylight, minimalIcon, color.minimalIconColor
                )
            )
        } ?: views.setViewVisibility(R.id.widget_day_week_icon, View.INVISIBLE)
        views.apply {
            setTextViewText(
                R.id.widget_day_week_title,
                getTitleText(context, location, viewStyle, temperatureUnit)
            )
            setTextViewText(
                R.id.widget_day_week_subtitle,
                getSubtitleText(context, weather, viewStyle, temperatureUnit)
            )
            setTextViewText(
                R.id.widget_day_week_time,
                getTimeText(context, location, viewStyle, subtitleData, temperatureUnit, speedUnit)
            )
        }
        if (color.textColor != Color.TRANSPARENT) {
            views.apply {
                setTextColor(R.id.widget_day_week_title, color.textColor)
                setTextColor(R.id.widget_day_week_subtitle, color.textColor)
                setTextColor(R.id.widget_day_week_time, color.textColor)
            }
        }
        if (textSize != 100) {
            val contentSize = context.resources.getDimensionPixelSize(R.dimen.widget_content_text_size)
                .toFloat() * textSize / 100f
            val timeSize = context.resources.getDimensionPixelSize(R.dimen.widget_time_text_size)
                .toFloat() * textSize / 100f
            views.apply {
                setTextViewTextSize(R.id.widget_day_week_title, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_day_week_subtitle, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_day_week_time, TypedValue.COMPLEX_UNIT_PX, timeSize)
            }
        }
        views.setViewVisibility(R.id.widget_day_week_time, if (hideSubtitle) View.GONE else View.VISIBLE)
        return views
    }

    fun isInUse(context: Context): Boolean {
        val widgetIds = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, WidgetDayWeekProvider::class.java))
        return widgetIds != null && widgetIds.isNotEmpty()
    }

    private fun getTitleText(
        context: Context, location: Location, viewStyle: String?, unit: TemperatureUnit
    ): String? {
        val weather = location.weather ?: return null
        return when (viewStyle) {
            "rectangle" -> Widgets.buildWidgetDayStyleText(context, weather, unit)[0]
            "symmetry" -> {
                val stringBuilder = StringBuilder()
                stringBuilder.append(location.getPlace(context))
                if (weather.current?.temperature?.temperature != null) {
                    stringBuilder.append("\n")
                        .append(weather.current.temperature.getTemperature(context, unit, 0))
                }
                stringBuilder.toString()
            }
            "tile" -> if (weather.current != null) {
                val stringBuilder = StringBuilder()
                if (!weather.current.weatherText.isNullOrEmpty()) {
                    stringBuilder.append(weather.current.weatherText)
                }
                if (weather.current.temperature?.temperature != null) {
                    if (stringBuilder.isNotEmpty()) {
                        stringBuilder.append(" ")
                    }
                    stringBuilder.append(weather.current.temperature.getTemperature(context, unit, 0))
                }
                return stringBuilder.toString()
            } else null
            else -> null
        }
    }

    private fun getSubtitleText(
        context: Context, weather: Weather, viewStyle: String?, unit: TemperatureUnit
    ): String? {
        return when (viewStyle) {
            "rectangle" -> Widgets.buildWidgetDayStyleText(context, weather, unit)[1]
            "tile" -> Temperature.getTrendTemperature(
                context,
                weather.today?.night?.temperature?.temperature,
                weather.today?.day?.temperature?.temperature,
                unit
            )
            "symmetry" -> if (weather.current != null) {
                val stringBuilder = StringBuilder()
                if (!weather.current.weatherText.isNullOrEmpty()) {
                    stringBuilder.append(weather.current.weatherText)
                }
                if (weather.dailyForecast.isNotEmpty()
                    && weather.today?.day?.temperature?.temperature != null
                    && weather.today?.night?.temperature?.temperature != null) {
                    if (stringBuilder.toString().isNotEmpty()) {
                        stringBuilder.append(" ")
                    }
                    return Temperature.getTrendTemperature(
                        context,
                        weather.today!!.night!!.temperature!!.temperature,
                        weather.today!!.day!!.temperature!!.temperature,
                        unit
                    )
                }
                stringBuilder.toString()
            } else null
            else -> null
        }
    }

    private fun getTimeText(
        context: Context, location: Location, viewStyle: String?, subtitleData: String?,
        temperatureUnit: TemperatureUnit, speedUnit: SpeedUnit
    ): String? {
        val weather = location.weather ?: return null
        return when (subtitleData) {
            "time" -> when (viewStyle) {
                "rectangle" -> (location.getPlace(context)
                        + " "
                        + (weather.base.refreshTime?.getFormattedTime(location.timeZone, context.is12Hour) ?: ""))

                "symmetry" -> (Widgets.getWeek(context, location.timeZone)
                        + " "
                        + (weather.base.refreshTime?.getFormattedTime(location.timeZone, context.is12Hour) ?: ""))

                "tile", "vertical" -> (location.getPlace(context)
                        + " " + Widgets.getWeek(context, location.timeZone)
                        + " " + (weather.base.refreshTime?.getFormattedTime(location.timeZone, context.is12Hour) ?: ""))

                else -> null
            }
            "aqi" -> if (weather.current?.airQuality?.getIndex() != null
                && weather.current.airQuality.getName(context) != null) {
                (weather.current.airQuality.getName(context)
                        + " ("
                        + weather.current.airQuality.getIndex()
                        + ")")
            } else null
            "wind" -> weather.current?.wind?.getShortDescription(context, speedUnit)
            "lunar" -> when (viewStyle) {
                "rectangle" -> (location.getPlace(context)
                        + " "
                        + LunarHelper.getLunarDate(Date()))

                "symmetry" -> (Widgets.getWeek(context, location.timeZone)
                        + " "
                        + LunarHelper.getLunarDate(Date()))

                "tile" -> (location.getPlace(context)
                        + " "
                        + Widgets.getWeek(context, location.timeZone)
                        + " "
                        + LunarHelper.getLunarDate(Date()))

                else -> null
            }
            "feels_like" -> if (weather.current?.temperature?.feelsLikeTemperature != null) {
                (context.getString(R.string.temperature_feels_like)
                        + " "
                        + weather.current.temperature.getFeelsLikeTemperature(context, temperatureUnit, 0))
            } else null
            else -> getCustomSubtitle(context, subtitleData, location, weather)
        }
    }

    private fun setOnClickPendingIntent(
        context: Context, views: RemoteViews, location: Location, subtitleData: String?
    ) {
        // weather.
        views.setOnClickPendingIntent(
            R.id.widget_day_week_weather,
            getWeatherPendingIntent(
                context, location, Widgets.DAY_WEEK_PENDING_INTENT_CODE_WEATHER
            )
        )

        // daily forecast.
        views.setOnClickPendingIntent(
            R.id.widget_day_week_icon_1,
            getDailyForecastPendingIntent(
                context, location, 0, Widgets.DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_1
            )
        )
        views.setOnClickPendingIntent(
            R.id.widget_day_week_icon_2,
            getDailyForecastPendingIntent(
                context, location, 1, Widgets.DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_2
            )
        )
        views.setOnClickPendingIntent(
            R.id.widget_day_week_icon_3,
            getDailyForecastPendingIntent(
                context, location, 2, Widgets.DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_3
            )
        )
        views.setOnClickPendingIntent(
            R.id.widget_day_week_icon_4,
            getDailyForecastPendingIntent(
                context, location, 3, Widgets.DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_4
            )
        )
        views.setOnClickPendingIntent(
            R.id.widget_day_week_icon_5,
            getDailyForecastPendingIntent(
                context, location, 4, Widgets.DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_5
            )
        )

        // time.
        if (subtitleData == "lunar") {
            views.setOnClickPendingIntent(
                R.id.widget_day_week_subtitle,
                getCalendarPendingIntent(
                    context, Widgets.DAY_WEEK_PENDING_INTENT_CODE_CALENDAR
                )
            )
        }
    }
}
