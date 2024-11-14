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
data class MfForecastDaily(
    @Serializable(DateSerializer::class) val time: Date,
    @SerialName("T_min") val tMin: Double?,
    @SerialName("T_max") val tMax: Double?,
    @SerialName("daily_weather_icon") val dailyWeatherIcon: String?,
    @SerialName("daily_weather_description") val dailyWeatherDescription: String?,
    @SerialName("sunrise_time") @Serializable(DateSerializer::class) val sunriseTime: Date?,
    @SerialName("sunset_time") @Serializable(DateSerializer::class) val sunsetTime: Date?,
    @SerialName("uv_index") val uvIndex: Int?,
)
