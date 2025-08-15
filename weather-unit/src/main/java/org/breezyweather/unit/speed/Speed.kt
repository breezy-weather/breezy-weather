package org.breezyweather.unit.speed

import org.breezyweather.unit.WeatherValue
import org.breezyweather.unit.formatting.UnitDecimals.Companion.formatToExactDecimals
import org.breezyweather.unit.speed.Speed.Companion.kilometersPerHour
import org.breezyweather.unit.speed.Speed.Companion.metersPerSecond
import org.breezyweather.unit.speed.Speed.Companion.milesPerHour
import kotlin.math.roundToInt
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
 * Represents the speed.
 *
 * To construct a speed, use either the extension function [toSpeed],
 * or the extension properties [metersPerSecond], [kilometersPerHour], [milesPerHour], and so on,
 * available on [Int], [Long], and [Double] numeric types.
 *
 * To get the value of this speed expressed in a particular [speed unit][SpeedUnit]
 * use the functions [toInt], [toLong], and [toDouble]
 * or the properties [inMetersPerSecond], [inKilometersPerHour], [inMilesPerHour], [inKnots], and so on.
 */
@JvmInline
value class Speed internal constructor(
    private val rawValue: Long,
) : Comparable<Speed>, WeatherValue<SpeedUnit> {
    val value: Long get() = rawValue
    private val storageUnit get() = SpeedUnit.CENTIMETER_PER_SECOND

    companion object {
        /** Returns a [Speed] equal to this [Int] number of centimeters per second. */
        inline val Int.centimetersPerSecond: Speed get() = toSpeed(SpeedUnit.CENTIMETER_PER_SECOND)

        /** Returns a [Speed] equal to this [Long] number of centimeters per second. */
        inline val Long.centimetersPerSecond: Speed get() = toSpeed(SpeedUnit.CENTIMETER_PER_SECOND)

        /** Returns a [Speed] equal to this [Double] number of centimeters per second. */
        inline val Double.centimetersPerSecond: Speed get() = toSpeed(SpeedUnit.CENTIMETER_PER_SECOND)

        /** Returns a [Speed] equal to this [Int] number of meters per second. */
        inline val Int.metersPerSecond: Speed get() = toSpeed(SpeedUnit.METER_PER_SECOND)

        /** Returns a [Speed] equal to this [Long] number of meters per second. */
        inline val Long.metersPerSecond: Speed get() = toSpeed(SpeedUnit.METER_PER_SECOND)

        /** Returns a [Speed] equal to this [Double] number of meters per second. */
        inline val Double.metersPerSecond: Speed get() = toSpeed(SpeedUnit.METER_PER_SECOND)

        /** Returns a [Speed] equal to this [Int] number of kilometers per hour. */
        inline val Int.kilometersPerHour: Speed get() = toSpeed(SpeedUnit.KILOMETER_PER_HOUR)

        /** Returns a [Speed] equal to this [Long] number of kilometers per hour. */
        inline val Long.kilometersPerHour: Speed get() = toSpeed(SpeedUnit.KILOMETER_PER_HOUR)

        /** Returns a [Speed] equal to this [Double] number of kilometers per hour. */
        inline val Double.kilometersPerHour: Speed get() = toSpeed(SpeedUnit.KILOMETER_PER_HOUR)

        /** Returns a [Speed] equal to this [Int] number of miles per hour. */
        inline val Int.milesPerHour: Speed get() = toSpeed(SpeedUnit.MILE_PER_HOUR)

        /** Returns a [Speed] equal to this [Long] number of miles per hour. */
        inline val Long.milesPerHour: Speed get() = toSpeed(SpeedUnit.MILE_PER_HOUR)

        /** Returns a [Speed] equal to this [Double] number of miles per hour. */
        inline val Double.milesPerHour: Speed get() = toSpeed(SpeedUnit.MILE_PER_HOUR)

        /** Returns a [Speed] equal to this [Int] number of knots. */
        inline val Int.knots: Speed get() = toSpeed(SpeedUnit.KNOT)

        /** Returns a [Speed] equal to this [Long] number of knots. */
        inline val Long.knots: Speed get() = toSpeed(SpeedUnit.KNOT)

        /** Returns a [Speed] equal to this [Double] number of knots. */
        inline val Double.knots: Speed get() = toSpeed(SpeedUnit.KNOT)

        /** Returns a [Speed] equal to this [Int] number of feet per second. */
        inline val Int.feetPerSecond: Speed get() = toSpeed(SpeedUnit.FOOT_PER_SECOND)

        /** Returns a [Speed] equal to this [Long] number of feet per second. */
        inline val Long.feetPerSecond: Speed get() = toSpeed(SpeedUnit.FOOT_PER_SECOND)

        /** Returns a [Speed] equal to this [Double] number of feet per second. */
        inline val Double.feetPerSecond: Speed get() = toSpeed(SpeedUnit.FOOT_PER_SECOND)

        /** Returns a [Speed] equal to this [Int] number on the Beaufort scale. */
        inline val Int.beaufort: Speed get() = toSpeed(SpeedUnit.BEAUFORT_SCALE)

        /** Returns a [Speed] equal to this [Long] number on the Beaufort scale. */
        inline val Long.beaufort: Speed get() = toSpeed(SpeedUnit.BEAUFORT_SCALE)

        /** Returns a [Speed] equal to this [Double] number on the Beaufort scale. */
        inline val Double.beaufort: Speed get() = toSpeed(SpeedUnit.BEAUFORT_SCALE)

        /**
         * Parses a string that represents a speed and returns the parsed [Speed] value.
         *
         * The following format is accepted:
         *
         * - The format of string returned by the default [Speed.toString] and `toString` in a specific unit,
         *   e.g. `50000m` or `30.5km`.
         *
         * @throws IllegalArgumentException if the string doesn't represent a speed in any of the supported formats.
         */
        fun parse(value: String): Speed = try {
            parseSpeed(value)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid speed string format: '$value'.", e)
        }

        /**
         * Parses a string that represents a speed and returns the parsed [Speed] value,
         * or `null` if the string doesn't represent a speed in any of the supported formats.
         *
         * The following formats is accepted:
         *
         * - The format of string returned by the default [Speed.toString] and `toString` in a specific unit,
         *   e.g. `50000m` or `30.5km`.
         */
        fun parseOrNull(value: String): Speed? = try {
            parseSpeed(value)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    override fun compareTo(other: Speed): Int {
        return this.rawValue.compareTo(other.rawValue)
    }

    // conversion to units

    /**
     * Returns the value of this speed expressed as a [Double] number of the specified [unit].
     *
     * The operation may involve rounding when the result cannot be represented exactly with a [Double] number.
     */
    override fun toDouble(unit: SpeedUnit): Double {
        return convertSpeedUnit(value.toDouble(), storageUnit, unit)
    }

    /** The value of this speed expressed as a [Double] number of centimeters per second. */
    val inCentimetersPerSecond: Double
        get() = toDouble(SpeedUnit.CENTIMETER_PER_SECOND)

    /** The value of this speed expressed as a [Double] number of meters per second. */
    val inMetersPerSecond: Double
        get() = toDouble(SpeedUnit.METER_PER_SECOND)

    /** The value of this speed expressed as a [Double] number of kilometers per hour. */
    val inKilometersPerHour: Double
        get() = toDouble(SpeedUnit.KILOMETER_PER_HOUR)

    /** The value of this speed expressed as a [Double] number of miles per hour. */
    val inMilesPerHour: Double
        get() = toDouble(SpeedUnit.MILE_PER_HOUR)

    /** The value of this speed expressed as a [Double] number of knots. */
    val inKnots: Double
        get() = toDouble(SpeedUnit.KNOT)

    /** The value of this speed expressed as a [Double] number of feet per second. */
    val inFeetPerSecond: Double
        get() = toDouble(SpeedUnit.FOOT_PER_SECOND)

    /** The value of this speed expressed as a [Int] number on the Beaufort scale. */
    val inBeaufort: Int
        get() = toDouble(SpeedUnit.BEAUFORT_SCALE).roundToInt()

    /**
     * Returns a string representation of this speed value
     */
    override fun toString(): String {
        return toString(storageUnit)
    }

    /**
     * Returns a string representation of this speed value expressed in the given [unit]
     * and formatted with the specified [decimals] number of digits after decimal point.
     *
     * @param decimals the number of digits after decimal point to show. The value must be non-negative.
     * No more than [unit.decimals.max] decimals will be shown, even if a larger number is requested.
     *
     * @return the value of speed in the specified [unit] followed by that unit abbreviated name:
     * `pa`, `hpa`, `mb`, `atm`, `mmhg`, `inhg`.
     *
     * @throws IllegalArgumentException if [decimals] is less than zero.
     */
    fun toString(unit: SpeedUnit, decimals: Int = unit.decimals.short): String {
        require(decimals >= 0) { "decimals must be not negative, but was $decimals" }
        return formatToExactDecimals(toDouble(unit), decimals.coerceAtMost(unit.decimals.long)) + unit.id
    }

    /**
     * Return null if the value is not within 0 and 150 m/s, otherwise this value
     */
    fun toValidOrNull(): Speed? {
        return takeIf { rawValue in 0..15000 }
    }
}

// constructing from number of units
// extension functions

/** Returns a [Speed] equal to this [Int] number of the specified [unit]. */
fun Int.toSpeed(unit: SpeedUnit): Speed {
    return toLong().toSpeed(unit)
}

/** Returns a [Speed] equal to this [Long] number of the specified [unit]. */
fun Long.toSpeed(unit: SpeedUnit): Speed {
    return speedOf(convertSpeedUnit(this.toDouble(), unit, SpeedUnit.CENTIMETER_PER_SECOND).toLong())
}

/**
 * Returns a [Speed] equal to this [Double] number of the specified [unit].
 *
 * Depending on its magnitude, the value is rounded to an integer number of nanoseconds or milliseconds.
 *
 * @throws IllegalArgumentException if this `Double` value is `NaN`.
 */
fun Double.toSpeed(unit: SpeedUnit): Speed {
    val valueInCmps = convertSpeedUnit(this, unit, SpeedUnit.CENTIMETER_PER_SECOND)
    require(!valueInCmps.isNaN()) { "Speed value cannot be NaN." }
    return speedOf(valueInCmps.roundToLong())
}

private fun parseSpeed(value: String): Speed {
    var length = value.length
    if (length == 0) throw IllegalArgumentException("The string is empty")

    TODO()
}

private fun speedOf(normalCentimetersPerSecond: Long) = Speed(normalCentimetersPerSecond)
