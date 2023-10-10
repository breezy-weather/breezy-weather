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
    /**
     * If not provided by the source, can be created from Object.hash().toString()
     * Usually, you will use three parameters: alert type or title, alert level, alert start time
     */
    val alertId: String,
    val startDate: Date? = null,
    val endDate: Date? = null,
    val description: String,
    val content: String? = null,
    /**
     * Priority.
     * The higher the number, the lower it will be in the list.
     * Number can be negative if necessary.
     * Example:
     * 1 => Very high priority
     * 2 => High
     * 3 => Medium
     * 4 => Low
     * 5 => Very low
     */
    val priority: Int,
    @ColorInt color: Int? = null
) : Serializable {

    @ColorInt
    val color: Int = color ?: Color.rgb(255, 184, 43)

}
