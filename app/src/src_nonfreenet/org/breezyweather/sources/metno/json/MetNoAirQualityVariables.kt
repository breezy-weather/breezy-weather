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

package org.breezyweather.sources.metno.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MetNoAirQualityVariables(
    @SerialName("no2_concentration") val no2Concentration: MetNoAirQualityConcentration?,
    @SerialName("pm10_concentration") val pm10Concentration: MetNoAirQualityConcentration?,
    @SerialName("pm25_concentration") val pm25Concentration: MetNoAirQualityConcentration?,
    @SerialName("o3_concentration") val o3Concentration: MetNoAirQualityConcentration?,
    @SerialName("so2_concentration") val so2Concentration: MetNoAirQualityConcentration?,
)
