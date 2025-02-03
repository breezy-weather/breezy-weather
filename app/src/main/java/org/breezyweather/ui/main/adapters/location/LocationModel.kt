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
import org.breezyweather.domain.location.model.getPlace

class LocationModel(
    context: Context,
    val location: Location,
    unit: TemperatureUnit, // TODO: Add back temperature
    var selected: Boolean,
) {
    var weatherCode: WeatherCode? = null
    var weatherText: String? = null
    val currentPosition: Boolean = location.isCurrentPosition
    val title: String = (if (location.isCurrentPosition) "⊙ " else "") + location.getPlace(context)
    var alerts: Boolean = false
    var body: String

    init {
        location.weather?.let { weather ->
            weatherCode = weather.current?.weatherCode
            weatherText = weather.current?.weatherText
            alerts = weather.currentAlertList.isNotEmpty()
        }
        body = getWeatherText(context)
    }

    private fun getWeatherText(context: Context): String {
        return if (!location.isUsable) {
            context.getString(R.string.location_current_not_found_yet)
        }
        /*else if (location.weather?.base?.refreshTime != null &&
            location.weather!!.base.refreshTime!!.time < Date().time - 24.hours.inWholeMilliseconds
        ) {
            // TODO: Consider displaying last update time when it's been more than 24 hours with no refresh
            context.getString(
                R.string.location_last_updated_x,
                location.weather!!.base.refreshTime!!.getRelativeTime(context)
            )
        } else if (alerts) {
            // TODO: Consider displaying currently active alerts
            // context.getString(R.string.location_has_active_alerts)
        }*/
        else if (!weatherText.isNullOrEmpty()) {
            weatherText!!
        } else {
            location.administrationLevels()
        }
    }

    fun areItemsTheSame(newItem: LocationModel) = location.formattedId == newItem.location.formattedId

    fun areContentsTheSame(newItem: LocationModel) = location == newItem.location && selected == newItem.selected
}
