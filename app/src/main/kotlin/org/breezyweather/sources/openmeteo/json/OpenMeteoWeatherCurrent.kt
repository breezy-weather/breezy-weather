/*
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

package org.breezyweather.sources.openmeteo.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoWeatherCurrent(
    @SerialName("temperature_2m") val temperature: Double?,
    @SerialName("apparent_temperature") val apparentTemperature: Double?,
    @SerialName("weather_code") val weatherCode: Int?,
    @SerialName("wind_speed_10m") val windSpeed: Double?,
    @SerialName("wind_direction_10m") val windDirection: Double?,
    @SerialName("wind_gusts_10m") val windGusts: Double?,
    @SerialName("uv_index") val uvIndex: Double?,
    @SerialName("relative_humidity_2m") val relativeHumidity: Int?,
    @SerialName("dew_point_2m") val dewPoint: Double?,
    @SerialName("pressure_msl") val pressureMsl: Double?,
    @SerialName("cloud_cover") val cloudCover: Int?,
    val visibility: Double?,
    val time: Long,
)
