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

package org.breezyweather.sources.lhmt.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateUtcSerializer
import java.util.Date

@Serializable
data class LhmtAlertSingleAlert(
    val description: LhmtAlertText?,
    val headline: LhmtAlertText?,
    val instruction: LhmtAlertText?,
    val phenomenon: String?,
    @SerialName("response_type") val responseType: LhmtAlertResponseType?,
    val severity: String?,
    @SerialName("t_from") val tFrom:
    @Serializable(DateUtcSerializer::class)
    Date?,
    @SerialName("t_to") val tTo:
    @Serializable(DateUtcSerializer::class)
    Date?,
)
