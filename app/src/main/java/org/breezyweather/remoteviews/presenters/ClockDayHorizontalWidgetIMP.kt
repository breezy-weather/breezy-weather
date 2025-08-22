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
import org.breezyweather.R
import org.breezyweather.background.receiver.widget.WidgetClockDayHorizontalProvider
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.common.extensions.getFormattedMediumDayAndMonthInAdditionalCalendar
import org.breezyweather.common.extensions.getShortWeekdayDayMonth
import org.breezyweather.common.options.appearance.CalendarHelper
import org.breezyweather.domain.location.model.getPlace
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.remoteviews.Widgets
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory
import org.breezyweather.unit.formatting.UnitWidth
import java.util.Date
import kotlin.math.roundToInt

object ClockDayHorizontalWidgetIMP : AbstractRemoteViewsPresenter() {

    fun updateWidgetView(
        context: Context,
        location: Location?,
    ) {
        val config = getWidgetConfig(context, context.getString(R.string.sp_widget_clock_day_horizontal_setting))
        val views = getRemoteViews(
            context,
            location,
            config.cardStyle,
            config.cardAlpha,
            config.textColor,
            config.textSize,
            config.clockFont,
            config.hideAlternateCalendar
        )
        AppWidgetManager.getInstance(context).updateAppWidget(
            ComponentName(context, WidgetClockDayHorizontalProvider::class.java),
            views
        )
    }

