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

package org.breezyweather.sources.here.json

import kotlinx.serialization.SerialName
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
    val temperature: Float?,
    val comfort: String?,
    val highTemperature: String?,
    val lowTemperature: String?,
    val humidity: String?,
    val dewPoint: Float?,
    val precipitation1H: Float?,
    val precipitation12H: Float?,
    val precipitation24H: Float?,
    val precipitationProbability: Int?,
    val precipitationDesc: String?,
    val rainFall: Float?,
    val snowFall: Float?,
    val airInfo: Int?,
    val windSpeed: Float?,
    val windDirection: Float?,
    val uvIndex: Int?,
    val barometerPressure: Float?,
    val visibility: Float?,
    val snowCover: Float?,
    val iconId: Int?
)