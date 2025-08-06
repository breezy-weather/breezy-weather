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
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Weather
import org.breezyweather.R
import org.breezyweather.background.receiver.widget.WidgetClockDayVerticalProvider
import org.breezyweather.common.basic.models.options.NotificationTextColor
import org.breezyweather.common.basic.models.options.basic.UnitUtils
import org.breezyweather.common.basic.models.options.unit.SpeedUnit
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.extensions.getFormattedMediumDayAndMonthInAdditionalCalendar
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.getShortWeekdayDayMonth
import org.breezyweather.common.extensions.getWeek
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.spToPx
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
import kotlin.math.roundToInt

object ClockDayVerticalWidgetIMP : AbstractRemoteViewsPresenter() {

    fun updateWidgetView(
        context: Context,
        location: Location?,
        pollenIndexSource: PollenIndexSource?,
    ) {
        val config = getWidgetConfig(context, context.getString(R.string.sp_widget_clock_day_vertical_setting))
        val views = getRemoteViews(
            context,
            location,
            config.viewStyle,
            config.cardStyle,
            config.cardAlpha,
            config.textColor,
            config.textSize,
            config.hideSubtitle,
            config.subtitleData,
            config.clockFont,
            pollenIndexSource
        )
        AppWidgetManager.getInstance(context).updateAppWidget(
            ComponentName(context, WidgetClockDayVerticalProvider::class.java),
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
        clockFont: String?,
        pollenIndexSource: PollenIndexSource?,
    ): RemoteViews {
        val color = WidgetColor(context, cardStyle!!, textColor!!, location?.isDaylight ?: true)
        val settings = SettingsManager.getInstance(context)
        val temperatureUnit = settings.getTemperatureUnit(context)
        val speedUnit = settings.getSpeedUnit(context)
        val minimalIcon = settings.isWidgetUsingMonochromeIcons
        val views = buildWidgetViewDayPart(
            context,
            location,
            temperatureUnit,
            speedUnit,
            color,
            textSize,
            minimalIcon,
            clockFont,
            viewStyle,
            hideSubtitle,
            subtitleData,
            pollenIndexSource
        )
        if (color.showCard) {
            views.setImageViewResource(R.id.widget_clock_day_card, getCardBackgroundId(color))
            views.setInt(R.id.widget_clock_day_card, "setImageAlpha", (cardAlpha / 100.0 * 255).toInt())
        }
        location?.let { setOnClickPendingIntent(context, views, it, subtitleData) }
        return views
    }

    private fun buildWidgetViewDayPart(
        context: Context,
        location: Location?,
        temperatureUnit: TemperatureUnit,
        speedUnit: SpeedUnit,
        color: WidgetColor,
        textSize: Int,
        minimalIcon: Boolean,
        clockFont: String?,
        viewStyle: String?,
        hideSubtitle: Boolean,
        subtitleData: String?,
        pollenIndexSource: PollenIndexSource?,
    ): RemoteViews {
        val views = RemoteViews(
            context.packageName,
            when (viewStyle) {
                "rectangle" -> if (!color.showCard) {
                    R.layout.widget_clock_day_rectangle
                } else {
                    R.layout.widget_clock_day_rectangle_card
                }
                "tile" -> if (!color.showCard) {
                    R.layout.widget_clock_day_tile
                } else {
                    R.layout.widget_clock_day_tile_card
                }
                "mini" -> if (!color.showCard) {
                    R.layout.widget_clock_day_mini
                } else {
                    R.layout.widget_clock_day_mini_card
                }
                "vertical" -> if (!color.showCard) {
                    R.layout.widget_clock_day_vertical
                } else {
                    R.layout.widget_clock_day_vertical_card
                }
                "temp" -> if (!color.showCard) {
                    R.layout.widget_clock_day_temp
                } else {
                    R.layout.widget_clock_day_temp_card
                }
                else -> if (!color.showCard) {
                    R.layout.widget_clock_day_symmetry
                } else {
                    R.layout.widget_clock_day_symmetry_card
                }
            }
        )
        val weather = location?.weather ?: return views

        // Clock
        views.setString(
            R.id.widget_clock_day_clock_1_light,
            "setTimeZone",
            location.timeZone
        )
        views.setString(
            R.id.widget_clock_day_clock_2_light,
            "setTimeZone",
            location.timeZone
        )
        views.setString(
            R.id.widget_clock_day_clock_1_normal,
            "setTimeZone",
            location.timeZone
        )
        views.setString(
            R.id.widget_clock_day_clock_2_normal,
            "setTimeZone",
            location.timeZone
        )
        views.setString(
            R.id.widget_clock_day_clock_1_black,
            "setTimeZone",
            location.timeZone
        )
        views.setString(
            R.id.widget_clock_day_clock_2_black,
            "setTimeZone",
            location.timeZone
        )
        views.setString(
            R.id.widget_clock_day_clock_light,
            "setTimeZone",
            location.timeZone
        )
        views.setString(
            R.id.widget_clock_day_clock_normal,
            "setTimeZone",
            location.timeZone
        )
        views.setString(
            R.id.widget_clock_day_clock_black,
            "setTimeZone",
            location.timeZone
        )
        views.setString(
            R.id.widget_clock_day_clock_aa_light,
            "setTimeZone",
            location.timeZone
        )
        views.setString(
            R.id.widget_clock_day_clock_aa_normal,
            "setTimeZone",
            location.timeZone
        )
        views.setString(
            R.id.widget_clock_day_clock_aa_black,
            "setTimeZone",
            location.timeZone
        )

        // Apply correct timezone on analog clock (only available on Android >= 12)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            views.setString(
                R.id.widget_clock_day_clock_analog_auto,
                "setTimeZone",
                location.timeZone
            )
            views.setString(
                R.id.widget_clock_day_clock_analog_light,
                "setTimeZone",
                location.timeZone
            )
            views.setString(
                R.id.widget_clock_day_clock_analog_dark,
                "setTimeZone",
                location.timeZone
            )
        }

