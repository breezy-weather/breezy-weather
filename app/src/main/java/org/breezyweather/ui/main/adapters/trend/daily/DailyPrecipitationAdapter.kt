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
import breezyweather.domain.weather.model.Precipitation
import org.breezyweather.R
import org.breezyweather.common.basic.BreezyActivity
import org.breezyweather.common.basic.models.options.appearance.DetailScreen
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.common.extensions.formatValue
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.domain.weather.model.getHalfDayPrecipitationColor
import org.breezyweather.ui.common.widgets.trend.TrendRecyclerView
import org.breezyweather.ui.common.widgets.trend.chart.DoubleHistogramView
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters

/**
 * Daily precipitation adapter.
 */
class DailyPrecipitationAdapter(
    activity: BreezyActivity,
    location: Location,
    provider: ResourceProvider,
) : AbsDailyTrendAdapter(activity, location) {
    private val mResourceProvider: ResourceProvider = provider
    private var mHighestPrecipitation: Float? = null

    inner class ViewHolder(itemView: View) : AbsDailyTrendAdapter.ViewHolder(itemView) {
        private val mDoubleHistogramView: DoubleHistogramView

        init {
            mDoubleHistogramView = DoubleHistogramView(itemView.context)
            dailyItem.chartItemView = mDoubleHistogramView
        }

        @SuppressLint("SetTextI18n, InflateParams")
        fun onBindView(activity: BreezyActivity, location: Location, position: Int) {
            val talkBackBuilder = StringBuilder(activity.getString(R.string.tag_precipitation))
            super.onBindView(activity, location, talkBackBuilder, position)
            val weather = location.weather!!
            val daily = weather.dailyForecast[position]
            val daytimePrecipitation = daily.day?.precipitation?.total
            val nighttimePrecipitation = daily.night?.precipitation?.total
            if ((daytimePrecipitation != null && daytimePrecipitation.value > 0) ||
                (nighttimePrecipitation != null && nighttimePrecipitation.value > 0)
            ) {
                talkBackBuilder.append(activity.getString(org.breezyweather.unit.R.string.locale_separator))
                    .append(activity.getString(R.string.daytime))
                    .append(activity.getString(R.string.colon_separator))
                    .append(
                        if (daytimePrecipitation != null && daytimePrecipitation.value > 0) {
                            daytimePrecipitation.formatMeasure(activity, unitWidth = UnitWidth.LONG)
                        } else {
                            activity.getString(R.string.precipitation_none)
                        }
                    )
                talkBackBuilder.append(activity.getString(org.breezyweather.unit.R.string.locale_separator))
                    .append(activity.getString(R.string.nighttime))
                    .append(activity.getString(R.string.colon_separator))
                    .append(
                        if (nighttimePrecipitation != null && nighttimePrecipitation.value > 0) {
                            nighttimePrecipitation.formatMeasure(activity, unitWidth = UnitWidth.LONG)
                        } else {
                            activity.getString(R.string.precipitation_none)
                        }
                    )
            } else {
                talkBackBuilder.append(activity.getString(org.breezyweather.unit.R.string.locale_separator))
                    .append(activity.getString(R.string.precipitation_none))
            }
            dailyItem.setDayIconDrawable(
                daily.day?.weatherCode?.let { ResourceHelper.getWeatherIcon(mResourceProvider, it, true) },
                missingIconVisibility = View.INVISIBLE
            )
            mDoubleHistogramView.setData(
                daily.day?.precipitation?.total?.value?.toFloat(),
                daily.night?.precipitation?.total?.value?.toFloat(),
                daytimePrecipitation?.formatValue(activity),
                nighttimePrecipitation?.formatValue(activity),
                mHighestPrecipitation
            )
            mDoubleHistogramView.setLineColors(
                daily.day?.precipitation?.getHalfDayPrecipitationColor(activity) ?: Color.TRANSPARENT,
                daily.night?.precipitation?.getHalfDayPrecipitationColor(activity) ?: Color.TRANSPARENT,
                activity.getThemeColor(com.google.android.material.R.attr.colorOutline)
            )
            mDoubleHistogramView.setTextColors(
                activity.getThemeColor(R.attr.colorBodyText)
            )
            mDoubleHistogramView.setHistogramAlphas(1f, 0.5f)
            dailyItem.setNightIconDrawable(
                daily.night?.weatherCode?.let { ResourceHelper.getWeatherIcon(mResourceProvider, it, false) },
                missingIconVisibility = View.INVISIBLE
            )
            dailyItem.contentDescription = talkBackBuilder.toString()
            dailyItem.setOnClickListener {
                onItemClicked(activity, location, bindingAdapterPosition, DetailScreen.TAG_PRECIPITATION)
            }
        }
    }

    init {
        val maxDailyPrecipitation = location.weather!!.dailyForecast
            .mapNotNull { it.day?.precipitation?.total }
            .maxOrNull()
        val maxNightPrecipitation = location.weather!!.dailyForecast
            .mapNotNull { it.night?.precipitation?.total }
            .maxOrNull()
        if (maxDailyPrecipitation != null || maxNightPrecipitation != null) {
            mHighestPrecipitation = maxOf(
                maxDailyPrecipitation?.value ?: 0,
                maxNightPrecipitation?.value ?: 0
            ).toFloat()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trend_daily, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbsDailyTrendAdapter.ViewHolder, position: Int) {
        (holder as ViewHolder).onBindView(activity, location, position)
    }

    override fun getItemCount() = location.weather!!.dailyForecast.size

    override fun isValid(location: Location) = mHighestPrecipitation != null

    override fun getDisplayName(context: Context) = context.getString(R.string.tag_precipitation)

    override fun bindBackgroundForHost(host: TrendRecyclerView) {
        mHighestPrecipitation?.let {
            val keyLineList = mutableListOf<TrendRecyclerView.KeyLine>()
            keyLineList.add(
                TrendRecyclerView.KeyLine(
                    Precipitation.PRECIPITATION_HALF_DAY_LIGHT.toFloat(),
                    Precipitation.PRECIPITATION_HALF_DAY_LIGHT.millimeters.formatValue(activity),
                    activity.getString(R.string.precipitation_intensity_light),
                    TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
            )
            keyLineList.add(
                TrendRecyclerView.KeyLine(
                    Precipitation.PRECIPITATION_HALF_DAY_HEAVY.toFloat(),
                    Precipitation.PRECIPITATION_HALF_DAY_HEAVY.millimeters.formatValue(activity),
                    activity.getString(R.string.precipitation_intensity_heavy),
                    TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
            )
            keyLineList.add(
                TrendRecyclerView.KeyLine(
                    -Precipitation.PRECIPITATION_HALF_DAY_LIGHT.toFloat(),
                    Precipitation.PRECIPITATION_HALF_DAY_LIGHT.millimeters.formatValue(activity),
                    activity.getString(R.string.precipitation_intensity_light),
                    TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
                )
            )
            keyLineList.add(
                TrendRecyclerView.KeyLine(
                    -Precipitation.PRECIPITATION_HALF_DAY_HEAVY.toFloat(),
                    Precipitation.PRECIPITATION_HALF_DAY_HEAVY.millimeters.formatValue(activity),
                    activity.getString(R.string.precipitation_intensity_heavy),
                    TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
                )
            )
            host.setData(keyLineList, it, -it)
        }
    }
}
