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

package org.breezyweather.common.source

enum class SecondaryWeatherSourceFeature(
    val id: String
) {
    FEATURE_CURRENT("current"),
    FEATURE_AIR_QUALITY("airQuality"),
    FEATURE_POLLEN("pollen"),
    FEATURE_MINUTELY("minutely"),
    FEATURE_ALERT("alert"),
    FEATURE_NORMALS("normals");

    companion object {

        fun getInstance(
            value: String
        ) = SecondaryWeatherSourceFeature.entries.firstOrNull {
            it.id == value
        }
    }
}
