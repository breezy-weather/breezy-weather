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

package org.breezyweather.ui.main.adapters.main

interface ViewType {
    companion object {
        const val HEADER = 0
        const val PRECIPITATION_NOWCAST = 1
        const val DAILY = 2
        const val HOURLY = 3
        const val AIR_QUALITY = 4
        const val POLLEN = 5
        const val ASTRO = 6
        const val LIVE = 7
        const val FOOTER = -1
    }
}
