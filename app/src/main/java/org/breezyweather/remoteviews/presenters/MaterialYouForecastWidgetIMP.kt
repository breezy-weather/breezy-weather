package org.breezyweather.remoteviews.presenters

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import org.breezyweather.R
import org.breezyweather.background.receiver.widget.WidgetMaterialYouForecastProvider
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.NotificationTextColor
import org.breezyweather.remoteviews.Widgets
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.ResourcesProviderFactory

class MaterialYouForecastWidgetIMP: AbstractRemoteViewsPresenter() {

    companion object {

        fun isEnabled(context: Context): Boolean {
            return AppWidgetManager.getInstance(context).getAppWidgetIds(
                ComponentName(context, WidgetMaterialYouForecastProvider::class.java)
            ).isNotEmpty()
        }

        fun updateWidgetView(context: Context, location: Location) {
            AppWidgetManager.getInstance(context).updateAppWidget(
                ComponentName(context, WidgetMaterialYouForecastProvider::class.java),
                buildWeatherWidget(context, location)
            )
        }
    }
}

private fun buildWeatherWidget(
    context: Context,
    location: Location
): RemoteViews = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    RemoteViews(
        mapOf(
            SizeF(1.0f, 1.0f) to buildRemoteViews(
                context, location, R.layout.widget_material_you_forecast_1x1
            ),
            SizeF (120.0f, 120.0f) to buildRemoteViews(
                context, location, R.layout.widget_material_you_forecast_2x1
            ),
            SizeF (156.0f, 156.0f) to buildRemoteViews(
                context, location, R.layout.widget_material_you_forecast_2x2
            ),
            SizeF (192.0f, 98.0f) to buildRemoteViews(
                context, location, R.layout.widget_material_you_forecast_3x1
            ),
            SizeF (148.0f, 198.0f) to buildRemoteViews(
                context, location, R.layout.widget_material_you_forecast_3x2
            ),
            SizeF (256.0f, 100.0f) to buildRemoteViews(
                context, location, R.layout.widget_material_you_forecast_4x1
            ),
            SizeF (256.0f, 198.0f) to buildRemoteViews(
                context, location, R.layout.widget_material_you_forecast_4x2
            ),
            SizeF (256.0f, 312.0f) to buildRemoteViews(
                context, location, R.layout.widget_material_you_forecast_4x3
            ),
            SizeF (298.0f, 198.0f) to buildRemoteViews(
                context, location, R.layout.widget_material_you_forecast_5x2
            ),
            SizeF (298.0f, 312.0f) to buildRemoteViews(
                context, location, R.layout.widget_material_you_forecast_5x3
            ),
        )
    )
} else {
    buildRemoteViews(context, location, R.layout.widget_material_you_forecast_4x3)
}