        if (viewStyle == "temp") {
            // Date
            val dateFormat = getShortWeekdayDayMonth(context)
            views.setString(
                R.id.widget_clock_day_date,
                "setTimeZone",
                location.timeZone
            )
            views.setCharSequence(
                R.id.widget_clock_day_date,
                "setFormat12Hour",
                dateFormat
            )
            views.setCharSequence(
                R.id.widget_clock_day_date,
                "setFormat24Hour",
                dateFormat
            )
        }

        val provider = ResourcesProviderFactory.newInstance
        weather.current?.weatherCode?.let {
            views.setViewVisibility(R.id.widget_clock_day_icon, View.VISIBLE)
            views.setImageViewUri(
                R.id.widget_clock_day_icon,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    it,
                    location.isDaylight,
                    minimalIcon,
                    color.minimalIconColor
                )
            )
        } ?: views.setViewVisibility(R.id.widget_clock_day_icon, View.INVISIBLE)

        views.apply {
            setTextViewText(
                R.id.widget_clock_day_title,
                getTitleText(context, location, viewStyle, temperatureUnit)
            )
            setTextViewText(
                R.id.widget_clock_day_subtitle,
                getSubtitleText(context, weather, viewStyle, temperatureUnit)
            )
            setTextViewText(
                R.id.widget_clock_day_time,
                getTimeText(context, location, viewStyle, subtitleData, temperatureUnit, speedUnit, pollenIndexSource)
            )
        }

        if (color.textColor != Color.TRANSPARENT) {
            views.apply {
                setTextColor(R.id.widget_clock_day_clock_light, color.textColor)
                setTextColor(R.id.widget_clock_day_clock_normal, color.textColor)
                setTextColor(R.id.widget_clock_day_clock_black, color.textColor)
                setTextColor(R.id.widget_clock_day_clock_aa_light, color.textColor)
                setTextColor(R.id.widget_clock_day_clock_aa_normal, color.textColor)
                setTextColor(R.id.widget_clock_day_clock_aa_black, color.textColor)
                setTextColor(R.id.widget_clock_day_clock_1_light, color.textColor)
                setTextColor(R.id.widget_clock_day_clock_1_normal, color.textColor)
                setTextColor(R.id.widget_clock_day_clock_1_black, color.textColor)
                setTextColor(R.id.widget_clock_day_clock_2_light, color.textColor)
                setTextColor(R.id.widget_clock_day_clock_2_normal, color.textColor)
                setTextColor(R.id.widget_clock_day_clock_2_black, color.textColor)
                setTextColor(R.id.widget_clock_day_date, color.textColor)
                setTextColor(R.id.widget_clock_day_title, color.textColor)
                setTextColor(R.id.widget_clock_day_subtitle, color.textColor)
                setTextColor(R.id.widget_clock_day_time, color.textColor)
            }
        }
        if (textSize != 100) {
            val clockSize = context.resources.getDimensionPixelSize(R.dimen.widget_current_weather_icon_size).toFloat()
                .times(textSize)
                .div(100f)
            val clockAASize = context.resources.getDimensionPixelSize(R.dimen.widget_aa_text_size).toFloat()
                .times(textSize)
                .div(100f)
            val verticalClockSize = context.spToPx(64).times(textSize).div(100f)
            views.apply {
                setTextViewTextSize(R.id.widget_clock_day_clock_light, TypedValue.COMPLEX_UNIT_PX, clockSize)
                setTextViewTextSize(R.id.widget_clock_day_clock_normal, TypedValue.COMPLEX_UNIT_PX, clockSize)
                setTextViewTextSize(R.id.widget_clock_day_clock_black, TypedValue.COMPLEX_UNIT_PX, clockSize)
                setTextViewTextSize(R.id.widget_clock_day_clock_aa_light, TypedValue.COMPLEX_UNIT_PX, clockAASize)
                setTextViewTextSize(R.id.widget_clock_day_clock_aa_normal, TypedValue.COMPLEX_UNIT_PX, clockAASize)
                setTextViewTextSize(R.id.widget_clock_day_clock_aa_black, TypedValue.COMPLEX_UNIT_PX, clockAASize)
                setTextViewTextSize(R.id.widget_clock_day_clock_1_light, TypedValue.COMPLEX_UNIT_PX, verticalClockSize)
                setTextViewTextSize(R.id.widget_clock_day_clock_1_normal, TypedValue.COMPLEX_UNIT_PX, verticalClockSize)
                setTextViewTextSize(R.id.widget_clock_day_clock_1_black, TypedValue.COMPLEX_UNIT_PX, verticalClockSize)
                setTextViewTextSize(R.id.widget_clock_day_clock_2_light, TypedValue.COMPLEX_UNIT_PX, verticalClockSize)
                setTextViewTextSize(R.id.widget_clock_day_clock_2_normal, TypedValue.COMPLEX_UNIT_PX, verticalClockSize)
                setTextViewTextSize(R.id.widget_clock_day_clock_2_black, TypedValue.COMPLEX_UNIT_PX, verticalClockSize)
                setTextViewTextSize(
                    R.id.widget_clock_day_date,
                    TypedValue.COMPLEX_UNIT_PX,
                    getTitleSize(context, viewStyle).times(textSize).div(100f)
                )
                setTextViewTextSize(
                    R.id.widget_clock_day_title,
                    TypedValue.COMPLEX_UNIT_PX,
                    getTitleSize(context, viewStyle).times(textSize).div(100f)
                )
                setTextViewTextSize(
                    R.id.widget_clock_day_subtitle,
                    TypedValue.COMPLEX_UNIT_PX,
                    getSubtitleSize(context, viewStyle).times(textSize).div(100f)
                )
                setTextViewTextSize(
                    R.id.widget_clock_day_time,
                    TypedValue.COMPLEX_UNIT_PX,
                    getTimeSize(context, viewStyle).times(textSize).div(100f)
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setInt(
                        R.id.widget_clock_day_title,
                        "setLineHeight",
                        getTitleSize(context, viewStyle).times(textSize).div(100f).roundToInt()
                    )
                    setInt(
                        R.id.widget_clock_day_subtitle,
                        "setLineHeight",
                        getSubtitleSize(context, viewStyle).times(textSize).div(100f).roundToInt()
                    )
                }
            }
        }
        views.setViewVisibility(R.id.widget_clock_day_time, if (hideSubtitle) View.GONE else View.VISIBLE)
        when (clockFont) {
            "normal" -> {
                views.apply {
                    setViewVisibility(R.id.widget_clock_day_clock_lightContainer, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_clock_normalContainer, View.VISIBLE)
                    setViewVisibility(R.id.widget_clock_day_clock_blackContainer, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_clock_analogContainer_auto, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_clock_analogContainer_light, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_clock_analogContainer_dark, View.GONE)
                }
            }
            "black" -> {
                views.apply {
                    setViewVisibility(R.id.widget_clock_day_clock_lightContainer, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_clock_normalContainer, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_clock_blackContainer, View.VISIBLE)
                    setViewVisibility(R.id.widget_clock_day_clock_analogContainer_auto, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_clock_analogContainer_light, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_clock_analogContainer_dark, View.GONE)
                }
            }
            "analog" -> {
                views.apply {
                    setViewVisibility(R.id.widget_clock_day_clock_lightContainer, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_clock_normalContainer, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_clock_blackContainer, View.GONE)
                    setViewVisibility(
                        R.id.widget_clock_day_clock_analogContainer_auto,
                        if (color.backgroundType == WidgetColor.WidgetBackgroundType.AUTO) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    )
                    setViewVisibility(
                        R.id.widget_clock_day_clock_analogContainer_light,
                        if (color.backgroundType == WidgetColor.WidgetBackgroundType.AUTO) {
                            View.GONE
                        } else if (color.textType == NotificationTextColor.DARK) {
                            View.GONE
                        } else {
                            View.VISIBLE
                        }
                    )
                    setViewVisibility(
                        R.id.widget_clock_day_clock_analogContainer_dark,
                        if (color.backgroundType == WidgetColor.WidgetBackgroundType.AUTO) {
                            View.GONE
                        } else if (color.textType == NotificationTextColor.DARK) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    )
                }
            }
            else -> {
                views.apply {
                    setViewVisibility(R.id.widget_clock_day_clock_lightContainer, View.VISIBLE)
                    setViewVisibility(R.id.widget_clock_day_clock_normalContainer, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_clock_blackContainer, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_clock_analogContainer_auto, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_clock_analogContainer_light, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_clock_analogContainer_dark, View.GONE)
                }
            }
        }
        return views
    }

    fun isInUse(context: Context): Boolean {
        val widgetIds = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, WidgetClockDayVerticalProvider::class.java))
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
                        .append(unit.formatMeasure(context, it, 0))
                }
                stringBuilder.toString()
            }
            "vertical", "tile" -> weather.current?.let { current ->
                val stringBuilder = StringBuilder()
                if (!current.weatherText.isNullOrEmpty()) {
                    stringBuilder.append(current.weatherText)
                }
                current.temperature?.temperature?.let {
                    if (stringBuilder.isNotEmpty()) {
                        stringBuilder.append(" ")
                    }
                    stringBuilder.append(unit.formatMeasure(context, it, 0))
                }
                stringBuilder.toString()
            }
            "mini" -> weather.current?.weatherText
            "temp" -> weather.current?.temperature?.temperature?.let {
                unit.formatMeasureShort(context, it)
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
            "symmetry" -> weather.current?.let { current ->
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
            "tile", "temp" -> weather.today?.getTrendTemperature(context, unit)
            "mini" -> weather.current?.temperature?.temperature?.let {
                unit.formatMeasure(context, it, 0)
            }
            else -> null
        }
    }

    private fun getTimeText(
        context: Context,
        location: Location,
        viewStyle: String?,
        subtitleData: String?,
        temperatureUnit: TemperatureUnit,
        speedUnit: SpeedUnit,
        pollenIndexSource: PollenIndexSource?,
    ): String? {
        val weather = location.weather ?: return null
        return when (subtitleData) {
            "time" -> when (viewStyle) {
                "rectangle" -> location.getPlace(context) +
                    " " +
                    (weather.base.refreshTime?.getFormattedTime(location, context, context.is12Hour) ?: "")

                "symmetry" -> Date().getWeek(location, context) +
                    " " +
                    (weather.base.refreshTime?.getFormattedTime(location, context, context.is12Hour) ?: "")

                "tile", "vertical" -> location.getPlace(context) +
                    " " +
                    Date().getWeek(location, context) +
                    " " +
                    (weather.base.refreshTime?.getFormattedTime(location, context, context.is12Hour) ?: "")

                else -> null
            }
            "aqi" -> weather.current?.airQuality?.let { airQuality ->
                if (airQuality.getIndex() != null && airQuality.getName(context) != null) {
                    context.getString(
                        R.string.parenthesis,
                        UnitUtils.formatInt(context, airQuality.getIndex()!!),
                        airQuality.getName(context)
                    )
                } else {
                    null
                }
            }
            "wind" -> weather.current?.wind?.getShortDescription(context, speedUnit)
            "lunar" -> when (viewStyle) {
                "rectangle" -> location.getPlace(context) +
                    " " +
                    Date().getFormattedMediumDayAndMonthInAdditionalCalendar(location, context)

                "symmetry" -> Date().getWeek(location, context) +
                    " " +
                    Date().getFormattedMediumDayAndMonthInAdditionalCalendar(location, context)

                "tile", "vertical" -> location.getPlace(context) +
                    " " +
                    Date().getWeek(location, context) +
                    " " +
                    Date().getFormattedMediumDayAndMonthInAdditionalCalendar(location, context)

                else -> null
            }
            "feels_like" -> weather.current?.temperature?.feelsLikeTemperature?.let {
                context.getString(R.string.temperature_feels_like) +
                    " " +
                    temperatureUnit.formatMeasure(context, it, 0)
            }
            else -> getCustomSubtitle(context, subtitleData, location, weather, pollenIndexSource)
        }
    }

    private fun getTitleSize(context: Context, viewStyle: String?): Float {
        return when (viewStyle) {
            "rectangle", "symmetry", "tile", "mini", "vertical" ->
                context.resources.getDimensionPixelSize(R.dimen.widget_content_text_size).toFloat()
            "temp" -> context.resources.getDimensionPixelSize(R.dimen.widget_title_text_size).toFloat()
            else -> 0f
        }
    }

    private fun getSubtitleSize(context: Context, viewStyle: String?): Float {
        return when (viewStyle) {
            "rectangle", "symmetry", "tile", "mini" ->
                context.resources.getDimensionPixelSize(R.dimen.widget_content_text_size).toFloat()
            "temp" -> context.resources.getDimensionPixelSize(R.dimen.widget_subtitle_text_size).toFloat()
            else -> 0f
        }
    }

    private fun getTimeSize(context: Context, viewStyle: String?): Float {
        return when (viewStyle) {
            "rectangle", "symmetry", "tile", "vertical", "mini" ->
                context.resources.getDimensionPixelSize(R.dimen.widget_time_text_size).toFloat()
            else -> 0f
        }
    }

    private fun setOnClickPendingIntent(
        context: Context,
        views: RemoteViews,
        location: Location,
        subtitleData: String?,
    ) {
        // weather.
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_weather,
            getWeatherPendingIntent(context, location, Widgets.CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_WEATHER)
        )

        // clock.
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_clock_light,
            getAlarmPendingIntent(context, Widgets.CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_LIGHT)
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_clock_normal,
            getAlarmPendingIntent(context, Widgets.CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_NORMAL)
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_clock_black,
            getAlarmPendingIntent(context, Widgets.CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_BLACK)
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_clock_1_light,
            getAlarmPendingIntent(context, Widgets.CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_LIGHT)
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_clock_1_normal,
            getAlarmPendingIntent(context, Widgets.CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_NORMAL)
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_clock_1_black,
            getAlarmPendingIntent(context, Widgets.CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_BLACK)
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_clock_2_light,
            getAlarmPendingIntent(context, Widgets.CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_LIGHT)
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_clock_2_normal,
            getAlarmPendingIntent(context, Widgets.CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_NORMAL)
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_clock_2_black,
            getAlarmPendingIntent(context, Widgets.CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_BLACK)
        )

        // time.
        if (subtitleData == "lunar") {
            views.setOnClickPendingIntent(
                R.id.widget_clock_day_time,
                getCalendarPendingIntent(context, Widgets.DAY_PENDING_INTENT_CODE_CALENDAR)
            )
        }
    }
}
