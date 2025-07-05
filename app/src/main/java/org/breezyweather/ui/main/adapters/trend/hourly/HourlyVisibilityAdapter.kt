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

package org.breezyweather.ui.main.adapters.trend.hourly

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Size
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.options.appearance.DetailScreen
import org.breezyweather.common.basic.models.options.unit.DistanceUnit
import org.breezyweather.ui.common.widgets.trend.TrendRecyclerView
import org.breezyweather.ui.common.widgets.trend.chart.PolylineAndHistogramView
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import org.breezyweather.ui.theme.weatherView.WeatherViewController
import kotlin.math.max

/**
 * Hourly visibility adapter.
 */
class HourlyVisibilityAdapter(
    activity: GeoActivity,
    location: Location,
    provider: ResourceProvider,
    unit: DistanceUnit,
) : AbsHourlyTrendAdapter(activity, location) {
    private val mResourceProvider: ResourceProvider = provider
    private val mVisibilityUnit: DistanceUnit = unit
    private val mVisibilities: Array<Float?>
    private var mHighestVisibility: Float? = null
    private var mLowestVisibility: Float? = null

    inner class ViewHolder(itemView: View) : AbsHourlyTrendAdapter.ViewHolder(itemView) {
        private val mPolylineAndHistogramView = PolylineAndHistogramView(itemView.context)

        init {
            hourlyItem.chartItemView = mPolylineAndHistogramView
        }

        fun onBindView(activity: GeoActivity, location: Location, position: Int) {
            val talkBackBuilder = StringBuilder(activity.getString(R.string.tag_visibility))
            super.onBindView(activity, location, talkBackBuilder, position)
            val weather = location.weather!!
            val hourly = weather.nextHourlyForecast[position]
            hourly.visibility?.let {
                talkBackBuilder.append(activity.getString(R.string.comma_separator))
                    .append(mVisibilityUnit.getValueText(activity, it))
            }
            hourlyItem.setIconDrawable(
                hourly.weatherCode?.let {
                    ResourceHelper.getWeatherIcon(mResourceProvider, it, hourly.isDaylight)
                },
                missingIconVisibility = View.INVISIBLE
            )
            mPolylineAndHistogramView.setData(
                buildVisibilityArrayForItem(mVisibilities, position),
                null,
                hourly.visibility?.let { mVisibilityUnit.getValueTextWithoutUnit(activity, it) },
                null,
                mHighestVisibility,
                mLowestVisibility,
                null,
                null,
                null,
                null
            )
            val themeColors = ThemeManager
                .getInstance(itemView.context)
                .weatherThemeDelegate
                .getThemeColors(
                    itemView.context,
                    WeatherViewController.getWeatherKind(location),
                    WeatherViewController.isDaylight(location)
                )
            val lightTheme = MainThemeColorProvider.isLightTheme(itemView.context, location)
            mPolylineAndHistogramView.setLineColors(
                themeColors[if (lightTheme) 1 else 2],
                themeColors[2],
                MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
            )
            mPolylineAndHistogramView.setShadowColors(
                themeColors[if (lightTheme) 1 else 2],
                themeColors[2],
                lightTheme
            )
            mPolylineAndHistogramView.setTextColors(
                MainThemeColorProvider.getColor(location, R.attr.colorTitleText),
                MainThemeColorProvider.getColor(location, R.attr.colorBodyText),
                MainThemeColorProvider.getColor(location, R.attr.colorPrecipitationProbability)
            )
            mPolylineAndHistogramView.setHistogramAlpha(if (lightTheme) 0.2f else 0.5f)
            hourlyItem.contentDescription = talkBackBuilder.toString()
            hourlyItem.setOnClickListener {
                onItemClicked(activity, location, bindingAdapterPosition, DetailScreen.TAG_VISIBILITY)
            }
        }

        @Size(3)
        private fun buildVisibilityArrayForItem(temps: Array<Float?>, adapterPosition: Int): Array<Float?> {
            val a = arrayOfNulls<Float>(3)
            a[1] = temps[2 * adapterPosition]
            if (2 * adapterPosition - 1 < 0) {
                a[0] = null
            } else {
                a[0] = temps[2 * adapterPosition - 1]
            }
            if (2 * adapterPosition + 1 >= temps.size) {
                a[2] = null
            } else {
                a[2] = temps[2 * adapterPosition + 1]
            }
            return a
        }
    }

    init {
        val weather = location.weather!!
        mVisibilities = arrayOfNulls(max(0, weather.nextHourlyForecast.size * 2 - 1))
        run {
            var i = 0
            while (i < mVisibilities.size) {
                mVisibilities[i] = weather.nextHourlyForecast.getOrNull(i / 2)?.visibility?.toFloat()
                i += 2
            }
        }
        run {
            var i = 1
            while (i < mVisibilities.size) {
                if (mVisibilities[i - 1] != null && mVisibilities[i + 1] != null) {
                    mVisibilities[i] = (mVisibilities[i - 1]!! + mVisibilities[i + 1]!!) * 0.5f
                } else {
                    mVisibilities[i] = null
                }
                i += 2
            }
        }
        weather.nextHourlyForecast
            .forEach { hourly ->
                hourly.visibility?.let {
                    if (mHighestVisibility == null || it > mHighestVisibility!!) {
                        mHighestVisibility = it.toFloat()
                    }
                    if (mLowestVisibility == null || it < mLowestVisibility!!) {
                        mLowestVisibility = it.toFloat()
                    }
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trend_hourly, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbsHourlyTrendAdapter.ViewHolder, position: Int) {
        (holder as ViewHolder).onBindView(activity, location, position)
    }

    override fun getItemCount() = location.weather!!.nextHourlyForecast.size

    override fun isValid(location: Location): Boolean {
        return mHighestVisibility != null && mLowestVisibility != null
    }

    override fun getDisplayName(context: Context) = context.getString(R.string.tag_visibility)

    override fun bindBackgroundForHost(host: TrendRecyclerView) {
        host.setData(null, 0f, 0f)
    }
}
