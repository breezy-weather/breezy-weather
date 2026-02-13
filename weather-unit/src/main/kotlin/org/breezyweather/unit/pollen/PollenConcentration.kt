package org.breezyweather.unit.pollen

import org.breezyweather.unit.WeatherValue
import org.breezyweather.unit.formatting.UnitDecimals.Companion.formatToExactDecimals
import org.breezyweather.unit.pollen.PollenConcentration.Companion.perCubicMeter
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
 * Represents the pollen concentration.
 *
 * To construct a pollen concentration, use either the extension function [toPollenConcentration],
 * or the extension property [perCubicMeter], available on [Int], [Long], and [Double] numeric types.
 *
 * To get the value of this pollen concentration expressed in a particular [pollen concentration unit][PollenConcentrationUnit]
 * use the functions [toInt], [toLong], and [toDouble]
 * or the property [inPerCubicMeter].
 */
@JvmInline
value class PollenConcentration internal constructor(
    private val rawValue: Long,
) : Comparable<PollenConcentration>, WeatherValue<PollenConcentrationUnit> {
    val value: Long get() = rawValue
    private val storageUnit get() = PollenConcentrationUnit.PER_CUBIC_METER

    companion object {
        /** Returns a [PollenConcentration] equal to this [Int] number of pollens per cubic meter. */
        inline val Int.perCubicMeter: PollenConcentration
            get() = toPollenConcentration(PollenConcentrationUnit.PER_CUBIC_METER)

        /** Returns a [PollenConcentration] equal to this [Long] number of pollens per cubic meter. */
        inline val Long.perCubicMeter: PollenConcentration
            get() = toPollenConcentration(PollenConcentrationUnit.PER_CUBIC_METER)

        /** Returns a [PollenConcentration] equal to this [Double] number of pollens per cubic meter. */
        inline val Double.perCubicMeter: PollenConcentration
            get() = toPollenConcentration(PollenConcentrationUnit.PER_CUBIC_METER)

        /** Store a pollen index level as a [PollenConcentration]. */
        inline val Int.pollenIndex: PollenConcentration
            get() = toPollenConcentration(PollenConcentrationUnit.PER_CUBIC_METER)

        /**
         * Parses a string that represents a pollen concentration and returns the parsed [PollenConcentration] value.
         *
         * The following format is accepted:
         *
         * - The format of string returned by the default [PollenConcentration.toString] and `toString` in a specific unit,
         *   e.g. `24pcum`.
         *
         * @throws IllegalArgumentException if the string doesn't represent a pollen concentration in any of the supported formats.
         */
        fun parse(value: String): PollenConcentration = try {
            parsePollenConcentration(value)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid pollen concentration string format: '$value'.", e)
        }

        /**
         * Parses a string that represents a pollen concentration and returns the parsed [PollenConcentration] value,
         * or `null` if the string doesn't represent a pollen concentration in any of the supported formats.
         *
         * The following formats is accepted:
         *
         * - The format of string returned by the default [PollenConcentration.toString] and `toString` in a specific unit,
         *   e.g. `24pcum`.
         */
        fun parseOrNull(value: String): PollenConcentration? = try {
            parsePollenConcentration(value)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    override fun compareTo(other: PollenConcentration): Int {
        return this.rawValue.compareTo(other.rawValue)
    }

    // conversion to units

    /**
     * Returns the value of this pollen concentration expressed as a [Double] number of the specified [unit].
     *
     * The operation may involve rounding when the result cannot be represented exactly with a [Double] number.
     */
    override fun toDouble(unit: PollenConcentrationUnit): Double {
        return value.toDouble()
    }

    /** The value of this pollen concentration expressed as a [Double] number of pollens per cubic meter. */
    val inPerCubicMeter: Double
        get() = toDouble(PollenConcentrationUnit.PER_CUBIC_METER)

    /** The value of the pollen index expressed as a [Int] pollen index. */
    val inPollenIndex: Int
        get() = value.toInt()

    /**
     * Returns a string representation of this pollenConcentration value
     */
    override fun toString(): String {
        return toString(storageUnit)
    }

    /**
     * Returns a string representation of this pollen concentration value expressed in the given [unit]
     * and formatted with the specified [decimals] number of digits after decimal point.
     *
     * @param decimals the number of digits after decimal point to show. The value must be non-negative.
     * No more than [unit.decimals.max] decimals will be shown, even if a larger number is requested.
     *
     * @return the value of pollen concentration in the specified [unit] followed by that unit abbreviated name:
     * `pcum`.
     *
     * @throws IllegalArgumentException if [decimals] is less than zero.
     */
    fun toString(unit: PollenConcentrationUnit, decimals: Int = unit.decimals.short): String {
        require(decimals >= 0) { "decimals must be not negative, but was $decimals" }
        return formatToExactDecimals(toDouble(unit), decimals.coerceAtMost(unit.decimals.long)) + unit.id
    }
}

// constructing from number of units
// extension functions

/** Returns a [PollenConcentration] equal to this [Int] number of the specified [unit]. */
fun Int.toPollenConcentration(unit: PollenConcentrationUnit): PollenConcentration {
    return toLong().toPollenConcentration(unit)
}

/** Returns a [PollenConcentration] equal to this [Long] number of the specified [unit]. */
fun Long.toPollenConcentration(unit: PollenConcentrationUnit): PollenConcentration {
    return pollenConcentrationOf(this)
}

/**
 * Returns a [PollenConcentration] equal to this [Double] number of the specified [unit].
 *
 * @throws IllegalArgumentException if this `Double` value is `NaN`.
 */
fun Double.toPollenConcentration(unit: PollenConcentrationUnit): PollenConcentration {
    val valueInPCuM = this
    require(!valueInPCuM.isNaN()) { "Pollen concentration value cannot be NaN." }
    return pollenConcentrationOf(valueInPCuM.roundToLong())
}

private fun parsePollenConcentration(value: String): PollenConcentration {
    var length = value.length
    if (length == 0) throw IllegalArgumentException("The string is empty")

    TODO()
}

private fun pollenConcentrationOf(normalPerCubicMeter: Long) = PollenConcentration(normalPerCubicMeter)
