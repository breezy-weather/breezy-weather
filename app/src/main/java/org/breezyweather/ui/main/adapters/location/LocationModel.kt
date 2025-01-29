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

package org.breezyweather.ui.main.adapters.location

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.WeatherCode
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.extensions.getFormattedShortDayAndMonth
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.domain.location.model.getPlace

class LocationModel(
    context: Context,
    val location: Location,
    val forecastWeatherSource: WeatherSource?,
    unit: TemperatureUnit, // TODO: Add back temperature
    var selected: Boolean,
) {
    var weatherCode: WeatherCode? = null
    var weatherText: String? = null
    val currentPosition: Boolean = location.isCurrentPosition
    val title: String = location.getPlace(context, true)
    val body: String = if (location.isUsable) {
        location.administrationLevels()
    } else {
        context.getString(R.string.location_current_not_found_yet)
    }
    var alerts: String? = null

    init {
        location.weather?.let { weather ->
            weatherCode = weather.current?.weatherCode
            weatherText = weather.current?.weatherText
            if (weather.currentAlertList.isNotEmpty()) {
                val builder = StringBuilder()
                weather.currentAlertList.forEach { alert ->
                    if (builder.toString().isNotEmpty()) {
                        builder.append("\n")
                    }
                    builder.append(
                        alert.headline?.ifEmpty {
                            context.getString(R.string.alert)
                        } ?: context.getString(R.string.alert)
                    )
                    alert.startDate?.let { startDate ->
                        val startDateDay = startDate.getFormattedShortDayAndMonth(location, context)
                        builder.append(context.getString(R.string.comma_separator))
                            .append(startDateDay)
                            .append(context.getString(R.string.comma_separator))
                            .append(startDate.getFormattedTime(location, context, context.is12Hour))
                        alert.endDate?.let { endDate ->
                            builder.append("-")
                            val endDateDay = endDate.getFormattedShortDayAndMonth(location, context)
                            if (startDateDay != endDateDay) {
                                builder.append(endDateDay)
                                    .append(context.getString(R.string.comma_separator))
                            }
                            builder.append(endDate.getFormattedTime(location, context, context.is12Hour))
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
