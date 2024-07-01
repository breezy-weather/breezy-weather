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

package org.breezyweather.sources.meteoam.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MeteoAmForecastData(
    @SerialName("0") val temp: Map<String, Int>?,
    @SerialName("1") val rhum: Map<String, Int>?,
    @SerialName("2") val pres: Map<String, Int>?,
    @SerialName("3") val tpop: Map<String, Int>?,
    @SerialName("4") val wdir: Map<String, Int>?,
    @SerialName("7") val wkph: Map<String, Int>?,
    @SerialName("9") val icon: Map<String, String>?
)