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
import org.breezyweather.common.extensions.formatValue
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.common.options.appearance.DetailScreen
import org.breezyweather.ui.common.widgets.trend.TrendRecyclerView
import org.breezyweather.ui.common.widgets.trend.chart.PolylineAndHistogramView
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import org.breezyweather.ui.theme.weatherView.WeatherViewController
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.pressure.Pressure.Companion.pascals
import org.breezyweather.unit.pressure.PressureUnit
import kotlin.math.max

/**
 * Hourly pressure adapter.
 */
class HourlyPressureAdapter(
    activity: BreezyActivity,
    location: Location,
    provider: ResourceProvider,
) : AbsHourlyTrendAdapter(activity, location) {
    private val mResourceProvider: ResourceProvider = provider
    private val mPressures: Array<Float?>
    private var mHighestPressure: Float? = null
    private var mLowestPressure: Float? = null

    inner class ViewHolder(itemView: View) : AbsHourlyTrendAdapter.ViewHolder(itemView) {
        private val mPolylineAndHistogramView = PolylineAndHistogramView(itemView.context)

        init {
            hourlyItem.chartItemView = mPolylineAndHistogramView
        }

        fun onBindView(activity: BreezyActivity, location: Location, position: Int) {
            val talkBackBuilder = StringBuilder(activity.getString(R.string.tag_pressure))
            super.onBindView(activity, location, talkBackBuilder, position)
            val weather = location.weather!!
            val hourly = weather.nextHourlyForecast[position]
            hourly.pressure?.let { pressure ->
                talkBackBuilder.append(activity.getString(org.breezyweather.unit.R.string.locale_separator))
                    .append(pressure.formatMeasure(activity, unitWidth = UnitWidth.LONG))
            }
            hourlyItem.setIconDrawable(
                hourly.weatherCode?.let {
                    ResourceHelper.getWeatherIcon(mResourceProvider, it, hourly.isDaylight)
                },
                missingIconVisibility = View.INVISIBLE
            )
            mPolylineAndHistogramView.setData(
                buildPressureArrayForItem(mPressures, position),
                null,
                hourly.pressure?.formatValue(activity, UnitWidth.NARROW),
                null,
                mHighestPressure,
                mLowestPressure,
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
                onItemClicked(activity, location, bindingAdapterPosition, DetailScreen.TAG_PRESSURE)
            }
        }

        @Size(3)
        private fun buildPressureArrayForItem(temps: Array<Float?>, adapterPosition: Int): Array<Float?> {
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
        mPressures = arrayOfNulls(max(0, weather.nextHourlyForecast.size * 2 - 1))
        run {
            var i = 0
            while (i < mPressures.size) {
                mPressures[i] = weather.nextHourlyForecast.getOrNull(i / 2)?.pressure?.value?.toFloat()
                i += 2
            }
        }
        run {
            var i = 1
            while (i < mPressures.size) {
                if (mPressures[i - 1] != null && mPressures[i + 1] != null) {
                    mPressures[i] = (mPressures[i - 1]!! + mPressures[i + 1]!!) * 0.5f
                } else {
                    mPressures[i] = null
                }
                i += 2
            }
        }
        mHighestPressure = PressureUnit.STANDARD.toFloat()
        mLowestPressure = PressureUnit.STANDARD.toFloat()
        weather.nextHourlyForecast
            .forEach { hourly ->
                hourly.pressure?.value?.let {
                    if (mHighestPressure == null || it > mHighestPressure!!) {
                        mHighestPressure = it.toFloat()
                    }
                    if (mLowestPressure == null || it < mLowestPressure!!) {
                        mLowestPressure = it.toFloat()
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
        return mHighestPressure != null &&
            mLowestPressure != null &&
            (mHighestPressure != PressureUnit.STANDARD.toFloat() || mLowestPressure != PressureUnit.STANDARD.toFloat())
    }

    override fun getDisplayName(context: Context) = context.getString(R.string.tag_pressure)

    override fun bindBackgroundForHost(host: TrendRecyclerView) {
        val keyLineList = mutableListOf<TrendRecyclerView.KeyLine>()
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                PressureUnit.STANDARD.toFloat(),
                PressureUnit.STANDARD.pascals.formatValue(activity, UnitWidth.NARROW),
                activity.getString(R.string.pressure_standard),
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        host.setData(keyLineList, mHighestPressure!!, mLowestPressure!!)
    }
}
