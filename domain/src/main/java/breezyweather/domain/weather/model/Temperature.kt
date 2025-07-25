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

package breezyweather.domain.weather.model

import breezyweather.domain.weather.wrappers.TemperatureWrapper
import java.io.Serializable

/**
 * Temperature.
 * default unit : [TemperatureUnit.C]
 */
data class Temperature(
    val temperature: Double? = null,
    val sourceFeelsLike: Double? = null,
    val computedApparent: Double? = null,
    val computedWindChill: Double? = null,
    val computedHumidex: Double? = null,
) : Serializable {

    val feelsLikeTemperature: Double? = sourceFeelsLike
        ?: computedApparent
        ?: computedWindChill
        ?: computedHumidex

    fun toTemperatureWrapper() = TemperatureWrapper(
        temperature = this.temperature,
        feelsLike = this.sourceFeelsLike
    )
}
