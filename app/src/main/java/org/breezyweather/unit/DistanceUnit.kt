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

package org.breezyweather.unit

import android.content.Context
import org.breezyweather.R
import org.breezyweather.unit.distance.Distance
import org.breezyweather.unit.distance.Distance.Companion.meters

/**
 * Source: https://weather.metoffice.gov.uk/guides/what-does-this-forecast-mean
 */
const val VISIBILITY_VERY_POOR = 1000.0
const val VISIBILITY_POOR = 4000.0
const val VISIBILITY_MODERATE = 10000.0
const val VISIBILITY_GOOD = 20000.0
const val VISIBILITY_CLEAR = 40000.0

val visibilityScaleThresholds = listOf(
    0.meters,
    VISIBILITY_VERY_POOR.meters,
    VISIBILITY_POOR.meters,
    VISIBILITY_MODERATE.meters,
    VISIBILITY_GOOD.meters,
    VISIBILITY_CLEAR.meters
)

/**
 * @param context
 * @param visibility
 */
fun getVisibilityDescription(context: Context, visibility: Distance?): String? {
    if (visibility == null) return null
    return when (visibility.inMeters) {
        in 0.0..<VISIBILITY_VERY_POOR -> context.getString(R.string.visibility_very_poor)
        in VISIBILITY_VERY_POOR..<VISIBILITY_POOR -> context.getString(R.string.visibility_poor)
        in VISIBILITY_POOR..<VISIBILITY_MODERATE -> context.getString(R.string.visibility_moderate)
        in VISIBILITY_MODERATE..<VISIBILITY_GOOD -> context.getString(R.string.visibility_good)
        in VISIBILITY_GOOD..<VISIBILITY_CLEAR -> context.getString(R.string.visibility_clear)
        in VISIBILITY_CLEAR..Double.MAX_VALUE -> context.getString(R.string.visibility_perfectly_clear)
        else -> null
    }
}
