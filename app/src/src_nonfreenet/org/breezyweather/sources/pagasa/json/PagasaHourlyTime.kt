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

package org.breezyweather.sources.pagasa.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PagasaHourlyTime(
    @SerialName("@attributes") val attributes: PagasaHourlyAttributes?,
    val symbol: PagasaHourlyElement?,
    val precipitation: PagasaHourlyElement?,
    val windDirection: PagasaHourlyElement?,
    val windSpeed: PagasaHourlyElement?,
    val temperature: PagasaHourlyElement?,
    val relativeHumidity: PagasaHourlyElement?,
)
