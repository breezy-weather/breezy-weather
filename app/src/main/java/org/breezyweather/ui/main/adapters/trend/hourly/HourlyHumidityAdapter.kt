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
import org.breezyweather.common.activities.BreezyActivity
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.common.extensions.formatPercent
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.common.options.appearance.DetailScreen
import org.breezyweather.ui.common.widgets.trend.TrendRecyclerView
import org.breezyweather.ui.common.widgets.trend.chart.PolylineAndHistogramView
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import org.breezyweather.ui.theme.weatherView.WeatherViewController
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.temperature.TemperatureUnit
import kotlin.math.max

/**
 * Hourly humidity / dew point adapter.
 */
class HourlyHumidityAdapter(
    activity: BreezyActivity,
    location: Location,
    provider: ResourceProvider,
    private val temperatureUnit: TemperatureUnit,
) : AbsHourlyTrendAdapter(activity, location) {
    private val mResourceProvider: ResourceProvider = provider
    private val mDewPoints: Array<Float?>
    private var mHighestDewPoint: Float? = null
    private var mLowestDewPoint: Float? = null

    inner class ViewHolder(itemView: View) : AbsHourlyTrendAdapter.ViewHolder(itemView) {
        private val mPolylineAndHistogramView = PolylineAndHistogramView(itemView.context)

        init {
            hourlyItem.chartItemView = mPolylineAndHistogramView
        }

        fun onBindView(activity: BreezyActivity, location: Location, position: Int) {
            val talkBackBuilder = StringBuilder()
            super.onBindView(activity, location, talkBackBuilder, position)
            val weather = location.weather!!
            val hourly = weather.nextHourlyForecast[position]
            hourly.relativeHumidity?.let {
                talkBackBuilder.append(activity.getString(org.breezyweather.unit.R.string.locale_separator))
                    .append(activity.getString(R.string.humidity))
                    .append(activity.getString(R.string.colon_separator))
                    .append(it.formatPercent(activity))
            }
            hourly.dewPoint?.let {
                talkBackBuilder.append(activity.getString(org.breezyweather.unit.R.string.locale_separator))
                    .append(activity.getString(R.string.dew_point))
                    .append(activity.getString(R.string.colon_separator))
                    .append(it.formatMeasure(activity, temperatureUnit, unitWidth = UnitWidth.LONG))
            }
            hourlyItem.setIconDrawable(
                hourly.weatherCode?.let {
                    ResourceHelper.getWeatherIcon(mResourceProvider, it, hourly.isDaylight)
                },
                missingIconVisibility = View.INVISIBLE
            )
            mPolylineAndHistogramView.setData(
                buildDewPointArrayForItem(mDewPoints, position),
                null,
                hourly.dewPoint?.formatMeasure(
                    activity,
                    temperatureUnit,
                    valueWidth = UnitWidth.NARROW,
                    unitWidth = UnitWidth.NARROW
                ),
                null,
                mHighestDewPoint,
                mLowestDewPoint,
                hourly.relativeHumidity?.inPercent?.toFloat(),
                hourly.relativeHumidity?.formatPercent(activity, UnitWidth.NARROW),
                100f,
                0f
            )
            val themeColors = ThemeManager
                .getInstance(itemView.context)
                .weatherThemeDelegate
                .getThemeColors(
                    itemView.context,
                    WeatherViewController.getWeatherKind(location),
                    WeatherViewController.isDaylight(location)
                )
            val lightTheme = ThemeManager.isLightTheme(itemView.context, location)
            mPolylineAndHistogramView.setLineColors(
                themeColors[if (lightTheme) 1 else 2],
                themeColors[2],
                activity.getThemeColor(com.google.android.material.R.attr.colorOutline)
            )
            mPolylineAndHistogramView.setShadowColors(
                themeColors[if (lightTheme) 1 else 2],
                themeColors[2],
                lightTheme
            )
            mPolylineAndHistogramView.setTextColors(
                activity.getThemeColor(R.attr.colorTitleText),
                activity.getThemeColor(R.attr.colorBodyText),
                activity.getThemeColor(R.attr.colorPrecipitationProbability)
            )
            mPolylineAndHistogramView.setHistogramAlpha(if (lightTheme) 0.2f else 0.5f)
            hourlyItem.contentDescription = talkBackBuilder.toString()
            hourlyItem.setOnClickListener {
                onItemClicked(activity, location, bindingAdapterPosition, DetailScreen.TAG_HUMIDITY)
            }
        }

        @Size(3)
        private fun buildDewPointArrayForItem(temps: Array<Float?>, adapterPosition: Int): Array<Float?> {
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
        mDewPoints = arrayOfNulls(max(0, weather.nextHourlyForecast.size * 2 - 1))
        run {
            var i = 0
            while (i < mDewPoints.size) {
                mDewPoints[i] = weather.nextHourlyForecast.getOrNull(i / 2)?.dewPoint?.value?.toFloat()
                i += 2
            }
        }
        run {
            var i = 1
            while (i < mDewPoints.size) {
                if (mDewPoints[i - 1] != null && mDewPoints[i + 1] != null) {
                    mDewPoints[i] = (mDewPoints[i - 1]!! + mDewPoints[i + 1]!!) * 0.5f
                } else {
                    mDewPoints[i] = null
                }
                i += 2
            }
        }
        weather.nextHourlyForecast
            .forEach { hourly ->
                hourly.dewPoint?.value?.let {
                    if (mHighestDewPoint == null || it > mHighestDewPoint!!) {
                        mHighestDewPoint = it.toFloat()
                    }
                    if (mLowestDewPoint == null || it < mLowestDewPoint!!) {
                        mLowestDewPoint = it.toFloat()
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
        return mHighestDewPoint != null && mLowestDewPoint != null
    }

    override fun getDisplayName(context: Context) = context.getString(R.string.tag_humidity_dew_point)

    override fun bindBackgroundForHost(host: TrendRecyclerView) {
        host.setData(null, 0f, 0f)
    }
}
