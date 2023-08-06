/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
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

import org.breezyweather.common.extensions.toTimezone
import java.io.Serializable
import java.util.*

class Astro(
    val riseDate: Date? = null,
    val setDate: Date? = null
) : Serializable {

    val isValid: Boolean
        get() = riseDate != null && setDate != null

    companion object {

        /**
         * (-inf, 0] -> not yet rise.
         * (0,    1) -> has risen, not yet set.
         * [1,  inf) -> has gone down.
         * */
        fun getRiseProgress(
            astro: Astro?,
            timeZone: TimeZone
        ): Double {
            val milliSecondPerHour = 60 * 60 * 1000
            val defaultRiseHour = 6
            val defaultDurationHour = 12

            val timezoneCalendar = Calendar.getInstance(timeZone)
            val currentTime = (timezoneCalendar[Calendar.HOUR_OF_DAY]
                    * 60 + timezoneCalendar[Calendar.MINUTE]) * 60 * 1000

            val riseTime = astro?.riseDate?.toTimezone(timeZone)?.time
            val setTime = astro?.setDate?.toTimezone(timeZone)?.time
            if (riseTime == null || setTime == null) {
                val riseHourMinuteTime = defaultRiseHour * milliSecondPerHour
                val setHourMinuteTime = riseHourMinuteTime + defaultDurationHour * milliSecondPerHour

                if (setHourMinuteTime == riseHourMinuteTime) {
                    return -1.0
                }
                return (currentTime - riseHourMinuteTime).toDouble() / (
                        setHourMinuteTime - riseHourMinuteTime).toDouble()
            }

            val riseCalendar = Calendar.getInstance().apply { time = Date(riseTime) }
            val riseHourMinuteTime = (riseCalendar[Calendar.HOUR_OF_DAY]
                    * 60 + riseCalendar[Calendar.MINUTE]) * 60 * 1000

            var safeSetTime = setTime
            while (safeSetTime <= riseTime) {
                safeSetTime += 24 * milliSecondPerHour
            }
            val setHourMinuteTime = riseHourMinuteTime + (safeSetTime - riseTime)

            return (currentTime - riseHourMinuteTime).toDouble() / (
                    setHourMinuteTime - riseHourMinuteTime).toDouble()
        }
    }
}