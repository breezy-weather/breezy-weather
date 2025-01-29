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

package org.breezyweather.remoteviews.presenters

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Weather
import org.breezyweather.R
import org.breezyweather.background.receiver.widget.WidgetDayProvider
import org.breezyweather.common.basic.models.options.unit.SpeedUnit
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.extensions.getFormattedMediumDayAndMonthInAdditionalCalendar
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.getLongWeekdayDayMonth
import org.breezyweather.common.extensions.getWeek
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.isRtl
import org.breezyweather.common.source.PollenIndexSource
import org.breezyweather.domain.location.model.getPlace
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.getIndex
import org.breezyweather.domain.weather.model.getName
import org.breezyweather.domain.weather.model.getShortDescription
import org.breezyweather.domain.weather.model.getTrendTemperature
import org.breezyweather.remoteviews.Widgets
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory
import java.util.Date
import kotlin.math.abs
import kotlin.math.roundToInt

object DayWidgetIMP : AbstractRemoteViewsPresenter() {

    fun updateWidgetView(
        context: Context,
        location: Location?,
        pollenIndexSource: PollenIndexSource?,
    ) {
        val config = getWidgetConfig(context, context.getString(R.string.sp_widget_day_setting))
        val views = getRemoteViews(
            context, location,
            config.viewStyle, config.cardStyle, config.cardAlpha, config.textColor, config.textSize,
            config.hideSubtitle, config.subtitleData, pollenIndexSource
        )
        AppWidgetManager.getInstance(context).updateAppWidget(
            ComponentName(context, WidgetDayProvider::class.java),
            views
        )
    }

    fun getRemoteViews(
        context: Context,
        location: Location?,
        viewStyle: String?,
        cardStyle: String?,
        cardAlpha: Int,
        textColor: String?,
        textSize: Int,
        hideSubtitle: Boolean,
        subtitleData: String?,
        pollenIndexSource: PollenIndexSource?,
    ): RemoteViews {
        val settings = SettingsManager.getInstance(context)
        val temperatureUnit = settings.temperatureUnit
        val speedUnit = settings.speedUnit
        val minimalIcon = settings.isWidgetUsingMonochromeIcons
        val color = WidgetColor(
            context,
            if (arrayOf("pixel", "nano", "oreo", "oreo_google_sans", "temp").contains(viewStyle)) {
                "none"
            } else {
                cardStyle!!
            },
            textColor!!,
            location?.isDaylight ?: true
        )
        val views = buildWidgetView(
            context,
            location,
            temperatureUnit,
            speedUnit,
            color,
            minimalIcon,
            viewStyle,
            textSize,
            hideSubtitle,
            subtitleData,
            pollenIndexSource
        )
        if (color.showCard) {
            views.setImageViewResource(R.id.widget_day_card, getCardBackgroundId(color))
            views.setInt(R.id.widget_day_card, "setImageAlpha", (cardAlpha / 100.0 * 255).toInt())
        }
        location?.let { setOnClickPendingIntent(context, views, it, viewStyle, subtitleData) }
        return views
    }

