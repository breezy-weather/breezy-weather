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

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import org.breezyweather.R
import java.io.Serializable

/**
 * Precipitation.
 *
 * default unit : [PrecipitationUnit.MM]
 */
class Precipitation(
    val total: Float? = null,
    val thunderstorm: Float? = null,
    val rain: Float? = null,
    val snow: Float? = null,
    val ice: Float? = null
) : Serializable {

    companion object {
        // TODO: Seems a bit high, should probably lower that
        const val PRECIPITATION_LIGHT = 10f
        const val PRECIPITATION_MIDDLE = 25f
        const val PRECIPITATION_HEAVY = 50f
        const val PRECIPITATION_RAINSTORM = 100f
    }

    @ColorInt
    fun getPrecipitationColor(context: Context): Int {
        return if (total == null) {
            Color.TRANSPARENT
        } else when (total) {
            in 0f.. PRECIPITATION_LIGHT -> ContextCompat.getColor(context, R.color.colorLevel_1)
            in PRECIPITATION_LIGHT.. PRECIPITATION_MIDDLE -> ContextCompat.getColor(context, R.color.colorLevel_2)
            in PRECIPITATION_MIDDLE.. PRECIPITATION_HEAVY -> ContextCompat.getColor(context, R.color.colorLevel_3)
            in PRECIPITATION_HEAVY.. PRECIPITATION_RAINSTORM -> ContextCompat.getColor(context, R.color.colorLevel_4)
            in PRECIPITATION_RAINSTORM.. Float.MAX_VALUE -> ContextCompat.getColor(context, R.color.colorLevel_5)
            else -> Color.TRANSPARENT
        }
    }

    val isValid: Boolean
        get() = total != null && total > 0
}
