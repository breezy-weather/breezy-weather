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

import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Astro
import org.breezyweather.common.extensions.toTimezone
import java.util.Calendar
import java.util.Date
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * (-inf, 0] -> not yet rise.
 * (0,    1) -> has risen, not yet set.
 * [1,  inf) -> has gone down.
 * TODO: Works but the way timezones are handled is wrong
 * */
fun getRiseProgress(
    astro: Astro?,
    location: Location,
): Double {
    val defaultRiseHour = 6
    val defaultDurationHour = 12

    val timezoneCalendar = Calendar.getInstance(location.javaTimeZone)
    val currentTime = (timezoneCalendar[Calendar.HOUR_OF_DAY].hours + timezoneCalendar[Calendar.MINUTE].minutes)
        .inWholeMilliseconds

    val riseTime = astro?.riseDate?.toTimezone(location.javaTimeZone)?.time
    val setTime = astro?.setDate?.toTimezone(location.javaTimeZone)?.time
    if (riseTime == null || setTime == null) {
        val riseHourMinuteTime = defaultRiseHour.hours.inWholeMilliseconds
        val setHourMinuteTime = riseHourMinuteTime + defaultDurationHour.hours.inWholeMilliseconds

        if (setHourMinuteTime == riseHourMinuteTime) {
            return -1.0
        }
        return (currentTime - riseHourMinuteTime).toDouble() / (setHourMinuteTime - riseHourMinuteTime).toDouble()
    }

    val riseCalendar = Calendar.getInstance().apply { time = Date(riseTime) }
    val riseHourMinuteTime = (riseCalendar[Calendar.HOUR_OF_DAY].hours + riseCalendar[Calendar.MINUTE].minutes)
        .inWholeMilliseconds

    var safeSetTime = setTime
    while (safeSetTime <= riseTime) {
        safeSetTime += 1.days.inWholeMilliseconds
    }
    val setHourMinuteTime = riseHourMinuteTime + (safeSetTime - riseTime)

    return (currentTime - riseHourMinuteTime).toDouble() / (setHourMinuteTime - riseHourMinuteTime).toDouble()
}
