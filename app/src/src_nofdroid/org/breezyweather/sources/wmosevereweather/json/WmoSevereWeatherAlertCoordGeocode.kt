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
import org.breezyweather.sources.wmosevereweather.WmoSevereWeatherGeocodeMultiPolygonSerializer

@Serializable(with = WmoSevereWeatherGeocodeMultiPolygonSerializer::class)
data class WmoSevereWeatherAlertCoordGeocode(
    val type: String? = null, // Values can be Polygon or MultiPolygon
    val coordinates: List<List<String>>? = null
)
