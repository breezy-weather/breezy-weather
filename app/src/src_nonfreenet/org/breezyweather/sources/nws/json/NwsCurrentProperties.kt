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

package org.breezyweather.sources.nws.json

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class NwsCurrentProperties(
    val elevation: NwsCurrentValue?,
    @Serializable(DateSerializer::class) val timestamp: Date?,
    val textDescription: String?,
    val icon: String?,
    val temperature: NwsCurrentValue?,
    val dewpoint: NwsCurrentValue?,
    val windDirection: NwsCurrentValue?,
    val windSpeed: NwsCurrentValue?,
    val windGust: NwsCurrentValue?,
    val barometricPressure: NwsCurrentValue?,
    val seaLevelPressure: NwsCurrentValue?,
    val visibility: NwsCurrentValue?,
    val relativeHumidity: NwsCurrentValue?,
    val windChill: NwsCurrentValue?,
)
