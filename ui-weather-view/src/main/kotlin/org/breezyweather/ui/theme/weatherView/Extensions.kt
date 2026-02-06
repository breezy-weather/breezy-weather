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

package org.breezyweather.ui.theme.weatherView

import android.content.Context
import android.content.res.Configuration
import android.hardware.SensorManager
import androidx.core.content.getSystemService

/**
 * Duplicate of existing extensions, so that the lib can be compiled separately
 * TODO: Move into a dedicated module to avoid duplicate
 */
val Context.isLandscape: Boolean
    get() = this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

fun Context.dpToPx(dp: Float): Float {
    return dp * (this.resources.displayMetrics.densityDpi / 160f)
}

val Context.sensorManager: SensorManager?
    get() = getSystemService()
