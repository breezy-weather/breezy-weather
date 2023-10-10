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
import org.breezyweather.R
import org.breezyweather.background.receiver.widget.WidgetWeekProvider
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.remoteviews.Widgets
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.ResourcesProviderFactory

object WeekWidgetIMP : AbstractRemoteViewsPresenter() {

    fun updateWidgetView(context: Context, location: Location) {
        val config = getWidgetConfig(context, context.getString(R.string.sp_widget_week_setting))
        val views = getRemoteViews(
            context, location, config.viewStyle, config.cardStyle, config.cardAlpha, config.textColor, config.textSize
        )
        AppWidgetManager.getInstance(context).updateAppWidget(
            ComponentName(context, WidgetWeekProvider::class.java),
            views
        )
    }

    fun getRemoteViews(
        context: Context, location: Location?, viewStyle: String?,
        cardStyle: String?, cardAlpha: Int, textColor: String?, textSize: Int
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
        val temperatureUnit = settings.temperatureUnit
        val weekIconMode = settings.widgetWeekIconMode
        val minimalIcon = settings.isWidgetUsingMonochromeIcons

        if (weather.current?.temperature?.temperature != null) {
            views.setTextViewText(
                R.id.widget_week_temp,
                weather.current.temperature.getShortTemperature(context, temperatureUnit)
            )
        } else views.setTextViewText(R.id.widget_week_temp, null)
        weather.current?.weatherCode?.let {
            views.setViewVisibility(R.id.widget_week_icon, View.VISIBLE)
            views.setImageViewUri(
                R.id.widget_week_icon,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider, it, dayTime, minimalIcon, color.minimalIconColor
                )
            )
        } ?: views.setViewVisibility(R.id.widget_week_icon, View.INVISIBLE)

        val weekIconDaytime = isWeekIconDaytime(weekIconMode, dayTime)
        val dailyIds = arrayOf(
            arrayOf(R.id.widget_week_week_1, R.id.widget_week_temp_1, R.id.widget_week_icon_1),
            arrayOf(R.id.widget_week_week_2, R.id.widget_week_temp_2, R.id.widget_week_icon_2),
            arrayOf(R.id.widget_week_week_3, R.id.widget_week_temp_3, R.id.widget_week_icon_3),
            arrayOf(R.id.widget_week_week_4, R.id.widget_week_temp_4, R.id.widget_week_icon_4),
            arrayOf(R.id.widget_week_week_5, R.id.widget_week_temp_5, R.id.widget_week_icon_5),
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
            val contentSize = context.resources.getDimensionPixelSize(R.dimen.widget_content_text_size)
                .toFloat() * textSize / 100f
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
        context: Context, views: RemoteViews, location: Location, viewType: String?
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
                context, location, index, Widgets.WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_1
            )
        )
        views.setOnClickPendingIntent(
            R.id.widget_week_icon_2,
            getDailyForecastPendingIntent(
                context, location, index + 1, Widgets.WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_2
            )
        )
        views.setOnClickPendingIntent(
            R.id.widget_week_icon_3,
            getDailyForecastPendingIntent(
                context, location, index + 2, Widgets.WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_3
            )
        )
        if (viewType == "5_days") {
            views.setOnClickPendingIntent(
                R.id.widget_week_icon_4,
                getDailyForecastPendingIntent(
                    context, location, index + 3, Widgets.WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_4
                )
            )
            views.setOnClickPendingIntent(
                R.id.widget_week_icon_5,
                getDailyForecastPendingIntent(
                    context, location, index + 4, Widgets.WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_5
                )
            )
        }
    }
}
