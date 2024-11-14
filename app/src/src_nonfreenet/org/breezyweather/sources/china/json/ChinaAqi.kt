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

package org.breezyweather.sources.china.json

import kotlinx.serialization.Serializable

@Serializable
data class ChinaAqi(
    val pubTime: String?,
    val primary: String?,
    val suggest: String?,
    val src: String?,
    val pm25: String?,
    val pm25Desc: String?,
    val pm10: String?,
    val pm10Desc: String?,
    val o3: String?,
    val o3Desc: String?,
    val so2: String?,
    val so2Desc: String?,
    val no2: String?,
    val no2Desc: String?,
    val co: String?,
    val coDesc: String?,
)
