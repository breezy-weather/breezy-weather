package org.breezyweather.main.adapters.trend.hourly

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.UV
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView.KeyLine
import org.breezyweather.common.ui.widgets.trend.chart.PolylineAndHistogramView
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.weatherView.WeatherViewController.getWeatherKind

/**
 * Hourly UV adapter.
 */
class HourlyUVAdapter(activity: GeoActivity, location: Location) : AbsHourlyTrendAdapter(
    activity, location
) {
    private var mHighestIndex: Int = 0

    inner class ViewHolder(itemView: View) : AbsHourlyTrendAdapter.ViewHolder(itemView) {
        private val mPolylineAndHistogramView = PolylineAndHistogramView(itemView.context)

        init {
            hourlyItem.chartItemView = mPolylineAndHistogramView
        }

        @SuppressLint("SetTextI18n, InflateParams", "DefaultLocale")
        fun onBindView(activity: GeoActivity, location: Location, position: Int) {
            val talkBackBuilder = StringBuilder(activity.getString(R.string.tag_uv))
            super.onBindView(activity, location, talkBackBuilder, position)
            val hourly = location.weather!!.hourlyForecast[position]
            hourly.uV?.let { uV ->
                val index = uV.index
                talkBackBuilder.append(", ").append(index).append(", ").append(uV.level)
                mPolylineAndHistogramView.setData(
                    null, null,
                    null, null,
                    null, null,
                    (index ?: 0).toFloat(), String.format("%d", index ?: 0), mHighestIndex.toFloat(),
                    0f
                )
                mPolylineAndHistogramView.setLineColors(
                    uV.getUVColor(activity),
                    uV.getUVColor(activity),
                    MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
                )
            }
            val themeColors = ThemeManager.getInstance(itemView.context)
                .weatherThemeDelegate
                .getThemeColors(
                    itemView.context,
                    getWeatherKind(location.weather),
                    location.isDaylight
                )
            val lightTheme = MainThemeColorProvider.isLightTheme(itemView.context, location)
            mPolylineAndHistogramView.setShadowColors(
                themeColors[if (lightTheme) 1 else 2],
                themeColors[2],
                lightTheme
            )
            mPolylineAndHistogramView.setTextColors(
                MainThemeColorProvider.getColor(location, R.attr.colorTitleText),
                MainThemeColorProvider.getColor(location, R.attr.colorBodyText),
                MainThemeColorProvider.getColor(location, R.attr.colorTitleText)
            )
            mPolylineAndHistogramView.setHistogramAlpha(if (lightTheme) 1f else 0.5f)
            hourlyItem.contentDescription = talkBackBuilder.toString()
        }
    }

    init {
        val hourlyWithUVIndex = location.weather!!.hourlyForecast.filter { it.uV?.index != null }
        if (hourlyWithUVIndex.isNotEmpty()) {
            mHighestIndex = hourlyWithUVIndex.maxOf { it.uV!!.index!! }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trend_hourly, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbsHourlyTrendAdapter.ViewHolder, position: Int) {
        (holder as ViewHolder).onBindView(activity, location, position)
    }

    override fun getItemCount() = location.weather!!.hourlyForecast.size

    override fun isValid(location: Location) = mHighestIndex > 0

    override fun getDisplayName(context: Context) = context.getString(R.string.tag_uv)

    override fun bindBackgroundForHost(host: TrendRecyclerView) {
        val keyLineList: MutableList<KeyLine> = ArrayList()
        keyLineList.add(
            KeyLine(
                UV.UV_INDEX_HIGH.toFloat(), UV.UV_INDEX_HIGH.toString(),
                activity.getString(R.string.uv_alert_level),
                KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        host.setData(keyLineList, mHighestIndex.toFloat(), 0f)
    }
}