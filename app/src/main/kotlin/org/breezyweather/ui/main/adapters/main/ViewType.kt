/*
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

package org.breezyweather.ui.main.adapters.main

interface ViewType {
    companion object {
        const val HEADER = 0
        const val ALERT = 1
        const val PRECIPITATION_NOWCAST = 2
        const val DAILY = 3
        const val HOURLY = 4
        const val PRECIPITATION = 5
        const val WIND = 6
        const val AIR_QUALITY = 7
        const val POLLEN = 8
        const val HUMIDITY = 9
        const val UV = 10
        const val VISIBILITY = 11
        const val PRESSURE = 12
        const val SUN = 13
        const val MOON = 14
        const val CLOCK = 15
        const val FOOTER = -1

        fun isHalfSizeableBlock(viewType: Int): Boolean? {
            return when (viewType) {
                HEADER, ALERT, PRECIPITATION_NOWCAST, DAILY, HOURLY, FOOTER -> false
                PRECIPITATION, WIND, AIR_QUALITY, POLLEN, HUMIDITY, UV, VISIBILITY, PRESSURE, SUN, MOON, CLOCK -> true
                else -> null
            }
        }
    }
}
