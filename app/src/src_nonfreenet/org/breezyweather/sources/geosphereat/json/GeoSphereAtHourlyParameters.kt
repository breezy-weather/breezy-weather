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

package org.breezyweather.sources.geosphereat.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeoSphereAtHourlyParameters(
    // Hourly
    val sy: GeoSphereAtHourlyDoubleParameter?, // Always integer but formatted as a double
    @SerialName("2t") val temp2: GeoSphereAtHourlyDoubleParameter?,
    val tp: GeoSphereAtHourlyDoubleParameter?, // kg m²
    val rain: GeoSphereAtHourlyDoubleParameter?, // kg m²
    val sf: GeoSphereAtHourlyDoubleParameter?, // kg m²
    @SerialName("10u") val wind10u: GeoSphereAtHourlyDoubleParameter?, // m/s
    @SerialName("10v") val wind10v: GeoSphereAtHourlyDoubleParameter?, // m/s
    @SerialName("10fg") val windgusts: GeoSphereAtHourlyDoubleParameter?, // m/s
    @SerialName("2r") val rh2: GeoSphereAtHourlyDoubleParameter?, // %
    val tcc: GeoSphereAtHourlyDoubleParameter?, // to be multiplied by 100
    val msl: GeoSphereAtHourlyDoubleParameter?, // Pa

    // Nowcast
    val rr: GeoSphereAtHourlyDoubleParameter?, // kg m²

    // Air quality
    val pm25surf: GeoSphereAtHourlyDoubleParameter?, // ug m-3
    val pm10surf: GeoSphereAtHourlyDoubleParameter?, // ug m-3
    val no2surf: GeoSphereAtHourlyDoubleParameter?, // ug m-3
    val o3surf: GeoSphereAtHourlyDoubleParameter?, // ug m-3
)
