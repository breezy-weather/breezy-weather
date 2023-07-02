package org.breezyweather.remoteviews.presenters

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.background.receiver.widget.WidgetMaterialYouCurrentProvider
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.NotificationTextColor
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.ResourcesProviderFactory

class MaterialYouCurrentWidgetIMP: AbstractRemoteViewsPresenter() {

    companion object {

        @JvmStatic
        fun isEnabled(context: Context): Boolean {
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

    val provider = ResourcesProviderFactory.newInstance

    val settings = SettingsManager.getInstance(context)
    val temperatureUnit = settings.temperatureUnit

    // current.
    weather?.current?.weatherCode?.let {
        views.setViewVisibility(R.id.widget_material_you_current_currentIcon, View.VISIBLE)
        views.setImageViewUri(
            R.id.widget_material_you_current_currentIcon,
            ResourceHelper.getWidgetNotificationIconUri(
                provider,
                it,
                dayTime,
                false,
                NotificationTextColor.LIGHT
            )
        )
    } ?: views.setViewVisibility(R.id.widget_material_you_current_currentIcon, View.INVISIBLE)

    views.setTextViewText(
        R.id.widget_material_you_current_currentTemperature,
        weather?.current?.temperature?.getShortTemperature(context, temperatureUnit)
    )

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