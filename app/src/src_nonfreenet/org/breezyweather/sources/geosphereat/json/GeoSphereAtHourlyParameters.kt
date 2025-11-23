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
    val t2m: GeoSphereAtHourlyDoubleParameter?,
    @SerialName("rr_acc") val rrAcc: GeoSphereAtHourlyDoubleParameter?, // kg m²
    @SerialName("rain_acc") val rainAcc: GeoSphereAtHourlyDoubleParameter?, // kg m²
    @SerialName("snow_acc") val snowAcc: GeoSphereAtHourlyDoubleParameter?, // kg m²
    val u10m: GeoSphereAtHourlyDoubleParameter?, // m/s
    val ugust: GeoSphereAtHourlyDoubleParameter?, // m/s
    val v10m: GeoSphereAtHourlyDoubleParameter?, // m/s
    val vgust: GeoSphereAtHourlyDoubleParameter?, // m/s
    val rh2m: GeoSphereAtHourlyDoubleParameter?, // %
    val tcc: GeoSphereAtHourlyDoubleParameter?, // to be multiplied by 100
    val sp: GeoSphereAtHourlyDoubleParameter?, // Pa

    // Nowcast
    val rr: GeoSphereAtHourlyDoubleParameter?, // kg m²

    // Air quality
    val pm25surf: GeoSphereAtHourlyDoubleParameter?, // ug m-3
    val pm10surf: GeoSphereAtHourlyDoubleParameter?, // ug m-3
    val no2surf: GeoSphereAtHourlyDoubleParameter?, // ug m-3
    val o3surf: GeoSphereAtHourlyDoubleParameter?, // ug m-3
)
