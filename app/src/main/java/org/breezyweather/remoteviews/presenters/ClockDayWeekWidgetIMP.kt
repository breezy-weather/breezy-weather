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
import org.breezyweather.R
import org.breezyweather.background.receiver.widget.WidgetClockDayWeekProvider
import org.breezyweather.common.basic.models.options.appearance.CalendarHelper
import org.breezyweather.common.extensions.getFormattedMediumDayAndMonthInAdditionalCalendar
import org.breezyweather.common.extensions.getShortWeekdayDayMonth
import org.breezyweather.domain.location.model.getPlace
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.getTrendTemperature
import org.breezyweather.domain.weather.model.getWeek
import org.breezyweather.domain.weather.model.isToday
import org.breezyweather.remoteviews.Widgets
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory
import java.util.Date

object ClockDayWeekWidgetIMP : AbstractRemoteViewsPresenter() {

    fun updateWidgetView(
        context: Context,
        location: Location?,
    ) {
        val config = getWidgetConfig(context, context.getString(R.string.sp_widget_clock_day_week_setting))
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
            ComponentName(context, WidgetClockDayWeekProvider::class.java),
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
            if (!color.showCard) R.layout.widget_clock_day_week else R.layout.widget_clock_day_week_card
        )
        val weather = location?.weather ?: return views
        val provider = ResourcesProviderFactory.newInstance
        val dayTime = location.isDaylight
        val settings = SettingsManager.getInstance(context)
        val temperatureUnit = settings.temperatureUnit
        val weekIconMode = settings.widgetWeekIconMode
        val minimalIcon = settings.isWidgetUsingMonochromeIcons

        // Clock
        views.setString(
            R.id.widget_clock_day_week_clock_light,
            "setTimeZone",
            location.timeZone
        )
        views.setString(
            R.id.widget_clock_day_week_clock_normal,
            "setTimeZone",
            location.timeZone
        )
        views.setString(
            R.id.widget_clock_day_week_clock_black,
            "setTimeZone",
            location.timeZone
        )
        views.setString(
            R.id.widget_clock_day_week_clock_aa_light,
            "setTimeZone",
            location.timeZone
        )
        views.setString(
            R.id.widget_clock_day_week_clock_aa_normal,
            "setTimeZone",
            location.timeZone
        )
        views.setString(
            R.id.widget_clock_day_week_clock_aa_black,
            "setTimeZone",
            location.timeZone
        )

        // Date
        val dateFormat = getShortWeekdayDayMonth(context)
        views.setString(
            R.id.widget_clock_day_week_title,
            "setTimeZone",
            location.timeZone
        )
        views.setCharSequence(
            R.id.widget_clock_day_week_title,
            "setFormat12Hour",
            dateFormat
        )
        views.setCharSequence(
            R.id.widget_clock_day_week_title,
            "setFormat24Hour",
            dateFormat
        )

        weather.current?.weatherCode?.let {
            views.setViewVisibility(R.id.widget_clock_day_week_icon, View.VISIBLE)
            views.setImageViewUri(
                R.id.widget_clock_day_week_icon,
                ResourceHelper.getWidgetNotificationIconUri(provider, it, dayTime, minimalIcon, color.minimalIconColor)
            )
        } ?: views.setViewVisibility(R.id.widget_clock_day_week_icon, View.INVISIBLE)
        views.setTextViewText(
            R.id.widget_clock_day_week_alternate_calendar,
            if (CalendarHelper.getAlternateCalendarSetting(context) != null && !hideAlternateCalendar) {
                " - ${Date().getFormattedMediumDayAndMonthInAdditionalCalendar(location, context)}"
            } else {
                ""
            }
        )
        val builder = StringBuilder()
        builder.append(location.getPlace(context))
        weather.current?.temperature?.temperature?.let {
            builder.append(" ").append(
                temperatureUnit.getValueText(context, it, 0)
            )
        }
        views.setTextViewText(R.id.widget_clock_day_week_subtitle, builder.toString())

