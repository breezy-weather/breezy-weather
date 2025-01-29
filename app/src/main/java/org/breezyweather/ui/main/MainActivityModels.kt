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

package org.breezyweather.ui.main

import breezyweather.domain.location.model.Location
import org.breezyweather.domain.location.model.isDaylight

class Indicator(
    val total: Int,
    val index: Int,
) {

    override fun equals(other: Any?): Boolean {
        return if (other is Indicator) {
            other.index == index && other.total == total
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = total
        result = 31 * result + index
        return result
    }
}

class PermissionsRequest(
    val permissionList: List<String>,
    val target: Location?,
    val triggeredByUser: Boolean,
) {

    private var consumed = false

    fun consume(): Boolean {
        if (consumed) {
            return false
        }

        consumed = true
        return true
    }
}

class DayNightLocation(
    val location: Location,
    val daylight: Boolean = location.isDaylight,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other is DayNightLocation) {
            return location == other.location && daylight == other.daylight
        }

        return false
    }

    override fun hashCode(): Int {
        var result = location.hashCode()
        result = 31 * result + daylight.hashCode()
        return result
    }
}
