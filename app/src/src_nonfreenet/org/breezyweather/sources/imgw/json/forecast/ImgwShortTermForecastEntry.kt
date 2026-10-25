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

package org.breezyweather.sources.imgw.json.forecast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class ImgwShortTermForecastEntry(
    @Serializable(DateSerializer::class) @SerialName("Date") val date: Date,
    @SerialName("Type") val type: String?,
    @SerialName("Icon10") val icon: String?,
    @SerialName("Temperature") val airTemperature: Double?,
    @SerialName("Temperature_Surface") val surfaceTemperature: Double?,
    @SerialName("Dewpoint_Temperature") val dewpointTemperature: Double?,
    @SerialName("Chill") val feelsLike: Double?,
    @SerialName("Humidity") val humidity: Double?,
    @SerialName("Cloud") val cloud: Int?,
    @SerialName("PressureMSL") val pressureMSL: Double?,
    @SerialName("Precipitation") val precipitation: Double?,
    @SerialName("Precipitation10m") val precipitation10m: Double?,
    @SerialName("Rain") val rain: Double?,
    @SerialName("Rain10m") val rain10m: Double?,
    @SerialName("Snow") val snow: Double?,
    @SerialName("Snow10m") val snow10m: Double?,
    @SerialName("Wind_Speed") val windSpeed: Double?,
    @SerialName("Wind_Dir") val windDir: Int?,
    @SerialName("Wind_Gust") val gustsSpeed: Double?,
    @SerialName("Gusts_Dir") val gustsDir: Int?,
    @SerialName("Irradiance_Radiation") val irradianceRadiation: Double?,
)
