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

package breezyweather.domain.weather.model

import java.io.Serializable

/**
 * Precipitation duration.
 *
 * default unit : [ProbabilityUnit.PERCENT]
 */
class PrecipitationProbability(
    val total: Double? = null,
    val thunderstorm: Double? = null,
    val rain: Double? = null,
    val snow: Double? = null,
    val ice: Double? = null
) : Serializable {

    val isValid: Boolean
        get() = total != null && total > 0
}
