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

package org.breezyweather.sources.brightsky.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class BrightSkyAlert(
    val id: Int?,
    @Serializable(DateSerializer::class) val onset: Date?,
    @Serializable(DateSerializer::class) val expires: Date?,
    val severity: String?,
    @SerialName("headline_en") val headlineEn: String?,
    @SerialName("headline_de") val headlineDe: String?,
    @SerialName("description_en") val descriptionEn: String?,
    @SerialName("description_de") val descriptionDe: String?,
    @SerialName("instruction_en") val instructionEn: String?,
    @SerialName("instruction_de") val instructionDe: String?,
)
