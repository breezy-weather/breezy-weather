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

package org.breezyweather.sources.imd.json

import kotlinx.serialization.Serializable
import org.breezyweather.sources.imd.serializers.ImdAnySerializer

@Serializable
@Suppress("ktlint")
data class ImdWeatherResult(
    val apcp: List<@Serializable(ImdAnySerializer::class) Any?>? = null,
    val temp: List<@Serializable(ImdAnySerializer::class) Any?>? = null,
    val wspd: List<@Serializable(ImdAnySerializer::class) Any?>? = null,
    val wdir: List<@Serializable(ImdAnySerializer::class) Any?>? = null,
    val rh: List<@Serializable(ImdAnySerializer::class) Any?>? = null,
    val tcdc: List<@Serializable(ImdAnySerializer::class) Any?>? = null,
    val gust: List<@Serializable(ImdAnySerializer::class) Any?>? = null,
)
