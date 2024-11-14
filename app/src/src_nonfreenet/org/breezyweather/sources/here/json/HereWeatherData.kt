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

package org.breezyweather.sources.here.json

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class HereWeatherData(
    @Serializable(DateSerializer::class) val time: Date,
    val weekday: String?,
    val description: String?,
    val daySegment: String?,
    val skyDesc: String?,
    val temperature: Double?,
    val comfort: String?,
    val highTemperature: String?,
    val lowTemperature: String?,
    val humidity: String?,
    val dewPoint: Double?,
    val precipitation1H: Double?,
    val precipitation12H: Double?,
    val precipitation24H: Double?,
    val precipitationProbability: Int?,
    val precipitationDesc: String?,
    val rainFall: Double?,
    val snowFall: Double?,
    val airInfo: Int?,
    val windSpeed: Double?,
    val windDirection: Double?,
    val uvIndex: Int?,
    val barometerPressure: Double?,
    val visibility: Double?,
    val snowCover: Double?,
    val iconId: Int?,
)
