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
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Weather
import org.breezyweather.R
import org.breezyweather.background.receiver.widget.WidgetTextProvider
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.common.extensions.getFormattedMediumDayAndMonthInAdditionalCalendar
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.getLongWeekdayDayMonth
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.spToPx
import org.breezyweather.common.source.PollenIndexSource
import org.breezyweather.common.utils.UnitUtils
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.domain.weather.model.getIndex
import org.breezyweather.domain.weather.model.getName
import org.breezyweather.domain.weather.model.getShortDescription
import org.breezyweather.remoteviews.Widgets
import org.breezyweather.unit.formatting.UnitWidth
import java.util.Date
import kotlin.math.roundToInt

object TextWidgetIMP : AbstractRemoteViewsPresenter() {

    fun updateWidgetView(
        context: Context,
        location: Location?,
        pollenIndexSource: PollenIndexSource?,
    ) {
        val config = getWidgetConfig(context, context.getString(R.string.sp_widget_text_setting))
        val views = getRemoteViews(
            context,
            location,
            config.textColor,
            config.textSize,
            config.alignEnd,
            config.hideSubtitle,
            config.subtitleData,
            pollenIndexSource
        )
        AppWidgetManager.getInstance(context).updateAppWidget(
            ComponentName(context, WidgetTextProvider::class.java),
            views
        )
    }

    fun getRemoteViews(
        context: Context,
        location: Location?,
        textColor: String?,
        textSize: Int,
        alignEnd: Boolean,
        hideHeader: Boolean,
        subtitleData: String?,
        pollenIndexSource: PollenIndexSource?,
    ): RemoteViews {
        val views = RemoteViews(
            context.packageName,
            if (alignEnd) R.layout.widget_text_end else R.layout.widget_text
        )
        val weather = location?.weather ?: return views

        val color = WidgetColor(
            context,
            "none",
            textColor!!,
            location.isDaylight
        )

        views.apply {
            if (hideHeader) {
                setViewVisibility(R.id.widget_text_date, View.GONE)
                setViewVisibility(R.id.widget_text_weather, View.GONE)
                setViewVisibility(R.id.widget_text_temperature, View.GONE)
            } else {
                val dateFormat = getLongWeekdayDayMonth(context)
                views.setString(
                    R.id.widget_text_date,
                    "setTimeZone",
                    location.timeZone.id
                )
                views.setCharSequence(
                    R.id.widget_text_date,
                    "setFormat12Hour",
                    dateFormat
                )
                views.setCharSequence(
                    R.id.widget_text_date,
                    "setFormat24Hour",
                    dateFormat
                )
                setViewVisibility(R.id.widget_text_date, View.VISIBLE)
                setViewVisibility(R.id.widget_text_weather, View.VISIBLE)
                setViewVisibility(R.id.widget_text_temperature, View.VISIBLE)
                setTextViewText(
                    R.id.widget_text_weather,
                    weather.current?.weatherText ?: context.getString(R.string.null_data_text)
                )
                setTextViewText(
                    R.id.widget_text_temperature,
                    weather.current?.temperature?.temperature?.formatMeasure(
                        context,
                        valueWidth = UnitWidth.NARROW,
                        unitWidth = UnitWidth.NARROW
                    )
                )
                setTextColor(R.id.widget_text_date, color.textColor)
                setTextColor(R.id.widget_text_weather, color.textColor)
                setTextColor(R.id.widget_text_temperature, color.textColor)
            }
            setTextViewText(
                R.id.widget_text_subtitle,
                getTimeText(
                    context,
                    location,
                    weather,
                    subtitleData,
                    pollenIndexSource
                )
            )
            setTextColor(R.id.widget_text_subtitle, color.textColor)
        }
        if (textSize != 100) {
            val contentSize = context.resources.getDimensionPixelSize(R.dimen.widget_content_text_size).toFloat()
                .times(textSize)
                .div(100f)
            val temperatureSize = context.spToPx(48).times(textSize).div(100f)
            views.apply {
                if (!hideHeader) {
                    setTextViewTextSize(R.id.widget_text_date, TypedValue.COMPLEX_UNIT_PX, contentSize)
                    setTextViewTextSize(R.id.widget_text_weather, TypedValue.COMPLEX_UNIT_PX, contentSize)
                    setTextViewTextSize(R.id.widget_text_temperature, TypedValue.COMPLEX_UNIT_PX, temperatureSize)
                }
                setTextViewTextSize(R.id.widget_text_subtitle, TypedValue.COMPLEX_UNIT_PX, contentSize)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setInt(R.id.widget_text_subtitle, "setLineHeight", contentSize.roundToInt())
                }
            }
        }
        setOnClickPendingIntent(context, views, location)
        return views
    }

    fun isInUse(context: Context): Boolean {
        val widgetIds = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, WidgetTextProvider::class.java))
        return widgetIds != null && widgetIds.isNotEmpty()
    }

    private fun getTimeText(
        context: Context,
        location: Location,
        weather: Weather,
        subtitleData: String?,
        pollenIndexSource: PollenIndexSource?,
    ): String? {
        return when (subtitleData) {
            "time" -> weather.base.refreshTime?.getFormattedTime(location, context, context.is12Hour)
            "aqi" -> weather.current?.airQuality?.let { airQuality ->
                if (airQuality.getIndex() != null && airQuality.getName(context) != null) {
                    context.getString(
                        R.string.parenthesis,
                        UnitUtils.formatInt(context, airQuality.getIndex()!!),
                        airQuality.getName(context, null)
                    )
                } else {
                    null
                }
            }
            "wind" -> weather.current?.wind?.getShortDescription(context)
            "lunar" -> Date().getFormattedMediumDayAndMonthInAdditionalCalendar(location, context)
            "feels_like" -> weather.current?.temperature?.feelsLikeTemperature?.let {
                context.getString(
                    R.string.temperature_feels_like_with_unit,
                    it.formatMeasure(context, unitWidth = UnitWidth.NARROW)
                )
            }
            else -> getCustomSubtitle(context, subtitleData, location, weather, pollenIndexSource)
        }
    }

    private fun setOnClickPendingIntent(
        context: Context,
        views: RemoteViews,
        location: Location,
    ) {
        // headerContainer.
        views.setOnClickPendingIntent(
            R.id.widget_text_container,
            getWeatherPendingIntent(context, location, Widgets.TEXT_PENDING_INTENT_CODE_WEATHER)
        )

        // date.
        views.setOnClickPendingIntent(
            R.id.widget_text_date,
            getCalendarPendingIntent(context, Widgets.TEXT_PENDING_INTENT_CODE_CALENDAR)
        )
    }
}
