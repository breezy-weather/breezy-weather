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
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.options.appearance.DetailScreen
import org.breezyweather.common.basic.models.options.unit.PrecipitationUnit
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.getHalfDayPrecipitationColor
import org.breezyweather.ui.common.widgets.trend.TrendRecyclerView
import org.breezyweather.ui.common.widgets.trend.chart.DoubleHistogramView
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.providers.ResourceProvider

/**
 * Daily precipitation adapter.
 */
class DailyPrecipitationAdapter(
    activity: GeoActivity,
    location: Location,
    provider: ResourceProvider,
    unit: PrecipitationUnit,
) : AbsDailyTrendAdapter(activity, location) {
    private val mResourceProvider: ResourceProvider = provider
    private val mPrecipitationUnit: PrecipitationUnit = unit
    private var mHighestPrecipitation: Float = 0f

    inner class ViewHolder(itemView: View) : AbsDailyTrendAdapter.ViewHolder(itemView) {
        private val mDoubleHistogramView: DoubleHistogramView

        init {
            mDoubleHistogramView = DoubleHistogramView(itemView.context)
            dailyItem.chartItemView = mDoubleHistogramView
        }

        @SuppressLint("SetTextI18n, InflateParams")
        fun onBindView(activity: GeoActivity, location: Location, position: Int) {
            val talkBackBuilder = StringBuilder(activity.getString(R.string.tag_precipitation))
            super.onBindView(activity, location, talkBackBuilder, position)
            val weather = location.weather!!
            val daily = weather.dailyForecast[position]
            val daytimePrecipitation = daily.day?.precipitation?.total
            val nighttimePrecipitation = daily.night?.precipitation?.total
            if ((daytimePrecipitation != null && daytimePrecipitation > 0f) ||
                (nighttimePrecipitation != null && nighttimePrecipitation > 0f)
            ) {
                talkBackBuilder.append(activity.getString(R.string.comma_separator))
                    .append(activity.getString(R.string.daytime))
                    .append(activity.getString(R.string.colon_separator))
                    .append(
                        if (daytimePrecipitation != null && daytimePrecipitation > 0f) {
                            mPrecipitationUnit.getValueVoice(activity, daytimePrecipitation)
                        } else {
                            activity.getString(R.string.precipitation_none)
                        }
                    )
                talkBackBuilder.append(activity.getString(R.string.comma_separator))
                    .append(activity.getString(R.string.nighttime))
                    .append(activity.getString(R.string.colon_separator))
                    .append(
                        if (nighttimePrecipitation != null && nighttimePrecipitation > 0f) {
                            mPrecipitationUnit.getValueVoice(activity, nighttimePrecipitation)
                        } else {
                            activity.getString(R.string.precipitation_none)
                        }
                    )
            } else {
                talkBackBuilder.append(activity.getString(R.string.comma_separator))
                    .append(activity.getString(R.string.precipitation_none))
            }
            dailyItem.setDayIconDrawable(
                daily.day?.weatherCode?.let { ResourceHelper.getWeatherIcon(mResourceProvider, it, true) },
                missingIconVisibility = View.INVISIBLE
            )
            mDoubleHistogramView.setData(
                daily.day?.precipitation?.total?.toFloat(),
                daily.night?.precipitation?.total?.toFloat(),
                daytimePrecipitation?.let { mPrecipitationUnit.getValueTextWithoutUnit(activity, it) },
                nighttimePrecipitation?.let { mPrecipitationUnit.getValueTextWithoutUnit(activity, it) },
                mHighestPrecipitation
            )
            mDoubleHistogramView.setLineColors(
                daily.day?.precipitation?.getHalfDayPrecipitationColor(activity) ?: Color.TRANSPARENT,
                daily.night?.precipitation?.getHalfDayPrecipitationColor(activity) ?: Color.TRANSPARENT,
                MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
            )
            mDoubleHistogramView.setTextColors(
                MainThemeColorProvider.getColor(location, R.attr.colorBodyText)
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
        mHighestPrecipitation = maxOf(
            location.weather!!.dailyForecast
                .mapNotNull { it.day?.precipitation?.total }
                .maxOrNull() ?: 0.0,
            location.weather!!.dailyForecast
                .mapNotNull { it.night?.precipitation?.total }
                .maxOrNull() ?: 0.0
        ).toFloat()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trend_daily, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbsDailyTrendAdapter.ViewHolder, position: Int) {
        (holder as ViewHolder).onBindView(activity, location, position)
    }

    override fun getItemCount() = location.weather!!.dailyForecast.size

    override fun isValid(location: Location) = mHighestPrecipitation > 0

    override fun getDisplayName(context: Context) = context.getString(R.string.tag_precipitation)

    override fun bindBackgroundForHost(host: TrendRecyclerView) {
        val unit = SettingsManager.getInstance(activity).getPrecipitationUnit(activity)
        val keyLineList = mutableListOf<TrendRecyclerView.KeyLine>()
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                Precipitation.PRECIPITATION_HALF_DAY_LIGHT.toFloat(),
                unit.getValueTextWithoutUnit(activity, Precipitation.PRECIPITATION_HALF_DAY_LIGHT),
                activity.getString(R.string.precipitation_intensity_light),
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                Precipitation.PRECIPITATION_HALF_DAY_HEAVY.toFloat(),
                unit.getValueTextWithoutUnit(activity, Precipitation.PRECIPITATION_HALF_DAY_HEAVY),
                activity.getString(R.string.precipitation_intensity_heavy),
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                -Precipitation.PRECIPITATION_HALF_DAY_LIGHT.toFloat(),
                unit.getValueTextWithoutUnit(activity, Precipitation.PRECIPITATION_HALF_DAY_LIGHT),
                activity.getString(R.string.precipitation_intensity_light),
                TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
            )
        )
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                -Precipitation.PRECIPITATION_HALF_DAY_HEAVY.toFloat(),
                unit.getValueTextWithoutUnit(activity, Precipitation.PRECIPITATION_HALF_DAY_HEAVY),
                activity.getString(R.string.precipitation_intensity_heavy),
                TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
            )
        )
        host.setData(keyLineList, mHighestPrecipitation, -mHighestPrecipitation)
    }
}