    fun getRemoteViews(
        context: Context,
        location: Location?,
        cardStyle: String?,
        cardAlpha: Int,
        textColor: String?,
        textSize: Int,
        clockFont: String?,
        hideAlternateCalendar: Boolean,
    ): RemoteViews {
        val color = WidgetColor(context, cardStyle!!, textColor!!, location?.isDaylight ?: true)
        val views = RemoteViews(
            context.packageName,
            if (!color.showCard) R.layout.widget_clock_day_horizontal else R.layout.widget_clock_day_horizontal_card
        )
        val weather = location?.weather ?: return views
        val provider = ResourcesProviderFactory.newInstance
        val dayTime = location.isDaylight
        val settings = SettingsManager.getInstance(context)
        val temperatureUnit = settings.getTemperatureUnit(context)
        val minimalIcon = settings.isWidgetUsingMonochromeIcons

        // Clock
        views.setString(
            R.id.widget_clock_day_clock_light,
            "setTimeZone",
            location.timeZone.id
        )
        views.setString(
            R.id.widget_clock_day_clock_normal,
            "setTimeZone",
            location.timeZone.id
        )
        views.setString(
            R.id.widget_clock_day_clock_black,
            "setTimeZone",
            location.timeZone.id
        )
        views.setString(
            R.id.widget_clock_day_clock_aa_light,
            "setTimeZone",
            location.timeZone.id
        )
        views.setString(
            R.id.widget_clock_day_clock_aa_normal,
            "setTimeZone",
            location.timeZone.id
        )
        views.setString(
            R.id.widget_clock_day_clock_aa_black,
            "setTimeZone",
            location.timeZone.id
        )

        // Date
        val dateFormat = getShortWeekdayDayMonth(context)
        views.setString(
            R.id.widget_clock_day_title,
            "setTimeZone",
            location.timeZone.id
        )
        views.setCharSequence(
            R.id.widget_clock_day_title,
            "setFormat12Hour",
            dateFormat
        )
        views.setCharSequence(
            R.id.widget_clock_day_title,
            "setFormat24Hour",
            dateFormat
        )

        weather.current?.weatherCode?.let {
            views.setViewVisibility(R.id.widget_clock_day_icon, View.VISIBLE)
            views.setImageViewUri(
                R.id.widget_clock_day_icon,
                ResourceHelper.getWidgetNotificationIconUri(provider, it, dayTime, minimalIcon, color.minimalIconColor)
            )
        } ?: views.setViewVisibility(R.id.widget_clock_day_icon, View.INVISIBLE)
        views.setTextViewText(
            R.id.widget_clock_day_alternate_calendar,
            if (CalendarHelper.getAlternateCalendarSetting(context) != null && !hideAlternateCalendar) {
                " – " + Date().getFormattedMediumDayAndMonthInAdditionalCalendar(location, context)
            } else {
                ""
            }
        )
        val builder = StringBuilder()
        builder.append(location.getPlace(context))
        weather.current?.temperature?.temperature?.let {
            builder.append(" ").append(
                it.formatMeasure(context, temperatureUnit, unitWidth = UnitWidth.NARROW)
            )
        }
        views.setTextViewText(R.id.widget_clock_day_subtitle, builder.toString())
        if (color.textColor != Color.TRANSPARENT) {
            views.apply {
                setTextColor(R.id.widget_clock_day_clock_light, color.textColor)
                setTextColor(R.id.widget_clock_day_clock_normal, color.textColor)
                setTextColor(R.id.widget_clock_day_clock_black, color.textColor)
                setTextColor(R.id.widget_clock_day_clock_aa_light, color.textColor)
                setTextColor(R.id.widget_clock_day_clock_aa_normal, color.textColor)
                setTextColor(R.id.widget_clock_day_clock_aa_black, color.textColor)
                setTextColor(R.id.widget_clock_day_title, color.textColor)
                setTextColor(R.id.widget_clock_day_alternate_calendar, color.textColor)
                setTextColor(R.id.widget_clock_day_subtitle, color.textColor)
            }
        }
        if (textSize != 100) {
            val clockSize = context.resources.getDimensionPixelSize(R.dimen.widget_current_weather_icon_size).toFloat()
                .times(textSize)
                .div(100f)
            val clockAASize = context.resources.getDimensionPixelSize(R.dimen.widget_aa_text_size).toFloat()
                .times(textSize)
                .div(100f)
            val contentSize = context.resources.getDimensionPixelSize(R.dimen.widget_content_text_size).toFloat()
                .times(textSize)
                .div(100f)
            views.apply {
                setTextViewTextSize(R.id.widget_clock_day_clock_light, TypedValue.COMPLEX_UNIT_PX, clockSize)
                setTextViewTextSize(R.id.widget_clock_day_clock_normal, TypedValue.COMPLEX_UNIT_PX, clockSize)
                setTextViewTextSize(R.id.widget_clock_day_clock_black, TypedValue.COMPLEX_UNIT_PX, clockSize)
                setTextViewTextSize(R.id.widget_clock_day_clock_aa_light, TypedValue.COMPLEX_UNIT_PX, clockAASize)
                setTextViewTextSize(R.id.widget_clock_day_clock_aa_normal, TypedValue.COMPLEX_UNIT_PX, clockAASize)
                setTextViewTextSize(R.id.widget_clock_day_clock_aa_black, TypedValue.COMPLEX_UNIT_PX, clockAASize)
                setTextViewTextSize(R.id.widget_clock_day_title, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_clock_day_alternate_calendar, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_clock_day_subtitle, TypedValue.COMPLEX_UNIT_PX, contentSize)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setInt(R.id.widget_clock_day_subtitle, "setLineHeight", contentSize.roundToInt())
                }
            }
        }
        if (color.showCard) {
            views.setImageViewResource(R.id.widget_clock_day_card, getCardBackgroundId(color))
            views.setInt(R.id.widget_clock_day_card, "setImageAlpha", (cardAlpha / 100.0 * 255).toInt())
        }
        when (clockFont) {
            "normal" -> {
                views.apply {
                    setViewVisibility(R.id.widget_clock_day_clock_lightContainer, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_clock_normalContainer, View.VISIBLE)
                    setViewVisibility(R.id.widget_clock_day_clock_blackContainer, View.GONE)
                }
            }
            "black" -> {
                views.apply {
                    setViewVisibility(R.id.widget_clock_day_clock_lightContainer, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_clock_normalContainer, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_clock_blackContainer, View.VISIBLE)
                }
            }
            else -> {
                views.apply {
                    setViewVisibility(R.id.widget_clock_day_clock_lightContainer, View.VISIBLE)
                    setViewVisibility(R.id.widget_clock_day_clock_normalContainer, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_clock_blackContainer, View.GONE)
                }
            }
        }
        setOnClickPendingIntent(context, views, location)
        return views
    }

    fun isInUse(context: Context): Boolean {
        val widgetIds = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, WidgetClockDayHorizontalProvider::class.java))
        return widgetIds != null && widgetIds.isNotEmpty()
    }

    private fun setOnClickPendingIntent(context: Context, views: RemoteViews, location: Location) {
        // weather.
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_weather,
            getWeatherPendingIntent(context, location, Widgets.CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_WEATHER)
        )

        // clock.
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_clock_light,
            getAlarmPendingIntent(context, Widgets.CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CLOCK_LIGHT)
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_clock_normal,
            getAlarmPendingIntent(context, Widgets.CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CLOCK_NORMAL)
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_clock_black,
            getAlarmPendingIntent(context, Widgets.CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CLOCK_BLACK)
        )

        // title.
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_title,
            getCalendarPendingIntent(context, Widgets.CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CALENDAR)
        )
    }
}
