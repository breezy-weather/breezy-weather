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

package org.breezyweather.sources.qweather.json

import kotlinx.serialization.Serializable

@Serializable
data class QWeatherLocation(
    val name: String?,
    val id: String?,
    val lat: String?,
    val lon: String?,
    val adm2: String?,
    val adm1: String?,
    val country: String?,
    val tz: String?,
    val utcOffset: String?,
    val isDst: String?,
    val type: String?,
    val rank: String?
)
