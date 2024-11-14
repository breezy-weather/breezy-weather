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

package org.breezyweather.sources.brightsky.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class BrightSkyCurrentWeather(
    @Serializable(DateSerializer::class) val timestamp: Date?,
    val icon: String?,
    val temperature: Double?,
    @SerialName("wind_direction_10") val windDirection: Int?,
    @SerialName("wind_speed_10") val windSpeed: Double?,
    @SerialName("wind_gust_direction_10") val windGustDirection: Int?,
    @SerialName("wind_gust_speed_10") val windGustSpeed: Double?,
    @SerialName("cloud_cover") val cloudCover: Int?,
    @SerialName("dew_point") val dewPoint: Double?,
    @SerialName("relative_humidity") val relativeHumidity: Int?,
    @SerialName("pressure_msl") val pressure: Double?,
    val visibility: Int?,
)
