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

package org.breezyweather.sources.ims.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImsHourly(
    val hour: String,
    @SerialName("weather_code") val weatherCode: String?,
    @SerialName("rain_chance") val rainChance: String?,
    @SerialName("precise_temperature") val preciseTemperature: String?,
    @SerialName("relative_humidity") val relativeHumidity: String?,
    @SerialName("wind_direction_id") val windDirectionId: String?,
    @SerialName("wind_speed") val windSpeed: Int?,
    @SerialName("wind_chill") val windChill: String?,
    @SerialName("u_v_index") val uvIndex: String?,
)
