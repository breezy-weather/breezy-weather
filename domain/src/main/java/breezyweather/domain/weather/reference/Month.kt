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

package breezyweather.domain.weather.reference

import java.text.DateFormatSymbols
import java.util.Locale

/**
 * Lightweight reimplementation of java.time.Month only available in SDK >= 26
 *
 * A month-of-year, such as 'July'.
 * <p>
 * {@code Month} is an enum representing the 12 months of the year -
 * January, February, March, April, May, June, July, August, September, October,
 * November and December.
 * <p>
 * In addition to the textual enum name, each month-of-year has an {@code int} value.
 * The {@code int} value follows normal usage and the ISO-8601 standard,
 * from 1 (January) to 12 (December). It is recommended that applications use the enum
 * rather than the {@code int} value to ensure code clarity.
 * <p>
 * <b>Do not use {@code ordinal()} to obtain the numeric representation of {@code Month}.
 * Use {@code getValue()} instead.</b>
 * <p>
 * This enum represents a common concept that is found in many calendar systems.
 * As such, this enum may be used by any calendar system that has the month-of-year
 * concept defined exactly equivalent to the ISO-8601 calendar system.
 */
enum class Month {
    /**
     * The singleton instance for the month of January.
     * This has the numeric value of {@code 1}.
     */
    JANUARY,

    /**
     * The singleton instance for the month of February.
     * This has the numeric value of {@code 2}.
     */
    FEBRUARY,

    /**
     * The singleton instance for the month of March.
     * This has the numeric value of {@code 3}.
     */
    MARCH,

    /**
     * The singleton instance for the month of April.
     * This has the numeric value of {@code 4}.
     */
    APRIL,

    /**
     * The singleton instance for the month of May.
     * This has the numeric value of {@code 5}.
     */
    MAY,

    /**
     * The singleton instance for the month of June.
     * This has the numeric value of {@code 6}.
     */
    JUNE,

    /**
     * The singleton instance for the month of July.
     * This has the numeric value of {@code 7}.
     */
    JULY,

    /**
     * The singleton instance for the month of August.
     * This has the numeric value of {@code 8}.
     */
    AUGUST,

    /**
     * The singleton instance for the month of September.
     * This has the numeric value of {@code 9}.
     */
    SEPTEMBER,

    /**
     * The singleton instance for the month of October.
     * This has the numeric value of {@code 10}.
     */
    OCTOBER,

    /**
     * The singleton instance for the month of November.
     * This has the numeric value of {@code 11}.
     */
    NOVEMBER,

    /**
     * The singleton instance for the month of December.
     * This has the numeric value of {@code 12}.
     */
    DECEMBER,
    ;

    val value: Int = ordinal + 1

    /**
     * Gets the textual representation, such as 'January' or 'December'.
     *
     * This returns the textual name used to identify the month-of-year,
     * suitable for presentation to the user.
     *
     * @param locale the locale to use, not null
     * @return the text value of the month-of-year, not null
     */
    fun getDisplayName(locale: Locale): String {
        return DateFormatSymbols.getInstance(locale).months[ordinal]
    }

    /**
     * Returns the month-of-year that is the specified number of months after this one.
     * <p>
     * The calculation rolls around the end of the year from December to January.
     * The specified period may be negative.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param months the months to add, positive or negative
     * @return the resulting month, not null
     */
    operator fun plus(months: Int): Month {
        val amount = (months % 12)
        return entries[(ordinal + (amount + 12)) % 12]
    }

    /**
     * Returns the month-of-year that is the specified number of months before this one.
     * <p>
     * The calculation rolls around the start of the year from January to December.
     * The specified period may be negative.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param months the months to subtract, positive or negative
     * @return the resulting month, not null
     */
    operator fun minus(months: Int): Month {
        return plus(-(months % 12))
    }

    /**
     * Gets the maximum length of this month in days.
     *
     * February has a maximum length of 29 days.
     * April, June, September and November have 30 days.
     * All other months have 31 days.
     *
     * @return the maximum length of this month in days, from 29 to 31
     */
    fun maxLength(): Int {
        return when (this) {
            FEBRUARY -> 29
            APRIL, JUNE, SEPTEMBER, NOVEMBER -> 30
            else -> 31
        }
    }

    companion object {
        /**
         * Obtains an instance of {@code Month} from an {@code int} value.
         * <p>
         * {@code Month} is an enum representing the 12 months of the year.
         * This factory allows the enum to be obtained from the {@code int} value.
         * The {@code int} value follows the ISO-8601 standard, from 1 (January) to 12 (December).
         *
         * @param month the month-of-year to represent, from 1 (January) to 12 (December)
         * @return the month-of-year, not null
         * @throws Exception if the month-of-year is invalid
         */
        fun of(month: Int): Month {
            if (month !in 1..12) {
                throw Exception("Invalid value for MonthOfYear: $month")
            }
            return entries[month - 1]
        }

        /**
         * Obtains an instance of {@code Month} from a Calendar.MONTH {@code int} value.
         * <p>
         * {@code Month} is an enum representing the 12 months of the year.
         * This factory allows the enum to be obtained from the Calendar.MONTH {@code int} value.
         * The Calendar.MONTH {@code int} value follows the Calendar.MONTH, from 0 (January) to 11 (December).
         *
         * @param month the month-of-year to represent, from 0 (January) to 11 (December)
         * @return the month-of-year, not null
         * @throws Exception if the month-of-year is invalid
         */
        fun fromCalendarMonth(month: Int): Month {
            if (month !in 0..11) {
                throw Exception("Invalid value for MonthOfYear: $month")
            }
            return entries[month - 1]
        }
    }
}
