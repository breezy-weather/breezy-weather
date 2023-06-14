package org.breezyweather.common.basic.models.weather

import android.content.Context
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
        @JvmStatic
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

            val riseTime = org.breezyweather.common.utils.DisplayUtils.toTimezone(astro?.riseDate, timeZone)?.time
            val setTime = org.breezyweather.common.utils.DisplayUtils.toTimezone(astro?.setDate, timeZone)?.time
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

    // rise time.
    fun getRiseTime(context: Context, timeZone: TimeZone): String? {
        return getRiseTime(org.breezyweather.common.utils.DisplayUtils.is12Hour(context), timeZone)
    }

    private fun getRiseTime(twelveHour: Boolean, timeZone: TimeZone): String? {
        if (riseDate == null) {
            return null
        }
        return org.breezyweather.common.utils.DisplayUtils.getFormattedDate(riseDate, timeZone, if (twelveHour) "h:mm aa" else "HH:mm")
    }

    // set time.
    fun getSetTime(context: Context, timeZone: TimeZone): String? {
        return getSetTime(org.breezyweather.common.utils.DisplayUtils.is12Hour(context), timeZone)
    }

    private fun getSetTime(twelveHour: Boolean, timeZone: TimeZone): String? {
        if (setDate == null) {
            return null
        }
        return org.breezyweather.common.utils.DisplayUtils.getFormattedDate(setDate, timeZone, if (twelveHour) "h:mm aa" else "HH:mm")
    }
}