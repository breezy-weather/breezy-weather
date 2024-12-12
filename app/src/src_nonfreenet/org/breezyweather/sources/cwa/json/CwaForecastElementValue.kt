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

package org.breezyweather.sources.cwa.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CwaForecastElementValue(
    @SerialName("Temperature") val temperature: String?,
    @SerialName("MaxTemperature") val maxTemperature: String?,
    @SerialName("MinTemperature") val minTemperature: String?,
    @SerialName("DewPoint") val dewPoint: String?,
    @SerialName("ApparentTemperature") val apparentTemperature: String?,
    @SerialName("MaxApparentTemperature") val maxApparentTemperature: String?,
    @SerialName("MinApparentTemperature") val minApparentTemperature: String?,
    @SerialName("RelativeHumidity") val relativeHumidity: String?,
    @SerialName("WindDirection") val windDirection: String?,
    @SerialName("WindSpeed") val windSpeed: String?,
    @SerialName("ProbabilityOfPrecipitation") val probabilityOfPrecipitation: String?,
    @SerialName("Weather") val weather: String?,
    @SerialName("WeatherCode") val weatherCode: String?,
    @SerialName("UVIndex") val uvIndex: String?,
)
