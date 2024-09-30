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
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class QWeatherAir(
    @Serializable(DateSerializer::class) val pubTime: Date?,
    val name: String?,
    val id: String?,
    val aqi: String?,
    val level: String?,
    val category: String?,
    val primary: String?,
    val pm10: String?,
    val pm2p5: String?,
    val no2: String?,
    val so2: String?,
    val co: String?,
    val o3: String?
)
