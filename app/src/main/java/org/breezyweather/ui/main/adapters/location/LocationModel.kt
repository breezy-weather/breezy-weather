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
import breezyweather.domain.weather.reference.WeatherCode
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getRelativeTime
import org.breezyweather.common.extensions.uncapitalize
import org.breezyweather.domain.location.model.getPlace
import org.breezyweather.domain.settings.SettingsManager
import java.util.Date
import kotlin.time.Duration.Companion.hours

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
    var alerts: Int = 0
    var body: String

    init {
        location.weather?.let { weather ->
            weatherCode = weather.current?.weatherCode
            weatherText = weather.current?.weatherText
            alerts = weather.currentAlertList.size
        }
        body = getWeatherText(context)
    }

    private fun getWeatherText(context: Context): String {
        if (!location.isUsable) {
            return context.getString(R.string.location_current_not_found_yet)
        }

        val refreshTime = location.weather?.base?.refreshTime
            ?: return location.administrationLevels()

        if (!location.alertSource.isNullOrEmpty()) {
            if (refreshTime.time < Date().time - 24.hours.inWholeMilliseconds) {
                return context.getString(
                    R.string.location_last_updated_x,
                    refreshTime.getRelativeTime(context).uncapitalize(context.currentLocale)
                )
            } else if (alerts > 0) {
                return "⚠ " + context.getString(R.string.location_has_active_alerts)
            }
        }

        val validity = SettingsManager.getInstance(context).updateInterval.validity
        return if (refreshTime.time < Date().time - validity.inWholeMilliseconds) {
            context.getString(
                R.string.location_last_updated_x,
                refreshTime.getRelativeTime(context).uncapitalize(context.currentLocale)
            )
        } else if (!weatherText.isNullOrEmpty()) {
            weatherText!!
        } else {
            location.administrationLevels()
        }
    }

    fun areItemsTheSame(newItem: LocationModel) = location.formattedId == newItem.location.formattedId

    fun areContentsTheSame(newItem: LocationModel) = location == newItem.location && selected == newItem.selected
}
