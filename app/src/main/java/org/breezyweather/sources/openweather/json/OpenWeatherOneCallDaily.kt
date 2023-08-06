/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
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
data class OpenWeatherOneCallDaily(
    val dt: Long,
    val sunrise: Long?,
    val sunset: Long?,
    val moonrise: Long?,
    val moonset: Long?,
    val temp: OpenWeatherOneCallDailyTemp?,
    @SerialName("feels_like") val feelsLike: OpenWeatherOneCallDailyFeelsLike?,
    val pressure: Int?,
    val humidity: Int?,
    @SerialName("dew_point") val dewPoint: Float?,
    @SerialName("wind_speed") val windSpeed: Float?,
    @SerialName("wind_deg") val windDeg: Int?,
    @SerialName("wind_gust") val windGust: Float?,
    val weather: List<OpenWeatherOneCallWeather>?,
    val clouds: Int?,
    val pop: Float?,
    val rain: Float?,
    val snow: Float?,
    val uvi: Float?
)
