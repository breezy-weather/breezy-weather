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
import java.util.Date

class Astro(
    val riseDate: Date? = null,
    val setDate: Date? = null,
) : Serializable {

    // Not made to be used for moon astro, only sun
    val duration: Double?
        get() = if (riseDate == null || setDate == null) {
            // Polar night
            0.0
        } else if (riseDate.after(setDate)) {
            null
        } else {
            (setDate.time - riseDate.time) // get delta milliseconds
                .div(1000) // seconds
                .div(60) // minutes
                .div(60.0) // hours
        }

    val isValid: Boolean
        get() = riseDate != null && setDate != null
}
