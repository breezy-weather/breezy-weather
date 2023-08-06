/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.main.adapters.main

interface ViewType {
    companion object {
        const val HEADER = 0
        const val DAILY = 1
        const val HOURLY = 2
        const val AIR_QUALITY = 3
        const val ALLERGEN = 4
        const val ASTRO = 5
        const val LIVE = 6
        const val FOOTER = -1
    }
}
