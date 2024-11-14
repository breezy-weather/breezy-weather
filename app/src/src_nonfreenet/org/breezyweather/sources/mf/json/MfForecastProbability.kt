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

package org.breezyweather.sources.mf.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class MfForecastProbability(
    @Serializable(DateSerializer::class) val time: Date,
    @SerialName("rain_hazard_3h") val rainHazard3h: Int?,
    @SerialName("rain_hazard_6h") val rainHazard6h: Int?,
    @SerialName("snow_hazard_3h") val snowHazard3h: Int?,
    @SerialName("snow_hazard_6h") val snowHazard6h: Int?,
    @SerialName("storm_hazard") val stormHazard: Int?,
    @SerialName("freezing_hazard") val freezingHazard: Int?,
)
