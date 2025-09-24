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

package org.breezyweather.sources.polleninfo.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PollenInfoResult(
    val contamination: List<PollenContamination>?,
)

@Serializable
data class PollenContamination(
    @SerialName("poll_id") val pollId: Int,
    @SerialName("poll_title") val pollTitle: String,
    @SerialName("contamination_1") val contamination1: Int,
    @SerialName("contamination_2") val contamination2: Int,
    @SerialName("contamination_3") val contamination3: Int,
    @SerialName("contamination_4") val contamination4: Int,
)
