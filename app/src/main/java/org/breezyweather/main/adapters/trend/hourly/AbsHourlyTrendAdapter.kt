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

package org.breezyweather.main.adapters.trend.hourly

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.extensions.getFormattedMediumDayAndMonth
import org.breezyweather.common.extensions.getFormattedShortDayAndMonth
import org.breezyweather.common.extensions.getHour
import org.breezyweather.common.extensions.getHourIn24Format
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerViewAdapter
import org.breezyweather.common.ui.widgets.trend.item.HourlyTrendItemView
import org.breezyweather.main.dialogs.HourlyWeatherDialog
import org.breezyweather.main.utils.MainThemeColorProvider

abstract class AbsHourlyTrendAdapter(
    val activity: GeoActivity,
    location: Location,
) : TrendRecyclerViewAdapter<AbsHourlyTrendAdapter.ViewHolder>(location) {

    open class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val hourlyItem: HourlyTrendItemView = itemView.findViewById(R.id.item_trend_hourly)

        fun onBindView(
            activity: GeoActivity,
            location: Location,
            talkBackBuilder: StringBuilder,
            position: Int,
        ) {
            val context = itemView.context
            val weather = location.weather!!
            val hourly = weather.nextHourlyForecast[position]
            hourlyItem.setDayText(hourly.date.getFormattedShortDayAndMonth(location, context))
            talkBackBuilder
                .append(context.getString(R.string.comma_separator))
                .append(hourly.date.getFormattedMediumDayAndMonth(location, context))
                .append(context.getString(R.string.comma_separator))
                .append(hourly.date.getHour(location, activity))
            hourlyItem.setHourText(hourly.date.getHour(location, activity))
            val useAccentColorForDate = position == 0 || hourly.date.getHourIn24Format(location) == "0"
            hourlyItem.setTextColor(
                MainThemeColorProvider.getColor(location, R.attr.colorTitleText),
                MainThemeColorProvider.getColor(
                    location,
                    if (useAccentColorForDate) R.attr.colorBodyText else R.attr.colorCaptionText
                )
            )
            hourlyItem.setOnClickListener {
                onItemClicked(activity, location, bindingAdapterPosition)
            }
        }
    }

    abstract fun isValid(location: Location): Boolean
    abstract fun getDisplayName(context: Context): String
    abstract fun bindBackgroundForHost(host: TrendRecyclerView)

    companion object {
        protected fun onItemClicked(
            activity: GeoActivity,
            location: Location,
            adapterPosition: Int,
        ) {
            if (activity.isActivityResumed) {
                HourlyWeatherDialog.show(
                    activity,
                    location,
                    location.weather!!.nextHourlyForecast[adapterPosition]
                )
            }
        }
    }
}
