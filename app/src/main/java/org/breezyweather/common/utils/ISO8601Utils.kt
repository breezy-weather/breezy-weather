/**
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
package org.breezyweather.common.utils

import java.text.ParseException
import java.text.ParsePosition
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.min

/**
 * Utilities methods for manipulating dates in iso8601 format. This is much faster and GC friendly than using SimpleDateFormat so
 * highly suitable if you (un)serialize lots of date objects.
 *
 * Supported parse format: [yyyy-MM-dd|yyyyMMdd][T(hh:mm[:ss[.sss]]|hhmm[ss[.sss]])]?[Z|[+-]hh[:]mm]]
 *
 * @see [this specification](http://www.w3.org/TR/NOTE-datetime)
 */
// Date parsing code from Jackson databind ISO8601Utils.java
// https://github.com/FasterXML/jackson-databind/blob/2.8/src/main/java/com/fasterxml/jackson/databind/util/ISO8601Utils.java
object ISO8601Utils {
    /**
     * ID to represent the 'UTC' string, default timezone since Jackson 2.7
     *
     * @since 2.7
     */
    private const val UTC_ID = "UTC"

    /**
     * The UTC timezone, prefetched to avoid more lookups.
     *
     * @since 2.7
     */
    private val TIMEZONE_UTC = TimeZone.getTimeZone(UTC_ID)

    /*
    / **********************************************************
    / * Formatting
    / **********************************************************
     */
    /**
     * Format date into yyyy-MM-ddThh:mm:ss[.sss][Z|[+-]hh:mm]
     *
     * @param date the date to format
     * @param millis true to include millis precision otherwise false
     * @param tz timezone to use for the formatting (UTC will produce 'Z')
     * @return the date formatted as yyyy-MM-ddThh:mm:ss[.sss][Z|[+-]hh:mm]
     */
    fun format(date: Date, millis: Boolean = false, tz: TimeZone = TIMEZONE_UTC): String {
        val calendar: Calendar = GregorianCalendar(tz, Locale.US).apply {
            time = date
        }

        // estimate capacity of buffer as close as we can (yeah, that's pedantic ;)
        var capacity = "yyyy-MM-ddThh:mm:ss".length
        capacity += if (millis) ".sss".length else 0
        capacity += if (tz.rawOffset == 0) "Z".length else "+hh:mm".length
        val formatted = StringBuilder(capacity)

        padInt(formatted, calendar[Calendar.YEAR], "yyyy".length)
        formatted.append('-')
        padInt(formatted, calendar[Calendar.MONTH] + 1, "MM".length)
        formatted.append('-')
        padInt(formatted, calendar[Calendar.DAY_OF_MONTH], "dd".length)
        formatted.append('T')
        padInt(formatted, calendar[Calendar.HOUR_OF_DAY], "hh".length)
        formatted.append(':')
        padInt(formatted, calendar[Calendar.MINUTE], "mm".length)
        formatted.append(':')
        padInt(formatted, calendar[Calendar.SECOND], "ss".length)
        if (millis) {
            formatted.append('.')
            padInt(formatted, calendar[Calendar.MILLISECOND], "sss".length)
        }

        val offset = tz.getOffset(calendar.timeInMillis)
        if (offset != 0) {
            val hours = abs(offset / (60 * 1000) / 60)
            val minutes = abs(offset / (60 * 1000) % 60)
            formatted.append(if (offset < 0) '-' else '+')
            padInt(formatted, hours, "hh".length)
            formatted.append(':')
            padInt(formatted, minutes, "mm".length)
        } else {
            formatted.append('Z')
        }

        return formatted.toString()
    }

    /*
    / **********************************************************
    / * Parsing
    / **********************************************************
     */

