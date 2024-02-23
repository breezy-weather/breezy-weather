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

import java.io.Serializable

/**
 * Current.
 *
 * default unit
 * [.relativeHumidity] : [RelativeHumidityUnit.PERCENT]
 * [.dewPoint] : [TemperatureUnit.C]
 * [.visibility] : [DistanceUnit.M]
 * [.ceiling] : [DistanceUnit.M]
 */
data class Current(
    val weatherText: String? = null,
    val weatherCode: WeatherCode? = null,
    val temperature: Temperature? = null,
    val wind: Wind? = null,
    val uV: UV? = null,
    val airQuality: AirQuality? = null,
    val relativeHumidity: Double? = null,
    val dewPoint: Double? = null,
    /**
     * Pressure at sea level
     */
    val pressure: Double? = null,
    val cloudCover: Int? = null,
    val visibility: Double? = null,
    val ceiling: Double? = null,
    val dailyForecast: String? = null,
    val hourlyForecast: String? = null
) : Serializable
