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

package org.breezyweather.domain.location.model

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Normals
import org.breezyweather.R
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.domain.weather.model.getRiseProgress
import java.util.Calendar
import java.util.Date

fun Location.getPlace(context: Context, showCurrentPositionInPriority: Boolean = false): String {
    if (showCurrentPositionInPriority && isCurrentPosition) {
        return context.getString(R.string.location_current)
    }
    if (!customName.isNullOrEmpty()) {
        return customName!!
    }
    if (cityAndDistrict.isNotEmpty()) {
        return cityAndDistrict
    }
    if (cityAndDistrict.isEmpty() && isCurrentPosition) {
        return context.getString(R.string.location_current)
    }
    return ""
}

val Location.isDaylight: Boolean
    get() {
        val sunRiseProgress = getRiseProgress(
            astro = this.weather?.today?.sun,
            location = this
        )
        return 0 < sunRiseProgress && sunRiseProgress < 1
    }

fun Location.toNormalsWrapper(): Normals? {
    return weather?.normals?.let { normals ->
        val cal = Date().toCalendarWithTimeZone(javaTimeZone)
        if (normals.month == cal[Calendar.MONTH]) normals else null
    }
}
