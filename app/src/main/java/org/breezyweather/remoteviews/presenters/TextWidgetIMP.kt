package org.breezyweather.remoteviews.presenters

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import org.breezyweather.R
import org.breezyweather.background.receiver.widget.WidgetTextProvider
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.extensions.spToPx
import org.breezyweather.remoteviews.Widgets
import org.breezyweather.settings.SettingsManager

object TextWidgetIMP : AbstractRemoteViewsPresenter() {

    fun updateWidgetView(context: Context, location: Location) {
        val config = getWidgetConfig(context, context.getString(R.string.sp_widget_text_setting))
        val views = getRemoteViews(context, location, config.textColor, config.textSize, config.alignEnd)
        AppWidgetManager.getInstance(context).updateAppWidget(
            ComponentName(context, WidgetTextProvider::class.java),
            views
        )
    }

    fun getRemoteViews(
        context: Context, location: Location, textColor: String?, textSize: Int, alignEnd: Boolean
    ): RemoteViews {
        val settings = SettingsManager.getInstance(context)
        val temperatureUnit = settings.temperatureUnit
        val views = RemoteViews(
            context.packageName,
            if (alignEnd) R.layout.widget_text_end else R.layout.widget_text
        )
        val weather = location.weather ?: return views
        val darkText = textColor == "dark" || textColor == "auto" && isLightWallpaper(context)
        val textColorInt: Int = ContextCompat.getColor(
            context, if (darkText) R.color.colorTextDark else R.color.colorTextLight
        )
        views.apply {
            setTextViewText(
                R.id.widget_text_weather,
                weather.current?.weatherText
                    ?: context.getString(R.string.null_data_text)
            )
            setTextViewText(
                R.id.widget_text_temperature,
                weather.current?.temperature?.getShortTemperature(context, temperatureUnit)
            )
            setTextColor(R.id.widget_text_date, textColorInt)
            setTextColor(R.id.widget_text_weather, textColorInt)
            setTextColor(R.id.widget_text_temperature, textColorInt)
        }
        if (textSize != 100) {
            val contentSize = context.resources.getDimensionPixelSize(R.dimen.widget_content_text_size)
                .toFloat() * textSize / 100f
            val temperatureSize = context.spToPx(48) * textSize / 100f
            views.apply {
                setTextViewTextSize(R.id.widget_text_date, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_text_weather, TypedValue.COMPLEX_UNIT_PX, contentSize)
                setTextViewTextSize(R.id.widget_text_temperature, TypedValue.COMPLEX_UNIT_PX, temperatureSize)
            }
        }
        setOnClickPendingIntent(context, views, location)
        return views
    }

    fun isInUse(context: Context): Boolean {
        val widgetIds = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, WidgetTextProvider::class.java))
        return widgetIds != null && widgetIds.isNotEmpty()
    }

    private fun setOnClickPendingIntent(context: Context, views: RemoteViews, location: Location) {
        // headerContainer.
        views.setOnClickPendingIntent(
            R.id.widget_text_container,
            getWeatherPendingIntent(
                context, location, Widgets.TEXT_PENDING_INTENT_CODE_WEATHER
            )
        )

        // date.
        views.setOnClickPendingIntent(
            R.id.widget_text_date,
            getCalendarPendingIntent(
                context, Widgets.TEXT_PENDING_INTENT_CODE_CALENDAR
            )
        )
    }
}
