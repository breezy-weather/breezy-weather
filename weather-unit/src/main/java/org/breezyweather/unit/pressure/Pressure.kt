package org.breezyweather.unit.pressure

import android.content.Context
import org.breezyweather.unit.WeatherValue
import org.breezyweather.unit.formatting.UnitDecimals.Companion.formatToExactDecimals
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import org.breezyweather.unit.pressure.Pressure.Companion.millibars
import org.breezyweather.unit.pressure.Pressure.Companion.pascals
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
 * Represents the pressure.
 *
 * To construct a pressure, use either the extension function [toPressure],
 * or the extension properties [pascals], [hectopascals], [millibars], and so on,
 * available on [Int], [Long], and [Double] numeric types.
 *
 * To get the value of this pressure expressed in a particular [pressure unit][PressureUnit]
 * use the functions [toInt], [toLong], and [toDouble]
 * or the properties [inPascals], [inHectopascals], [inMillibars], [inAtmospheres], and so on.
 */
@JvmInline
value class Pressure internal constructor(
    private val rawValue: Long,
) : Comparable<Pressure>, WeatherValue<PressureUnit> {
    val value: Long get() = rawValue
    private val storageUnit get() = PressureUnit.PASCAL

    companion object {
        /** Returns a [Pressure] equal to this [Int] number of pascals. */
        inline val Int.pascals: Pressure get() = toPressure(PressureUnit.PASCAL)

        /** Returns a [Pressure] equal to this [Long] number of pascals. */
        inline val Long.pascals: Pressure get() = toPressure(PressureUnit.PASCAL)

        /** Returns a [Pressure] equal to this [Double] number of pascals. */
        inline val Double.pascals: Pressure get() = toPressure(PressureUnit.PASCAL)

        /** Returns a [Pressure] equal to this [Int] number of kilopascals. */
        inline val Int.kilopascals: Pressure get() = toPressure(PressureUnit.KILOPASCAL)

        /** Returns a [Pressure] equal to this [Long] number of kilopascals. */
        inline val Long.kilopascals: Pressure get() = toPressure(PressureUnit.KILOPASCAL)

        /** Returns a [Pressure] equal to this [Double] number of kilopascals. */
        inline val Double.kilopascals: Pressure get() = toPressure(PressureUnit.KILOPASCAL)

        /** Returns a [Pressure] equal to this [Int] number of hectopascals. */
        inline val Int.hectopascals: Pressure get() = toPressure(PressureUnit.HECTOPASCAL)

        /** Returns a [Pressure] equal to this [Long] number of hectopascals. */
        inline val Long.hectopascals: Pressure get() = toPressure(PressureUnit.HECTOPASCAL)

        /** Returns a [Pressure] equal to this [Double] number of hectopascals. */
        inline val Double.hectopascals: Pressure get() = toPressure(PressureUnit.HECTOPASCAL)

        /** Returns a [Pressure] equal to this [Int] number of millibars. */
        inline val Int.millibars: Pressure get() = toPressure(PressureUnit.MILLIBAR)

        /** Returns a [Pressure] equal to this [Long] number of millibars. */
        inline val Long.millibars: Pressure get() = toPressure(PressureUnit.MILLIBAR)

        /** Returns a [Pressure] equal to this [Double] number of millibars. */
        inline val Double.millibars: Pressure get() = toPressure(PressureUnit.MILLIBAR)

        /** Returns a [Pressure] equal to this [Int] number of atmospheres. */
        inline val Int.atmospheres: Pressure get() = toPressure(PressureUnit.ATMOSPHERE)

        /** Returns a [Pressure] equal to this [Long] number of atmospheres. */
        inline val Long.atmospheres: Pressure get() = toPressure(PressureUnit.ATMOSPHERE)

        /** Returns a [Pressure] equal to this [Double] number of atmospheres. */
        inline val Double.atmospheres: Pressure get() = toPressure(PressureUnit.ATMOSPHERE)

        /** Returns a [Pressure] equal to this [Int] number of millimeters of mercury. */
        inline val Int.millimetersOfMercury: Pressure get() = toPressure(PressureUnit.MILLIMETER_OF_MERCURY)

        /** Returns a [Pressure] equal to this [Long] number of millimeters of mercury. */
        inline val Long.millimetersOfMercury: Pressure get() = toPressure(PressureUnit.MILLIMETER_OF_MERCURY)

        /** Returns a [Pressure] equal to this [Double] number of millimeters of mercury. */
        inline val Double.millimetersOfMercury: Pressure get() = toPressure(PressureUnit.MILLIMETER_OF_MERCURY)

        /** Returns a [Pressure] equal to this [Int] number of inches of mercury. */
        inline val Int.inchesOfMercury: Pressure get() = toPressure(PressureUnit.INCH_OF_MERCURY)

        /** Returns a [Pressure] equal to this [Long] number of inches of mercury. */
        inline val Long.inchesOfMercury: Pressure get() = toPressure(PressureUnit.INCH_OF_MERCURY)

        /** Returns a [Pressure] equal to this [Double] number of inches of mercury. */
        inline val Double.inchesOfMercury: Pressure get() = toPressure(PressureUnit.INCH_OF_MERCURY)

        /**
         * Parses a string that represents a pressure and returns the parsed [Pressure] value.
         *
         * The following format is accepted:
         *
         * - The format of string returned by the default [Pressure.toString] and `toString` in a specific unit,
         *   e.g. `1013.25hpa` or `29.95inhg`.
         *
         * @throws IllegalArgumentException if the string doesn't represent a pressure in any of the supported formats.
         */
        fun parse(value: String): Pressure = try {
            parsePressure(value)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid pressure string format: '$value'.", e)
        }

        /**
         * Parses a string that represents a pressure and returns the parsed [Pressure] value,
         * or `null` if the string doesn't represent a pressure in any of the supported formats.
         *
         * The following formats is accepted:
         *
         * - The format of string returned by the default [Pressure.toString] and `toString` in a specific unit,
         *   e.g. `1013.25hpa` or `29.95inhg`.
         */
        fun parseOrNull(value: String): Pressure? = try {
            parsePressure(value)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    override fun compareTo(other: Pressure): Int {
        return this.rawValue.compareTo(other.rawValue)
    }

    // conversion to units

    /**
     * Returns the value of this pressure expressed as a [Double] number of the specified [unit].
     *
     * The operation may involve rounding when the result cannot be represented exactly with a [Double] number.
     */
    override fun toDouble(unit: PressureUnit): Double {
        return convertPressureUnit(value.toDouble(), storageUnit, unit)
    }

    /** The value of this pressure expressed as a [Double] number of pascals. */
    val inPascals: Double
        get() = toDouble(PressureUnit.PASCAL)

    /** The value of this pressure expressed as a [Double] number of hectopascals. */
    val inHectopascals: Double
        get() = toDouble(PressureUnit.HECTOPASCAL)

    /** The value of this pressure expressed as a [Double] number of kilopascals. */
    val inKilopascals: Double
        get() = toDouble(PressureUnit.KILOPASCAL)

    /** The value of this pressure expressed as a [Double] number of millibars. */
    val inMillibars: Double
        get() = toDouble(PressureUnit.MILLIBAR)

    /** The value of this pressure expressed as a [Double] number of atmospheres. */
    val inAtmospheres: Double
        get() = toDouble(PressureUnit.ATMOSPHERE)

    /** The value of this pressure expressed as a [Double] number of millimeters of mercury. */
    val inMillimetersOfMercury: Double
        get() = toDouble(PressureUnit.MILLIMETER_OF_MERCURY)

    /** The value of this pressure expressed as a [Double] number of inches of mercury. */
    val inInchesOfMercury: Double
        get() = toDouble(PressureUnit.INCH_OF_MERCURY)

    /**
     * Returns a string representation of this pressure value
     */
    override fun toString(): String {
        return toString(PressureUnit.PASCAL)
    }

    /**
     * Returns a string representation of this pressure value expressed in the given [unit]
     * and formatted with the specified [decimals] number of digits after decimal point.
     *
     * @param decimals the number of digits after decimal point to show. The value must be non-negative.
     * No more than [unit.decimals.max] decimals will be shown, even if a larger number is requested.
     *
     * @return the value of pressure in the specified [unit] followed by that unit abbreviated name:
     * `pa`, `hpa`, `mb`, `atm`, `mmhg`, `inhg`.
     *
     * @throws IllegalArgumentException if [decimals] is less than zero.
     */
    fun toString(unit: PressureUnit, decimals: Int = unit.decimals.short): String {
        require(decimals >= 0) { "decimals must be not negative, but was $decimals" }
        return formatToExactDecimals(toDouble(unit), decimals.coerceAtMost(unit.decimals.long)) + unit.id
    }

    /**
     * Return null if the value is not between 800 hPa and 1200 hPa, otherwise this value
     */
    fun toValidOrNull(): Pressure? {
        return if (rawValue in 80000..120000) this else null
    }

    override fun format(
        context: Context,
        unit: PressureUnit,
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

/** Returns a [Pressure] equal to this [Int] number of the specified [unit]. */
fun Int.toPressure(unit: PressureUnit): Pressure {
    return toLong().toPressure(unit)
}

/** Returns a [Pressure] equal to this [Long] number of the specified [unit]. */
fun Long.toPressure(unit: PressureUnit): Pressure {
    return pressureOf(convertPressureUnit(this.toDouble(), unit, PressureUnit.PASCAL).toLong())
}

/**
 * Returns a [Pressure] equal to this [Double] number of the specified [unit].
 *
 * Depending on its magnitude, the value is rounded to an integer number of nanoseconds or milliseconds.
 *
 * @throws IllegalArgumentException if this `Double` value is `NaN`.
 */
fun Double.toPressure(unit: PressureUnit): Pressure {
    val valueInPa = convertPressureUnit(this, unit, PressureUnit.PASCAL)
    require(!valueInPa.isNaN()) { "Pressure value cannot be NaN." }
    return pressureOf(valueInPa.roundToLong())
}

private fun parsePressure(value: String): Pressure {
    var length = value.length
    if (length == 0) throw IllegalArgumentException("The string is empty")

    TODO()
}

private fun pressureOf(normalPascals: Long) = Pressure(normalPascals)
