/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
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

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import org.breezyweather.R
import org.breezyweather.background.receiver.widget.WidgetTrendHourlyProvider
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.extensions.getTabletListAdaptiveWidth
import org.breezyweather.common.utils.helpers.AsyncHelper
import org.breezyweather.remoteviews.Widgets
import org.breezyweather.remoteviews.trend.TrendLinearLayout
import org.breezyweather.remoteviews.trend.WidgetItemView
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.ResourcesProviderFactory
import org.breezyweather.theme.weatherView.WeatherViewController
import kotlin.math.max
import kotlin.math.min

object HourlyTrendWidgetIMP : AbstractRemoteViewsPresenter() {

    fun updateWidgetView(context: Context, location: Location) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            innerUpdateWidget(context, location)
            return
        }
        AsyncHelper.runOnIO { innerUpdateWidget(context, location) }
    }

    @WorkerThread
    private fun innerUpdateWidget(context: Context, location: Location) {
        val config = getWidgetConfig(
            context,
            context.getString(R.string.sp_widget_hourly_trend_setting)
        )
        if (config.cardStyle == "none") {
            config.cardStyle = "light"
        }
        AppWidgetManager.getInstance(context).updateAppWidget(
            ComponentName(context, WidgetTrendHourlyProvider::class.java),
            getRemoteViews(
                context, location,
                context.getTabletListAdaptiveWidth(context.resources.displayMetrics.widthPixels),
                config.cardStyle, config.cardAlpha
            )
        )
    }

    @WorkerThread
    @SuppressLint("InflateParams", "WrongThread")
    private fun getDrawableView(context: Context, location: Location?, cardStyle: String?): View? {
        val weather = location?.weather ?: return null
        val provider = ResourcesProviderFactory.newInstance
        val itemCount = min(5, weather.hourlyForecast.size)
        var highestTemperature: Float? = null
        var lowestTemperature: Float? = null
        val minimalIcon = SettingsManager.getInstance(context).isWidgetUsingMonochromeIcons
        val temperatureUnit = SettingsManager.getInstance(context).temperatureUnit
        val lightTheme: Boolean = when (cardStyle) {
            "light" -> true
            "dark" -> false
            else -> location.isDaylight
        }

        // TODO: Redundant with HourlyTemperatureAdapter
        val temperatures: Array<Float?> = arrayOfNulls(max(0, itemCount * 2 - 1))
        run {
            var i = 0
            while (i < temperatures.size) {
                temperatures[i] = weather.hourlyForecast.getOrNull(i / 2)?.temperature?.temperature
                i += 2
            }
        }
        run {
            var i = 1
            while (i < temperatures.size) {
                if (temperatures[i - 1] != null && temperatures[i + 1] != null) {
                    temperatures[i] = (temperatures[i - 1]!! + temperatures[i + 1]!!) * 0.5f
                } else {
                    temperatures[i] = null
                }
                i += 2
            }
        }
        weather.yesterday?.let { yesterday ->
            highestTemperature = yesterday.daytimeTemperature
            lowestTemperature = yesterday.nighttimeTemperature
        }
        for (i in 0 until itemCount) {
            weather.hourlyForecast[i].temperature?.temperature?.let {
                if (highestTemperature == null || it > highestTemperature!!) {
                    highestTemperature = it
                }
                if (lowestTemperature == null || it < lowestTemperature!!) {
                    lowestTemperature = it
                }
            }
        }

        val drawableView = LayoutInflater.from(context)
            .inflate(R.layout.widget_trend_hourly, null, false)
        if (weather.yesterday?.daytimeTemperature != null && weather.yesterday.nighttimeTemperature != null
            && highestTemperature != null && lowestTemperature != null) {
            val trendParent = drawableView.findViewById<TrendLinearLayout>(R.id.widget_trend_hourly)
            trendParent.setData(
                arrayOf(weather.yesterday.daytimeTemperature, weather.yesterday.nighttimeTemperature),
                highestTemperature!!,
                lowestTemperature!!,
                temperatureUnit,
                false
            )
            trendParent.setColor(lightTheme)
            trendParent.setKeyLineVisibility(
                SettingsManager.getInstance(context).isTrendHorizontalLinesEnabled
            )
        }

        val colors = ThemeManager.getInstance(context).weatherThemeDelegate.getThemeColors(
            context, WeatherViewController.getWeatherKind(weather), location.isDaylight
        )
        val widgetItemViews: Array<WidgetItemView> = arrayOf(
            drawableView.findViewById(R.id.widget_trend_hourly_item_1),
            drawableView.findViewById(R.id.widget_trend_hourly_item_2),
            drawableView.findViewById(R.id.widget_trend_hourly_item_3),
            drawableView.findViewById(R.id.widget_trend_hourly_item_4),
            drawableView.findViewById(R.id.widget_trend_hourly_item_5)
        )
        widgetItemViews.forEachIndexed { i, widgetItemView ->
            weather.hourlyForecast.getOrNull(i)?.let { hourly ->
                widgetItemView.setTitleText(hourly.getHour(context, location.timeZone))
                widgetItemView.setSubtitleText(null)
                hourly.weatherCode?.let {
                    widgetItemView.setTopIconDrawable(
                        ResourceHelper.getWidgetNotificationIcon(
                            provider, it, hourly.isDaylight, minimalIcon, lightTheme
                        )
                    )
                }
                widgetItemView.trendItemView.setData(
                    buildTemperatureArrayForItem(temperatures, i),
                    null,
                    hourly.temperature?.getShortTemperature(context, temperatureUnit),
                    null,
                    highestTemperature,
                    lowestTemperature,
                    null, null, null, null
                )
                widgetItemView.trendItemView.setLineColors(
                    colors[1],
                    colors[2],
                    if (lightTheme) {
                        ColorUtils.setAlphaComponent(Color.BLACK, (255 * 0.05).toInt())
                    } else ColorUtils.setAlphaComponent(Color.WHITE, (255 * 0.1).toInt())
                )
                widgetItemView.trendItemView.setShadowColors(colors[1], colors[2], lightTheme)
                widgetItemView.trendItemView.setTextColors(
                    ContextCompat.getColor(context, if (lightTheme) R.color.colorTextDark else R.color.colorTextLight),
                    ContextCompat.getColor(context, if (lightTheme) R.color.colorTextDark2nd else R.color.colorTextLight2nd),
                    ContextCompat.getColor(context, if (lightTheme) R.color.colorTextGrey2nd else R.color.colorTextGrey)
                )
                widgetItemView.trendItemView.setHistogramAlpha(if (lightTheme) 0.2f else 0.5f)
                widgetItemView.setBottomIconDrawable(null)
                widgetItemView.setColor(lightTheme)
            }
        }
        return drawableView
    }

    @WorkerThread
    fun getRemoteViews(
        context: Context, location: Location?, width: Int, cardStyle: String?, cardAlpha: Int
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_remote)
        val drawableView = getDrawableView(context, location, cardStyle) ?: return views

        val items: Array<WidgetItemView> = arrayOf(
            drawableView.findViewById(R.id.widget_trend_hourly_item_1),
            drawableView.findViewById(R.id.widget_trend_hourly_item_2),
            drawableView.findViewById(R.id.widget_trend_hourly_item_3),
            drawableView.findViewById(R.id.widget_trend_hourly_item_4),
            drawableView.findViewById(R.id.widget_trend_hourly_item_5)
        )
        for (i in items) {
            i.setSize(width / 5f)
        }
        drawableView.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        drawableView.layout(
            0,
            0,
            drawableView.measuredWidth,
            drawableView.measuredHeight
        )
        val cache = Bitmap.createBitmap(
            drawableView.measuredWidth,
            drawableView.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(cache)
        drawableView.draw(canvas)
        views.setImageViewBitmap(R.id.widget_remote_drawable, cache)
        views.setViewVisibility(R.id.widget_remote_progress, View.GONE)
        val colorType = when (cardStyle) {
            "light" -> WidgetColor.ColorType.LIGHT
            "dark" -> WidgetColor.ColorType.DARK
            else -> WidgetColor.ColorType.AUTO
        }
        views.setImageViewResource(R.id.widget_remote_card, getCardBackgroundId(colorType))
        views.setInt(
            R.id.widget_remote_card, "setImageAlpha", (cardAlpha / 100.0 * 255).toInt()
        )
        setOnClickPendingIntent(context, views, location!!)
        return views
    }

    fun isInUse(context: Context): Boolean {
        val widgetIds = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, WidgetTrendHourlyProvider::class.java))
        return widgetIds != null && widgetIds.isNotEmpty()
    }

    private fun buildTemperatureArrayForItem(temps: Array<Float?>, index: Int): Array<Float?> {
        val a = arrayOfNulls<Float>(3)
        a[1] = temps[2 * index]
        if (2 * index - 1 < 0) {
            a[0] = null
        } else {
            a[0] = temps[2 * index - 1]
        }
        if (2 * index + 1 >= temps.size) {
            a[2] = null
        } else {
            a[2] = temps[2 * index + 1]
        }
        return a
    }

    private fun setOnClickPendingIntent(
        context: Context, views: RemoteViews, location: Location
    ) {
        views.setOnClickPendingIntent(
            R.id.widget_remote_drawable,
            getWeatherPendingIntent(
                context, location, Widgets.TREND_HOURLY_PENDING_INTENT_CODE_WEATHER
            )
        )
    }
}
