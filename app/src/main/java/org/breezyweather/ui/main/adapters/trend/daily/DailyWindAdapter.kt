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
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.activities.BreezyActivity
import org.breezyweather.common.extensions.formatValue
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.common.options.appearance.DetailScreen
import org.breezyweather.domain.weather.model.drawableArrow
import org.breezyweather.domain.weather.model.getColor
import org.breezyweather.domain.weather.model.getContentDescription
import org.breezyweather.ui.common.widgets.trend.TrendRecyclerView
import org.breezyweather.ui.common.widgets.trend.chart.DoubleHistogramView
import org.breezyweather.unit.speed.Speed.Companion.metersPerSecond

/**
 * Daily wind adapter.
 */
class DailyWindAdapter(
    activity: BreezyActivity,
    location: Location,
) : AbsDailyTrendAdapter(activity, location) {
    private var mHighestWindSpeed = 15.metersPerSecond.inCentimetersPerSecond.toFloat() // TODO: Make this a const

    inner class ViewHolder(itemView: View) : AbsDailyTrendAdapter.ViewHolder(itemView) {
        private val mDoubleHistogramView = DoubleHistogramView(itemView.context)

        init {
            dailyItem.chartItemView = mDoubleHistogramView
        }

        @SuppressLint("SetTextI18n, InflateParams")
        fun onBindView(activity: BreezyActivity, location: Location, position: Int) {
            val talkBackBuilder = StringBuilder()
            super.onBindView(activity, location, talkBackBuilder, position)
            val daily = location.weather!!.dailyForecast[position]

            if (daily.day?.wind?.isValid == true) {
                talkBackBuilder
                    .append(activity.getString(org.breezyweather.unit.R.string.locale_separator))
                    .append(activity.getString(R.string.daytime))
                    .append(activity.getString(R.string.colon_separator))
                    .append(daily.day!!.wind!!.getContentDescription(activity))
            }
            val dayWindColor = daily.day?.wind?.getColor(activity) ?: Color.TRANSPARENT
            val dayIcon = daily.day?.wind?.drawableArrow?.let {
                AppCompatResources.getDrawable(activity, it)
            }
            dayIcon?.colorFilter = PorterDuffColorFilter(dayWindColor, PorterDuff.Mode.SRC_ATOP)
            dailyItem.setDayIconDrawable(dayIcon, missingIconVisibility = View.INVISIBLE)

            val nightWindColor = daily.night?.wind?.getColor(activity) ?: Color.TRANSPARENT

            mDoubleHistogramView.setData(
                daily.day?.wind?.speed?.value?.toFloat() ?: 0f,
                daily.night?.wind?.speed?.value?.toFloat() ?: 0f,
                daily.day?.wind?.speed?.formatValue(activity),
                daily.night?.wind?.speed?.formatValue(activity),
                mHighestWindSpeed
            )
            mDoubleHistogramView.setLineColors(
                dayWindColor,
                nightWindColor,
                activity.getThemeColor(com.google.android.material.R.attr.colorOutline)
            )
            mDoubleHistogramView.setTextColors(activity.getThemeColor(R.attr.colorBodyText))
            mDoubleHistogramView.setHistogramAlphas(1f, 0.5f)

            if (daily.night?.wind?.isValid == true) {
                talkBackBuilder
                    .append(activity.getString(org.breezyweather.unit.R.string.locale_separator))
                    .append(activity.getString(R.string.nighttime))
                    .append(activity.getString(R.string.colon_separator))
                    .append(daily.night!!.wind!!.getContentDescription(activity))
            }
            val nightIcon = daily.night?.wind?.drawableArrow?.let {
                AppCompatResources.getDrawable(activity, it)
            }
            nightIcon?.colorFilter = PorterDuffColorFilter(nightWindColor, PorterDuff.Mode.SRC_ATOP)
            dailyItem.setNightIconDrawable(nightIcon, missingIconVisibility = View.INVISIBLE)

            dailyItem.contentDescription = talkBackBuilder.toString()
            dailyItem.setOnClickListener {
                onItemClicked(activity, location, bindingAdapterPosition, DetailScreen.TAG_WIND)
            }
        }
    }

    init {
        maxOf(
            location.weather!!.dailyForecast
                .mapNotNull { it.day?.wind?.speed?.value }
                .maxOrNull() ?: 0L,
            location.weather!!.dailyForecast
                .mapNotNull { it.night?.wind?.speed?.value }
                .maxOrNull() ?: 0L
        ).let {
            if (it > mHighestWindSpeed) {
                mHighestWindSpeed = it.toFloat()
            }
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

    override fun isValid(location: Location) = location.weather!!.dailyForecast.any {
        it.day?.precipitation?.total != null || it.night?.precipitation?.total != null
    }

    override fun getDisplayName(context: Context) = context.getString(R.string.tag_wind)

    override fun bindBackgroundForHost(host: TrendRecyclerView) {
        val keyLineList = mutableListOf<TrendRecyclerView.KeyLine>()
        /*keyLineList.add(
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
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                4.beaufort.inCentimetersPerSecond.times(-1.0).toFloat(),
                4.beaufort.formatValue(activity),
                4.beaufort.getBeaufortScaleStrength(activity),
                TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
            )
        )
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                8.beaufort.inCentimetersPerSecond.times(-1.0).toFloat(),
                8.beaufort.formatValue(activity),
                8.beaufort.getBeaufortScaleStrength(activity),
                TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
            )
        )*/
        host.setData(keyLineList, mHighestWindSpeed, -mHighestWindSpeed)
    }
}
