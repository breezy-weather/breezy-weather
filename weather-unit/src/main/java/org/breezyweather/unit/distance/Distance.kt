package org.breezyweather.unit.distance

import android.content.Context
import org.breezyweather.unit.WeatherValue
import org.breezyweather.unit.distance.Distance.Companion.kilometers
import org.breezyweather.unit.distance.Distance.Companion.meters
import org.breezyweather.unit.distance.Distance.Companion.miles
import org.breezyweather.unit.formatting.UnitDecimals.Companion.formatToExactDecimals
import org.breezyweather.unit.formatting.UnitWidth
import java.util.Locale
import kotlin.math.roundToLong

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

/**
 * Represents the distance.
 *
 * To construct a distance, use either the extension function [toDistance],
 * or the extension properties [meters], [kilometers], [miles], and so on,
 * available on [Int], [Long], and [Double] numeric types.
 *
 * To get the value of this distance expressed in a particular [distance unit][DistanceUnit]
 * use the functions [toInt], [toLong], and [toDouble]
 * or the properties [inMeters], [inKilometers], [inMiles], [inNauticalMiles], and so on.
 */
@JvmInline
value class Distance internal constructor(
    private val rawValue: Long,
) : Comparable<Distance>, WeatherValue<DistanceUnit> {
    val value: Long get() = rawValue
    private val storageUnit get() = DistanceUnit.METER

    companion object {
        /** Returns a [Distance] equal to this [Int] number of meters. */
        inline val Int.meters: Distance get() = toDistance(DistanceUnit.METER)

        /** Returns a [Distance] equal to this [Long] number of meters. */
        inline val Long.meters: Distance get() = toDistance(DistanceUnit.METER)

        /** Returns a [Distance] equal to this [Double] number of meters. */
        inline val Double.meters: Distance get() = toDistance(DistanceUnit.METER)

        /** Returns a [Distance] equal to this [Int] number of kilometers. */
        inline val Int.kilometers: Distance get() = toDistance(DistanceUnit.KILOMETER)

        /** Returns a [Distance] equal to this [Long] number of kilometers. */
        inline val Long.kilometers: Distance get() = toDistance(DistanceUnit.KILOMETER)

        /** Returns a [Distance] equal to this [Double] number of kilometers. */
        inline val Double.kilometers: Distance get() = toDistance(DistanceUnit.KILOMETER)

        /** Returns a [Distance] equal to this [Int] number of miles. */
        inline val Int.miles: Distance get() = toDistance(DistanceUnit.MILE)

        /** Returns a [Distance] equal to this [Long] number of miles. */
        inline val Long.miles: Distance get() = toDistance(DistanceUnit.MILE)

        /** Returns a [Distance] equal to this [Double] number of miles. */
        inline val Double.miles: Distance get() = toDistance(DistanceUnit.MILE)

        /** Returns a [Distance] equal to this [Int] number of nautical miles. */
        inline val Int.nauticalMiles: Distance get() = toDistance(DistanceUnit.NAUTICAL_MILE)

        /** Returns a [Distance] equal to this [Long] number of nautical miles. */
        inline val Long.nauticalMiles: Distance get() = toDistance(DistanceUnit.NAUTICAL_MILE)

        /** Returns a [Distance] equal to this [Double] number of nautical miles. */
        inline val Double.nauticalMiles: Distance get() = toDistance(DistanceUnit.NAUTICAL_MILE)

        /** Returns a [Distance] equal to this [Int] number of feet. */
        inline val Int.feet: Distance get() = toDistance(DistanceUnit.FOOT)

        /** Returns a [Distance] equal to this [Long] number of feet. */
        inline val Long.feet: Distance get() = toDistance(DistanceUnit.FOOT)

        /** Returns a [Distance] equal to this [Double] number of feet. */
        inline val Double.feet: Distance get() = toDistance(DistanceUnit.FOOT)

        /**
         * Parses a string that represents a distance and returns the parsed [Distance] value.
         *
         * The following format is accepted:
         *
         * - The format of string returned by the default [Distance.toString] and `toString` in a specific unit,
         *   e.g. `50000m` or `30.5km`.
         *
         * @throws IllegalArgumentException if the string doesn't represent a distance in any of the supported formats.
         */
        fun parse(value: String): Distance = try {
            parseDistance(value)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid distance string format: '$value'.", e)
        }

        /**
         * Parses a string that represents a distance and returns the parsed [Distance] value,
         * or `null` if the string doesn't represent a distance in any of the supported formats.
         *
         * The following formats is accepted:
         *
         * - The format of string returned by the default [Distance.toString] and `toString` in a specific unit,
         *   e.g. `50000m` or `30.5km`.
         */
        fun parseOrNull(value: String): Distance? = try {
            parseDistance(value)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    override fun compareTo(other: Distance): Int {
        return this.rawValue.compareTo(other.rawValue)
    }

    // conversion to units

    /**
     * Returns the value of this distance expressed as a [Double] number of the specified [unit].
     *
     * The operation may involve rounding when the result cannot be represented exactly with a [Double] number.
     */
    override fun toDouble(unit: DistanceUnit): Double {
        return convertDistanceUnit(value.toDouble(), storageUnit, unit)
    }

    /** The value of this distance expressed as a [Double] number of meters. */
    val inMeters: Double
        get() = toDouble(DistanceUnit.METER)

    /** The value of this distance expressed as a [Double] number of kilometers. */
    val inKilometers: Double
        get() = toDouble(DistanceUnit.KILOMETER)

    /** The value of this distance expressed as a [Double] number of miles. */
    val inMiles: Double
        get() = toDouble(DistanceUnit.MILE)

    /** The value of this distance expressed as a [Double] number of nautical miles. */
    val inNauticalMiles: Double
        get() = toDouble(DistanceUnit.NAUTICAL_MILE)

    /** The value of this distance expressed as a [Double] number of feet. */
    val inFeet: Double
        get() = toDouble(DistanceUnit.FOOT)

    /**
     * Returns a string representation of this distance value
     */
    override fun toString(): String {
        return toString(storageUnit)
    }

    /**
     * Returns a string representation of this distance value expressed in the given [unit]
     * and formatted with the specified [decimals] number of digits after decimal point.
     *
     * @param decimals the number of digits after decimal point to show. The value must be non-negative.
     * No more than [unit.decimals.max] decimals will be shown, even if a larger number is requested.
     *
     * @return the value of distance in the specified [unit] followed by that unit abbreviated name:
     * `pa`, `hpa`, `mb`, `atm`, `mmhg`, `inhg`.
     *
     * @throws IllegalArgumentException if [decimals] is less than zero.
     */
    fun toString(unit: DistanceUnit, decimals: Int = unit.decimals.short): String {
        require(decimals >= 0) { "decimals must be not negative, but was $decimals" }
        return formatToExactDecimals(toDouble(unit), decimals.coerceAtMost(unit.decimals.long)) + unit.id
    }

    /**
     * Return null if the value is not a positive value, otherwise this value
     * Used by visibility and ceiling
     */
    fun toValidOrNull(): Distance? {
        return if (rawValue >= 0) this else null
    }

    override fun format(
        context: Context,
        unit: DistanceUnit,
        valueWidth: UnitWidth,
        unitWidth: UnitWidth,
        locale: Locale,
        useNumberFormatter: Boolean,
        useMeasureFormat: Boolean,
    ): String {
        return super.format(
            context = context,
            unit = unit,
            valueWidth = valueWidth,
            unitWidth = unitWidth,
            locale = locale.let {
                /**
                 * Use English units with Traditional Chinese
                 *
                 * Taiwan guidelines: https://www.bsmi.gov.tw/wSite/public/Attachment/f1736149048776.pdf
                 * Ongoing issue: https://unicode-org.atlassian.net/jira/software/c/projects/CLDR/issues/CLDR-10604
                 */
                if (it.language.equals("zh", ignoreCase = true) &&
                    arrayOf("TW", "HK", "MO").any { c -> it.country.equals(c, ignoreCase = true) } &&
                    unitWidth != UnitWidth.LONG
                ) {
                    Locale.Builder().setLanguage("en").setRegion("001").build()
                } else {
                    it
                }
            },
            useNumberFormatter = useNumberFormatter,
            useMeasureFormat = useMeasureFormat
        )
    }
}

// constructing from number of units
// extension functions

/** Returns a [Distance] equal to this [Int] number of the specified [unit]. */
fun Int.toDistance(unit: DistanceUnit): Distance {
    return toLong().toDistance(unit)
}

/** Returns a [Distance] equal to this [Long] number of the specified [unit]. */
fun Long.toDistance(unit: DistanceUnit): Distance {
    return distanceOf(convertDistanceUnit(this.toDouble(), unit, DistanceUnit.METER).toLong())
}

/**
 * Returns a [Distance] equal to this [Double] number of the specified [unit].
 *
 * Depending on its magnitude, the value is rounded to an integer number of nanoseconds or milliseconds.
 *
 * @throws IllegalArgumentException if this `Double` value is `NaN`.
 */
fun Double.toDistance(unit: DistanceUnit): Distance {
    val valueInM = convertDistanceUnit(this, unit, DistanceUnit.METER)
    require(!valueInM.isNaN()) { "Distance value cannot be NaN." }
    return distanceOf(valueInM.roundToLong())
}

private fun parseDistance(value: String): Distance {
    var length = value.length
    if (length == 0) throw IllegalArgumentException("The string is empty")

    TODO()
}

private fun distanceOf(normalMeters: Long) = Distance(normalMeters)
