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

package breezyweather.domain.weather.wrappers

import org.breezyweather.unit.temperature.Temperature

/**
 * Temperature.
 */
data class TemperatureWrapper(
    val temperature: Temperature? = null,
    val feelsLike: Temperature? = null,
) {
    fun toTemperature(
        computedApparent: Temperature? = null,
        computedWindChill: Temperature? = null,
        computedHumidex: Temperature? = null,
    ) = breezyweather.domain.weather.model.Temperature(
        temperature = this.temperature,
        sourceFeelsLike = this.feelsLike,
        computedApparent = computedApparent,
        computedWindChill = computedWindChill,
        computedHumidex = computedHumidex
    )
}
