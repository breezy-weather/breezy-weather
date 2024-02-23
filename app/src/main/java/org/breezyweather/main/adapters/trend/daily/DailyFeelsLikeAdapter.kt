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

package org.breezyweather.main.adapters.trend.daily

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Size
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import breezyweather.domain.location.model.Location
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView
import org.breezyweather.common.ui.widgets.trend.chart.PolylineAndHistogramView
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.providers.ResourceProvider
import org.breezyweather.theme.weatherView.WeatherViewController
import kotlin.math.max

/**
 * Daily feels like adapter.
 */
class DailyFeelsLikeAdapter(
    activity: GeoActivity,
    location: Location,
    provider: ResourceProvider,
    unit: TemperatureUnit
) : AbsDailyTrendAdapter(activity, location) {
    private val mResourceProvider: ResourceProvider = provider
    private val mTemperatureUnit: TemperatureUnit = unit
    private val mDaytimeTemperatures: Array<Float?>
    private val mNighttimeTemperatures: Array<Float?>
    private var mHighestTemperature: Float? = null
    private var mLowestTemperature: Float? = null

    inner class ViewHolder(itemView: View) : AbsDailyTrendAdapter.ViewHolder(itemView) {
        private val mPolylineAndHistogramView = PolylineAndHistogramView(itemView.context)

        init {
            dailyItem.chartItemView = mPolylineAndHistogramView
        }

        @SuppressLint("SetTextI18n, InflateParams")
        fun onBindView(activity: GeoActivity, location: Location, position: Int) {
            val talkBackBuilder = StringBuilder(activity.getString(R.string.tag_feels_like))
            super.onBindView(activity, location, talkBackBuilder, position)
            val daily = location.weather!!.dailyForecast[position]
            daily.day?.let { day ->
                talkBackBuilder.append(", ").append(activity.getString(R.string.daytime)).append(" : ")
                day.temperature?.feelsLikeTemperature?.let {
                    talkBackBuilder.append(mTemperatureUnit.getValueText(activity, it))
                }
            }
            daily.night?.let { night ->
                talkBackBuilder.append(", ").append(activity.getString(R.string.nighttime)).append(" : ")
                night.temperature?.feelsLikeTemperature?.let {
                    talkBackBuilder.append(mTemperatureUnit.getValueText(activity, it))
                }
            }
            dailyItem.setDayIconDrawable(
                daily.day?.weatherCode?.let { ResourceHelper.getWeatherIcon(mResourceProvider, it, true) },
                missingIconVisibility = View.INVISIBLE
            )
            mPolylineAndHistogramView.setData(
                buildTemperatureArrayForItem(mDaytimeTemperatures, position),
                buildTemperatureArrayForItem(mNighttimeTemperatures, position),
                daily.day?.temperature?.temperature?.let {
                    mTemperatureUnit.getShortValueText(activity, it)
                },
                daily.night?.temperature?.temperature?.let {
                    mTemperatureUnit.getShortValueText(activity, it)
                },
                mHighestTemperature,
                mLowestTemperature,
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
                    WeatherViewController.getWeatherKind(location.weather),
                    location.isDaylight
                )
            val lightTheme = MainThemeColorProvider.isLightTheme(itemView.context, location)
            mPolylineAndHistogramView.setLineColors(
                themeColors[1],
                themeColors[2],
                MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
            )
            mPolylineAndHistogramView.setShadowColors(
                themeColors[1],
                themeColors[2],
                lightTheme
            )
            mPolylineAndHistogramView.setTextColors(
                MainThemeColorProvider.getColor(location, R.attr.colorTitleText),
                MainThemeColorProvider.getColor(location, R.attr.colorBodyText),
                MainThemeColorProvider.getColor(location, R.attr.colorPrecipitationProbability)
            )
            mPolylineAndHistogramView.setHistogramAlpha(if (lightTheme) 0.2f else 0.5f)
            dailyItem.setNightIconDrawable(
                daily.night?.weatherCode?.let { ResourceHelper.getWeatherIcon(mResourceProvider, it, false) },
                missingIconVisibility = View.INVISIBLE
            )
            dailyItem.contentDescription = talkBackBuilder.toString()
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
        mDaytimeTemperatures = arrayOfNulls(max(0, weather.dailyForecast.size * 2 - 1))
        run {
            var i = 0
            while (i < mDaytimeTemperatures.size) {
                mDaytimeTemperatures[i] = weather.dailyForecast.getOrNull(i / 2)?.day?.temperature?.feelsLikeTemperature?.toFloat()
                i += 2
            }
        }
        run {
            var i = 1
            while (i < mDaytimeTemperatures.size) {
                if (mDaytimeTemperatures[i - 1] != null && mDaytimeTemperatures[i + 1] != null) {
                    mDaytimeTemperatures[i] = (mDaytimeTemperatures[i - 1]!! + mDaytimeTemperatures[i + 1]!!) * 0.5f
                } else {
                    mDaytimeTemperatures[i] = null
                }
                i += 2
            }
        }
        mNighttimeTemperatures = arrayOfNulls(max(0, weather.dailyForecast.size * 2 - 1))
        run {
            var i = 0
            while (i < mNighttimeTemperatures.size) {
                mNighttimeTemperatures[i] = weather.dailyForecast.getOrNull(i / 2)?.night?.temperature?.feelsLikeTemperature?.toFloat()
                i += 2
            }
        }
        run {
            var i = 1
            while (i < mNighttimeTemperatures.size) {
                if (mNighttimeTemperatures[i - 1] != null && mNighttimeTemperatures[i + 1] != null) {
                    mNighttimeTemperatures[i] =
                        (mNighttimeTemperatures[i - 1]!! + mNighttimeTemperatures[i + 1]!!) * 0.5f
                } else {
                    mNighttimeTemperatures[i] = null
                }
                i += 2
            }
        }
        weather.dailyForecast.forEach { daily ->
            daily.day?.temperature?.feelsLikeTemperature?.let {
                if (mHighestTemperature == null || it > mHighestTemperature!!) {
                    mHighestTemperature = it.toFloat()
                }
            }
            daily.night?.temperature?.feelsLikeTemperature?.let {
                if (mLowestTemperature == null || it < mLowestTemperature!!) {
                    mLowestTemperature = it.toFloat()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trend_daily, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbsDailyTrendAdapter.ViewHolder, position: Int) {
        (holder as ViewHolder).onBindView(activity, location, position)
    }

    override fun getItemCount() = location.weather!!.dailyForecast.size

    override fun isValid(location: Location): Boolean {
        return mHighestTemperature != null && mLowestTemperature != null
    }

    override fun getDisplayName(context: Context) = context.getString(R.string.tag_feels_like)

    override fun bindBackgroundForHost(host: TrendRecyclerView) {
        host.setData(null, 0f, 0f)
    }
}