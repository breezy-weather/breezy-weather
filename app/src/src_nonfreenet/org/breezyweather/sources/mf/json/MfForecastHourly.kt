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

package org.breezyweather.sources.mf.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class MfForecastHourly(
    @Serializable(DateSerializer::class) val time: Date,
    @SerialName("T") val t: Double?,
    @SerialName("T_windchill") val tWindchill: Double?,
    @SerialName("weather_icon") val weatherIcon: String?,
    @SerialName("weather_description") val weatherDescription: String?,
    @SerialName("wind_direction") val windDirection: Int?,
    @SerialName("wind_icon") val windIcon: String?,
    @SerialName("wind_speed") val windSpeed: Int?,
    @SerialName("wind_speed_gust") val windSpeedGust: Int?,
    @SerialName("rain_1h") val rain1h: Double?,
    @SerialName("rain_3h") val rain3h: Double?,
    @SerialName("rain_6h") val rain6h: Double?,
    @SerialName("rain_12h") val rain12h: Double?,
    @SerialName("rain_24h") val rain24h: Double?,
    @SerialName("relative_humidity") val relativeHumidity: Int?,
    @SerialName("P_sea") val pSea: Double?,
    @SerialName("snow_1h") val snow1h: Double?,
    @SerialName("snow_3h") val snow3h: Double?,
    @SerialName("snow_6h") val snow6h: Double?,
    @SerialName("snow_12h") val snow12h: Double?,
    @SerialName("snow_24h") val snow24h: Double?,
    @SerialName("total_cloud_cover") val totalCloudCover: Int?,
)
