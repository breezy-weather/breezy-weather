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

package org.breezyweather.sources.openmeteo.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoWeatherHourly(
    val time: LongArray,
    @SerialName("temperature_2m") val temperature: Array<Double?>?,
    @SerialName("apparent_temperature") val apparentTemperature: Array<Double?>?,
    @SerialName("precipitation_probability") val precipitationProbability: Array<Int?>?,
    val precipitation: Array<Double?>?,
    val rain: Array<Double?>?,
    val showers: Array<Double?>?,
    val snowfall: Array<Double?>?,
    @SerialName("weathercode") val weatherCode: Array<Int?>?,
    @SerialName("windspeed_10m") val windSpeed: Array<Double?>?,
    @SerialName("winddirection_10m") val windDirection: Array<Int?>?,
    @SerialName("windgusts_10m") val windGusts: Array<Double?>?,
    @SerialName("uv_index") val uvIndex: Array<Double?>?,
    @SerialName("is_day") val isDay: IntArray?, /* Should be a boolean (true or false) but API returns an integer */
    @SerialName("relativehumidity_2m") val relativeHumidity: Array<Int?>?,
    @SerialName("dewpoint_2m") val dewPoint: Array<Double?>?,
    @SerialName("pressure_msl") val pressureMsl: Array<Double?>?,
    @SerialName("cloudcover") val cloudCover: Array<Int?>?,
    val visibility: Array<Double?>?,
)
