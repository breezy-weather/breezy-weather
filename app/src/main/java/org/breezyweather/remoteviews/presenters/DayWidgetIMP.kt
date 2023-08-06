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
import org.breezyweather.background.receiver.widget.WidgetDayProvider
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
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

object DayWidgetIMP : AbstractRemoteViewsPresenter() {

    fun updateWidgetView(context: Context, location: Location) {
        val config = getWidgetConfig(context, context.getString(R.string.sp_widget_day_setting))
        val views = getRemoteViews(
            context, location,
            config.viewStyle, config.cardStyle, config.cardAlpha, config.textColor, config.textSize,
            config.hideSubtitle, config.subtitleData
        )
        AppWidgetManager.getInstance(context).updateAppWidget(
            ComponentName(context, WidgetDayProvider::class.java),
            views
        )
    }

    fun getRemoteViews(
        context: Context, location: Location?,
        viewStyle: String?, cardStyle: String?, cardAlpha: Int, textColor: String?, textSize: Int,
        hideSubtitle: Boolean, subtitleData: String?
    ): RemoteViews {
        val settings = SettingsManager.getInstance(context)
        val temperatureUnit = settings.temperatureUnit
        val speedUnit = settings.speedUnit
        val minimalIcon = settings.isWidgetUsingMonochromeIcons
        val color = WidgetColor(
            context,
            if (viewStyle == "pixel" || viewStyle == "nano" || viewStyle == "oreo" || viewStyle == "oreo_google_sans" || viewStyle == "temp") {
                "none"
            } else cardStyle!!,
            textColor!!
        )
        val views = buildWidgetView(
            context, location, temperatureUnit, speedUnit,
            color, minimalIcon, viewStyle, textSize, hideSubtitle, subtitleData
        )
        if (color.showCard) {
            views.setImageViewResource(R.id.widget_day_card, getCardBackgroundId(color.cardColor))
            views.setInt(R.id.widget_day_card, "setImageAlpha", (cardAlpha / 100.0 * 255).toInt())
        }
        location?.let { setOnClickPendingIntent(context, views, it, viewStyle, subtitleData) }
        return views
    }

    private fun buildWidgetView(
        context: Context, location: Location?, temperatureUnit: TemperatureUnit, speedUnit: SpeedUnit,
        color: WidgetColor, minimalIcon: Boolean, viewStyle: String?, textSize: Int,
        hideSubtitle: Boolean, subtitleData: String?
    ): RemoteViews {
        val views = RemoteViews(
            context.packageName,
            when (viewStyle) {
                "rectangle" -> if (!color.showCard) R.layout.widget_day_rectangle else R.layout.widget_day_rectangle_card
                "tile" -> if (!color.showCard) R.layout.widget_day_tile else R.layout.widget_day_tile_card
                "mini" -> if (!color.showCard) R.layout.widget_day_mini else R.layout.widget_day_mini_card
                "nano" -> if (!color.showCard) R.layout.widget_day_nano else R.layout.widget_day_nano_card
                "pixel" -> if (!color.showCard) R.layout.widget_day_pixel else R.layout.widget_day_pixel_card
                "vertical" -> if (!color.showCard) R.layout.widget_day_vertical else R.layout.widget_day_vertical_card
                "oreo" -> if (!color.showCard) R.layout.widget_day_oreo else R.layout.widget_day_oreo_card
                "oreo_google_sans" -> if (!color.showCard) R.layout.widget_day_oreo_google_sans else R.layout.widget_day_oreo_google_sans_card
                "temp" -> if (!color.showCard) R.layout.widget_day_temp else R.layout.widget_day_temp_card
                else -> if (!color.showCard) R.layout.widget_day_symmetry else R.layout.widget_day_symmetry_card
            }
        )
        val weather = location?.weather ?: return views
        val provider = ResourcesProviderFactory.newInstance
        weather.current?.weatherCode?.let {
            views.setViewVisibility(R.id.widget_day_icon, View.VISIBLE)
            views.setImageViewUri(
                R.id.widget_day_icon,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider, it, location.isDaylight, minimalIcon, color.minimalIconColor
                )
            )
        } ?: views.setViewVisibility(R.id.widget_day_icon, View.INVISIBLE)

