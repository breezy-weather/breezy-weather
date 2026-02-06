package org.breezyweather.unit.ratio

import org.breezyweather.unit.WeatherValue
import org.breezyweather.unit.formatting.UnitDecimals.Companion.formatToExactDecimals
import org.breezyweather.unit.ratio.Ratio.Companion.fraction
import org.breezyweather.unit.ratio.Ratio.Companion.percent
import org.breezyweather.unit.ratio.Ratio.Companion.permille
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
 * Represents the ratio.
 *
 * To construct a ratio, use either the extension function [toRatio],
 * or the extension properties [permille], [percent] and [fraction],
 * available on [Int], [Long], and [Double] numeric types.
 *
 * To get the value of this ratio expressed in a particular [ratio unit][RatioUnit]
 * use the functions [toInt], [toLong], and [toDouble]
 * or the properties [inPermille], [inPercent] and [inFraction].
 */
@JvmInline
value class Ratio internal constructor(
    private val rawValue: Long,
) : Comparable<Ratio>, WeatherValue<RatioUnit> {
    val value: Long get() = rawValue
    private val storageUnit get() = RatioUnit.PERMILLE

    companion object {
        /** Returns a [Ratio] equal to this [Int] number of permille. */
        inline val Int.permille: Ratio get() = toRatio(RatioUnit.PERMILLE)

        /** Returns a [Ratio] equal to this [Long] number of permille. */
        inline val Long.permille: Ratio get() = toRatio(RatioUnit.PERMILLE)

        /** Returns a [Ratio] equal to this [Double] number of permille. */
        inline val Double.permille: Ratio get() = toRatio(RatioUnit.PERMILLE)

        /** Returns a [Ratio] equal to this [Int] number of percent. */
        inline val Int.percent: Ratio get() = toRatio(RatioUnit.PERCENT)

        /** Returns a [Ratio] equal to this [Long] number of percent. */
        inline val Long.percent: Ratio get() = toRatio(RatioUnit.PERCENT)

        /** Returns a [Ratio] equal to this [Double] number of percent. */
        inline val Double.percent: Ratio get() = toRatio(RatioUnit.PERCENT)

        /** Returns a [Ratio] equal to this [Int] number of fraction. */
        inline val Int.fraction: Ratio get() = toRatio(RatioUnit.FRACTION)

        /** Returns a [Ratio] equal to this [Long] number of fraction. */
        inline val Long.fraction: Ratio get() = toRatio(RatioUnit.FRACTION)

        /** Returns a [Ratio] equal to this [Double] number of percent. */
        inline val Double.fraction: Ratio get() = toRatio(RatioUnit.FRACTION)

        /**
         * Parses a string that represents a ratio and returns the parsed [Ratio] value.
         *
         * The following format is accepted:
         *
         * - The format of string returned by the default [Ratio.toString] and `toString` in a specific unit,
         *   e.g. `30percent` or `542permille`.
         *
         * @throws IllegalArgumentException if the string doesn't represent a ratio in any of the supported formats.
         */
        fun parse(value: String): Ratio = try {
            parseRatio(value)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid ratio string format: '$value'.", e)
        }

        /**
         * Parses a string that represents a ratio and returns the parsed [Ratio] value,
         * or `null` if the string doesn't represent a ratio in any of the supported formats.
         *
         * The following formats is accepted:
         *
         * - The format of string returned by the default [Ratio.toString] and `toString` in a specific unit,
         *   e.g. `30percent` or `542permille`.
         */
        fun parseOrNull(value: String): Ratio? = try {
            parseRatio(value)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    override fun compareTo(other: Ratio): Int {
        return this.rawValue.compareTo(other.rawValue)
    }

    // conversion to units

    /**
     * Returns the value of this ratio expressed as a [Double] number of the specified [unit].
     *
     * The operation may involve rounding when the result cannot be represented exactly with a [Double] number.
     */
    override fun toDouble(unit: RatioUnit): Double {
        return convertRatioUnit(value.toDouble(), storageUnit, unit)
    }

    /** The value of this ratio expressed as a [Double] number of permille. */
    val inPermille: Double
        get() = toDouble(RatioUnit.PERMILLE)

    /** The value of this ratio expressed as a [Double] number of percent. */
    val inPercent: Double
        get() = toDouble(RatioUnit.PERCENT)

    /** The value of this ratio expressed as a [Double] number of fraction. */
    val inFraction: Double
        get() = toDouble(RatioUnit.FRACTION)

    /**
     * Returns a string representation of this ratio value
     */
    override fun toString(): String {
        return toString(storageUnit)
    }

    /**
     * Returns a string representation of this ratio value expressed in the given [unit]
     * and formatted with the specified [decimals] number of digits after decimal point.
     *
     * @param decimals the number of digits after decimal point to show. The value must be non-negative.
     * No more than [unit.decimals.max] decimals will be shown, even if a larger number is requested.
     *
     * @return the value of ratio in the specified [unit] followed by that unit abbreviated name:
     * `fraction`, `percent`, `permille`.
     *
     * @throws IllegalArgumentException if [decimals] is less than zero.
     */
    fun toString(unit: RatioUnit, decimals: Int = unit.decimals.short): String {
        require(decimals >= 0) { "decimals must be not negative, but was $decimals" }
        return formatToExactDecimals(toDouble(unit), decimals.coerceAtMost(unit.decimals.long)) + unit.id
    }

    /**
     * Return null if the value is not between the provided range, otherwise this value
     */
    fun toValidRangeOrNull(range: IntRange = 0..1000): Ratio? {
        return takeIf { rawValue in range }
    }
}

// constructing from number of units
// extension functions

/** Returns a [Ratio] equal to this [Int] number of the specified [unit]. */
fun Int.toRatio(unit: RatioUnit): Ratio {
    return toLong().toRatio(unit)
}

/** Returns a [Ratio] equal to this [Long] number of the specified [unit]. */
fun Long.toRatio(unit: RatioUnit): Ratio {
    return ratioOf(convertRatioUnit(this.toDouble(), unit, RatioUnit.PERMILLE).toLong())
}

/**
 * Returns a [Ratio] equal to this [Double] number of the specified [unit].
 *
 * @throws IllegalArgumentException if this `Double` value is `NaN`.
 */
fun Double.toRatio(unit: RatioUnit): Ratio {
    val valueInPermille = convertRatioUnit(this, unit, RatioUnit.PERMILLE)
    require(!valueInPermille.isNaN()) { "Ratio value cannot be NaN." }
    return ratioOf(valueInPermille.roundToLong())
}

private fun parseRatio(value: String): Ratio {
    var length = value.length
    if (length == 0) throw IllegalArgumentException("The string is empty")

    TODO()
}

private fun ratioOf(normalPermille: Long) = Ratio(normalPermille)
