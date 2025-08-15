package org.breezyweather.unit.pollutant

import android.content.Context
import org.breezyweather.unit.WeatherValue
import org.breezyweather.unit.formatting.UnitDecimals.Companion.formatToExactDecimals
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.pollutant.PollutantConcentration.Companion.microgramsPerCubicMeter
import org.breezyweather.unit.pollutant.PollutantConcentration.Companion.milligramsPerCubicMeter
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
 * Represents the pollutant concentration.
 *
 * To construct a pollutant concentration, use either the extension function [toPollutantConcentration],
 * or the extension properties [microgramsPerCubicMeter] and [milligramsPerCubicMeter],
 * available on [Int], [Long], and [Double] numeric types.
 *
 * To get the value of this pollutant concentration expressed in a particular [pollutant concentration unit][PollutantConcentrationUnit]
 * use the functions [toInt], [toLong], and [toDouble]
 * or the properties [inMicrogramsPerCubicMeter] and [milligramsPerCubicMeter].
 */
@JvmInline
value class PollutantConcentration internal constructor(
    private val rawValue: Long,
) : Comparable<PollutantConcentration>, WeatherValue<PollutantConcentrationUnit> {
    val value: Long get() = rawValue
    private val storageUnit get() = PollutantConcentrationUnit.MICROGRAM_PER_CUBIC_METER

    companion object {
        /** Returns a [PollutantConcentration] equal to this [Int] number of micrograms per cubic meter. */
        inline val Int.microgramsPerCubicMeter: PollutantConcentration
            get() = toPollutantConcentration(PollutantConcentrationUnit.MICROGRAM_PER_CUBIC_METER)

        /** Returns a [PollutantConcentration] equal to this [Long] number of micrograms per cubic meter. */
        inline val Long.microgramsPerCubicMeter: PollutantConcentration
            get() = toPollutantConcentration(PollutantConcentrationUnit.MICROGRAM_PER_CUBIC_METER)

        /** Returns a [PollutantConcentration] equal to this [Double] number of micrograms per cubic meter. */
        inline val Double.microgramsPerCubicMeter: PollutantConcentration
            get() = toPollutantConcentration(PollutantConcentrationUnit.MICROGRAM_PER_CUBIC_METER)

        /** Returns a [PollutantConcentration] equal to this [Int] number of milligrams per cubic meter. */
        inline val Int.milligramsPerCubicMeter: PollutantConcentration
            get() = toPollutantConcentration(PollutantConcentrationUnit.MILLIGRAM_PER_CUBIC_METER)

        /** Returns a [PollutantConcentration] equal to this [Long] number of milligrams per cubic meter. */
        inline val Long.milligramsPerCubicMeter: PollutantConcentration
            get() = toPollutantConcentration(PollutantConcentrationUnit.MILLIGRAM_PER_CUBIC_METER)

        /** Returns a [PollutantConcentration] equal to this [Double] number of milligrams per cubic meter. */
        inline val Double.milligramsPerCubicMeter: PollutantConcentration
            get() = toPollutantConcentration(PollutantConcentrationUnit.MILLIGRAM_PER_CUBIC_METER)

        /**
         * Parses a string that represents a pollutant concentration and returns the parsed [PollutantConcentration] value.
         *
         * The following format is accepted:
         *
         * - The format of string returned by the default [PollutantConcentration.toString] and `toString` in a specific unit,
         *   e.g. `1013.25hpa` or `29.95inhg`.
         *
         * @throws IllegalArgumentException if the string doesn't represent a pollutant concentration in any of the supported formats.
         */
        fun parse(value: String): PollutantConcentration = try {
            parsePollutantConcentration(value)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid pollutant concentration string format: '$value'.", e)
        }

        /**
         * Parses a string that represents a pollutant concentration and returns the parsed [PollutantConcentration] value,
         * or `null` if the string doesn't represent a pollutant concentration in any of the supported formats.
         *
         * The following formats is accepted:
         *
         * - The format of string returned by the default [PollutantConcentration.toString] and `toString` in a specific unit,
         *   e.g. `1013.25hpa` or `29.95inhg`.
         */
        fun parseOrNull(value: String): PollutantConcentration? = try {
            parsePollutantConcentration(value)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    override fun compareTo(other: PollutantConcentration): Int {
        return this.rawValue.compareTo(other.rawValue)
    }

    // conversion to units

    /**
     * Returns the value of this pollutant concentration expressed as a [Double] number of the specified [unit].
     *
     * The operation may involve rounding when the result cannot be represented exactly with a [Double] number.
     */
    override fun toDouble(unit: PollutantConcentrationUnit): Double {
        return convertPollutantConcentrationUnit(value.toDouble(), storageUnit, unit)
    }

    /** The value of this pollutant concentration expressed as a [Double] number of micrograms per cubic meter. */
    val inMicrogramsPerCubicMeter: Double
        get() = toDouble(PollutantConcentrationUnit.MICROGRAM_PER_CUBIC_METER)

    /** The value of this pollutant concentration expressed as a [Double] number of milligrams per cubic meter. */
    val inMilligramsPerCubicMeter: Double
        get() = toDouble(PollutantConcentrationUnit.MILLIGRAM_PER_CUBIC_METER)

    /**
     * Returns a string representation of this pollutantConcentration value
     */
    override fun toString(): String {
        return toString(storageUnit)
    }

    /**
     * Returns a string representation of this pollutant concentration value expressed in the given [unit]
     * and formatted with the specified [decimals] number of digits after decimal point.
     *
     * @param decimals the number of digits after decimal point to show. The value must be non-negative.
     * No more than [unit.decimals.max] decimals will be shown, even if a larger number is requested.
     *
     * @return the value of pollutant concentration in the specified [unit] followed by that unit abbreviated name:
     * `pa`, `hpa`, `mb`, `atm`, `mmhg`, `inhg`.
     *
     * @throws IllegalArgumentException if [decimals] is less than zero.
     */
    fun toString(unit: PollutantConcentrationUnit, decimals: Int = unit.decimals.short): String {
        require(decimals >= 0) { "decimals must be not negative, but was $decimals" }
        return formatToExactDecimals(toDouble(unit), decimals.coerceAtMost(unit.decimals.long)) + unit.id
    }
}

// constructing from number of units
// extension functions

/** Returns a [PollutantConcentration] equal to this [Int] number of the specified [unit]. */
fun Int.toPollutantConcentration(unit: PollutantConcentrationUnit): PollutantConcentration {
    return toLong().toPollutantConcentration(unit)
}

/** Returns a [PollutantConcentration] equal to this [Long] number of the specified [unit]. */
fun Long.toPollutantConcentration(unit: PollutantConcentrationUnit): PollutantConcentration {
    return pollutantConcentrationOf(
        convertPollutantConcentrationUnit(
            this.toDouble(),
            unit,
            PollutantConcentrationUnit.MICROGRAM_PER_CUBIC_METER
        ).toLong()
    )
}

/**
 * Returns a [PollutantConcentration] equal to this [Double] number of the specified [unit].
 *
 * Depending on its magnitude, the value is rounded to an integer number of nanoseconds or milliseconds.
 *
 * @throws IllegalArgumentException if this `Double` value is `NaN`.
 */
fun Double.toPollutantConcentration(unit: PollutantConcentrationUnit): PollutantConcentration {
    val valueInMicrogPCuM =
        convertPollutantConcentrationUnit(this, unit, PollutantConcentrationUnit.MICROGRAM_PER_CUBIC_METER)
    require(!valueInMicrogPCuM.isNaN()) { "Pollutant concentration value cannot be NaN." }
    return pollutantConcentrationOf(valueInMicrogPCuM.roundToLong())
}

private fun parsePollutantConcentration(value: String): PollutantConcentration {
    var length = value.length
    if (length == 0) throw IllegalArgumentException("The string is empty")

    TODO()
}

private fun pollutantConcentrationOf(normalMicrogramsPerCubicMeter: Long) =
    PollutantConcentration(normalMicrogramsPerCubicMeter)
