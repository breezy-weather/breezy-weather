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

/**
 * Base.
 */
data class Base(
    // val publishDate: Date = Date(),
    val refreshTime: Date? = null,
    val forecastUpdateTime: Date? = null,
    val currentUpdateTime: Date? = null,
    val airQualityUpdateTime: Date? = null,
    val pollenUpdateTime: Date? = null,
    val minutelyUpdateTime: Date? = null,
    val alertsUpdateTime: Date? = null,
    val normalsUpdateTime: Date? = null,
    val normalsUpdateLatitude: Double = 0.0,
    val normalsUpdateLongitude: Double = 0.0,
) : Serializable
