package org.breezyweather.remoteviews.presenters

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import org.breezyweather.R
import org.breezyweather.background.receiver.widget.WidgetClockDayWeekProvider
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.utils.helpers.LunarHelper
import org.breezyweather.remoteviews.Widgets
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.ResourcesProviderFactory
import java.util.*

object ClockDayWeekWidgetIMP : AbstractRemoteViewsPresenter() {

    fun updateWidgetView(context: Context, location: Location) {
        val config = getWidgetConfig(context, context.getString(R.string.sp_widget_clock_day_week_setting))
        val views = getRemoteViews(
            context, location,
            config.cardStyle, config.cardAlpha, config.textColor, config.textSize, config.clockFont, config.hideLunar
        )
        AppWidgetManager.getInstance(context).updateAppWidget(
            ComponentName(context, WidgetClockDayWeekProvider::class.java),
            views
        )
    }

    fun getRemoteViews(
        context: Context, location: Location?,
        cardStyle: String?, cardAlpha: Int, textColor: String?, textSize: Int, clockFont: String?, hideLunar: Boolean
    ): RemoteViews {
        val color = WidgetColor(context, cardStyle!!, textColor!!)
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
        weather.current?.weatherCode?.let {
            views.setViewVisibility(R.id.widget_clock_day_week_icon, View.VISIBLE)
            views.setImageViewUri(
                R.id.widget_clock_day_week_icon,
                ResourceHelper.getWidgetNotificationIconUri(
                    provider, it, dayTime, minimalIcon, color.minimalIconColor
                )
            )
        } ?: views.setViewVisibility(R.id.widget_clock_day_week_icon, View.INVISIBLE)
        views.setTextViewText(
            R.id.widget_clock_day_week_lunar,
            if (settings.language.isChinese && !hideLunar) " - " + LunarHelper.getLunarDate(Date()) else ""
        )
        val builder = StringBuilder()
        builder.append(location.getPlace(context))
        if (weather.current?.temperature?.temperature != null) {
            builder.append(" ").append(weather.current.temperature.getTemperature(context, temperatureUnit, 0))
        }
        views.setTextViewText(R.id.widget_clock_day_week_subtitle, builder.toString())

        val weekIconDaytime = isWeekIconDaytime(weekIconMode, dayTime)
        val dailyIds = arrayOf(
            arrayOf(R.id.widget_clock_day_week_week_1, R.id.widget_clock_day_week_temp_1, R.id.widget_clock_day_week_icon_1),
            arrayOf(R.id.widget_clock_day_week_week_2, R.id.widget_clock_day_week_temp_2, R.id.widget_clock_day_week_icon_2),
            arrayOf(R.id.widget_clock_day_week_week_3, R.id.widget_clock_day_week_temp_3, R.id.widget_clock_day_week_icon_3),
            arrayOf(R.id.widget_clock_day_week_week_4, R.id.widget_clock_day_week_temp_4, R.id.widget_clock_day_week_icon_4),
            arrayOf(R.id.widget_clock_day_week_week_5, R.id.widget_clock_day_week_temp_5, R.id.widget_clock_day_week_icon_5),
        )
        dailyIds.forEachIndexed { i, dailyId ->
            weather.dailyForecast.getOrNull(i)?.let {
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
                    weather.dailyForecast.getOrNull(i)?.night?.temperature?.temperature,
                    weather.dailyForecast.getOrNull(i)?.day?.temperature?.temperature,
                    temperatureUnit
                )
            )
            if (weekIconDaytime) {
                weather.dailyForecast.getOrNull(i)?.day?.weatherCode?.let {
                    views.setViewVisibility(dailyId[2], View.VISIBLE)
                    views.setImageViewUri(
                        dailyId[2],
                        ResourceHelper.getWidgetNotificationIconUri(
                            provider, it, weekIconDaytime, minimalIcon, color.minimalIconColor
                        )
                    )
                } ?: views.setViewVisibility(dailyId[2], View.INVISIBLE)
            } else {
                weather.dailyForecast.getOrNull(i)?.night?.weatherCode?.let {
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
                setTextColor(R.id.widget_clock_day_week_clock_light, color.textColor)
                setTextColor(R.id.widget_clock_day_week_clock_normal, color.textColor)
                setTextColor(R.id.widget_clock_day_week_clock_black, color.textColor)
                setTextColor(R.id.widget_clock_day_week_clock_aa_light, color.textColor)
                setTextColor(R.id.widget_clock_day_week_clock_aa_normal, color.textColor)
                setTextColor(R.id.widget_clock_day_week_clock_aa_black, color.textColor)
                setTextColor(R.id.widget_clock_day_week_title, color.textColor)
                setTextColor(R.id.widget_clock_day_week_lunar, color.textColor)
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
                setTextViewTextSize(R.id.widget_clock_day_week_lunar, TypedValue.COMPLEX_UNIT_PX, contentSize)
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
            views.setImageViewResource(R.id.widget_clock_day_week_card, getCardBackgroundId(color.cardColor))
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
            getWeatherPendingIntent(
                context, location, Widgets.CLOCK_DAY_WEEK_PENDING_INTENT_CODE_WEATHER
            )
        )

        // daily forecast.
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_week_icon_1,
            getDailyForecastPendingIntent(
                context, location, 0, Widgets.CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_1
            )
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_week_icon_2,
            getDailyForecastPendingIntent(
                context, location, 1, Widgets.CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_2
            )
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_week_icon_3,
            getDailyForecastPendingIntent(
                context, location, 2, Widgets.CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_3
            )
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_week_icon_4,
            getDailyForecastPendingIntent(
                context, location, 3, Widgets.CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_4
            )
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_week_icon_5,
            getDailyForecastPendingIntent(
                context, location,4, Widgets.CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_5
            )
        )

        // clock.
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_week_clock_light,
            getAlarmPendingIntent(
                context, Widgets.CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CLOCK_LIGHT
            )
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_week_clock_normal,
            getAlarmPendingIntent(
                context, Widgets.CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CLOCK_NORMAL
            )
        )
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_week_clock_black,
            getAlarmPendingIntent(
                context, Widgets.CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CLOCK_BLACK
            )
        )

        // title.
        views.setOnClickPendingIntent(
            R.id.widget_clock_day_week_title,
            getCalendarPendingIntent(
                context, Widgets.CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CALENDAR
            )
        )
    }
}