        val weekIconDaytime = isWeekIconDaytime(weekIconMode, dayTime)
        val dailyIds = arrayOf(
            arrayOf(
                R.id.widget_clock_day_week_week_1,
                R.id.widget_clock_day_week_temp_1,
                R.id.widget_clock_day_week_icon_1
            ),
            arrayOf(
                R.id.widget_clock_day_week_week_2,
                R.id.widget_clock_day_week_temp_2,
                R.id.widget_clock_day_week_icon_2
            ),
            arrayOf(
                R.id.widget_clock_day_week_week_3,
                R.id.widget_clock_day_week_temp_3,
                R.id.widget_clock_day_week_icon_3
            ),
            arrayOf(
                R.id.widget_clock_day_week_week_4,
                R.id.widget_clock_day_week_temp_4,
                R.id.widget_clock_day_week_icon_4
            ),
            arrayOf(
                R.id.widget_clock_day_week_week_5,
                R.id.widget_clock_day_week_temp_5,
                R.id.widget_clock_day_week_icon_5
            )
        )
        dailyIds.forEachIndexed { i, dailyId ->
            weather.dailyForecastStartingToday.getOrNull(i)?.let {
                views.setTextViewText(
                    dailyId[0],
                    if (it.isToday(location)) {
                        context.getString(R.string.short_today)
                    } else {
                        it.getWeek(location, context)
                    }
                )
            } ?: views.setTextViewText(dailyId[0], null)
            views.setTextViewText(
                dailyId[1],
                weather.dailyForecastStartingToday.getOrNull(i)?.getTrendTemperature(
                    context,
                    temperatureUnit
                )
            )
            if (weekIconDaytime) {
                weather.dailyForecastStartingToday.getOrNull(i)?.day?.weatherCode?.let {
                    views.setViewVisibility(dailyId[2], View.VISIBLE)
                    views.setImageViewUri(
                        dailyId[2],
                        ResourceHelper.getWidgetNotificationIconUri(
                            provider,
                            it,
                            dayTime = true,
                            minimalIcon,
                            color.minimalIconColor
                        )
                    )
                } ?: views.setViewVisibility(dailyId[2], View.INVISIBLE)
            } else {
                weather.dailyForecastStartingToday.getOrNull(i)?.night?.weatherCode?.let {
                    views.setViewVisibility(dailyId[2], View.VISIBLE)
                    views.setImageViewUri(
                        dailyId[2],
                        ResourceHelper.getWidgetNotificationIconUri(
                            provider,
                            it,
                            dayTime = false,
                            minimalIcon,
                            color.minimalIconColor
                        )
                    )
                } ?: views.setViewVisibility(dailyId[2], View.INVISIBLE)
            }
        }

