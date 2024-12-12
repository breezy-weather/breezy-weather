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

package org.breezyweather.sources.namem.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class NamemDailyForecast(
    @SerialName("fore_date") @Serializable(DateSerializer::class) val foreDate: Date?,
    @SerialName("ww_n") val wwN: Int?,
    @SerialName("ww_d") val wwD: Int?,
    @SerialName("ww_n_per") val wwNPer: Double?,
    @SerialName("ww_d_per") val wwDPer: Double?,
    @SerialName("tem_n") val temN: Double?,
    @SerialName("tem_d") val temD: Double?,
    @SerialName("tem_n_feel") val temNFeel: Double?,
    @SerialName("tem_d_feel") val temDFeel: Double?,
    @SerialName("wnd_n") val wndN: Double?,
    @SerialName("wnd_d") val wndD: Double?,
)
