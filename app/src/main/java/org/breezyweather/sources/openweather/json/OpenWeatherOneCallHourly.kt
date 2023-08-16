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

package org.breezyweather.sources.openweather.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenWeatherOneCallHourly(
    val dt: Long,
    val temp: Float?,
    @SerialName("feels_like") val feelsLike: Float?,
    val pressure: Int?,
    val humidity: Int?,
    @SerialName("dew_point") val dewPoint: Float?,
    val uvi: Float?,
    val clouds: Int?,
    val visibility: Int?,
    @SerialName("wind_speed") val windSpeed: Float?,
    @SerialName("wind_deg") val windDeg: Int?,
    @SerialName("wind_gust") val windGust: Float?,
    val weather: List<OpenWeatherOneCallWeather>?,
    val pop: Float?,
    val rain: OpenWeatherOneCallPrecipitation?,
    val snow: OpenWeatherOneCallPrecipitation?
)