    private fun buildWidgetView(
        context: Context,
        location: Location?,
        temperatureUnit: TemperatureUnit,
        speedUnit: SpeedUnit,
        color: WidgetColor,
        minimalIcon: Boolean,
        viewStyle: String?,
        textSize: Int,
        hideSubtitle: Boolean,
        subtitleData: String?,
        pollenIndexSource: PollenIndexSource?,
    ): RemoteViews {
        val views = RemoteViews(
            context.packageName,
            when (viewStyle) {
                "rectangle" -> if (!color.showCard) {
                    R.layout.widget_day_rectangle
                } else {
                    R.layout.widget_day_rectangle_card
                }
                "tile" -> if (!color.showCard) {
                    R.layout.widget_day_tile
                } else {
                    R.layout.widget_day_tile_card
                }
                "mini" -> if (!color.showCard) {
                    R.layout.widget_day_mini
                } else {
                    R.layout.widget_day_mini_card
                }
                "nano" -> if (!color.showCard) {
                    R.layout.widget_day_nano
                } else {
                    R.layout.widget_day_nano_card
                }
                "pixel" -> if (!color.showCard) {
                    R.layout.widget_day_pixel
                } else {
                    R.layout.widget_day_pixel_card
                }
                "vertical" -> if (!color.showCard) {
                    R.layout.widget_day_vertical
                } else {
                    R.layout.widget_day_vertical_card
                }
                "oreo", "oreo_google_sans" -> if (!color.showCard) {
                    R.layout.widget_day_oreo
                } else {
                    R.layout.widget_day_oreo_card
                }
                "temp" -> if (!color.showCard) {
                    R.layout.widget_day_temp
                } else {
                    R.layout.widget_day_temp_card
                }
                else -> if (!color.showCard) {
                    R.layout.widget_day_symmetry
                } else {
                    R.layout.widget_day_symmetry_card
                }
            }
        )
        val weather = location?.weather ?: return views
        val provider = ResourcesProviderFactory.newInstance
        weather.current?.weatherCode?.let {
            views.setViewVisibility(R.id.widget_day_icon, View.VISIBLE)
            views.setImageViewUri(
                R.id.widget_day_icon,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    it,
                    location.isDaylight,
                    minimalIcon,
                    color.minimalIconColor
                )
            )
        } ?: views.setViewVisibility(R.id.widget_day_icon, View.INVISIBLE)

        if (viewStyle == "pixel") {
            val dateFormat = getLongWeekdayDayMonth(context)
            views.setString(
                R.id.widget_day_time,
                "setTimeZone",
                location.timeZone
            )
            views.setCharSequence(
                R.id.widget_day_time,
                "setFormat12Hour",
                dateFormat
            )
            views.setCharSequence(
                R.id.widget_day_time,
                "setFormat24Hour",
                dateFormat
            )
        }

        if (viewStyle == "oreo" || viewStyle == "oreo_google_sans") {
            val dateFormat = (if (context.isRtl) " | " else "") +
                getLongWeekdayDayMonth(context) +
                (if (!context.isRtl) " | " else "")
            views.setString(
                R.id.widget_day_title,
                "setTimeZone",
                location.timeZone
            )
            views.setCharSequence(
                R.id.widget_day_title,
                "setFormat12Hour",
                dateFormat
            )
            views.setCharSequence(
                R.id.widget_day_title,
                "setFormat24Hour",
                dateFormat
            )
        }

