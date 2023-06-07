package wangdaye.com.geometricweather.remoteviews.presenters

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.SizeF
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import wangdaye.com.geometricweather.GeometricWeather
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.background.receiver.widget.WidgetMaterialYouForecastProvider
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.options.NotificationTextColor
import wangdaye.com.geometricweather.settings.SettingsManager
import wangdaye.com.geometricweather.theme.resource.ResourceHelper
import wangdaye.com.geometricweather.theme.resource.ResourcesProviderFactory

class MaterialYouForecastWidgetIMP: AbstractRemoteViewsPresenter() {

    companion object {

        @JvmStatic
        fun isEnable(context: Context): Boolean {
            return AppWidgetManager.getInstance(
                context
            ).getAppWidgetIds(
                ComponentName(
                    context,
                    WidgetMaterialYouForecastProvider::class.java
                )
            ).isNotEmpty()
        }

        @JvmStatic
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
                context,
                location,
                R.layout.widget_material_you_forecast_1x1
            ),
            SizeF (120.0f, 120.0f) to buildRemoteViews(
                context,
                location,
                R.layout.widget_material_you_forecast_2x1
            ),
            SizeF (156.0f, 156.0f) to buildRemoteViews(
                context,
                location,
                R.layout.widget_material_you_forecast_2x2
            ),
            SizeF (192.0f, 98.0f) to buildRemoteViews(
                context,
                location,
                R.layout.widget_material_you_forecast_3x1
            ),
            SizeF (148.0f, 198.0f) to buildRemoteViews(
                context,
                location,
                R.layout.widget_material_you_forecast_3x2
            ),
            SizeF (256.0f, 100.0f) to buildRemoteViews(
                context,
                location,
                R.layout.widget_material_you_forecast_4x1
            ),
            SizeF (256.0f, 198.0f) to buildRemoteViews(
                context,
                location,
                R.layout.widget_material_you_forecast_4x2
            ),
            SizeF (256.0f, 312.0f) to buildRemoteViews(
                context,
                location,
                R.layout.widget_material_you_forecast_4x3
            ),
            SizeF (298.0f, 198.0f) to buildRemoteViews(
                context,
                location,
                R.layout.widget_material_you_forecast_5x2
            ),
            SizeF (298.0f, 312.0f) to buildRemoteViews(
                context,
                location,
                R.layout.widget_material_you_forecast_5x3
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

    val views = RemoteViews(
        context.packageName,
        layoutId
    )

    val weather = location.weather
    val dayTime = location.isDaylight

    val provider = ResourcesProviderFactory.getNewInstance()

    val settings = SettingsManager.getInstance(context)
    val temperatureUnit = settings.temperatureUnit

    views.setTextViewText(
        R.id.widget_material_you_forecast_city,
        location.getCityName(context)
    )
    if (weather == null) {
        return views
    }

    // current.
    weather.current?.weatherCode?.let {
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
    }

    views.setTextViewText(
        R.id.widget_material_you_forecast_currentTemperature,
        weather.current?.temperature?.getShortTemperature(context, temperatureUnit)
    )
    views.setTextViewText(
        R.id.widget_material_you_forecast_daytimeTemperature,
        weather.dailyForecast.getOrNull(0)?.day?.temperature?.getShortTemperature(context, temperatureUnit)
    )
    views.setTextViewText(
        R.id.widget_material_you_forecast_nighttimeTemperature,
        weather.dailyForecast.getOrNull(0)?.night?.temperature?.getShortTemperature(context, temperatureUnit)
    )

    views.setTextViewText(
        R.id.widget_material_you_forecast_weatherText,
        location.weather.current?.weatherText
    )

    if (weather.current?.airQuality != null && weather.current.airQuality.isValid) {
        views.setTextViewText(
            R.id.widget_material_you_forecast_aqiOrWind,
             "AQI - " + weather.current.airQuality.getName(context)
        )
    } else if (weather.current?.wind != null && weather.current.wind.shortWindDescription.isNotEmpty()) {
        views.setTextViewText(
            R.id.widget_material_you_forecast_aqiOrWind,
            context.getString(R.string.wind) + " - " + weather.current.wind.shortWindDescription
        )
    }

    // hourly.
    weather.hourlyForecast.getOrNull(0)?.let {
        views.setTextViewText(
            R.id.widget_material_you_forecast_hour_1,
            weather.hourlyForecast[0].getHour(context, location.timeZone)
        )
        weather.hourlyForecast[0].weatherCode?.let {
            views.setImageViewUri(
                R.id.widget_material_you_forecast_hourlyIcon_1,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    weather.hourlyForecast[0].weatherCode,
                    weather.hourlyForecast[0].isDaylight,
                    false,
                    NotificationTextColor.LIGHT
                )
            )
        }
        views.setTextViewText(
            R.id.widget_material_you_forecast_hourlyTemperature_1,
            weather.hourlyForecast[0].temperature?.getShortTemperature(context, temperatureUnit)
        )
    }

