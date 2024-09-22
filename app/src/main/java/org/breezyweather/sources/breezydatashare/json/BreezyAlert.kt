/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources.breezydatashare.json

import androidx.annotation.ColorInt
import breezyweather.domain.weather.model.AlertSeverity
import kotlinx.serialization.Serializable

@Serializable
data class BreezyAlert (
    val alertId: String,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val headline: String? = null,
    val description: String? = null,
    val instruction: String? = null,
    val source: String? = null,
    val severity: Int = AlertSeverity.UNKNOWN.id,
    @ColorInt val color: Int? = null
)