private fun buildRemoteViews(
    context: Context,
    location: Location,
    @LayoutRes layoutId: Int,
): RemoteViews {

    val views = RemoteViews(context.packageName, layoutId)

    val weather = location.weather
    val dayTime = location.isDaylight

    val provider = ResourcesProviderFactory.newInstance

    val settings = SettingsManager.getInstance(context)
    val temperatureUnit = settings.temperatureUnit
    val speedUnit = settings.speedUnit

    views.setTextViewText(
        R.id.widget_material_you_forecast_city,
        location.getCityName(context)
    )
    if (weather == null) {
        return views
    }

    // current.
    weather.current?.weatherCode?.let {
        views.setViewVisibility(R.id.widget_material_you_forecast_currentIcon, View.VISIBLE)
        views.setImageViewUri(
            R.id.widget_material_you_forecast_currentIcon,
            ResourceHelper.getWidgetNotificationIconUri(
                provider, it, dayTime, false, NotificationTextColor.LIGHT
            )
        )
    } ?: views.setViewVisibility(R.id.widget_material_you_forecast_currentIcon, View.INVISIBLE)

    views.apply {
        setTextViewText(
            R.id.widget_material_you_forecast_currentTemperature,
            weather.current?.temperature?.getShortTemperature(context, temperatureUnit)
        )
        setTextViewText(
            R.id.widget_material_you_forecast_daytimeTemperature,
            weather.dailyForecast.getOrNull(0)?.day?.temperature?.getShortTemperature(context, temperatureUnit)
        )
        setTextViewText(
            R.id.widget_material_you_forecast_nighttimeTemperature,
            weather.dailyForecast.getOrNull(0)?.night?.temperature?.getShortTemperature(context, temperatureUnit)
        )
        setTextViewText(
            R.id.widget_material_you_forecast_weatherText,
            location.weather.current?.weatherText
        )
    }

    if (weather.current?.airQuality != null && weather.current.airQuality.isValid) {
        views.setTextViewText(
            R.id.widget_material_you_forecast_aqiOrWind,
            context.getString(R.string.air_quality) + " - " + weather.current.airQuality.getName(context)
        )
    } else if (weather.current?.wind != null && weather.current.wind.getShortDescription(context, speedUnit).isNotEmpty()) {
        views.setTextViewText(
            R.id.widget_material_you_forecast_aqiOrWind,
            context.getString(R.string.wind) + " - " + weather.current.wind.getShortDescription(context, speedUnit)
        )
    } else views.setTextViewText(R.id.widget_material_you_forecast_aqiOrWind, null)

    // Hourly
    val hourlyIds = arrayOf(
        arrayOf(R.id.widget_material_you_forecast_hour_1, R.id.widget_material_you_forecast_hourlyIcon_1, R.id.widget_material_you_forecast_hourlyTemperature_1),
        arrayOf(R.id.widget_material_you_forecast_hour_2, R.id.widget_material_you_forecast_hourlyIcon_2, R.id.widget_material_you_forecast_hourlyTemperature_2),
        arrayOf(R.id.widget_material_you_forecast_hour_3, R.id.widget_material_you_forecast_hourlyIcon_3, R.id.widget_material_you_forecast_hourlyTemperature_3),
        arrayOf(R.id.widget_material_you_forecast_hour_4, R.id.widget_material_you_forecast_hourlyIcon_4, R.id.widget_material_you_forecast_hourlyTemperature_4),
        arrayOf(R.id.widget_material_you_forecast_hour_5, R.id.widget_material_you_forecast_hourlyIcon_5, R.id.widget_material_you_forecast_hourlyTemperature_5),
        arrayOf(R.id.widget_material_you_forecast_hour_6, R.id.widget_material_you_forecast_hourlyIcon_6, R.id.widget_material_you_forecast_hourlyTemperature_6)
    )
    // Loop through next 6 hours
    hourlyIds.forEachIndexed { i, hourlyId ->
        views.setTextViewText(hourlyId[0], weather.hourlyForecast.getOrNull(i)?.getHour(context, location.timeZone))
        weather.hourlyForecast.getOrNull(i)?.weatherCode?.let {
            views.setViewVisibility(hourlyId[1], View.VISIBLE)
            views.setImageViewUri(
                hourlyId[1],
                ResourceHelper.getWidgetNotificationIconUri(
                    provider, it, weather.hourlyForecast[i].isDaylight, false, NotificationTextColor.LIGHT
                )
            )
        } ?: views.setViewVisibility(hourlyId[1], View.INVISIBLE)
        views.setTextViewText(hourlyId[2], weather.hourlyForecast.getOrNull(i)?.temperature?.getShortTemperature(context, temperatureUnit))
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
        ),
    )
    // Loop through 6 first days
    dailyIds.forEachIndexed { i, dailyId ->
        weather.dailyForecast.getOrNull(i)?.let {
            views.setTextViewText(
                dailyId[0],
                if (it.isToday(location.timeZone)) {
                    context.getString(R.string.short_today)
                } else it.getWeek(context, location.timeZone)
            )
        } ?: views.setTextViewText(dailyId[0], null)
        weather.dailyForecast.getOrNull(i)?.day?.weatherCode?.let {
            views.setViewVisibility(dailyId[1], View.VISIBLE)
            views.setImageViewUri(
                dailyId[1],
                ResourceHelper.getWidgetNotificationIconUri(
                    provider, it, true, false, NotificationTextColor.LIGHT
                )
            )
        } ?: views.setViewVisibility(dailyId[1], View.INVISIBLE)
        views.setTextViewText(
            dailyId[2],
            weather.dailyForecast.getOrNull(i)?.day?.temperature?.getShortTemperature(context, temperatureUnit)
        )
        views.setTextViewText(
            dailyId[3],
            weather.dailyForecast.getOrNull(i)?.night?.temperature?.getShortTemperature(context, temperatureUnit)
        )
        weather.dailyForecast.getOrNull(i)?.night?.weatherCode?.let {
            views.setViewVisibility(dailyId[4], View.VISIBLE)
            views.setImageViewUri(
                dailyId[4],
                ResourceHelper.getWidgetNotificationIconUri(
                    provider, it, false, false, NotificationTextColor.LIGHT
                )
            )
        } ?: views.setViewVisibility(dailyId[4], View.INVISIBLE)
    }

    // pending intent.
    views.setOnClickPendingIntent(
        android.R.id.background,
        AbstractRemoteViewsPresenter.getWeatherPendingIntent(
            context, location, Widgets.MATERIAL_YOU_FORECAST_PENDING_INTENT_CODE_WEATHER
        )
    )

    return views
}