    /**
     * Parse a date from ISO-8601 formatted string. It expects a format
     * [yyyy-MM-dd|yyyyMMdd][T(hh:mm[:ss[.sss]]|hhmm[ss[.sss]])]?[Z|[+-]hh[:mm]]]
     *
     * @param date ISO string to parse in the appropriate format.
     * @return the parsed date
     * @throws ParseException if the date is not in the appropriate format
     */
    @Throws(ParseException::class)
    fun parse(date: String?): Date {
        val pos = ParsePosition(0)
        var fail: Exception? = null
        try {
            var offset = pos.index

            // extract year
            val year = parseInt(date, offset, 4.let { offset += it; offset })
            if (checkOffset(date, offset, '-')) {
                offset += 1
            }

            // extract month
            val month = parseInt(date, offset, 2.let { offset += it; offset })
            if (checkOffset(date, offset, '-')) {
                offset += 1
            }

            // extract day
            val day = parseInt(date, offset, 2.let { offset += it; offset })
            // default time value
            var hour = 0
            var minutes = 0
            var seconds = 0
            var milliseconds = 0 // always use 0 otherwise returned date will include millis of current time

            // if the value has no time component (and no time zone), we are done
            val hasT = checkOffset(date, offset, 'T')

            if (!hasT && date!!.length <= offset) {
                val calendar: Calendar = GregorianCalendar(year, month - 1, day).apply {
                    isLenient = false
                }
                pos.index = offset
                return calendar.time
            }

            if (hasT) {
                // extract hours, minutes, seconds and milliseconds
                hour = parseInt(date, 1.let { offset += it; offset }, 2.let { offset += it; offset })
                if (checkOffset(date, offset, ':')) {
                    offset += 1
                }

                minutes = parseInt(date, offset, 2.let { offset += it; offset })
                if (checkOffset(date, offset, ':')) {
                    offset += 1
                }
                // second and milliseconds can be optional
                if (date!!.length > offset) {
                    val c = date[offset]
                    if (c != 'Z' && c != '+' && c != '-') {
                        seconds = parseInt(date, offset, 2.let { offset += it; offset })
                        if (seconds in 60..62) seconds = 59 // truncate up to 3 leap seconds
                        // milliseconds can be optional in the format
                        if (checkOffset(date, offset, '.')) {
                            offset += 1
                            val endOffset = indexOfNonDigit(date, offset + 1) // assume at least one digit
                            val parseEndOffset = min(endOffset, offset + 3) // parse up to 3 digits
                            val fraction = parseInt(date, offset, parseEndOffset)
                            milliseconds = when (parseEndOffset - offset) {
                                2 -> fraction * 10
                                1 -> fraction * 100
                                else -> fraction
                            }
                            offset = endOffset
                        }
                    }
                }
            }

            // extract timezone
            require(date!!.length > offset) { "No time zone indicator" }

            var timezone: TimeZone? = null
            val timezoneIndicator = date[offset]

            if (timezoneIndicator == 'Z') {
                timezone = TIMEZONE_UTC
                offset += 1
            } else if (timezoneIndicator == '+' || timezoneIndicator == '-') {
                var timezoneOffset = date.substring(offset)

                // When timezone has no minutes, we should append it, valid timezones are, for example: +00:00, +0000 and +00
                timezoneOffset = if (timezoneOffset.length >= 5) timezoneOffset else timezoneOffset + "00"

                offset += timezoneOffset.length
                // 18-Jun-2015, tatu: Minor simplification, skip offset of "+0000"/"+00:00"
                if ("+0000" == timezoneOffset || "+00:00" == timezoneOffset) {
                    timezone = TIMEZONE_UTC
                } else {
                    // 18-Jun-2015, tatu: Looks like offsets only work from GMT, not UTC…
                    //    not sure why, but that's the way it looks. Further, Javadocs for
                    //    `java.util.TimeZone` specifically instruct use of GMT as base for
                    //    custom timezones… odd.
                    val timezoneId = "GMT$timezoneOffset"
                    // val timezoneId = "UTC$timezoneOffset;

                    timezone = TimeZone.getTimeZone(timezoneId)

                    val act = timezone.id
                    if (act != timezoneId) {
                        /* 22-Jan-2015, tatu: Looks like canonical version has colons, but we may be given
                         *    one without. If so, don't sweat.
                         *   Yes, very inefficient. Hopefully not hit often.
                         *   If it becomes a perf problem, add 'loose' comparison instead.
                         */
                        val cleaned = act.replace(":", "")
                        if (cleaned != timezoneId) {
                            throw IndexOutOfBoundsException(
                                "Mismatching time zone indicator: " + timezoneId + " given, resolves to " + timezone.id
                            )
                        }
                    }
                }
            } else {
                throw IndexOutOfBoundsException("Invalid time zone indicator '$timezoneIndicator'")
            }
            val calendar: Calendar = GregorianCalendar(timezone).apply {
                isLenient = false
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minutes)
                set(Calendar.SECOND, seconds)
                set(Calendar.MILLISECOND, milliseconds)
            }
            pos.index = offset
            return calendar.time
            // If we get a ParseException it'll already have the right message/offset.
            // Other exception types can convert here.
        } catch (e: IndexOutOfBoundsException) {
            fail = e
        } catch (e: IllegalArgumentException) {
            fail = e
        }
        val input = if (date == null) null else '"'.toString() + date + '"'
        var msg = fail!!.message
        if (msg.isNullOrEmpty()) {
            msg = "(" + fail.javaClass.name + ")"
        }
        val ex = ParseException("Failed to parse date [$input]: $msg", pos.index)
        ex.initCause(fail)
        throw ex
    }

    /**
     * Check if the expected character exist at the given offset in the value.
     *
     * @param value the string to check at the specified offset
     * @param offset the offset to look for the expected character
     * @param expected the expected character
     * @return true if the expected character exist at the given offset
     */
    private fun checkOffset(value: String?, offset: Int, expected: Char): Boolean {
        return offset < value!!.length && value[offset] == expected
    }

    /**
     * Parse an integer located between 2 given offsets in a string
     *
     * @param value the string to parse
     * @param beginIndex the start index for the integer in the string
     * @param endIndex the end index for the integer in the string
     * @return the int
     * @throws NumberFormatException if the value is not a number
     */
    @Throws(NumberFormatException::class)
    private fun parseInt(value: String?, beginIndex: Int, endIndex: Int): Int {
        if (beginIndex < 0 || endIndex > value!!.length || beginIndex > endIndex) {
            throw NumberFormatException(value)
        }
        // use same logic as in Integer.parseInt() but less generic we're not supporting negative values
        var i = beginIndex
        var result = 0
        var digit: Int
        if (i < endIndex) {
            digit = value[i++].digitToIntOrNull() ?: -1
            if (digit < 0) {
                throw NumberFormatException(
                    "Invalid number: " + value.substring(
                        beginIndex,
                        endIndex
                    )
                )
            }
            result = -digit
        }
        while (i < endIndex) {
            digit = value[i++].digitToIntOrNull() ?: -1
            if (digit < 0) {
                throw NumberFormatException(
                    "Invalid number: " + value.substring(
                        beginIndex,
                        endIndex
                    )
                )
            }
            result *= 10
            result -= digit
        }
        return -result
    }

    /**
     * Zero pad a number to a specified length
     *
     * @param buffer buffer to use for padding
     * @param value the integer value to pad if necessary.
     * @param length the length of the string we should zero pad
     */
    private fun padInt(buffer: StringBuilder, value: Int, length: Int) {
        val strValue = value.toString()
        for (i in length - strValue.length downTo 1) {
            buffer.append('0')
        }
        buffer.append(strValue)
    }

    /**
     * Returns the index of the first character in the string that is not a digit, starting at offset.
     */
    private fun indexOfNonDigit(string: String?, offset: Int): Int {
        for (i in offset until string!!.length) {
            val c = string[i]
            if (c < '0' || c > '9') return i
        }
        return string.length
    }
}
