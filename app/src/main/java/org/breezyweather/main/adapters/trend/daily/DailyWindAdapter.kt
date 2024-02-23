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
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import breezyweather.domain.location.model.Location
import org.breezyweather.common.basic.models.options.unit.SpeedUnit
import breezyweather.domain.weather.model.Wind
import org.breezyweather.common.ui.images.RotateDrawable
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView
import org.breezyweather.common.ui.widgets.trend.chart.DoubleHistogramView
import org.breezyweather.domain.weather.model.getColor
import org.breezyweather.domain.weather.model.getDescription
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.settings.SettingsManager

/**
 * Daily wind adapter.
 */
class DailyWindAdapter(activity: GeoActivity, location: Location, unit: SpeedUnit) : AbsDailyTrendAdapter(
    activity, location
) {
    private val mSpeedUnit: SpeedUnit = unit
    private var mHighestWindSpeed: Float = 0f

    inner class ViewHolder(itemView: View) : AbsDailyTrendAdapter.ViewHolder(itemView) {
        private val mDoubleHistogramView = DoubleHistogramView(itemView.context)

        init {
            dailyItem.chartItemView = mDoubleHistogramView
        }

        @SuppressLint("SetTextI18n, InflateParams")
        fun onBindView(activity: GeoActivity, location: Location, position: Int) {
            val talkBackBuilder = StringBuilder(activity.getString(R.string.tag_wind))
            super.onBindView(activity, location, talkBackBuilder, position)
            val daily = location.weather!!.dailyForecast[position]

            if (daily.day?.wind?.isValid == true) {
                talkBackBuilder
                    .append(", ").append(activity.getString(R.string.daytime))
                    .append(" : ").append(daily.day!!.wind!!.getDescription(activity, mSpeedUnit))
            }
            val dayWindColor = daily.day?.wind?.getColor(activity) ?: Color.TRANSPARENT
            val dayIcon = daily.day?.wind?.degree?.let { degree ->
                if (degree == -1.0) {
                    AppCompatResources.getDrawable(activity, R.drawable.ic_replay)
                } else {
                    RotateDrawable(
                        AppCompatResources.getDrawable(activity, R.drawable.ic_navigation)
                    ).apply {
                        rotate(degree.toFloat() + 180f)
                    }
                }
            }
            dayIcon?.colorFilter = PorterDuffColorFilter(dayWindColor, PorterDuff.Mode.SRC_ATOP)
            dailyItem.setDayIconDrawable(dayIcon, missingIconVisibility = View.INVISIBLE)

            val nightWindColor = daily.night?.wind?.getColor(activity) ?: Color.TRANSPARENT

            mDoubleHistogramView.setData(
                daily.day?.wind?.speed?.toFloat() ?: 0f,
                daily.night?.wind?.speed?.toFloat() ?: 0f,
                daily.day?.wind?.speed?.let { mSpeedUnit.getValueTextWithoutUnit(it) },
                daily.night?.wind?.speed?.let { mSpeedUnit.getValueTextWithoutUnit(it) },
                mHighestWindSpeed
            )
            mDoubleHistogramView.setLineColors(
                dayWindColor,
                nightWindColor,
                MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
            )
            mDoubleHistogramView.setTextColors(MainThemeColorProvider.getColor(location, R.attr.colorBodyText))
            mDoubleHistogramView.setHistogramAlphas(1f, 0.5f)

            if (daily.night?.wind?.isValid == true) {
                talkBackBuilder
                    .append(", ").append(activity.getString(R.string.nighttime))
                    .append(" : ").append(daily.night!!.wind!!.getDescription(activity, mSpeedUnit))
            }
            val nightIcon = daily.night?.wind?.degree?.let { degree ->
                if (degree == -1.0) {
                    AppCompatResources.getDrawable(activity, R.drawable.ic_replay)
                } else {
                    RotateDrawable(
                        AppCompatResources.getDrawable(activity, R.drawable.ic_navigation)
                    ).apply {
                        rotate(degree.toFloat() + 180f)
                    }
                }
            }
            nightIcon?.colorFilter = PorterDuffColorFilter(nightWindColor, PorterDuff.Mode.SRC_ATOP)
            dailyItem.setNightIconDrawable(nightIcon, missingIconVisibility = View.INVISIBLE)

            dailyItem.contentDescription = talkBackBuilder.toString()
        }
    }

    init {
        mHighestWindSpeed = maxOf(
            location.weather!!.dailyForecast
                .mapNotNull { it.day?.wind?.speed }
                .maxOrNull() ?: 0.0,
            location.weather!!.dailyForecast
                .mapNotNull { it.night?.wind?.speed }
                .maxOrNull() ?: 0.0
        ).toFloat()
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

    override fun isValid(location: Location) = mHighestWindSpeed > 0

    override fun getDisplayName(context: Context) = context.getString(R.string.tag_wind)

    override fun bindBackgroundForHost(host: TrendRecyclerView) {
        val unit = SettingsManager.getInstance(activity).speedUnit
        val keyLineList: MutableList<TrendRecyclerView.KeyLine> = ArrayList()
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                Wind.WIND_SPEED_3.toFloat(),
                unit.getValueTextWithoutUnit(Wind.WIND_SPEED_3),
                activity.getString(R.string.wind_strength_3),
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                Wind.WIND_SPEED_7.toFloat(),
                unit.getValueTextWithoutUnit(Wind.WIND_SPEED_7),
                activity.getString(R.string.wind_strength_7),
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                -Wind.WIND_SPEED_3.toFloat(),
                unit.getValueTextWithoutUnit(Wind.WIND_SPEED_3),
                activity.getString(R.string.wind_strength_3),
                TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
            )
        )
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                -Wind.WIND_SPEED_7.toFloat(),
                unit.getValueTextWithoutUnit(Wind.WIND_SPEED_7),
                activity.getString(R.string.wind_strength_7),
                TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
            )
        )
        host.setData(keyLineList, mHighestWindSpeed, -mHighestWindSpeed)
    }
}