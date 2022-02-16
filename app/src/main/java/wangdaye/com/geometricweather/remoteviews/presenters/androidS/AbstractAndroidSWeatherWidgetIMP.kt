package wangdaye.com.geometricweather.remoteviews.presenters.androidS

import android.content.Context
import android.graphics.Color
import android.widget.RemoteViews
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.options.NotificationTextColor
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode
import wangdaye.com.geometricweather.remoteviews.presenters.AbstractRemoteViewsPresenter
import wangdaye.com.geometricweather.theme.resource.ResourceHelper
import wangdaye.com.geometricweather.theme.resource.ResourcesProviderFactory
import wangdaye.com.geometricweather.settings.SettingsManager

open class AbstractAndroidSWeatherWidgetIMP: AbstractRemoteViewsPresenter()

internal fun buildWeatherWidget(
    context: Context,
    layoutId: Int,
    pendingIntentCode: Int,
    location: Location
): RemoteViews {

    val views = RemoteViews(
        context.packageName,
        layoutId
    )

    val weather = location.weather
    val dayTime = location.isDaylight

    val provider = ResourcesProviderFactory.getNewInstance()

    val settings = SettingsManager.getInstance(context)
    val temperatureUnit = settings.getTemperatureUnit()

    views.setTextViewText(
        R.id.widget_s_card_background_city,
        location.getCityName(context)
    )
    if (weather == null) {
        return views
    }

    // current.

    views.setImageViewUri(
        R.id.widget_s_card_background_currentIcon,
        ResourceHelper.getWidgetNotificationIconUri(
            provider,
            weather.current.weatherCode,
            dayTime,
            false,
            NotificationTextColor.LIGHT
        )
    )

    views.setTextViewText(
        R.id.widget_s_card_background_currentTemperature,
        weather.current.temperature.getShortTemperature(context, temperatureUnit)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_daytimeTemperature,
        weather.dailyForecast[0].day().temperature.getShortTemperature(context, temperatureUnit)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_nighttimeTemperature,
        weather.dailyForecast[0].night().temperature.getShortTemperature(context, temperatureUnit)
    )

    if (weather.current.temperature.realFeelTemperature != null) {
        views.setTextViewText(
            R.id.widget_s_card_background_realFeelTemperature,
            context.getString(R.string.feels_like)
                    + " "
                    + weather.current.temperature.getShortRealFeeTemperature(context, temperatureUnit)
        )
    } else {
        views.setTextViewText(
            R.id.widget_s_card_background_realFeelTemperature,
            weather.current.weatherText
        )
    }

    if (weather.current.airQuality.aqiText == null) {
        views.setTextViewText(
            R.id.widget_s_card_background_windOrAqi,
            context.getString(R.string.wind) + " - "
                    + location.weather!!.current.wind.shortWindDescription
        )
    } else {
        views.setTextViewText(
            R.id.widget_s_card_background_windOrAqi,
            (context.getString(R.string.air_quality) + " - "
                    + location.weather!!.current.airQuality.aqiText)
        )
    }

    // hourly.

    views.setTextViewText(
        R.id.widget_s_card_background_hour_1,
        weather.hourlyForecast[0].getHour(context)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_hour_2,
        weather.hourlyForecast[1].getHour(context)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_hour_3,
        weather.hourlyForecast[2].getHour(context)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_hour_4,
        weather.hourlyForecast[3].getHour(context)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_hour_5,
        weather.hourlyForecast[4].getHour(context)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_hour_6,
        weather.hourlyForecast[5].getHour(context)
    )

    views.setImageViewUri(
        R.id.widget_s_card_background_hourlyIcon_1,
        ResourceHelper.getWidgetNotificationIconUri(
            provider,
            weather.hourlyForecast[0].weatherCode,
            weather.hourlyForecast[0].isDaylight,
            false,
            NotificationTextColor.LIGHT
        )
    )
    views.setImageViewUri(
        R.id.widget_s_card_background_hourlyIcon_2,
        ResourceHelper.getWidgetNotificationIconUri(
            provider,
            weather.hourlyForecast[1].weatherCode,
            weather.hourlyForecast[1].isDaylight,
            false,
            NotificationTextColor.LIGHT
        )
    )
    views.setImageViewUri(
        R.id.widget_s_card_background_hourlyIcon_3,
        ResourceHelper.getWidgetNotificationIconUri(
            provider,
            weather.hourlyForecast[2].weatherCode,
            weather.hourlyForecast[2].isDaylight,
            false,
            NotificationTextColor.LIGHT
        )
    )
    views.setImageViewUri(
        R.id.widget_s_card_background_hourlyIcon_4,
        ResourceHelper.getWidgetNotificationIconUri(
            provider,
            weather.hourlyForecast[3].weatherCode,
            weather.hourlyForecast[3].isDaylight,
            false,
            NotificationTextColor.LIGHT
        )
    )
    views.setImageViewUri(
        R.id.widget_s_card_background_hourlyIcon_5,
        ResourceHelper.getWidgetNotificationIconUri(
            provider,
            weather.hourlyForecast[4].weatherCode,
            weather.hourlyForecast[4].isDaylight,
            false,
            NotificationTextColor.LIGHT
        )
    )
    views.setImageViewUri(
        R.id.widget_s_card_background_hourlyIcon_6,
        ResourceHelper.getWidgetNotificationIconUri(
            provider,
            weather.hourlyForecast[5].weatherCode,
            weather.hourlyForecast[5].isDaylight,
            false,
            NotificationTextColor.LIGHT
        )
    )

    views.setTextViewText(
        R.id.widget_s_card_background_hourlyTemperature_1,
        weather.hourlyForecast[0].temperature.getShortTemperature(context, temperatureUnit)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_hourlyTemperature_2,
        weather.hourlyForecast[1].temperature.getShortTemperature(context, temperatureUnit)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_hourlyTemperature_3,
        weather.hourlyForecast[2].temperature.getShortTemperature(context, temperatureUnit)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_hourlyTemperature_4,
        weather.hourlyForecast[3].temperature.getShortTemperature(context, temperatureUnit)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_hourlyTemperature_5,
        weather.hourlyForecast[4].temperature.getShortTemperature(context, temperatureUnit)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_hourlyTemperature_6,
        weather.hourlyForecast[5].temperature.getShortTemperature(context, temperatureUnit)
    )

    // summary.

    if (weather.alertList.isNotEmpty()) {
        views.setTextViewText(
            R.id.widget_s_card_background_summary,
            weather.alertList[0].description
        )
    } else if (!weather.current.dailyForecast.isNullOrEmpty()) {
        views.setTextViewText(
            R.id.widget_s_card_background_summary,
            weather.current.dailyForecast
        )
    } else if (!weather.current.hourlyForecast.isNullOrEmpty()) {
        views.setTextViewText(
            R.id.widget_s_card_background_summary,
            weather.current.hourlyForecast
        )
    } else {
        views.setTextViewText(
            R.id.widget_s_card_background_summary,
            if (dayTime) {
                weather.dailyForecast[0].day().weatherText
            } else {
                weather.dailyForecast[0].night().weatherText
            }
        )
    }

    // daily.

    views.setTextViewText(
        R.id.widget_s_card_background_week_1,
        if (weather.dailyForecast[0].isToday(location.timeZone)) {
            context.getString(R.string.today)
        } else {
            weather.dailyForecast[0].getWeek(context)
        }
    )
    views.setTextViewText(
        R.id.widget_s_card_background_week_2,
        if (weather.dailyForecast[1].isToday(location.timeZone)) {
            context.getString(R.string.today)
        } else {
            weather.dailyForecast[1].getWeek(context)
        }
    )
    views.setTextViewText(
        R.id.widget_s_card_background_week_3,
        weather.dailyForecast[2].getWeek(context)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_week_4,
        weather.dailyForecast[3].getWeek(context)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_week_5,
        weather.dailyForecast[4].getWeek(context)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_week_6,
        weather.dailyForecast[5].getWeek(context)
    )

    views.setImageViewUri(
        R.id.widget_s_card_background_dayIcon_1,
        ResourceHelper.getWidgetNotificationIconUri(
            provider,
            weather.dailyForecast[0].day().weatherCode,
            true,
            false,
            NotificationTextColor.LIGHT
        )
    )
    views.setImageViewUri(
        R.id.widget_s_card_background_dayIcon_2,
        ResourceHelper.getWidgetNotificationIconUri(
            provider,
            weather.dailyForecast[1].day().weatherCode,
            true,
            false,
            NotificationTextColor.LIGHT
        )
    )
    views.setImageViewUri(
        R.id.widget_s_card_background_dayIcon_3,
        ResourceHelper.getWidgetNotificationIconUri(
            provider,
            weather.dailyForecast[2].day().weatherCode,
            true,
            false,
            NotificationTextColor.LIGHT
        )
    )
    views.setImageViewUri(
        R.id.widget_s_card_background_dayIcon_4,
        ResourceHelper.getWidgetNotificationIconUri(
            provider,
            weather.dailyForecast[3].day().weatherCode,
            true,
            false,
            NotificationTextColor.LIGHT
        )
    )
    views.setImageViewUri(
        R.id.widget_s_card_background_dayIcon_5,
        ResourceHelper.getWidgetNotificationIconUri(
            provider,
            weather.dailyForecast[4].day().weatherCode,
            true,
            false,
            NotificationTextColor.LIGHT
        )
    )
    views.setImageViewUri(
        R.id.widget_s_card_background_dayIcon_6,
        ResourceHelper.getWidgetNotificationIconUri(
            provider,
            weather.dailyForecast[5].day().weatherCode,
            true,
            false,
            NotificationTextColor.LIGHT
        )
    )

    views.setTextViewText(
        R.id.widget_s_card_background_dayTemperature_1,
        weather.dailyForecast[0].day().temperature.getShortTemperature(context, temperatureUnit)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_dayTemperature_2,
        weather.dailyForecast[1].day().temperature.getShortTemperature(context, temperatureUnit)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_dayTemperature_3,
        weather.dailyForecast[2].day().temperature.getShortTemperature(context, temperatureUnit)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_dayTemperature_4,
        weather.dailyForecast[3].day().temperature.getShortTemperature(context, temperatureUnit)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_dayTemperature_5,
        weather.dailyForecast[4].day().temperature.getShortTemperature(context, temperatureUnit)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_dayTemperature_6,
        weather.dailyForecast[5].day().temperature.getShortTemperature(context, temperatureUnit)
    )

    views.setTextViewText(
        R.id.widget_s_card_background_nightTemperature_1,
        weather.dailyForecast[0].night().temperature.getShortTemperature(context, temperatureUnit)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_nightTemperature_2,
        weather.dailyForecast[1].night().temperature.getShortTemperature(context, temperatureUnit)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_nightTemperature_3,
        weather.dailyForecast[2].night().temperature.getShortTemperature(context, temperatureUnit)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_nightTemperature_4,
        weather.dailyForecast[3].night().temperature.getShortTemperature(context, temperatureUnit)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_nightTemperature_5,
        weather.dailyForecast[4].night().temperature.getShortTemperature(context, temperatureUnit)
    )
    views.setTextViewText(
        R.id.widget_s_card_background_nightTemperature_6,
        weather.dailyForecast[5].night().temperature.getShortTemperature(context, temperatureUnit)
    )

    views.setImageViewUri(
        R.id.widget_s_card_background_nightIcon_1,
        ResourceHelper.getWidgetNotificationIconUri(
            provider,
            weather.dailyForecast[0].night().weatherCode,
            false,
            false,
            NotificationTextColor.LIGHT
        )
    )
    views.setImageViewUri(
        R.id.widget_s_card_background_nightIcon_2,
        ResourceHelper.getWidgetNotificationIconUri(
            provider,
            weather.dailyForecast[1].night().weatherCode,
            false,
            false,
            NotificationTextColor.LIGHT
        )
    )
    views.setImageViewUri(
        R.id.widget_s_card_background_nightIcon_3,
        ResourceHelper.getWidgetNotificationIconUri(
            provider,
            weather.dailyForecast[2].night().weatherCode,
            false,
            false,
            NotificationTextColor.LIGHT
        )
    )
    views.setImageViewUri(
        R.id.widget_s_card_background_nightIcon_4,
        ResourceHelper.getWidgetNotificationIconUri(
            provider,
            weather.dailyForecast[3].night().weatherCode,
            false,
            false,
            NotificationTextColor.LIGHT
        )
    )
    views.setImageViewUri(
        R.id.widget_s_card_background_nightIcon_5,
        ResourceHelper.getWidgetNotificationIconUri(
            provider,
            weather.dailyForecast[4].night().weatherCode,
            false,
            false,
            NotificationTextColor.LIGHT
        )
    )
    views.setImageViewUri(
        R.id.widget_s_card_background_nightIcon_6,
        ResourceHelper.getWidgetNotificationIconUri(
            provider,
            weather.dailyForecast[5].night().weatherCode,
            false,
            false,
            NotificationTextColor.LIGHT
        )
    )

    // color.
    if (false) {
        // never color widget ... bad effect.
        views.setInt(
            R.id.widget_s_card_container,
            "setBackgroundResource",
            getWeatherBackgroundId(WeatherCode.CLEAR, true)
        )

        views.setTextColor(R.id.widget_s_card_background_city, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_currentTemperature, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_daytimeTemperature, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_nighttimeTemperature, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_realFeelTemperature, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_windOrAqi, Color.WHITE)

        views.setTextColor(R.id.widget_s_card_background_hour_1, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_hour_2, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_hour_3, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_hour_4, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_hour_5, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_hour_6, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_hourlyTemperature_1, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_hourlyTemperature_2, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_hourlyTemperature_3, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_hourlyTemperature_4, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_hourlyTemperature_5, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_hourlyTemperature_6, Color.WHITE)

        views.setTextColor(R.id.widget_s_card_background_summary, Color.WHITE)

        views.setTextColor(R.id.widget_s_card_background_week_1, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_week_2, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_week_3, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_week_4, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_week_5, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_week_6, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_dayTemperature_1, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_dayTemperature_2, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_dayTemperature_3, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_dayTemperature_4, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_dayTemperature_5, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_dayTemperature_6, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_nightTemperature_1, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_nightTemperature_2, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_nightTemperature_3, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_nightTemperature_4, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_nightTemperature_5, Color.WHITE)
        views.setTextColor(R.id.widget_s_card_background_nightTemperature_6, Color.WHITE)
    }

    // pending intent.
    views.setOnClickPendingIntent(
        R.id.widget_s_card_container,
        AbstractRemoteViewsPresenter.getWeatherPendingIntent(
            context,
            location,
            pendingIntentCode
        )
    )

    return views
}

