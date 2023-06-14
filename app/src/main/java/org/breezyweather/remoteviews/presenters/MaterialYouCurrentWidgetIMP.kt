package org.breezyweather.remoteviews.presenters

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.background.receiver.widget.WidgetMaterialYouCurrentProvider
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.NotificationTextColor
import org.breezyweather.settings.SettingsManager

class MaterialYouCurrentWidgetIMP: AbstractRemoteViewsPresenter() {

    companion object {

        @JvmStatic
        fun isEnable(context: Context): Boolean {
            return AppWidgetManager.getInstance(
                context
            ).getAppWidgetIds(
                ComponentName(
                    context,
                    WidgetMaterialYouCurrentProvider::class.java
                )
            ).isNotEmpty()
        }

        @JvmStatic
        fun updateWidgetView(context: Context, location: Location) {
            AppWidgetManager.getInstance(context).updateAppWidget(
                ComponentName(context, WidgetMaterialYouCurrentProvider::class.java),
                buildRemoteViews(context, location, R.layout.widget_material_you_current)
            )
        }
    }
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

    val provider = org.breezyweather.theme.resource.ResourcesProviderFactory.getNewInstance()

    val settings = SettingsManager.getInstance(context)
    val temperatureUnit = settings.temperatureUnit

    if (weather?.current == null) {
        return views
    }

    // current.
    if (weather.current.weatherCode != null) {
        views.setImageViewUri(
            R.id.widget_material_you_current_currentIcon,
            org.breezyweather.theme.resource.ResourceHelper.getWidgetNotificationIconUri(
                provider,
                weather.current.weatherCode,
                dayTime,
                false,
                NotificationTextColor.LIGHT
            )
        )
    }
    if (weather.current.temperature?.temperature != null) {
        views.setTextViewText(
            R.id.widget_material_you_current_currentTemperature,
            weather.current.temperature.getShortTemperature(context, temperatureUnit)
        )
    }

    // pending intent.
    views.setOnClickPendingIntent(
        android.R.id.background,
        AbstractRemoteViewsPresenter.getWeatherPendingIntent(
            context,
            location,
            BreezyWeather.WIDGET_MATERIAL_YOU_CURRENT_PENDING_INTENT_CODE_WEATHER
        )
    )

    return views
}