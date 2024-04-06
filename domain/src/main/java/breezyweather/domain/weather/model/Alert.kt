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

package breezyweather.domain.weather.model

import android.graphics.Color
import androidx.annotation.ColorInt
import java.io.Serializable
import java.util.Date

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
    val headline: String? = null,
    val description: String? = null,
    val instruction: String? = null,
    val source: String? = null,
    val severity: AlertSeverity = AlertSeverity.UNKNOWN,
    @ColorInt color: Int? = null
) : Serializable {

    @ColorInt
    val color: Int = color ?: when (severity) {
        AlertSeverity.EXTREME -> Color.rgb(212, 45, 65)
        AlertSeverity.SEVERE -> Color.rgb(240, 140, 17)
        AlertSeverity.MODERATE -> Color.rgb(244, 207, 0)
        AlertSeverity.MINOR -> Color.rgb(57, 156, 199)
        else -> Color.rgb(130, 168, 223)
    }

}
