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

package org.breezyweather.sources.breezydatashare.json

import kotlinx.serialization.Serializable

/**
 * Daily.
 */
@Serializable
data class BreezyDaily(
    /**
     * Daily date initialized at 00:00 in the TimeZone of the location
     */
    val date: Long,
    val day: BreezyHalfDay? = null,
    val night: BreezyHalfDay? = null,
    val degreeDay: BreezyDegreeDay? = null,
    val sun: BreezyAstro? = null,
    val moon: BreezyAstro? = null,
    val moonPhase: BreezyMoonPhase? = null,
    val airQuality: BreezyAirQuality? = null,
    // id of the pollen => details
    val pollen: Map<String, BreezyPollen>? = null,
    val uV: BreezyUV? = null,
    val sunshineDuration: BreezyDoubleUnit? = null
)
