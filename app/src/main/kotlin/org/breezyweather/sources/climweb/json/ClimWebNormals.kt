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

package org.breezyweather.sources.climweb.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClimWebNormals(
    val date: String?,
    @SerialName("max-temp") val maxTemp: Double?,
    @SerialName("maximum-temperature") val maximumTemperature: Double?,
    @SerialName("mean-maximum-temperature") val meanMaximumTemperature: Double?,
    @SerialName("temperature-maximale") val temperatureMaximale: Double?,
    @SerialName("min-temp") val minTemp: Double?,
    @SerialName("minimum-temperature") val minimumTemperature: Double?,
    @SerialName("mean-minimum-temperature") val meanMinimumTemperature: Double?,
    @SerialName("temperature-minimale") val temperatureMinimale: Double?,
)
