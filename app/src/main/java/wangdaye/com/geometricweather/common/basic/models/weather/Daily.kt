package wangdaye.com.geometricweather.common.basic.models.weather

import android.content.Context
import androidx.annotation.Size
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.utils.DisplayUtils
import wangdaye.com.geometricweather.common.utils.helpers.LunarHelper
import java.io.Serializable
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * Daily.
 */
class Daily(
    val date: Date,
    day: HalfDay? = null,
    night: HalfDay? = null,
    sun: Astro? = null,
    moon: Astro? = null,
    val moonPhase: MoonPhase? = null,
    val airQuality: AirQuality? = null,
    val pollen: Pollen? = null,
    val uV: UV? = null,
    val hoursOfSun: Float? = null
) : Serializable {

    @Size(2)
    private val halfDays: Array<HalfDay?>

    @Size(2)
    private val astros: Array<Astro?>

    init {
        halfDays = arrayOf(day, night)
        astros = arrayOf(sun, moon)
    }

    fun day(): HalfDay? {
        return halfDays[0]
    }

    fun night(): HalfDay? {
        return halfDays[1]
    }

    fun setDay(day: HalfDay?) {
        halfDays[0] = day
    }

    fun setNight(night: HalfDay?) {
        halfDays[1] = night
    }

    fun sun(): Astro? {
        return astros[0]
    }

    fun moon(): Astro? {
        return astros[1]
    }

    fun getLongDate(context: Context, timeZone: TimeZone): String {
        return getDate(context.getString(R.string.date_format_long), timeZone)
    }

    fun getShortDate(context: Context, timeZone: TimeZone): String {
        return getDate(context.getString(R.string.date_format_short), timeZone)
    }

    fun getDate(format: String?, timeZone: TimeZone): String {
        return DisplayUtils.getFormattedDate(date, timeZone, format)
    }

    fun getWeek(context: Context, timeZone: TimeZone): String {
        val calendar = Calendar.getInstance(timeZone)
        calendar.time = date
        val day = calendar[Calendar.DAY_OF_WEEK]
        return if (day == 1) {
            context.getString(R.string.week_7)
        } else if (day == 2) {
            context.getString(R.string.week_1)
        } else if (day == 3) {
            context.getString(R.string.week_2)
        } else if (day == 4) {
            context.getString(R.string.week_3)
        } else if (day == 5) {
            context.getString(R.string.week_4)
        } else if (day == 6) {
            context.getString(R.string.week_5)
        } else {
            context.getString(R.string.week_6)
        }
    }

    val lunar: String
        get() = LunarHelper.getLunarDate(date)

    fun isToday(timeZone: TimeZone): Boolean {
        val current = Calendar.getInstance(timeZone)
        val thisDay = Calendar.getInstance(timeZone)
        thisDay.time = date
        return (current[Calendar.YEAR] == thisDay[Calendar.YEAR]
                && current[Calendar.DAY_OF_YEAR] == thisDay[Calendar.DAY_OF_YEAR])
    }
}
