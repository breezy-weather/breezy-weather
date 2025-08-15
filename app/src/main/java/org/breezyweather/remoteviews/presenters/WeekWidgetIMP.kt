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
import org.breezyweather.background.receiver.widget.WidgetWeekProvider
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.getTrendTemperature
import org.breezyweather.domain.weather.model.getWeek
import org.breezyweather.domain.weather.model.isToday
import org.breezyweather.remoteviews.Widgets
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory
import org.breezyweather.unit.formatting.UnitWidth
import kotlin.math.roundToInt

object WeekWidgetIMP : AbstractRemoteViewsPresenter() {

    fun updateWidgetView(
        context: Context,
        location: Location?,
    ) {
        val config = getWidgetConfig(context, context.getString(R.string.sp_widget_week_setting))
        val views = getRemoteViews(
            context,
            location,
            config.viewStyle,
            config.cardStyle,
            config.cardAlpha,
            config.textColor,
            config.textSize
        )
        AppWidgetManager.getInstance(context).updateAppWidget(
            ComponentName(context, WidgetWeekProvider::class.java),
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
    ): RemoteViews {
        val color = WidgetColor(context, cardStyle!!, textColor!!, location?.isDaylight ?: true)
        val views = RemoteViews(
            context.packageName,
            if ("3_days" == viewStyle) {
                if (!color.showCard) R.layout.widget_week_3 else R.layout.widget_week_3_card
            } else {
                if (!color.showCard) R.layout.widget_week else R.layout.widget_week_card
            }
        )
        val weather = location?.weather ?: return views
        val provider = ResourcesProviderFactory.newInstance
        val dayTime = location.isDaylight
        val settings = SettingsManager.getInstance(context)
        val weekIconMode = settings.widgetWeekIconMode
        val minimalIcon = settings.isWidgetUsingMonochromeIcons

        weather.current?.temperature?.temperature?.let {
            views.setTextViewText(
                R.id.widget_week_temp,
                it.formatMeasure(context, valueWidth = UnitWidth.NARROW, unitWidth = UnitWidth.NARROW)
            )
        } ?: run {
            views.setTextViewText(R.id.widget_week_temp, null)
        }
        weather.current?.weatherCode?.let {
            views.setViewVisibility(R.id.widget_week_icon, View.VISIBLE)
            views.setImageViewUri(
                R.id.widget_week_icon,
                ResourceHelper.getWidgetNotificationIconUri(provider, it, dayTime, minimalIcon, color.minimalIconColor)
            )
        } ?: views.setViewVisibility(R.id.widget_week_icon, View.INVISIBLE)

        val weekIconDaytime = isWeekIconDaytime(weekIconMode, dayTime)
        val dailyIds = arrayOf(
            arrayOf(R.id.widget_week_week_1, R.id.widget_week_temp_1, R.id.widget_week_icon_1),
            arrayOf(R.id.widget_week_week_2, R.id.widget_week_temp_2, R.id.widget_week_icon_2),
            arrayOf(R.id.widget_week_week_3, R.id.widget_week_temp_3, R.id.widget_week_icon_3),
            arrayOf(R.id.widget_week_week_4, R.id.widget_week_temp_4, R.id.widget_week_icon_4),
            arrayOf(R.id.widget_week_week_5, R.id.widget_week_temp_5, R.id.widget_week_icon_5)
        )
        dailyIds.forEachIndexed { i, dailyId ->
            weather.dailyForecastStartingToday.getOrNull(i)?.let {
                views.setTextViewText(
                    dailyId[0],
                    if (it.isToday(location)) {
                        context.getString(R.string.daily_today_short)
                    } else {
                        it.getWeek(location, context)
                    }
                )
            } ?: views.setTextViewText(dailyId[0], null)
            views.setTextViewText(
                dailyId[1],
                weather.dailyForecastStartingToday.getOrNull(i)?.getTrendTemperature(context)
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
                setTextColor(R.id.widget_week_week_1, color.textColor)
                setTextColor(R.id.widget_week_week_2, color.textColor)
                setTextColor(R.id.widget_week_week_3, color.textColor)
                setTextColor(R.id.widget_week_week_4, color.textColor)
                setTextColor(R.id.widget_week_week_5, color.textColor)
                setTextColor(R.id.widget_week_temp, color.textColor)
                setTextColor(R.id.widget_week_temp_1, color.textColor)
                setTextColor(R.id.widget_week_temp_2, color.textColor)
                setTextColor(R.id.widget_week_temp_3, color.textColor)
                setTextColor(R.id.widget_week_temp_4, color.textColor)
                setTextColor(R.id.widget_week_temp_5, color.textColor)
            }
        }

        // set text size.
        if (textSize != 100) {
            val contentSize = context.resources.getDimensionPixelSize(R.dimen.widget_content_text_size).toFloat()
                .times(textSize)
                .div(100f)
            views.apply {
                setTextViewTextSize(R.id.widget_week_week_1, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_week_week_2, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_week_week_3, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_week_week_4, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_week_week_5, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_week_temp_1, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_week_temp_2, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_week_temp_3, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_week_temp_4, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_week_temp_5, TypedValue.COMPLEX_UNIT_PX, contentSize)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setInt(R.id.widget_week_temp_1, "setLineHeight", contentSize.roundToInt())
                    setInt(R.id.widget_week_temp_2, "setLineHeight", contentSize.roundToInt())
                    setInt(R.id.widget_week_temp_3, "setLineHeight", contentSize.roundToInt())
                    setInt(R.id.widget_week_temp_4, "setLineHeight", contentSize.roundToInt())
                    setInt(R.id.widget_week_temp_5, "setLineHeight", contentSize.roundToInt())
                }
            }
        }

        // set card visibility.
        if (color.showCard) {
            views.setImageViewResource(R.id.widget_week_card, getCardBackgroundId(color))
            views.setInt(R.id.widget_week_card, "setImageAlpha", (cardAlpha / 100.0 * 255).toInt())
        }

        // set intent.
        setOnClickPendingIntent(context, views, location, viewStyle)

        // commit.
        return views
    }

