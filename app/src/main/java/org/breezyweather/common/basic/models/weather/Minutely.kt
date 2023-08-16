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

package org.breezyweather.common.basic.models.weather

import java.io.Serializable
import java.util.*
import kotlin.math.log10
import kotlin.math.pow

/**
 * Minutely.
 */
class Minutely : Serializable {
    val date: Date
    val minuteInterval: Int
    val dbz: Int?

    constructor(date: Date, minuteInterval: Int, dbz: Int?) {
        this.date = date
        this.minuteInterval = minuteInterval
        this.dbz = dbz
    }

    constructor(date: Date, minuteInterval: Int, precipitationIntensity: Double?) {
        this.date = date
        this.minuteInterval = minuteInterval
        this.dbz = precipitationIntensityToDBZ(precipitationIntensity)
    }

    val precipitationIntensity: Double?
        get() {
            if (dbz == null) return null
            return if (dbz <= 5)  0.0 else (10.0.pow(dbz / 10.0) / 200.0).pow(5.0 / 8.0)
        }

    companion object {
        private fun precipitationIntensityToDBZ(intensity: Double?): Int? {
            return if (intensity == null) null else (10.0 * log10(
                200.0 * Math.pow(intensity, 8.0 / 5.0)
            )).toInt()
        }
    }
}
