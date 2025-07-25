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

package org.breezyweather.domain.weather.model

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import breezyweather.domain.weather.model.Hourly
import org.breezyweather.R

@ColorInt
fun Hourly.getCloudCoverColor(context: Context): Int {
    if (cloudCover == null) return Color.TRANSPARENT
    return when (cloudCover!!.toDouble()) {
        in 0.0..CLOUD_COVER_CLEAR -> ContextCompat.getColor(context, R.color.colorLevel_1)
        in CLOUD_COVER_CLEAR..CLOUD_COVER_PARTLY -> ContextCompat.getColor(context, R.color.colorLevel_2)
        in CLOUD_COVER_PARTLY..100.0 -> ContextCompat.getColor(context, R.color.colorLevel_3)
        else -> Color.TRANSPARENT
    }
}

const val CLOUD_COVER_CLEAR = 37.5
const val CLOUD_COVER_PARTLY = 75.0
