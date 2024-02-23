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

package org.breezyweather.main.adapters.trend.hourly

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import breezyweather.domain.location.model.Location
import org.breezyweather.domain.weather.index.PollutantIndex
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView
import org.breezyweather.common.ui.widgets.trend.chart.PolylineAndHistogramView
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.domain.weather.model.getColor
import org.breezyweather.domain.weather.model.getIndex
import org.breezyweather.domain.weather.model.getName
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
            val hourly = location.weather!!.nextHourlyForecast[position]

            val index = hourly.airQuality?.getIndex()
            if (index != null) {
                talkBackBuilder.append(", ").append(index).append(", ")
                    .append(hourly.airQuality!!.getName(itemView.context))
            }
            mPolylineAndHistogramView.setData(
                null, null,
                null, null,
                null, null,
                index?.toFloat(), if (index != null) String.format("%d", index) else null,
                mHighestIndex.toFloat(), 0f
            )
            mPolylineAndHistogramView.setLineColors(
                hourly.airQuality?.getColor(activity) ?: Color.TRANSPARENT,
                hourly.airQuality?.getColor(activity) ?: Color.TRANSPARENT,
                MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
            )

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
        mHighestIndex = location.weather!!.nextHourlyForecast
            .mapNotNull { it.airQuality?.getIndex() }
            .maxOrNull() ?: 0
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
        return location.weather!!.nextHourlyForecast.size
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