        if (viewStyle != "oreo" && viewStyle != "oreo_google_sans") {
            views.setTextViewText(R.id.widget_day_title, getTitleText(context, location, viewStyle, temperatureUnit))
        }
        if (viewStyle == "vertical") {
            weather.current?.temperature?.temperature?.let {
                val negative = temperatureUnit.getValueWithoutUnit(it) < 0
                views.setViewVisibility(R.id.widget_day_sign, if (negative) View.VISIBLE else View.GONE)
            } ?: run {
                views.setViewVisibility(R.id.widget_day_symbol, View.GONE)
                views.setViewVisibility(R.id.widget_day_sign, View.GONE)
            }
        }
        views.setTextViewText(R.id.widget_day_subtitle, getSubtitleText(context, weather, viewStyle, temperatureUnit))
        if (viewStyle != "pixel") {
            views.setTextViewText(
                R.id.widget_day_time,
                getTimeText(
                    context,
                    location,
                    weather,
                    viewStyle,
                    subtitleData,
                    temperatureUnit,
                    speedUnit,
                    pollenIndexSource
                )
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
                    R.id.widget_day_title,
                    TypedValue.COMPLEX_UNIT_PX,
                    getTitleSize(context, viewStyle) * textSize / 100f
                )
                setTextViewTextSize(R.id.widget_day_sign, TypedValue.COMPLEX_UNIT_PX, signSymbolSize)
                setTextViewTextSize(R.id.widget_day_symbol, TypedValue.COMPLEX_UNIT_PX, signSymbolSize)
                setTextViewTextSize(
                    R.id.widget_day_subtitle,
                    TypedValue.COMPLEX_UNIT_PX,
                    getSubtitleSize(context, viewStyle) * textSize / 100f
                )
                setTextViewTextSize(
                    R.id.widget_day_time,
                    TypedValue.COMPLEX_UNIT_PX,
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
        context: Context,
        location: Location,
        viewStyle: String?,
        unit: TemperatureUnit,
    ): String? {
        val weather = location.weather ?: return null
        return when (viewStyle) {
            "rectangle" -> Widgets.buildWidgetDayStyleText(context, weather, unit)[0]
            "symmetry" -> {
                val stringBuilder = StringBuilder()
                stringBuilder.append(location.getPlace(context))
                weather.current?.temperature?.temperature?.let {
                    stringBuilder.append("\n")
                        .append(unit.getValueText(context, it, 0))
                }
                stringBuilder.toString()
            }
            "tile", "mini" -> weather.current?.let { current ->
                val stringBuilder = StringBuilder()
                if (!current.weatherText.isNullOrEmpty()) {
                    stringBuilder.append(current.weatherText)
                }
                current.temperature?.temperature?.let {
                    if (stringBuilder.toString().isNotEmpty()) {
                        stringBuilder.append(" ")
                    }
                    stringBuilder.append(unit.getValueText(context, it, 0))
                }
                stringBuilder.toString()
            }
            "nano", "pixel" -> weather.current?.temperature?.temperature?.let {
                unit.getValueText(context, it, 0)
            }
            "temp" -> weather.current?.temperature?.temperature?.let {
                unit.getShortValueText(context, it)
            }
            "vertical" -> weather.current?.temperature?.temperature?.let {
                abs(unit.getValueWithoutUnit(it).roundToInt()).toString()
            }
            else -> null
        }
    }

    private fun getSubtitleText(
        context: Context,
        weather: Weather,
        viewStyle: String?,
        unit: TemperatureUnit,
    ): String? {
        return when (viewStyle) {
            "rectangle" -> Widgets.buildWidgetDayStyleText(context, weather, unit)[1]
            "tile" -> weather.today?.getTrendTemperature(context, unit)
            "symmetry", "vertical" -> weather.current?.let { current ->
                val stringBuilder = StringBuilder()
                if (!current.weatherText.isNullOrEmpty()) {
                    stringBuilder.append(current.weatherText)
                }
                if (weather.dailyForecast.isNotEmpty() &&
                    weather.today?.day?.temperature?.temperature != null &&
                    weather.today?.night?.temperature?.temperature != null
                ) {
                    if (stringBuilder.toString().isNotEmpty()) {
                        stringBuilder.append(" ")
                    }
                    stringBuilder.append(weather.today!!.getTrendTemperature(context, unit))
                }
                stringBuilder.toString()
            }
            "oreo", "oreo_google_sans" -> weather.current?.temperature?.temperature?.let {
                unit.getValueText(context, it, 0)
            }
            else -> null
        }
    }

    private fun getTimeText(
        context: Context,
        location: Location,
        weather: Weather,
        viewStyle: String?,
        subtitleData: String?,
        temperatureUnit: TemperatureUnit,
        speedUnit: SpeedUnit,
        pollenIndexSource: PollenIndexSource?,
    ): String? {
        return when (subtitleData) {
            "time" -> when (viewStyle) {
                "rectangle" -> location.getPlace(context) + " " +
                    (weather.base.refreshTime?.getFormattedTime(location, context, context.is12Hour) ?: "")

                "symmetry" -> Date().getWeek(location, context) + " " +
                    (weather.base.refreshTime?.getFormattedTime(location, context, context.is12Hour) ?: "")

                "tile", "vertical" -> location.getPlace(context) + " " + Date().getWeek(location, context) + " " +
                    (weather.base.refreshTime?.getFormattedTime(location, context, context.is12Hour) ?: "")

                else -> null
            }
            "aqi" -> weather.current?.airQuality?.let { airQuality ->
                if (airQuality.getIndex() != null && airQuality.getName(context) != null) {
                    airQuality.getName(context, null) + " (" + airQuality.getIndex(null) + ")"
                } else {
                    null
                }
            }
            "wind" -> weather.current?.wind?.getShortDescription(context, speedUnit)
            "lunar" -> when (viewStyle) {
                "rectangle" -> location.getPlace(context) + " " +
                    Date().getFormattedMediumDayAndMonthInAdditionalCalendar(location, context)

                "symmetry" -> Date().getWeek(location, context) + " " +
                    Date().getFormattedMediumDayAndMonthInAdditionalCalendar(location, context)

                "tile", "mini", "vertical" -> location.getPlace(context) + " " + Date().getWeek(location, context) +
                    " " + Date().getFormattedMediumDayAndMonthInAdditionalCalendar(location, context)

                else -> null
            }
            "feels_like" -> weather.current?.temperature?.feelsLikeTemperature?.let {
                context.getString(R.string.temperature_feels_like) + " " +
                    temperatureUnit.getValueText(context, it, 0)
            }
            else -> getCustomSubtitle(context, subtitleData, location, weather, pollenIndexSource)
        }
    }

    private fun getTitleSize(context: Context, viewStyle: String?): Float {
        return when (viewStyle) {
            "rectangle", "symmetry", "tile" ->
                context.resources.getDimensionPixelSize(R.dimen.widget_content_text_size).toFloat()
            "mini", "nano" -> context.resources.getDimensionPixelSize(R.dimen.widget_subtitle_text_size).toFloat()
            "pixel" -> context.resources.getDimensionPixelSize(R.dimen.widget_design_title_text_size).toFloat()
            "vertical" -> context.resources.getDimensionPixelSize(R.dimen.widget_current_weather_icon_size).toFloat()
            "oreo", "oreo_google_sans", "temp" ->
                context.resources.getDimensionPixelSize(R.dimen.widget_large_title_text_size).toFloat()
            else -> 0f
        }
    }

    private fun getSubtitleSize(context: Context, viewStyle: String?): Float {
        return when (viewStyle) {
            "rectangle", "symmetry", "tile", "vertical" ->
                context.resources.getDimensionPixelSize(R.dimen.widget_content_text_size).toFloat()
            "oreo", "oreo_google_sans" ->
                context.resources.getDimensionPixelSize(R.dimen.widget_large_title_text_size).toFloat()
            else -> 0f
        }
    }

    private fun getTimeSize(context: Context, viewStyle: String?): Float {
        return when (viewStyle) {
            "rectangle", "symmetry", "tile", "vertical", "mini" ->
                context.resources.getDimensionPixelSize(R.dimen.widget_time_text_size).toFloat()
            "pixel" -> context.resources.getDimensionPixelSize(R.dimen.widget_subtitle_text_size).toFloat()
            else -> 0f
        }
    }

    private fun setOnClickPendingIntent(
        context: Context,
        views: RemoteViews,
        location: Location,
        viewStyle: String?,
        subtitleData: String?,
    ) {
        // weather.
        views.setOnClickPendingIntent(
            R.id.widget_day_weather,
            getWeatherPendingIntent(context, location, Widgets.DAY_PENDING_INTENT_CODE_WEATHER)
        )

        // title.
        if (viewStyle == "oreo" || viewStyle == "oreo_google_sans") {
            views.setOnClickPendingIntent(
                R.id.widget_day_title,
                getCalendarPendingIntent(context, Widgets.DAY_PENDING_INTENT_CODE_CALENDAR)
            )
        }

        // time.
        if (viewStyle == "pixel" || subtitleData == "lunar") {
            views.setOnClickPendingIntent(
                R.id.widget_day_time,
                getCalendarPendingIntent(context, Widgets.DAY_PENDING_INTENT_CODE_CALENDAR)
            )
        }
    }
}
