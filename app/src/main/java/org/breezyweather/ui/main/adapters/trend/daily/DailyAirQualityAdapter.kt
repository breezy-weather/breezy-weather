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

package org.breezyweather.ui.main.adapters.trend.daily

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.basic.BreezyActivity
import org.breezyweather.common.basic.models.options.appearance.DetailScreen
import org.breezyweather.common.basic.models.options.basic.UnitUtils
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.domain.weather.index.PollutantIndex
import org.breezyweather.domain.weather.model.getColor
import org.breezyweather.domain.weather.model.getIndex
import org.breezyweather.domain.weather.model.getName
import org.breezyweather.ui.common.widgets.trend.TrendRecyclerView
import org.breezyweather.ui.common.widgets.trend.chart.PolylineAndHistogramView
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.weatherView.WeatherViewController

/**
 * Daily air quality adapter.
 */
class DailyAirQualityAdapter(
    activity: BreezyActivity,
    location: Location,
) : AbsDailyTrendAdapter(activity, location) {
    private var mHighestIndex: Int = 0

    inner class ViewHolder(itemView: View) : AbsDailyTrendAdapter.ViewHolder(itemView) {
        private val mPolylineAndHistogramView = PolylineAndHistogramView(itemView.context)

        init {
            dailyItem.chartItemView = mPolylineAndHistogramView
        }

        @SuppressLint("DefaultLocale")
        fun onBindView(
            activity: BreezyActivity,
            location: Location,
            position: Int,
        ) {
            val talkBackBuilder = StringBuilder(activity.getString(R.string.tag_aqi))
            super.onBindView(activity, location, talkBackBuilder, position)
            val daily = location.weather!!.dailyForecast[position]

            val index = daily.airQuality?.getIndex()
            if (index != null) {
                talkBackBuilder.append(activity.getString(org.breezyweather.unit.R.string.locale_separator))
                    .append(UnitUtils.formatInt(activity, index))
                    .append(activity.getString(org.breezyweather.unit.R.string.locale_separator))
                    .append(daily.airQuality!!.getName(itemView.context))
            }
            mPolylineAndHistogramView.setData(
                null, null,
                null, null,
                null, null,
                index?.toFloat(), index?.let { UnitUtils.formatInt(activity, it) },
                mHighestIndex.toFloat(), 0f
            )
            mPolylineAndHistogramView.setLineColors(
                if (index != null) daily.airQuality!!.getColor(activity) else Color.TRANSPARENT,
                if (index != null) daily.airQuality!!.getColor(activity) else Color.TRANSPARENT,
                activity.getThemeColor(com.google.android.material.R.attr.colorOutline)
            )

            val themeColors = ThemeManager.getInstance(itemView.context)
                .weatherThemeDelegate
                .getThemeColors(
                    itemView.context,
                    WeatherViewController.getWeatherKind(location),
                    WeatherViewController.isDaylight(location)
                )
            val lightTheme = ThemeManager.isLightTheme(itemView.context, location)
            mPolylineAndHistogramView.setShadowColors(themeColors[1], themeColors[2], lightTheme)
            mPolylineAndHistogramView.setTextColors(
                activity.getThemeColor(R.attr.colorTitleText),
                activity.getThemeColor(R.attr.colorBodyText),
                activity.getThemeColor(R.attr.colorTitleText)
            )
            mPolylineAndHistogramView.setHistogramAlpha(if (lightTheme) 1f else 0.5f)
            dailyItem.contentDescription = talkBackBuilder.toString()
            dailyItem.setOnClickListener {
                onItemClicked(activity, location, bindingAdapterPosition, DetailScreen.TAG_AIR_QUALITY)
            }
        }
    }

    init {
        mHighestIndex = location.weather!!.dailyForecast
            .mapNotNull { it.airQuality?.getIndex() }
            .maxOrNull() ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trend_daily, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbsDailyTrendAdapter.ViewHolder, position: Int) {
        (holder as ViewHolder).onBindView(activity, location, position)
    }

    override fun getItemCount() = location.weather!!.dailyForecast.size

    override fun isValid(location: Location) = mHighestIndex > 0

    override fun getDisplayName(context: Context) = context.getString(R.string.tag_aqi)

    override fun bindBackgroundForHost(host: TrendRecyclerView) {
        val keyLineList = mutableListOf<TrendRecyclerView.KeyLine>()
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                PollutantIndex.indexFreshAir.toFloat(),
                PollutantIndex.indexFreshAir.toString(),
                activity.resources.getStringArray(R.array.air_quality_levels)[1],
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                PollutantIndex.indexHighPollution.toFloat(),
                PollutantIndex.indexHighPollution.toString(),
                activity.resources.getStringArray(R.array.air_quality_levels)[3],
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                PollutantIndex.indexExcessivePollution.toFloat(),
                PollutantIndex.indexExcessivePollution.toString(),
                activity.resources.getStringArray(R.array.air_quality_levels)[5],
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        host.setData(keyLineList, mHighestIndex.toFloat(), 0f)
    }
}
