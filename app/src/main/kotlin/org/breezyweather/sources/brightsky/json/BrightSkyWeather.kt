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

package org.breezyweather.sources.brightsky.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class BrightSkyWeather(
    @Serializable(DateSerializer::class) val timestamp: Date,
    val icon: String?,
    val precipitation: Double?, // Previous hour
    @SerialName("precipitation_probability") val precipitationProbability: Int?, // Previous hour
    val temperature: Double?, // At timestamp
    @SerialName("wind_direction") val windDirection: Int?, // Previous hour
    @SerialName("wind_speed") val windSpeed: Double?, // Previous hour
    @SerialName("wind_gust_direction") val windGustDirection: Int?, // Previous hour
    @SerialName("wind_gust_speed") val windGustSpeed: Double?, // Previous hour
    @SerialName("cloud_cover") val cloudCover: Int?, // At timestamp
    @SerialName("dew_point") val dewPoint: Double?, // At timestamp
    @SerialName("relative_humidity") val relativeHumidity: Int?, // At timestamp
    val pressure: Double?, // At timestamp
    val visibility: Int?, // At timestamp
    val sunshine: Double?, // Previous hour, in minutes
)