    weather.hourlyForecast.getOrNull(1)?.let {
        views.setTextViewText(
            R.id.widget_material_you_forecast_hour_2,
            weather.hourlyForecast[1].getHour(context, location.timeZone)
        )
        weather.hourlyForecast[1].weatherCode?.let {
            views.setImageViewUri(
                R.id.widget_material_you_forecast_hourlyIcon_2,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    weather.hourlyForecast[1].weatherCode,
                    weather.hourlyForecast[1].isDaylight,
                    false,
                    NotificationTextColor.LIGHT
                )
            )
        }
        views.setTextViewText(
            R.id.widget_material_you_forecast_hourlyTemperature_2,
            weather.hourlyForecast[1].temperature?.getShortTemperature(context, temperatureUnit)
        )
    }

    weather.hourlyForecast.getOrNull(2)?.let {
        views.setTextViewText(
            R.id.widget_material_you_forecast_hour_3,
            weather.hourlyForecast[2].getHour(context, location.timeZone)
        )
        weather.hourlyForecast[2].weatherCode?.let {
            views.setImageViewUri(
                R.id.widget_material_you_forecast_hourlyIcon_3,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    weather.hourlyForecast[2].weatherCode,
                    weather.hourlyForecast[2].isDaylight,
                    false,
                    NotificationTextColor.LIGHT
                )
            )
        }
        views.setTextViewText(
            R.id.widget_material_you_forecast_hourlyTemperature_3,
            weather.hourlyForecast[2].temperature?.getShortTemperature(context, temperatureUnit)
        )
    }

    weather.hourlyForecast.getOrNull(3)?.let {
        views.setTextViewText(
            R.id.widget_material_you_forecast_hour_4,
            weather.hourlyForecast[3].getHour(context, location.timeZone)
        )
        weather.hourlyForecast[3].weatherCode?.let {
            views.setImageViewUri(
                R.id.widget_material_you_forecast_hourlyIcon_4,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    weather.hourlyForecast[3].weatherCode,
                    weather.hourlyForecast[3].isDaylight,
                    false,
                    NotificationTextColor.LIGHT
                )
            )
        }
        views.setTextViewText(
            R.id.widget_material_you_forecast_hourlyTemperature_4,
            weather.hourlyForecast[3].temperature?.getShortTemperature(context, temperatureUnit)
        )
    }

    weather.hourlyForecast.getOrNull(4)?.let {
        views.setTextViewText(
            R.id.widget_material_you_forecast_hour_5,
            weather.hourlyForecast[4].getHour(context, location.timeZone)
        )
        weather.hourlyForecast[4].weatherCode?.let {
            views.setImageViewUri(
                R.id.widget_material_you_forecast_hourlyIcon_5,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    weather.hourlyForecast[4].weatherCode,
                    weather.hourlyForecast[4].isDaylight,
                    false,
                    NotificationTextColor.LIGHT
                )
            )
        }
        views.setTextViewText(
            R.id.widget_material_you_forecast_hourlyTemperature_5,
            weather.hourlyForecast[4].temperature?.getShortTemperature(context, temperatureUnit)
        )
    }

    weather.hourlyForecast.getOrNull(5)?.let {
        views.setTextViewText(
            R.id.widget_material_you_forecast_hour_6,
            weather.hourlyForecast[5].getHour(context, location.timeZone)
        )
        weather.hourlyForecast[5].weatherCode?.let {
            views.setImageViewUri(
                R.id.widget_material_you_forecast_hourlyIcon_6,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    weather.hourlyForecast[5].weatherCode,
                    weather.hourlyForecast[5].isDaylight,
                    false,
                    NotificationTextColor.LIGHT
                )
            )
        }
        views.setTextViewText(
            R.id.widget_material_you_forecast_hourlyTemperature_6,
            weather.hourlyForecast[5].temperature?.getShortTemperature(context, temperatureUnit)
        )
    }

    // daily.
    weather.dailyForecast.getOrNull(0)?.let {
        views.setTextViewText(
            R.id.widget_material_you_forecast_week_1,
            if (weather.dailyForecast[0].isToday(location.timeZone)) {
                context.getString(R.string.today)
            } else {
                weather.dailyForecast[0].getWeek(context, location.timeZone)
            }
        )
        weather.dailyForecast[0].day?.weatherCode?.let {
            views.setImageViewUri(
                R.id.widget_material_you_forecast_dayIcon_1,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    weather.dailyForecast[0].day?.weatherCode,
                    true,
                    false,
                    NotificationTextColor.LIGHT
                )
            )
        }
        views.setTextViewText(
            R.id.widget_material_you_forecast_dayTemperature_1,
            weather.dailyForecast[0].day?.temperature?.getShortTemperature(context, temperatureUnit)
        )
        views.setTextViewText(
            R.id.widget_material_you_forecast_nightTemperature_1,
            weather.dailyForecast[0].night?.temperature?.getShortTemperature(context, temperatureUnit)
        )
        weather.dailyForecast[0].night?.weatherCode?.let {
            views.setImageViewUri(
                R.id.widget_material_you_forecast_nightIcon_1,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    weather.dailyForecast[0].night?.weatherCode,
                    false,
                    false,
                    NotificationTextColor.LIGHT
                )
            )
        }
    }
    weather.dailyForecast.getOrNull(1)?.let {
        views.setTextViewText(
            R.id.widget_material_you_forecast_week_2,
            if (weather.dailyForecast[1].isToday(location.timeZone)) {
                context.getString(R.string.today)
            } else {
                weather.dailyForecast[1].getWeek(context, location.timeZone)
            }
        )
        weather.dailyForecast[1].day?.weatherCode?.let {
            views.setImageViewUri(
                R.id.widget_material_you_forecast_dayIcon_2,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    weather.dailyForecast[1].day?.weatherCode,
                    true,
                    false,
                    NotificationTextColor.LIGHT
                )
            )
        }
        views.setTextViewText(
            R.id.widget_material_you_forecast_dayTemperature_2,
            weather.dailyForecast[1].day?.temperature?.getShortTemperature(context, temperatureUnit)
        )
        views.setTextViewText(
            R.id.widget_material_you_forecast_nightTemperature_2,
            weather.dailyForecast[1].night?.temperature?.getShortTemperature(context, temperatureUnit)
        )
        weather.dailyForecast[1].night?.weatherCode?.let {
            views.setImageViewUri(
                R.id.widget_material_you_forecast_nightIcon_2,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    weather.dailyForecast[1].night?.weatherCode,
                    false,
                    false,
                    NotificationTextColor.LIGHT
                )
            )
        }
    }
    weather.dailyForecast.getOrNull(2)?.let {
        views.setTextViewText(
            R.id.widget_material_you_forecast_week_3,
            weather.dailyForecast[2].getWeek(context, location.timeZone)
        )
        weather.dailyForecast[2].day?.weatherCode?.let {
            views.setImageViewUri(
                R.id.widget_material_you_forecast_dayIcon_3,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    weather.dailyForecast[2].day?.weatherCode,
                    true,
                    false,
                    NotificationTextColor.LIGHT
                )
            )
        }
        views.setTextViewText(
            R.id.widget_material_you_forecast_dayTemperature_3,
            weather.dailyForecast[2].day?.temperature?.getShortTemperature(context, temperatureUnit)
        )
        views.setTextViewText(
            R.id.widget_material_you_forecast_nightTemperature_3,
            weather.dailyForecast[2].night?.temperature?.getShortTemperature(context, temperatureUnit)
        )
        weather.dailyForecast[2].night?.weatherCode?.let {
            views.setImageViewUri(
                R.id.widget_material_you_forecast_nightIcon_3,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    weather.dailyForecast[2].night?.weatherCode,
                    false,
                    false,
                    NotificationTextColor.LIGHT
                )
            )
        }
    }
    weather.dailyForecast.getOrNull(3)?.let {
        views.setTextViewText(
            R.id.widget_material_you_forecast_week_4,
            weather.dailyForecast[3].getWeek(context, location.timeZone)
        )
        weather.dailyForecast[3].day?.weatherCode?.let {
            views.setImageViewUri(
                R.id.widget_material_you_forecast_dayIcon_4,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    weather.dailyForecast[3].day?.weatherCode,
                    true,
                    false,
                    NotificationTextColor.LIGHT
                )
            )
        }
        views.setTextViewText(
            R.id.widget_material_you_forecast_dayTemperature_4,
            weather.dailyForecast[3].day?.temperature?.getShortTemperature(context, temperatureUnit)
        )
        views.setTextViewText(
            R.id.widget_material_you_forecast_nightTemperature_4,
            weather.dailyForecast[3].night?.temperature?.getShortTemperature(context, temperatureUnit)
        )
        weather.dailyForecast[3].night?.weatherCode?.let {
            views.setImageViewUri(
                R.id.widget_material_you_forecast_nightIcon_4,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    weather.dailyForecast[3].night?.weatherCode,
                    false,
                    false,
                    NotificationTextColor.LIGHT
                )
            )
        }
    }
    weather.dailyForecast.getOrNull(4)?.let {
        views.setTextViewText(
            R.id.widget_material_you_forecast_week_5,
            weather.dailyForecast[4].getWeek(context, location.timeZone)
        )
        weather.dailyForecast[4].day?.weatherCode?.let {
            views.setImageViewUri(
                R.id.widget_material_you_forecast_dayIcon_5,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    weather.dailyForecast[4].day?.weatherCode,
                    true,
                    false,
                    NotificationTextColor.LIGHT
                )
            )
        }
        views.setTextViewText(
            R.id.widget_material_you_forecast_dayTemperature_5,
            weather.dailyForecast[4].day?.temperature?.getShortTemperature(context, temperatureUnit)
        )
        views.setTextViewText(
            R.id.widget_material_you_forecast_nightTemperature_5,
            weather.dailyForecast[4].night?.temperature?.getShortTemperature(context, temperatureUnit)
        )
        weather.dailyForecast[4].night?.weatherCode?.let {
            views.setImageViewUri(
                R.id.widget_material_you_forecast_nightIcon_5,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    weather.dailyForecast[4].night?.weatherCode,
                    false,
                    false,
                    NotificationTextColor.LIGHT
                )
            )
        }
    }
    weather.dailyForecast.getOrNull(5)?.let {
        views.setTextViewText(
            R.id.widget_material_you_forecast_week_6,
            weather.dailyForecast[5].getWeek(context, location.timeZone)
        )
        weather.dailyForecast[5].day?.weatherCode?.let {
            views.setImageViewUri(
                R.id.widget_material_you_forecast_dayIcon_6,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    weather.dailyForecast[5].day?.weatherCode,
                    true,
                    false,
                    NotificationTextColor.LIGHT
                )
            )
        }
        views.setTextViewText(
            R.id.widget_material_you_forecast_dayTemperature_6,
            weather.dailyForecast[5].day?.temperature?.getShortTemperature(context, temperatureUnit)
        )
        views.setTextViewText(
            R.id.widget_material_you_forecast_nightTemperature_6,
            weather.dailyForecast[5].night?.temperature?.getShortTemperature(context, temperatureUnit)
        )
        weather.dailyForecast[5].night?.weatherCode?.let {
            views.setImageViewUri(
                R.id.widget_material_you_forecast_nightIcon_6,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider,
                    weather.dailyForecast[5].night?.weatherCode,
                    false,
                    false,
                    NotificationTextColor.LIGHT
                )
            )
        }
    }

    // pending intent.
    views.setOnClickPendingIntent(
        android.R.id.background,
        AbstractRemoteViewsPresenter.getWeatherPendingIntent(
            context,
            location,
            GeometricWeather.WIDGET_MATERIAL_YOU_FORECAST_PENDING_INTENT_CODE_WEATHER
        )
    )

    return views
}