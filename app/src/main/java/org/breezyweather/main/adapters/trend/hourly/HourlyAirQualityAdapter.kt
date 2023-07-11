package org.breezyweather.main.adapters.trend.hourly

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.index.PollutantIndex
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView
import org.breezyweather.common.ui.widgets.trend.chart.PolylineAndHistogramView
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.weatherView.WeatherViewController

/**
 * Hourly air quality adapter.
 */
class HourlyAirQualityAdapter(activity: GeoActivity, location: Location) : AbsHourlyTrendAdapter(activity, location) {
    private var mHighestIndex: Int = 0

    inner class ViewHolder(itemView: View) : AbsHourlyTrendAdapter.ViewHolder(itemView) {
        private val mPolylineAndHistogramView = PolylineAndHistogramView(itemView.context)

        init {
            hourlyItem.chartItemView = mPolylineAndHistogramView
        }

        @SuppressLint("DefaultLocale")
        fun onBindView(
            activity: GeoActivity,
            location: Location,
            position: Int
        ) {
            val talkBackBuilder = StringBuilder(activity.getString(R.string.tag_aqi))
            super.onBindView(activity, location, talkBackBuilder, position)
            val hourly = location.weather!!.hourlyForecast[position]
            hourly.airQuality?.let { airQuality ->
                val index = airQuality.getIndex()
                talkBackBuilder.append(", ").append(index).append(", ")
                    .append(airQuality.getName(itemView.context))
                mPolylineAndHistogramView.setData(
                    null, null,
                    null, null,
                    null, null,
                    (index ?: 0).toFloat(), String.format("%d", index ?: 0),
                    mHighestIndex.toFloat(), 0f
                )
                mPolylineAndHistogramView.setLineColors(
                    airQuality.getColor(activity),
                    airQuality.getColor(activity),
                    MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
                )
            }
            val themeColors = ThemeManager.getInstance(itemView.context)
                .weatherThemeDelegate
                .getThemeColors(
                    itemView.context,
                    WeatherViewController.getWeatherKind(location.weather),
                    location.isDaylight
                )
            val lightTheme = MainThemeColorProvider.isLightTheme(itemView.context, location)
            mPolylineAndHistogramView.setShadowColors(themeColors[1], themeColors[2], lightTheme)
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
        val hourlyWithAirQualityIndex = location.weather!!.hourlyForecast.filter { it.airQuality?.getIndex() != null }
        if (hourlyWithAirQualityIndex.isNotEmpty()) {
            mHighestIndex = hourlyWithAirQualityIndex.maxOf { it.airQuality!!.getIndex()!! }
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

    override fun getItemCount(): Int {
        return location.weather!!.hourlyForecast.size
    }

    override fun isValid(location: Location): Boolean {
        return mHighestIndex > 0
    }

    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.tag_aqi)
    }

    override fun bindBackgroundForHost(host: TrendRecyclerView) {
        val keyLineList: MutableList<TrendRecyclerView.KeyLine> = ArrayList()
        val goodPollutionLevel = PollutantIndex.indexFreshAir
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                goodPollutionLevel.toFloat(), goodPollutionLevel.toString(),
                activity.resources.getStringArray(R.array.air_quality_levels)[1],
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        val moderatePollutionLevel = PollutantIndex.indexHighPollution
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                moderatePollutionLevel.toFloat(), moderatePollutionLevel.toString(),
                activity.resources.getStringArray(R.array.air_quality_levels)[3],
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        val heavyPollutionLevel = PollutantIndex.indexExcessivePollution
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                heavyPollutionLevel.toFloat(), heavyPollutionLevel.toString(),
                activity.resources.getStringArray(R.array.air_quality_levels)[5],
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        host.setData(keyLineList, mHighestIndex.toFloat(), 0f)
    }
}