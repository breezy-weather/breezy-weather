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
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Daily
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.extensions.getWeek
import java.util.Calendar

fun Daily.getWeek(location: Location, context: Context?, full: Boolean = false): String {
    return date.getWeek(location, context, full)
}

fun Daily.isToday(location: Location): Boolean {
    val current = Calendar.getInstance(location.javaTimeZone) // TODO: Use ICU
    val thisDay = Calendar.getInstance(location.javaTimeZone) // TODO: Use ICU
    thisDay.time = date
    return current[Calendar.YEAR] == thisDay[Calendar.YEAR] &&
        current[Calendar.DAY_OF_YEAR] == thisDay[Calendar.DAY_OF_YEAR]
}

fun Daily.getTrendTemperature(context: Context, unit: TemperatureUnit): String? {
    if (day?.temperature?.temperature == null || night?.temperature?.temperature == null) {
        return null
    }
    return unit.formatMeasureShort(context, day!!.temperature!!.temperature!!) + "/" +
        unit.formatMeasureShort(context, night!!.temperature!!.temperature!!)
}
