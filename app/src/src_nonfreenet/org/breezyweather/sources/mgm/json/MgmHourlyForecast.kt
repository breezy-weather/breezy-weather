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

package org.breezyweather.sources.mgm.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MgmHourlyForecast(
    // Do not use @Serializable(DateSerializer::class) for "tarih".
    // The timestamp is in actually Europe/Istanbul, not Etc/UTC.
    // The 'Z' at the end of the timestamp is misused.
    @SerialName("tarih") val time: String,
    @SerialName("hadise") val condition: String?,
    @SerialName("sicaklik") val temperature: Double?,
    @SerialName("nem") val humidity: Double?,
    @SerialName("ruzgarYonu") val windDirection: Double?,
    @SerialName("ruzgarHizi") val windSpeed: Double?,
    @SerialName("maksimumRuzgarHizi") val gust: Double?,
)
