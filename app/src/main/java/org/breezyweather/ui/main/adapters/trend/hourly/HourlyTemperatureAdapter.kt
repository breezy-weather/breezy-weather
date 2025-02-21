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
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.widgets.trend.TrendRecyclerView
import org.breezyweather.ui.common.widgets.trend.chart.PolylineAndHistogramView
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import org.breezyweather.ui.theme.weatherView.WeatherViewController
import java.text.NumberFormat
import kotlin.math.max

/**
 * Hourly temperature adapter.
 */
class HourlyTemperatureAdapter(
    activity: GeoActivity,
    location: Location,
    provider: ResourceProvider,
    unit: TemperatureUnit,
    showPrecipitationProbability: Boolean = true,
) : AbsHourlyTrendAdapter(activity, location) {
    private val mResourceProvider: ResourceProvider = provider
    private val mTemperatureUnit: TemperatureUnit = unit
    private val mTemperatures: Array<Float?>
    private var mHighestTemperature: Float? = null
    private var mLowestTemperature: Float? = null
    private val mShowPrecipitationProbability: Boolean

    inner class ViewHolder(itemView: View) : AbsHourlyTrendAdapter.ViewHolder(itemView) {
        private val mPolylineAndHistogramView = PolylineAndHistogramView(itemView.context)

        init {
            hourlyItem.chartItemView = mPolylineAndHistogramView
        }

        fun onBindView(activity: GeoActivity, location: Location, position: Int) {
            val talkBackBuilder = StringBuilder(activity.getString(R.string.tag_temperature))
            super.onBindView(activity, location, talkBackBuilder, position)
            val weather = location.weather!!
            val hourly = weather.nextHourlyForecast[position]
            hourly.temperature?.temperature?.let {
                talkBackBuilder.append(activity.getString(R.string.comma_separator))
                    .append(mTemperatureUnit.getValueVoice(activity, it))
            }
            if (!hourly.weatherText.isNullOrEmpty()) {
                talkBackBuilder.append(activity.getString(R.string.comma_separator))
                    .append(hourly.weatherText)
            }
            hourlyItem.setIconDrawable(
                hourly.weatherCode?.let {
                    ResourceHelper.getWeatherIcon(mResourceProvider, it, hourly.isDaylight)
                },
                missingIconVisibility = View.INVISIBLE
            )
            val precipitationProbability = hourly.precipitationProbability?.total
            var p: Float = precipitationProbability?.toFloat() ?: 0f
            if (!mShowPrecipitationProbability) {
                p = 0f
            } else if (hourly.precipitationProbability?.total != null) {
                talkBackBuilder.append(activity.getString(R.string.comma_separator))
                    .append(activity.getString(R.string.precipitation_probability))
                    .append(activity.getString(R.string.colon_separator))
                    .append(
                        NumberFormat.getPercentInstance(activity.currentLocale).apply {
                            maximumFractionDigits = 0
                        }.format(if (p > 0) p.div(100.0) else 0)
                    )
            }
            mPolylineAndHistogramView.setData(
                buildTemperatureArrayForItem(mTemperatures, position),
                null,
                hourly.temperature?.temperature?.let {
                    mTemperatureUnit.getShortValueText(activity, it)
                },
                null,
                mHighestTemperature,
                mLowestTemperature,
                if (p > 0) p else null,
                if (p > 0) {
                    NumberFormat.getPercentInstance(activity.currentLocale).apply {
                        maximumFractionDigits = 0
                    }.format(p.div(100.0))
                } else {
                    null
                },
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
        }

        @Size(3)
        private fun buildTemperatureArrayForItem(temps: Array<Float?>, adapterPosition: Int): Array<Float?> {
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
        mTemperatures = arrayOfNulls(max(0, weather.nextHourlyForecast.size * 2 - 1))
        run {
            var i = 0
            while (i < mTemperatures.size) {
                mTemperatures[i] =
                    weather.nextHourlyForecast.getOrNull(i / 2)?.temperature?.temperature?.toFloat()
                i += 2
            }
        }
        run {
            var i = 1
            while (i < mTemperatures.size) {
                if (mTemperatures[i - 1] != null && mTemperatures[i + 1] != null) {
                    mTemperatures[i] = (mTemperatures[i - 1]!! + mTemperatures[i + 1]!!) * 0.5f
                } else {
                    mTemperatures[i] = null
                }
                i += 2
            }
        }
        weather.normals?.let { normals ->
            mHighestTemperature = normals.daytimeTemperature?.toFloat()
            mLowestTemperature = normals.nighttimeTemperature?.toFloat()
        }
        weather.nextHourlyForecast
            .forEach { hourly ->
                hourly.temperature?.temperature?.let {
                    if (mHighestTemperature == null || it > mHighestTemperature!!) {
                        mHighestTemperature = it.toFloat()
                    }
                    if (mLowestTemperature == null || it < mLowestTemperature!!) {
                        mLowestTemperature = it.toFloat()
                    }
                }
            }
        mShowPrecipitationProbability = showPrecipitationProbability
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trend_hourly, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbsHourlyTrendAdapter.ViewHolder, position: Int) {
        (holder as ViewHolder).onBindView(activity, location, position)
    }

    override fun getItemCount() = location.weather!!.nextHourlyForecast.size

    // FIXME
    override fun isValid(location: Location) = true

    override fun getDisplayName(context: Context) = context.getString(R.string.tag_temperature)

    override fun bindBackgroundForHost(host: TrendRecyclerView) {
        val normals = location.weather?.normals
        if (normals?.daytimeTemperature == null || normals.nighttimeTemperature == null) {
            host.setData(null, 0f, 0f)
        } else {
            val keyLineList = mutableListOf<TrendRecyclerView.KeyLine>()
            keyLineList.add(
                TrendRecyclerView.KeyLine(
                    normals.daytimeTemperature!!.toFloat(),
                    SettingsManager.getInstance(activity).temperatureUnit.getShortValueText(
                        activity,
                        normals.daytimeTemperature!!
                    ),
                    activity.getString(
                        if (normals.month != null) {
                            R.string.temperature_normal_short
                        } else {
                            R.string.temperature_average_short
                        }
                    ),
                    TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
            )
            keyLineList.add(
                TrendRecyclerView.KeyLine(
                    normals.nighttimeTemperature!!.toFloat(),
                    SettingsManager.getInstance(activity).temperatureUnit.getShortValueText(
                        activity,
                        normals.nighttimeTemperature!!
                    ),
                    activity.getString(
                        if (normals.month != null) {
                            R.string.temperature_normal_short
                        } else {
                            R.string.temperature_average_short
                        }
                    ),
                    TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
                )
            )
            host.setData(keyLineList, mHighestTemperature!!, mLowestTemperature!!)
        }
    }
}
