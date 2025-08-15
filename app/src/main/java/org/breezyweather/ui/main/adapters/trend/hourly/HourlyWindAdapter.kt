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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.basic.BreezyActivity
import org.breezyweather.common.basic.models.options.appearance.DetailScreen
import org.breezyweather.common.extensions.formatValue
import org.breezyweather.common.extensions.getBeaufortScaleStrength
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.domain.weather.model.getColor
import org.breezyweather.domain.weather.model.getContentDescription
import org.breezyweather.ui.common.images.RotateDrawable
import org.breezyweather.ui.common.widgets.trend.TrendRecyclerView
import org.breezyweather.ui.common.widgets.trend.chart.PolylineAndHistogramView
import org.breezyweather.unit.speed.Speed.Companion.beaufort

/**
 * Hourly wind adapter.
 */
class HourlyWindAdapter(
    activity: BreezyActivity,
    location: Location,
) : AbsHourlyTrendAdapter(activity, location) {
    private var mHighestWindSpeed: Float = 0f

    inner class ViewHolder(itemView: View) : AbsHourlyTrendAdapter.ViewHolder(itemView) {
        private val mPolylineAndHistogramView = PolylineAndHistogramView(itemView.context)

        init {
            hourlyItem.chartItemView = mPolylineAndHistogramView
        }

        @SuppressLint("SetTextI18n, InflateParams")
        fun onBindView(activity: BreezyActivity, location: Location, position: Int) {
            val talkBackBuilder = StringBuilder()
            super.onBindView(activity, location, talkBackBuilder, position)
            val hourly = location.weather!!.nextHourlyForecast[position]

            if (hourly.wind?.isValid == true) {
                talkBackBuilder
                    .append(activity.getString(org.breezyweather.unit.R.string.locale_separator))
                    .append(hourly.wind!!.getContentDescription(activity))
            }
            val windColor = hourly.wind?.getColor(activity) ?: Color.TRANSPARENT
            val hourlyIcon = hourly.wind?.degree?.let { degree ->
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
            hourlyIcon?.colorFilter = PorterDuffColorFilter(windColor, PorterDuff.Mode.SRC_ATOP)
            hourlyItem.setIconDrawable(hourlyIcon, missingIconVisibility = View.INVISIBLE)

            mPolylineAndHistogramView.setData(
                null, null,
                null, null,
                null, null,
                hourly.wind?.speed?.value?.toFloat() ?: 0f,
                hourly.wind?.speed?.formatValue(activity),
                mHighestWindSpeed, 0f
            )
            mPolylineAndHistogramView.setLineColors(
                windColor,
                windColor,
                activity.getThemeColor(com.google.android.material.R.attr.colorOutline)
            )

            mPolylineAndHistogramView.setTextColors(
                activity.getThemeColor(R.attr.colorTitleText),
                activity.getThemeColor(R.attr.colorBodyText),
                activity.getThemeColor(R.attr.colorTitleText)
            )
            mPolylineAndHistogramView.setHistogramAlpha(1f)
            hourlyItem.contentDescription = talkBackBuilder.toString()
            hourlyItem.setOnClickListener {
                onItemClicked(activity, location, bindingAdapterPosition, DetailScreen.TAG_WIND)
            }
        }
    }

    init {
        mHighestWindSpeed = location.weather!!.nextHourlyForecast
            .mapNotNull { it.wind?.speed?.value }
            .maxOrNull()?.toFloat() ?: 0f
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trend_hourly, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbsHourlyTrendAdapter.ViewHolder, position: Int) {
        (holder as ViewHolder).onBindView(activity, location, position)
    }

    override fun getItemCount(): Int {
        return location.weather!!.nextHourlyForecast.size
    }

    override fun isValid(location: Location): Boolean {
        return mHighestWindSpeed > 0
    }

    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.tag_wind)
    }

    override fun bindBackgroundForHost(host: TrendRecyclerView) {
        val keyLineList = mutableListOf<TrendRecyclerView.KeyLine>()
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                4.beaufort.inCentimetersPerSecond.toFloat(),
                4.beaufort.formatValue(activity),
                4.beaufort.getBeaufortScaleStrength(activity),
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                8.beaufort.inCentimetersPerSecond.toFloat(),
                8.beaufort.formatValue(activity),
                8.beaufort.getBeaufortScaleStrength(activity),
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        host.setData(keyLineList, mHighestWindSpeed, 0f)
    }
}