    fun isInUse(context: Context): Boolean {
        val widgetIds = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, WidgetWeekProvider::class.java))
        return widgetIds != null && widgetIds.isNotEmpty()
    }

    private fun setOnClickPendingIntent(
        context: Context,
        views: RemoteViews,
        location: Location,
        viewType: String?,
    ) {
        // weather.
        views.setOnClickPendingIntent(
            R.id.widget_week_weather,
            getWeatherPendingIntent(context, location, Widgets.WEEK_PENDING_INTENT_CODE_WEATHER)
        )

        // daily forecast.
        val index = location.weather?.todayIndex ?: 0
        views.setOnClickPendingIntent(
            R.id.widget_week_icon_1,
            getDailyForecastPendingIntent(
                context,
                location,
                index,
                Widgets.WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_1
            )
        )
        views.setOnClickPendingIntent(
            R.id.widget_week_icon_2,
            getDailyForecastPendingIntent(
                context,
                location,
                index + 1,
                Widgets.WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_2
            )
        )
        views.setOnClickPendingIntent(
            R.id.widget_week_icon_3,
            getDailyForecastPendingIntent(
                context,
                location,
                index + 2,
                Widgets.WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_3
            )
        )
        if (viewType == "5_days") {
            views.setOnClickPendingIntent(
                R.id.widget_week_icon_4,
                getDailyForecastPendingIntent(
                    context,
                    location,
                    index + 3,
                    Widgets.WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_4
                )
            )
            views.setOnClickPendingIntent(
                R.id.widget_week_icon_5,
                getDailyForecastPendingIntent(
                    context,
                    location,
                    index + 4,
                    Widgets.WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_5
                )
            )
        }
    }
}
