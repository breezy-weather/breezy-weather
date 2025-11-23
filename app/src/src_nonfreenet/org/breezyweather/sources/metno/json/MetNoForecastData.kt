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
data class MetNoForecastData(
    val instant: MetNoForecastDataInstant?,
    @SerialName("next_12_hours") val next12Hours: MetNoForecastDataNextHours?,
    @SerialName("next_1_hours") val next1Hours: MetNoForecastDataNextHours?,
    @SerialName("next_6_hours") val next6Hours: MetNoForecastDataNextHours?,
) {
    val symbolCode: String?
        get() = next1Hours?.summary?.symbolCode
            ?: next6Hours?.summary?.symbolCode
            ?: next12Hours?.summary?.symbolCode
}
