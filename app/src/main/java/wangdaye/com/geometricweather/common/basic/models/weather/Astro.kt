package wangdaye.com.geometricweather.common.basic.models.weather

import android.annotation.SuppressLint
import android.content.Context
import wangdaye.com.geometricweather.common.utils.DisplayUtils
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class Astro(
    val riseDate: Date?,
    val setDate: Date?
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

            val riseTime = astro?.riseDate?.time
            val setTime = astro?.setDate?.time
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
        return getRiseTime(DisplayUtils.is12Hour(context), timeZone)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getRiseTime(twelveHour: Boolean, timeZone: TimeZone): String? {
        if (riseDate == null) {
            return null
        }
        val df = SimpleDateFormat(if (twelveHour) "h:mm aa" else "HH:mm")
        df.timeZone = timeZone
        return df.format(riseDate)
    }

    // set time.

    fun getSetTime(context: Context, timeZone: TimeZone): String? {
        return getSetTime(DisplayUtils.is12Hour(context), timeZone)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getSetTime(twelveHour: Boolean, timeZone: TimeZone): String? {
        if (setDate == null) {
            return null
        }
        val df = SimpleDateFormat(if (twelveHour) "h:mm aa" else "HH:mm")
        df.timeZone = timeZone
        return df.format(setDate)
    }
}