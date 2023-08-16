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

package org.breezyweather.main.adapters.location

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.source.MainWeatherSource

class LocationModel(
    context: Context,
    val location: Location,
    val mainWeatherSource: MainWeatherSource?,
    unit: TemperatureUnit, // TODO: Add back temperature
    var selected: Boolean
) {
    var weatherCode: WeatherCode? = null
    var weatherText: String? = null
    val currentPosition: Boolean = location.isCurrentPosition
    val residentPosition: Boolean = location.isResidentPosition
    val title: String = location.getPlace(context, true)
    val body: String = if (location.isUsable) location.administrationLevels() else context.getString(R.string.location_current_not_found_yet)
    var alerts: String? = null

    init {
        if (location.weather != null) {
            weatherCode = location.weather.current?.weatherCode
            weatherText = location.weather.current?.weatherText
            if (location.weather.currentAlertList.size > 0) {
                val builder = StringBuilder()
                location.weather.currentAlertList.forEach { alert ->
                    if (builder.toString().isNotEmpty()) {
                        builder.append("\n")
                    }
                    builder.append(alert.description)
                    if (alert.startDate != null) {
                        val startDateDay = alert.startDate.getFormattedDate(
                            location.timeZone, context.getString(R.string.date_format_short)
                        )
                        builder.append(", ")
                            .append(startDateDay)
                            .append(", ")
                            .append(alert.startDate.getFormattedTime(location.timeZone, context.is12Hour))
                        if (alert.endDate != null) {
                            builder.append("-")
                            val endDateDay = alert.endDate.getFormattedDate(
                                location.timeZone, context.getString(R.string.date_format_short)
                            )
                            if (startDateDay != endDateDay) {
                                builder.append(endDateDay)
                                    .append(", ")
                            }
                            builder.append(alert.endDate.getFormattedTime(location.timeZone, context.is12Hour))
                        }
                    }
                }
                alerts = builder.toString()
            }
        }
    }

    fun areItemsTheSame(newItem: LocationModel) = location.formattedId == newItem.location.formattedId

    fun areContentsTheSame(newItem: LocationModel) = location == newItem.location && selected == newItem.selected
}
