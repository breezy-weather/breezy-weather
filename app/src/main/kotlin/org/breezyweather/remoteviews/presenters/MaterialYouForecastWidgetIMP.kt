/*
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
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.background.receiver.widget.WidgetMaterialYouForecastProvider
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.common.extensions.getHour
import org.breezyweather.common.options.NotificationTextColor
import org.breezyweather.domain.location.model.getPlace
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.getName
import org.breezyweather.domain.weather.model.getShortDescription
import org.breezyweather.domain.weather.model.getWeek
import org.breezyweather.domain.weather.model.isToday
import org.breezyweather.remoteviews.Widgets
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory
import org.breezyweather.unit.formatting.UnitWidth

class MaterialYouForecastWidgetIMP : AbstractRemoteViewsPresenter() {

    companion object {

        fun isEnabled(context: Context): Boolean {
            return AppWidgetManager.getInstance(context).getAppWidgetIds(
                ComponentName(context, WidgetMaterialYouForecastProvider::class.java)
            ).isNotEmpty()
        }

        fun updateWidgetView(context: Context, location: Location?) {
            AppWidgetManager.getInstance(context).updateAppWidget(
                ComponentName(context, WidgetMaterialYouForecastProvider::class.java),
                buildWeatherWidget(context, location)
            )
        }
    }
}

private fun buildWeatherWidget(
    context: Context,
    location: Location?,
): RemoteViews = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    RemoteViews(
        mapOf(
            SizeF(1.0f, 1.0f) to buildRemoteViews(
                context,
                location,
                R.layout.widget_material_you_forecast_1x1
            ),
            SizeF(120.0f, 120.0f) to buildRemoteViews(
                context,
                location,
                R.layout.widget_material_you_forecast_2x1
            ),
            SizeF(156.0f, 156.0f) to buildRemoteViews(
                context,
                location,
                R.layout.widget_material_you_forecast_2x2
            ),
            SizeF(192.0f, 98.0f) to buildRemoteViews(
                context,
                location,
                R.layout.widget_material_you_forecast_3x1
            ),
            SizeF(148.0f, 198.0f) to buildRemoteViews(
                context,
                location,
                R.layout.widget_material_you_forecast_3x2
            ),
            SizeF(256.0f, 100.0f) to buildRemoteViews(
                context,
                location,
                R.layout.widget_material_you_forecast_4x1
            ),
            SizeF(256.0f, 198.0f) to buildRemoteViews(
                context,
                location,
                R.layout.widget_material_you_forecast_4x2
            ),
            SizeF(256.0f, 312.0f) to buildRemoteViews(
                context,
                location,
                R.layout.widget_material_you_forecast_4x3
            ),
            SizeF(298.0f, 198.0f) to buildRemoteViews(
                context,
                location,
                R.layout.widget_material_you_forecast_5x2
            ),
            SizeF(298.0f, 312.0f) to buildRemoteViews(
                context,
                location,
                R.layout.widget_material_you_forecast_5x3
            )
        )
    )
} else {
    buildRemoteViews(context, location, R.layout.widget_material_you_forecast_4x3)
}

private fun buildRemoteViews(
    context: Context,
    location: Location?,
    @LayoutRes layoutId: Int,
): RemoteViews {
    val views = RemoteViews(context.packageName, layoutId)

    val weather = location?.weather ?: return views
    val dayTime = location.isDaylight

    val provider = ResourcesProviderFactory.newInstance
    val temperatureUnit = SettingsManager.getInstance(context).getTemperatureUnit(context)

    views.setTextViewText(
        R.id.widget_material_you_forecast_city,
        location.getPlace(context)
    )

    // current.
    weather.current?.weatherCode?.let {
        views.setViewVisibility(R.id.widget_material_you_forecast_currentIcon, View.VISIBLE)
        views.setImageViewUri(
            R.id.widget_material_you_forecast_currentIcon,
            ResourceHelper.getWidgetNotificationIconUri(
                provider,
                it,
                dayTime,
                false,
                NotificationTextColor.LIGHT
            )
        )
    } ?: views.setViewVisibility(R.id.widget_material_you_forecast_currentIcon, View.INVISIBLE)

    views.apply {
        setTextViewText(
            R.id.widget_material_you_forecast_currentTemperature,
            weather.current?.temperature?.temperature?.formatMeasure(
                context,
                temperatureUnit,
                valueWidth = UnitWidth.NARROW,
                unitWidth = UnitWidth.NARROW
            )
        )
        setTextViewText(
            R.id.widget_material_you_forecast_daytimeTemperature,
            weather.today?.day?.temperature?.temperature?.formatMeasure(
                context,
                temperatureUnit,
                valueWidth = UnitWidth.NARROW,
                unitWidth = UnitWidth.NARROW
            )
        )
        setTextViewText(
            R.id.widget_material_you_forecast_nighttimeTemperature,
            weather.today?.night?.temperature?.temperature?.formatMeasure(
                context,
                temperatureUnit,
                valueWidth = UnitWidth.NARROW,
                unitWidth = UnitWidth.NARROW
            )
        )
        setTextViewText(
            R.id.widget_material_you_forecast_weatherText,
            weather.current?.weatherText
        )
    }

    views.setTextViewText(
        R.id.widget_material_you_forecast_aqiOrWind,
        weather.current?.let { current ->
            if (current.airQuality?.isIndexValid == true) {
                context.getString(R.string.air_quality) + " – " + current.airQuality!!.getName(context)
            } else {
                current.wind?.let { wind ->
                    wind.getShortDescription(context)?.let {
                        context.getString(R.string.wind) + " – " + it
                    }
                }
            }
        }
    )

    // Hourly
    val hourlyIds = arrayOf(
        arrayOf(
            R.id.widget_material_you_forecast_hour_1,
            R.id.widget_material_you_forecast_hourlyIcon_1,
            R.id.widget_material_you_forecast_hourlyTemperature_1
        ),
        arrayOf(
            R.id.widget_material_you_forecast_hour_2,
            R.id.widget_material_you_forecast_hourlyIcon_2,
            R.id.widget_material_you_forecast_hourlyTemperature_2
        ),
        arrayOf(
            R.id.widget_material_you_forecast_hour_3,
            R.id.widget_material_you_forecast_hourlyIcon_3,
            R.id.widget_material_you_forecast_hourlyTemperature_3
        ),
        arrayOf(
            R.id.widget_material_you_forecast_hour_4,
            R.id.widget_material_you_forecast_hourlyIcon_4,
            R.id.widget_material_you_forecast_hourlyTemperature_4
        ),
        arrayOf(
            R.id.widget_material_you_forecast_hour_5,
            R.id.widget_material_you_forecast_hourlyIcon_5,
            R.id.widget_material_you_forecast_hourlyTemperature_5
        ),
        arrayOf(
            R.id.widget_material_you_forecast_hour_6,
            R.id.widget_material_you_forecast_hourlyIcon_6,
            R.id.widget_material_you_forecast_hourlyTemperature_6
        )
    )
    // Loop through next 6 hours
    hourlyIds.forEachIndexed { i, hourlyId ->
        views.setTextViewText(hourlyId[0], weather.nextHourlyForecast.getOrNull(i)?.date?.getHour(location, context))
        weather.nextHourlyForecast.getOrNull(i)?.weatherCode?.let {
            views.setViewVisibility(hourlyId[1], View.VISIBLE)
            views.setImageViewUri(
                hourlyId[1],
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    it,
                    weather.nextHourlyForecast[i].isDaylight,
                    false,
                    NotificationTextColor.LIGHT
                )
            )
        } ?: views.setViewVisibility(hourlyId[1], View.INVISIBLE)
        views.setTextViewText(
            hourlyId[2],
            weather.nextHourlyForecast.getOrNull(i)?.temperature?.temperature?.formatMeasure(
                context,
                temperatureUnit,
                valueWidth = UnitWidth.NARROW,
                unitWidth = UnitWidth.NARROW
            )
        )
    }

    // Daily
    val dailyIds = arrayOf(
        arrayOf(
            R.id.widget_material_you_forecast_week_1,
            R.id.widget_material_you_forecast_dayIcon_1,
            R.id.widget_material_you_forecast_dayTemperature_1,
            R.id.widget_material_you_forecast_nightTemperature_1,
            R.id.widget_material_you_forecast_nightIcon_1
        ),
        arrayOf(
            R.id.widget_material_you_forecast_week_2,
            R.id.widget_material_you_forecast_dayIcon_2,
            R.id.widget_material_you_forecast_dayTemperature_2,
            R.id.widget_material_you_forecast_nightTemperature_2,
            R.id.widget_material_you_forecast_nightIcon_2
        ),
        arrayOf(
            R.id.widget_material_you_forecast_week_3,
            R.id.widget_material_you_forecast_dayIcon_3,
            R.id.widget_material_you_forecast_dayTemperature_3,
            R.id.widget_material_you_forecast_nightTemperature_3,
            R.id.widget_material_you_forecast_nightIcon_3
        ),
        arrayOf(
            R.id.widget_material_you_forecast_week_4,
            R.id.widget_material_you_forecast_dayIcon_4,
            R.id.widget_material_you_forecast_dayTemperature_4,
            R.id.widget_material_you_forecast_nightTemperature_4,
            R.id.widget_material_you_forecast_nightIcon_4
        ),
        arrayOf(
            R.id.widget_material_you_forecast_week_5,
            R.id.widget_material_you_forecast_dayIcon_5,
            R.id.widget_material_you_forecast_dayTemperature_5,
            R.id.widget_material_you_forecast_nightTemperature_5,
            R.id.widget_material_you_forecast_nightIcon_5
        ),
        arrayOf(
            R.id.widget_material_you_forecast_week_6,
            R.id.widget_material_you_forecast_dayIcon_6,
            R.id.widget_material_you_forecast_dayTemperature_6,
            R.id.widget_material_you_forecast_nightTemperature_6,
            R.id.widget_material_you_forecast_nightIcon_6
        )
    )
    // Loop through 6 first days
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
        weather.dailyForecastStartingToday.getOrNull(i)?.day?.weatherCode?.let {
            views.setViewVisibility(dailyId[1], View.VISIBLE)
            views.setImageViewUri(
                dailyId[1],
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    it,
                    dayTime = true,
                    minimal = false,
                    NotificationTextColor.LIGHT
                )
            )
        } ?: views.setViewVisibility(dailyId[1], View.INVISIBLE)
        views.setTextViewText(
            dailyId[2],
            weather.dailyForecastStartingToday.getOrNull(i)?.day?.temperature?.temperature?.formatMeasure(
                context,
                temperatureUnit,
                valueWidth = UnitWidth.NARROW,
                unitWidth = UnitWidth.NARROW
            )
        )
        views.setTextViewText(
            dailyId[3],
            weather.dailyForecastStartingToday.getOrNull(i)?.night?.temperature?.temperature?.formatMeasure(
                context,
                temperatureUnit,
                valueWidth = UnitWidth.NARROW,
                unitWidth = UnitWidth.NARROW
            )
        )
        weather.dailyForecastStartingToday.getOrNull(i)?.night?.weatherCode?.let {
            views.setViewVisibility(dailyId[4], View.VISIBLE)
            views.setImageViewUri(
                dailyId[4],
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    it,
                    dayTime = false,
                    minimal = false,
                    NotificationTextColor.LIGHT
                )
            )
        } ?: views.setViewVisibility(dailyId[4], View.INVISIBLE)
    }

    // pending intent.
    views.setOnClickPendingIntent(
        android.R.id.background,
        AbstractRemoteViewsPresenter.getWeatherPendingIntent(
            context,
            location,
            Widgets.MATERIAL_YOU_FORECAST_PENDING_INTENT_CODE_WEATHER
        )
    )

    return views
}
