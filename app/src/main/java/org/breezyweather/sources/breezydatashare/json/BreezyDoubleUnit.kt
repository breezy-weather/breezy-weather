/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License.
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
 * Class that provides a weather data as it is stored initially in database, and its converted
 * value in the preferred unit by the user
 *
 * For simple needs, you probably only need preferredUnitFormatted
 */
@Serializable
data class BreezyDoubleUnit (
    /**
     * Example: 24.0
     */
    val originalValue: Double? = null,
    /**
     * Example: c
     */
    val originalUnit: String? = null,
    /**
     * Example: 75.2
     */
    val preferredUnitValue: Double? = null,
    /**
     * Example: f
     */
    val preferredUnitUnit: String? = null,
    /**
     * Example: 75.2 °F
     */
    val preferredUnitFormatted: String? = null,
    /**
     * Example: 75.2°
     */
    val preferredUnitFormattedShort: String? = null
)
