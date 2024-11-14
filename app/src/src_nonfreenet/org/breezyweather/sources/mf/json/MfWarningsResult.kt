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

/**
 * Mf warning current phenomenons
 */
@Serializable
data class MfWarningsResult(
    @Serializable(DateSerializer::class) @SerialName("update_time") val updateTime: Date? = null,
    @Serializable(DateSerializer::class) @SerialName("end_validity_time") val endValidityTime: Date? = null,
    val timelaps: List<MfWarningTimelaps>? = null,
    @SerialName("phenomenons_items") val phenomenonsItems: List<MfWarningPhenomenonMaxColor>? = null,
    val advices: List<MfWarningAdvice>? = null,
    val consequences: List<MfWarningConsequence>? = null,
    @SerialName("max_count_items") val maxCountItems: List<MfWarningMaxCountItems>? = null,
    val comments: MfWarningComments? = null,
    val text: MfWarningComments? = null,
    @SerialName("text_avalanche") val textAvalanche: MfWarningComments? = null,
)
