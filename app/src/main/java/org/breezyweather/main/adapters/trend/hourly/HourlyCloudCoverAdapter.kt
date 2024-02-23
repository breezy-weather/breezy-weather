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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import breezyweather.domain.location.model.Location
import org.breezyweather.common.basic.models.options.unit.ProbabilityUnit
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView
import org.breezyweather.common.ui.widgets.trend.chart.PolylineAndHistogramView
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.weatherView.WeatherViewController

/**
 * Hourly Cloud Cover adapter.
 */
class HourlyCloudCoverAdapter(activity: GeoActivity, location: Location) : AbsHourlyTrendAdapter(
    activity, location
) {
    private var mHighestCloudCover: Float = 0f

    inner class ViewHolder(itemView: View) : AbsHourlyTrendAdapter.ViewHolder(itemView) {
        private val mPolylineAndHistogramView = PolylineAndHistogramView(itemView.context)

        init {
            hourlyItem.chartItemView = mPolylineAndHistogramView
        }

        @SuppressLint("SetTextI18n, InflateParams", "DefaultLocale")
        fun onBindView(activity: GeoActivity, location: Location, position: Int) {
            val talkBackBuilder = StringBuilder(activity.getString(R.string.tag_cloud_cover))
            super.onBindView(activity, location, talkBackBuilder, position)
            val hourly = location.weather!!.nextHourlyForecast[position]

            hourly.cloudCover?.let { cloudCover ->
                talkBackBuilder.append(", ").append(ProbabilityUnit.PERCENT.getValueVoice(activity, cloudCover))
            }
            mPolylineAndHistogramView.setData(
                null, null,
                null, null,
                null, null,
                hourly.cloudCover?.toFloat() ?: 0f, hourly.cloudCover?.let { ProbabilityUnit.PERCENT.getValueText(activity, it) },
                100f, 0f
            )
            mPolylineAndHistogramView.setLineColors(
                MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline),
                MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline),
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
        mHighestCloudCover = location.weather!!.nextHourlyForecast
            .mapNotNull { it.cloudCover }
            .maxOrNull()
            ?.toFloat() ?: 0f
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trend_hourly, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbsHourlyTrendAdapter.ViewHolder, position: Int) {
        (holder as ViewHolder).onBindView(activity, location, position)
    }

    override fun getItemCount() = location.weather!!.nextHourlyForecast.size

    override fun isValid(location: Location) = mHighestCloudCover > 0

    override fun getDisplayName(context: Context) = context.getString(R.string.tag_cloud_cover)

    override fun bindBackgroundForHost(host: TrendRecyclerView) {
        val keyLineList: MutableList<TrendRecyclerView.KeyLine> = ArrayList()
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                75f,
                ProbabilityUnit.PERCENT.getValueText(activity, 75),
                activity.getString(R.string.weather_kind_partly_cloudy),
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                37.5f,
                ProbabilityUnit.PERCENT.getValueText(activity, 38),
                activity.getString(R.string.weather_kind_clear),
                TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
            )
        )
        host.setData(keyLineList, 100f, 0f)
    }
}