        if (viewStyle != "oreo" && viewStyle != "oreo_google_sans") {
            views.setTextViewText(R.id.widget_day_title, getTitleText(context, location, viewStyle, temperatureUnit))
        }
        if (viewStyle == "vertical") {
            if (weather.current?.temperature?.temperature != null) {
                val negative = temperatureUnit.getValueWithoutUnit(weather.current.temperature.temperature) < 0
                views.setViewVisibility(R.id.widget_day_sign, if (negative) View.VISIBLE else View.GONE)
            } else {
                views.setViewVisibility(R.id.widget_day_symbol, View.GONE)
                views.setViewVisibility(R.id.widget_day_sign, View.GONE)
            }
        }
        views.setTextViewText(R.id.widget_day_subtitle, getSubtitleText(context, weather, viewStyle, temperatureUnit))
        if (viewStyle != "pixel") {
            views.setTextViewText(
                R.id.widget_day_time,
                getTimeText(context, location, weather, viewStyle, subtitleData, temperatureUnit, speedUnit)
            )
        }
        if (color.textColor != Color.TRANSPARENT) {
            views.apply {
                setTextColor(R.id.widget_day_title, color.textColor)
                setTextColor(R.id.widget_day_sign, color.textColor)
                setTextColor(R.id.widget_day_symbol, color.textColor)
                setTextColor(R.id.widget_day_subtitle, color.textColor)
                setTextColor(R.id.widget_day_time, color.textColor)
            }
        }
        if (textSize != 100) {
            val signSymbolSize = context.resources.getDimensionPixelSize(R.dimen.widget_current_weather_icon_size)
                .toFloat() * textSize / 100f
            views.apply {
                setTextViewTextSize(
                    R.id.widget_day_title, TypedValue.COMPLEX_UNIT_PX,
                    getTitleSize(context, viewStyle) * textSize / 100f
                )
                setTextViewTextSize(R.id.widget_day_sign, TypedValue.COMPLEX_UNIT_PX, signSymbolSize)
                setTextViewTextSize(R.id.widget_day_symbol, TypedValue.COMPLEX_UNIT_PX, signSymbolSize)
                setTextViewTextSize(
                    R.id.widget_day_subtitle, TypedValue.COMPLEX_UNIT_PX,
                    getSubtitleSize(context, viewStyle) * textSize / 100f
                )
                setTextViewTextSize(
                    R.id.widget_day_time, TypedValue.COMPLEX_UNIT_PX,
                    getTimeSize(context, viewStyle) * textSize / 100f
                )
            }
        }
        views.setViewVisibility(R.id.widget_day_time, if (hideSubtitle) View.GONE else View.VISIBLE)
        return views
    }

    fun isInUse(context: Context): Boolean {
        val widgetIds = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, WidgetDayProvider::class.java))
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
            "tile", "mini" -> if (weather.current != null) {
                val stringBuilder = StringBuilder()
                if (!weather.current.weatherText.isNullOrEmpty()) {
                    stringBuilder.append(weather.current.weatherText)
                }
                if (weather.current.temperature?.temperature != null) {
                    if (stringBuilder.toString().isNotEmpty()) {
                        stringBuilder.append(" ")
                    }
                    stringBuilder.append(weather.current.temperature.getTemperature(context, unit, 0))
                }
                stringBuilder.toString()
            } else null
            "nano", "pixel" -> weather.current?.temperature?.getTemperature(context, unit, 0)
            "temp" -> weather.current?.temperature?.getShortTemperature(context, unit)
            "vertical" -> weather.current?.temperature?.temperature?.let {
                abs(unit.getValueWithoutUnit(it).roundToInt()).toString()
            }
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
                weather.dailyForecast.getOrNull(0)?.night?.temperature?.temperature,
                weather.dailyForecast.getOrNull(0)?.day?.temperature?.temperature,
                unit
            )
            "symmetry", "vertical" -> if (weather.current != null) {
                val stringBuilder = StringBuilder()
                if (!weather.current.weatherText.isNullOrEmpty()) {
                    stringBuilder.append(weather.current.weatherText)
                }
                if (weather.dailyForecast.isNotEmpty()
                    && weather.dailyForecast.getOrNull(0)?.day?.temperature?.temperature != null
                    && weather.dailyForecast.getOrNull(0)?.night?.temperature?.temperature != null) {
                    if (stringBuilder.toString().isNotEmpty()) {
                        stringBuilder.append(" ")
                    }
                    stringBuilder.append(
                        Temperature.getTrendTemperature(
                            context,
                            weather.dailyForecast[0].night!!.temperature!!.temperature,
                            weather.dailyForecast[0].day!!.temperature!!.temperature,
                            unit
                        )
                    )
                }
                stringBuilder.toString()
            } else null
            "oreo" -> weather.current?.temperature?.getTemperature(context, unit, 0)
            "oreo_google_sans" -> weather.current?.temperature?.temperature?.let {
                unit.getValueText(context, it, 0)
            }
            else -> null
        }
    }

    private fun getTimeText(
        context: Context, location: Location, weather: Weather,
        viewStyle: String?, subtitleData: String?, temperatureUnit: TemperatureUnit, speedUnit: SpeedUnit
    ): String? {
        return when (subtitleData) {
            "time" -> when (viewStyle) {
                "rectangle" -> (location.getPlace(context)
                        + " "
                        + weather.base.updateDate.getFormattedTime(location.timeZone, context.is12Hour))

                "symmetry" -> (Widgets.getWeek(context, location.timeZone)
                        + " "
                        + weather.base.updateDate.getFormattedTime(location.timeZone, context.is12Hour))

                "tile", "mini", "vertical" -> (location.getPlace(context)
                        + " " + Widgets.getWeek(context, location.timeZone)
                        + " " + weather.base.updateDate.getFormattedTime(location.timeZone, context.is12Hour))

                else -> null
            }
            "aqi" -> if (weather.current?.airQuality?.getIndex() != null
                && weather.current.airQuality.getName(context) != null) {
                    (weather.current.airQuality.getName(context, null)
                            + " ("
                            + weather.current.airQuality.getIndex(null)
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

                "tile", "mini", "vertical" -> (location.getPlace(context)
                        + " " + Widgets.getWeek(context, location.timeZone)
                        + " " + LunarHelper.getLunarDate(Date()))

                else -> null
            }
            "feels_like" -> {
                return if (weather.current?.temperature?.feelsLikeTemperature != null) {
                    (context.getString(R.string.temperature_feels_like)
                            + " "
                            + weather.current.temperature.getFeelsLikeTemperature(context, temperatureUnit, 0))
                } else null
            }
            else -> getCustomSubtitle(context, subtitleData, location, weather)
        }
    }

    private fun getTitleSize(context: Context, viewStyle: String?): Float {
        return when (viewStyle) {
            "rectangle", "symmetry", "tile" -> context.resources.getDimensionPixelSize(R.dimen.widget_content_text_size).toFloat()
            "mini", "nano" -> context.resources.getDimensionPixelSize(R.dimen.widget_subtitle_text_size).toFloat()
            "pixel" -> context.resources.getDimensionPixelSize(R.dimen.widget_design_title_text_size).toFloat()
            "vertical" -> context.resources.getDimensionPixelSize(R.dimen.widget_current_weather_icon_size).toFloat()
            "oreo", "oreo_google_sans", "temp" -> context.resources.getDimensionPixelSize(R.dimen.widget_large_title_text_size).toFloat()
            else -> 0f
        }
    }

    private fun getSubtitleSize(context: Context, viewStyle: String?): Float {
        return when (viewStyle) {
            "rectangle", "symmetry", "tile", "vertical" -> context.resources.getDimensionPixelSize(R.dimen.widget_content_text_size).toFloat()
            "oreo", "oreo_google_sans" -> context.resources.getDimensionPixelSize(R.dimen.widget_large_title_text_size).toFloat()
            else -> 0f
        }
    }

    private fun getTimeSize(context: Context, viewStyle: String?): Float {
        return when (viewStyle) {
            "rectangle", "symmetry", "tile", "vertical", "mini" -> context.resources.getDimensionPixelSize(R.dimen.widget_time_text_size).toFloat()
            "pixel" -> context.resources.getDimensionPixelSize(R.dimen.widget_subtitle_text_size).toFloat()
            else -> 0f
        }
    }

    private fun setOnClickPendingIntent(
        context: Context, views: RemoteViews, location: Location, viewStyle: String?, subtitleData: String?
    ) {
        // weather.
        views.setOnClickPendingIntent(
            R.id.widget_day_weather,
            getWeatherPendingIntent(
                context, location, Widgets.DAY_PENDING_INTENT_CODE_WEATHER
            )
        )

        // title.
        if (viewStyle == "oreo" || viewStyle == "oreo_google_sans") {
            views.setOnClickPendingIntent(
                R.id.widget_day_title,
                getCalendarPendingIntent(
                    context, Widgets.DAY_PENDING_INTENT_CODE_CALENDAR
                )
            )
        }

        // time.
        if (viewStyle == "pixel" || subtitleData == "lunar") {
            views.setOnClickPendingIntent(
                R.id.widget_day_time,
                getCalendarPendingIntent(
                    context, Widgets.DAY_PENDING_INTENT_CODE_CALENDAR
                )
            )
        }
    }
}
