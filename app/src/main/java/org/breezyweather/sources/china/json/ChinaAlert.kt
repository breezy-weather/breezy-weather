/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources.china.json

import java.util.*

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer

@Serializable
data class ChinaAlert(
    val locationKey: String?,
    val level: String?,
    @Serializable(DateSerializer::class) val pubTime: Date?,
    val alertId: String?,
    val detail: String?,
    val title: String?,
    val type: String?
)
