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

package org.breezyweather.common.basic.models.weather

import android.graphics.Color
import androidx.annotation.ColorInt
import java.io.Serializable
import java.util.*

/**
 * Alert.
 *
 * All properties are [androidx.annotation.NonNull].
 */
class Alert(
    val alertId: Long, // TODO: Replace with a hash
    val startDate: Date? = null,
    val endDate: Date? = null,
    val description: String,
    val content: String? = null,
    val priority: Int,
    @ColorInt color: Int? = null
) : Serializable {

    @ColorInt
    val color: Int = color ?: Color.rgb(255, 184, 43)

}
