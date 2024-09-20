/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources.breezydatashare.json

import kotlinx.serialization.Serializable

@Serializable
data class BreezyWeather (
    val refreshTime: Long? = null,
    val bulletin: BreezyBulletin? = null,
    val current: BreezyCurrent? = null,
    val daily: List<BreezyDaily>? = null,
    val hourly: List<BreezyHourly>? = null,
    val minutely: List<BreezyMinutely>? = null,
    val alerts: List<BreezyAlert>? = null,
    val normals: BreezyNormals? = null
)
