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
 * UV.
 */
class UV(
    val index: Double? = null,
) : Serializable {

    val isValid: Boolean
        get() = index != null

    companion object {
        const val UV_INDEX_LOW = 3.0
        const val UV_INDEX_MIDDLE = 6.0
        const val UV_INDEX_HIGH = 8.0
        const val UV_INDEX_EXCESSIVE = 11.0
    }
}