private fun getWeatherBackgroundId(weatherCode: WeatherCode, daylight: Boolean): Int {
    when (weatherCode) {
        WeatherCode.CLEAR -> return if (daylight) {
            R.drawable.widget_background_s_clear_day
        } else {
            R.drawable.widget_background_s_clear_night
        }

        WeatherCode.PARTLY_CLOUDY -> return if (daylight) {
            R.drawable.widget_background_s_partly_cloudy_day
        } else {
            R.drawable.widget_background_s_partly_cloudy_night
        }

        WeatherCode.CLOUDY -> return if (daylight) {
            R.drawable.widget_background_s_cloudy_day
        } else {
            R.drawable.widget_background_s_cloudy_night
        }

        WeatherCode.RAIN -> return if (daylight) {
            R.drawable.widget_background_s_rain_day
        } else {
            R.drawable.widget_background_s_rain_night
        }

        WeatherCode.SNOW -> return if (daylight) {
            R.drawable.widget_background_s_snow_day
        } else {
            R.drawable.widget_background_s_snow_night
        }

        WeatherCode.WIND -> return R.drawable.widget_background_s_wind

        WeatherCode.FOG -> return R.drawable.widget_background_s_fog

        WeatherCode.HAZE -> return R.drawable.widget_background_s_haze

        WeatherCode.SLEET -> return if (daylight) {
            R.drawable.widget_background_s_sleet_day
        } else {
            R.drawable.widget_background_s_sleet_night
        }

        WeatherCode.HAIL -> return if (daylight) {
            R.drawable.widget_background_s_hail_day
        } else {
            R.drawable.widget_background_s_hail_night
        }

        WeatherCode.THUNDER -> return R.drawable.widget_background_s_thunder

        WeatherCode.THUNDERSTORM -> return R.drawable.widget_background_s_thunderstrom
    }
}