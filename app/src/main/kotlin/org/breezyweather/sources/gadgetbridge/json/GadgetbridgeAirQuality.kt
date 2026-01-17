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

package org.breezyweather.sources.gadgetbridge.json

import kotlinx.serialization.Serializable

@Serializable
data class GadgetbridgeAirQuality(
    val aqi: Int? = null,
    val co: Float? = null,
    val no2: Float? = null,
    val o3: Float? = null,
    val pm10: Float? = null,
    val pm25: Float? = null,
    val so2: Float? = null,
    val coAqi: Int? = null,
    val no2Aqi: Int? = null,
    val o3Aqi: Int? = null,
    val pm10Aqi: Int? = null,
    val pm25Aqi: Int? = null,
    val so2Aqi: Int? = null,
)
