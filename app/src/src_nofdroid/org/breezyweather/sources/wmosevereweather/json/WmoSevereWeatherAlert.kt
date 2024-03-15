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

package org.breezyweather.sources.wmosevereweather.json

import kotlinx.serialization.Serializable

@Serializable
data class WmoSevereWeatherAlert(
    val identifier: String?,
    val url: String?,
    val capURL: String?,
    val effective: String?, // Can’t be parsed automatically
    val onset: String?, // Can’t be parsed automatically
    val sent: String?, // Can’t be parsed automatically
    val expires: String?, // Can’t be parsed automatically
    val event: String?,
    val description: String?,
    val s: Int?,
    val coord: List<WmoSevereWeatherAlertCoord>
)