        if (color.textColor != Color.TRANSPARENT) {
            views.apply {
                setTextColor(R.id.widget_clock_day_week_clock_light, color.textColor)
                setTextColor(R.id.widget_clock_day_week_clock_normal, color.textColor)
                setTextColor(R.id.widget_clock_day_week_clock_black, color.textColor)
                setTextColor(R.id.widget_clock_day_week_clock_aa_light, color.textColor)
                setTextColor(R.id.widget_clock_day_week_clock_aa_normal, color.textColor)
                setTextColor(R.id.widget_clock_day_week_clock_aa_black, color.textColor)
                setTextColor(R.id.widget_clock_day_week_title, color.textColor)
                setTextColor(R.id.widget_clock_day_week_alternate_calendar, color.textColor)
                setTextColor(R.id.widget_clock_day_week_subtitle, color.textColor)
                setTextColor(R.id.widget_clock_day_week_week_1, color.textColor)
                setTextColor(R.id.widget_clock_day_week_week_2, color.textColor)
                setTextColor(R.id.widget_clock_day_week_week_3, color.textColor)
                setTextColor(R.id.widget_clock_day_week_week_4, color.textColor)
                setTextColor(R.id.widget_clock_day_week_week_5, color.textColor)
                setTextColor(R.id.widget_clock_day_week_temp_1, color.textColor)
                setTextColor(R.id.widget_clock_day_week_temp_2, color.textColor)
                setTextColor(R.id.widget_clock_day_week_temp_3, color.textColor)
                setTextColor(R.id.widget_clock_day_week_temp_4, color.textColor)
                setTextColor(R.id.widget_clock_day_week_temp_5, color.textColor)
            }
        }
        if (textSize != 100) {
            val clockSize = context.resources.getDimensionPixelSize(R.dimen.widget_current_weather_icon_size)
                .toFloat() * textSize / 100f
            val clockAASize = context.resources.getDimensionPixelSize(R.dimen.widget_aa_text_size)
                .toFloat() * textSize / 100f
            val contentSize = context.resources.getDimensionPixelSize(R.dimen.widget_content_text_size)
                .toFloat() * textSize / 100f
            views.apply {
                setTextViewTextSize(R.id.widget_clock_day_week_clock_light, TypedValue.COMPLEX_UNIT_PX, clockSize)
                setTextViewTextSize(R.id.widget_clock_day_week_clock_normal, TypedValue.COMPLEX_UNIT_PX, clockSize)
                setTextViewTextSize(R.id.widget_clock_day_week_clock_black, TypedValue.COMPLEX_UNIT_PX, clockSize)
                setTextViewTextSize(R.id.widget_clock_day_week_clock_aa_light, TypedValue.COMPLEX_UNIT_PX, clockAASize)
                setTextViewTextSize(R.id.widget_clock_day_week_clock_aa_normal, TypedValue.COMPLEX_UNIT_PX, clockAASize)
                setTextViewTextSize(R.id.widget_clock_day_week_clock_aa_black, TypedValue.COMPLEX_UNIT_PX, clockAASize)
                setTextViewTextSize(R.id.widget_clock_day_week_title, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(
                    R.id.widget_clock_day_week_alternate_calendar,
                    TypedValue.COMPLEX_UNIT_PX,
                    contentSize
                )
                setTextViewTextSize(R.id.widget_clock_day_week_subtitle, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_clock_day_week_week_1, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_clock_day_week_week_2, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_clock_day_week_week_3, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_clock_day_week_week_4, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_clock_day_week_week_5, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_clock_day_week_temp_1, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_clock_day_week_temp_2, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_clock_day_week_temp_3, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_clock_day_week_temp_4, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_clock_day_week_temp_5, TypedValue.COMPLEX_UNIT_PX, contentSize)
            }
        }
        if (color.showCard) {
            views.setImageViewResource(R.id.widget_clock_day_week_card, getCardBackgroundId(color))
            views.setInt(R.id.widget_clock_day_week_card, "setImageAlpha", (cardAlpha / 100.0 * 255).toInt())
        }
        when (clockFont) {
            "normal" -> {
                views.apply {
                    setViewVisibility(R.id.widget_clock_day_week_clock_lightContainer, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_week_clock_normalContainer, View.VISIBLE)
                    setViewVisibility(R.id.widget_clock_day_week_clock_blackContainer, View.GONE)
                }
            }
            "black" -> {
                views.apply {
                    setViewVisibility(R.id.widget_clock_day_week_clock_lightContainer, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_week_clock_normalContainer, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_week_clock_blackContainer, View.VISIBLE)
                }
            }
            else -> {
                views.apply {
                    setViewVisibility(R.id.widget_clock_day_week_clock_lightContainer, View.VISIBLE)
                    setViewVisibility(R.id.widget_clock_day_week_clock_normalContainer, View.GONE)
                    setViewVisibility(R.id.widget_clock_day_week_clock_blackContainer, View.GONE)
                }
            }
        }
        setOnClickPendingIntent(context, views, location)
        return views
    }

    fun isInUse(context: Context): Boolean {
        val widgetIds = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, WidgetClockDayWeekProvider::class.java))
        return widgetIds != null && widgetIds.isNotEmpty()
    }

    private fun setOnClickPendingIntent(context: Context, views: RemoteViews, location: Location) {
        // weather.
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_week_weather,
            getWeatherPendingIntent(context, location, Widgets.CLOCK_DAY_WEEK_PENDING_INTENT_CODE_WEATHER)
        )

        // daily forecast.
        val index = location.weather?.todayIndex ?: 0
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_week_icon_1,
            getDailyForecastPendingIntent(
                context,
                location,
                index,
                Widgets.CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_1
            )
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_week_icon_2,
            getDailyForecastPendingIntent(
                context,
                location,
                index + 1,
                Widgets.CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_2
            )
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_week_icon_3,
            getDailyForecastPendingIntent(
                context,
                location,
                index + 2,
                Widgets.CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_3
            )
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_week_icon_4,
            getDailyForecastPendingIntent(
                context,
                location,
                index + 3,
                Widgets.CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_4
            )
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_week_icon_5,
            getDailyForecastPendingIntent(
                context,
                location,
                index + 4,
                Widgets.CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_5
            )
        )

        // clock.
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_week_clock_light,
            getAlarmPendingIntent(context, Widgets.CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CLOCK_LIGHT)
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_week_clock_normal,
            getAlarmPendingIntent(context, Widgets.CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CLOCK_NORMAL)
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_week_clock_black,
            getAlarmPendingIntent(context, Widgets.CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CLOCK_BLACK)
        )

        // title.
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_week_title,
            getCalendarPendingIntent(context, Widgets.CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CALENDAR)
        )
    }
}
