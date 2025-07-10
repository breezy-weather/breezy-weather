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

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.createBitmap
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.background.receiver.widget.WidgetTrendDailyProvider
import org.breezyweather.common.basic.models.options.basic.Utils
import org.breezyweather.common.extensions.getFormattedShortDayAndMonth
import org.breezyweather.common.extensions.getTabletListAdaptiveWidth
import org.breezyweather.common.utils.helpers.AsyncHelper
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.getWeek
import org.breezyweather.domain.weather.model.isToday
import org.breezyweather.remoteviews.Widgets
import org.breezyweather.remoteviews.trend.TrendLinearLayout
import org.breezyweather.remoteviews.trend.WidgetItemView
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory
import org.breezyweather.ui.theme.weatherView.WeatherViewController
import kotlin.math.max
import kotlin.math.min

object DailyTrendWidgetIMP : AbstractRemoteViewsPresenter() {

    fun updateWidgetView(
        context: Context,
        location: Location?,
    ) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            innerUpdateWidget(context, location)
            return
        }
        AsyncHelper.runOnIO { innerUpdateWidget(context, location) }
    }

    @WorkerThread
    private fun innerUpdateWidget(
        context: Context,
        location: Location?,
    ) {
        val config = getWidgetConfig(context, context.getString(R.string.sp_widget_daily_trend_setting))
        if (config.cardStyle == "none") {
            config.cardStyle = "auto"
        }
        AppWidgetManager.getInstance(context).updateAppWidget(
            ComponentName(context, WidgetTrendDailyProvider::class.java),
            getRemoteViews(
                context,
                location,
                context.getTabletListAdaptiveWidth(context.resources.displayMetrics.widthPixels),
                config.cardStyle,
                config.cardAlpha
            )
        )
    }

    @WorkerThread
    @SuppressLint("InflateParams", "WrongThread")
    private fun getDrawableView(
        context: Context,
        location: Location?,
        color: WidgetColor,
    ): View? {
        val weather = location?.weather ?: return null
        val provider = ResourcesProviderFactory.newInstance
        val itemCount = min(5, weather.dailyForecastStartingToday.size)
        var highestTemperature: Float? = null
        var lowestTemperature: Float? = null
        val minimalIcon = SettingsManager.getInstance(context).isWidgetUsingMonochromeIcons
        val temperatureUnit = SettingsManager.getInstance(context).temperatureUnit
        val lightTheme = color.isLightThemed

        // TODO: Redundant with DailyTemperatureAdapter
        val daytimeTemperatures: Array<Float?> = arrayOfNulls(max(0, itemCount * 2 - 1))
        run {
            var i = 0
            while (i < daytimeTemperatures.size) {
                daytimeTemperatures[i] =
                    weather.dailyForecastStartingToday.getOrNull(i / 2)?.day?.temperature?.temperature?.toFloat()
                i += 2
            }
        }
        run {
            var i = 1
            while (i < daytimeTemperatures.size) {
                if (daytimeTemperatures[i - 1] != null && daytimeTemperatures[i + 1] != null) {
                    daytimeTemperatures[i] = (daytimeTemperatures[i - 1]!! + daytimeTemperatures[i + 1]!!) * 0.5f
                } else {
                    daytimeTemperatures[i] = null
                }
                i += 2
            }
        }

        val nighttimeTemperatures: Array<Float?> = arrayOfNulls(max(0, itemCount * 2 - 1))
        run {
            var i = 0
            while (i < nighttimeTemperatures.size) {
                nighttimeTemperatures[i] =
                    weather.dailyForecastStartingToday.getOrNull(i / 2)?.night?.temperature?.temperature?.toFloat()
                i += 2
            }
        }
        run {
            var i = 1
            while (i < nighttimeTemperatures.size) {
                if (nighttimeTemperatures[i - 1] != null && nighttimeTemperatures[i + 1] != null) {
                    nighttimeTemperatures[i] = (nighttimeTemperatures[i - 1]!! + nighttimeTemperatures[i + 1]!!) * 0.5f
                } else {
                    nighttimeTemperatures[i] = null
                }
                i += 2
            }
        }

        weather.normals?.let { normals ->
            highestTemperature = normals.daytimeTemperature?.toFloat()
            lowestTemperature = normals.nighttimeTemperature?.toFloat()
        }

        for (i in 0 until itemCount) {
            weather.dailyForecast[i].day?.temperature?.temperature?.let {
                if (highestTemperature == null || it > highestTemperature!!) {
                    highestTemperature = it.toFloat()
                }
            }
            weather.dailyForecast[i].night?.temperature?.temperature?.let {
                if (lowestTemperature == null || it < lowestTemperature!!) {
                    lowestTemperature = it.toFloat()
                }
            }
        }

        val drawableView = LayoutInflater.from(context)
            .inflate(R.layout.widget_trend_daily, null, false)
        weather.normals?.let { normals ->
            if (normals.daytimeTemperature != null &&
                normals.nighttimeTemperature != null &&
                highestTemperature != null &&
                lowestTemperature != null
            ) {
                val trendParent = drawableView.findViewById<TrendLinearLayout>(R.id.widget_trend_daily)
                trendParent.normals = normals.month != null
                trendParent.setData(
                    arrayOf(normals.daytimeTemperature!!.toFloat(), normals.nighttimeTemperature!!.toFloat()),
                    highestTemperature!!,
                    lowestTemperature!!,
                    temperatureUnit,
                    true
                )
                trendParent.setColor(lightTheme)
                trendParent.setKeyLineVisibility(SettingsManager.getInstance(context).isTrendHorizontalLinesEnabled)
            }
        }

        val colors = ThemeManager.getInstance(context).weatherThemeDelegate.getThemeColors(
            context,
            WeatherViewController.getWeatherKind(location),
            WeatherViewController.isDaylight(location)
        )
        val widgetItemViews: Array<WidgetItemView> = arrayOf(
            drawableView.findViewById(R.id.widget_trend_daily_item_1),
            drawableView.findViewById(R.id.widget_trend_daily_item_2),
            drawableView.findViewById(R.id.widget_trend_daily_item_3),
            drawableView.findViewById(R.id.widget_trend_daily_item_4),
            drawableView.findViewById(R.id.widget_trend_daily_item_5)
        )
        widgetItemViews.forEachIndexed { i, widgetItemView ->
            weather.dailyForecastStartingToday.getOrNull(i)?.let { daily ->
                widgetItemView.setTitleText(
                    if (daily.isToday(location)) {
                        context.getString(R.string.daily_today_short)
                    } else {
                        daily.getWeek(location, context)
                    }
                )
                widgetItemView.setSubtitleText(
                    daily.date.getFormattedShortDayAndMonth(location, context)
                )
                daily.day?.weatherCode?.let {
                    widgetItemView.setTopIconDrawable(
                        ResourceHelper.getWidgetNotificationIcon(provider, it, true, minimalIcon, lightTheme)
                    )
                }
                val daytimePrecipitationProbability = daily.day?.precipitationProbability?.total?.toFloat()
                val nighttimePrecipitationProbability = daily.night?.precipitationProbability?.total?.toFloat()
                val p = max(
                    daytimePrecipitationProbability ?: 0f,
                    nighttimePrecipitationProbability ?: 0f
                )
                widgetItemView.trendItemView.setData(
                    buildTemperatureArrayForItem(daytimeTemperatures, i),
                    buildTemperatureArrayForItem(nighttimeTemperatures, i),
                    daily.day?.temperature?.temperature?.let {
                        temperatureUnit.getShortValueText(context, it)
                    },
                    daily.night?.temperature?.temperature?.let {
                        temperatureUnit.getShortValueText(context, it)
                    },
                    highestTemperature,
                    lowestTemperature,
                    if (p > 0) p else null,
                    if (p > 0) {
                        Utils.formatPercent(context, p.toDouble())
                    } else {
                        null
                    },
                    100f,
                    0f
                )
                widgetItemView.trendItemView.setLineColors(
                    colors[1],
                    colors[2],
                    if (lightTheme) {
                        ColorUtils.setAlphaComponent(Color.BLACK, (255 * 0.05).toInt())
                    } else {
                        ColorUtils.setAlphaComponent(Color.WHITE, (255 * 0.1).toInt())
                    }
                )
                widgetItemView.trendItemView.setShadowColors(colors[1], colors[2], lightTheme)
                widgetItemView.trendItemView.setTextColors(
                    ContextCompat.getColor(
                        context,
                        if (lightTheme) R.color.colorTextDark else R.color.colorTextLight
                    ),
                    ColorUtils.setAlphaComponent(
                        ContextCompat.getColor(
                            context,
                            if (lightTheme) R.color.colorTextDark else R.color.colorTextLight
                        ),
                        (255 * 0.6).toInt()
                    ),
                    ColorUtils.setAlphaComponent(
                        ContextCompat.getColor(
                            context,
                            if (lightTheme) R.color.colorTextDark2nd else R.color.colorTextLight2nd
                        ),
                        (255 * 0.6).toInt()
                    )
                )
                widgetItemView.trendItemView.setHistogramAlpha(if (lightTheme) 0.2f else 0.5f)
                daily.night?.weatherCode?.let {
                    widgetItemView.setBottomIconDrawable(
                        ResourceHelper.getWidgetNotificationIcon(provider, it, false, minimalIcon, lightTheme)
                    )
                }
                widgetItemView.setColor(lightTheme)
            }
        }
        return drawableView
    }

    @WorkerThread
    fun getRemoteViews(
        context: Context,
        location: Location?,
        width: Int,
        cardStyle: String?,
        cardAlpha: Int,
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_remote)
        val color = WidgetColor(
            context,
            cardStyle!!,
            "auto",
            location?.isDaylight ?: true
        )
        val drawableView = getDrawableView(context, location, color) ?: return views

        val widgetItemViews: Array<WidgetItemView> = arrayOf(
            drawableView.findViewById(R.id.widget_trend_daily_item_1),
            drawableView.findViewById(R.id.widget_trend_daily_item_2),
            drawableView.findViewById(R.id.widget_trend_daily_item_3),
            drawableView.findViewById(R.id.widget_trend_daily_item_4),
            drawableView.findViewById(R.id.widget_trend_daily_item_5)
        )
        for (i in widgetItemViews) {
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
        val cache = createBitmap(drawableView.measuredWidth, drawableView.measuredHeight)
        val canvas = Canvas(cache)
        drawableView.draw(canvas)
        views.setImageViewBitmap(R.id.widget_remote_drawable, cache)
        views.setViewVisibility(R.id.widget_remote_progress, View.GONE)
        views.setImageViewResource(R.id.widget_remote_card, getCardBackgroundId(color))
        views.setInt(R.id.widget_remote_card, "setImageAlpha", (cardAlpha / 100.0 * 255).toInt())
        setOnClickPendingIntent(context, views, location!!)
        return views
    }

    fun isInUse(context: Context): Boolean {
        val widgetIds = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, WidgetTrendDailyProvider::class.java))
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
        context: Context,
        views: RemoteViews,
        location: Location,
    ) {
        views.setOnClickPendingIntent(
            R.id.widget_remote_drawable,
            getWeatherPendingIntent(context, location, Widgets.TREND_DAILY_PENDING_INTENT_CODE_WEATHER)
        )
    }
}
