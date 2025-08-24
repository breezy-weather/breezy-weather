package org.breezyweather.unit.precipitation

import android.content.Context
import org.breezyweather.unit.WeatherValue
import org.breezyweather.unit.formatting.UnitDecimals.Companion.formatToExactDecimals
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.precipitation.Precipitation.Companion.centimeters
import org.breezyweather.unit.precipitation.Precipitation.Companion.inches
import org.breezyweather.unit.precipitation.Precipitation.Companion.litersPerSquareMeter
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
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
 * Represents the precipitation.
 *
 * To construct a precipitation, use either the extension function [toPrecipitation],
 * or the extension properties [millimeters], [centimeters], [inches] and [litersPerSquareMeter],
 * available on [Int], [Long], and [Double] numeric types.
 *
 * To get the value of this precipitation expressed in a particular [precipitation unit][PrecipitationUnit]
 * use the functions [toInt], [toLong], and [toDouble]
 * or the properties [inMillimeters], [inCentimeters], [inInches] and [inLitersPerSquareMeter].
 */
@JvmInline
value class Precipitation internal constructor(
    private val rawValue: Long,
) : Comparable<Precipitation>, WeatherValue<PrecipitationUnit> {
    val value: Long get() = rawValue
    private val storageUnit get() = PrecipitationUnit.MICROMETER

    companion object {
        /** Returns a [Precipitation] equal to this [Int] number of micrometers. */
        inline val Int.micrometers: Precipitation get() = toPrecipitation(PrecipitationUnit.MICROMETER)

        /** Returns a [Precipitation] equal to this [Long] number of micrometers. */
        inline val Long.micrometers: Precipitation get() = toPrecipitation(PrecipitationUnit.MICROMETER)

        /** Returns a [Precipitation] equal to this [Double] number of micrometers. */
        inline val Double.micrometers: Precipitation get() = toPrecipitation(PrecipitationUnit.MICROMETER)

        /** Returns a [Precipitation] equal to this [Int] number of millimeters. */
        inline val Int.millimeters: Precipitation get() = toPrecipitation(PrecipitationUnit.MILLIMETER)

        /** Returns a [Precipitation] equal to this [Long] number of millimeters. */
        inline val Long.millimeters: Precipitation get() = toPrecipitation(PrecipitationUnit.MILLIMETER)

        /** Returns a [Precipitation] equal to this [Double] number of millimeters. */
        inline val Double.millimeters: Precipitation get() = toPrecipitation(PrecipitationUnit.MILLIMETER)

        /** Returns a [Precipitation] equal to this [Int] number of millimeters. */
        inline val Int.centimeters: Precipitation get() = toPrecipitation(PrecipitationUnit.CENTIMETER)

        /** Returns a [Precipitation] equal to this [Long] number of centimeters. */
        inline val Long.centimeters: Precipitation get() = toPrecipitation(PrecipitationUnit.CENTIMETER)

        /** Returns a [Precipitation] equal to this [Double] number of centimeters. */
        inline val Double.centimeters: Precipitation get() = toPrecipitation(PrecipitationUnit.CENTIMETER)

        /** Returns a [Precipitation] equal to this [Int] number of inches. */
        inline val Int.inches: Precipitation get() = toPrecipitation(PrecipitationUnit.INCH)

        /** Returns a [Precipitation] equal to this [Long] number of inches. */
        inline val Long.inches: Precipitation get() = toPrecipitation(PrecipitationUnit.INCH)

        /** Returns a [Precipitation] equal to this [Double] number of inches. */
        inline val Double.inches: Precipitation get() = toPrecipitation(PrecipitationUnit.INCH)

        /** Returns a [Precipitation] equal to this [Int] number of liters per square meter. */
        inline val Int.litersPerSquareMeter: Precipitation
            get() = toPrecipitation(PrecipitationUnit.LITER_PER_SQUARE_METER)

        /** Returns a [Precipitation] equal to this [Long] number of liters per square meter. */
        inline val Long.litersPerSquareMeter: Precipitation
            get() = toPrecipitation(PrecipitationUnit.LITER_PER_SQUARE_METER)

        /** Returns a [Precipitation] equal to this [Double] number of liters per square meter. */
        inline val Double.litersPerSquareMeter: Precipitation
            get() = toPrecipitation(PrecipitationUnit.LITER_PER_SQUARE_METER)

        /**
         * Parses a string that represents a precipitation and returns the parsed [Precipitation] value.
         *
         * The following format is accepted:
         *
         * - The format of string returned by the default [Precipitation.toString] and `toString` in a specific unit,
         *   e.g. `5mm` or `1cm`.
         *
         * @throws IllegalArgumentException if the string doesn't represent a precipitation in any of the supported formats.
         */
        fun parse(value: String): Precipitation = try {
            parsePrecipitation(value)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid precipitation string format: '$value'.", e)
        }

        /**
         * Parses a string that represents a precipitation and returns the parsed [Precipitation] value,
         * or `null` if the string doesn't represent a precipitation in any of the supported formats.
         *
         * The following formats is accepted:
         *
         * - The format of string returned by the default [Precipitation.toString] and `toString` in a specific unit,
         *   e.g. `5mm` or `1cm`.
         */
        fun parseOrNull(value: String): Precipitation? = try {
            parsePrecipitation(value)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    override fun compareTo(other: Precipitation): Int {
        return this.rawValue.compareTo(other.rawValue)
    }

    // conversion to units

    /**
     * Returns the value of this precipitation expressed as a [Double] number of the specified [unit].
     *
     * The operation may involve rounding when the result cannot be represented exactly with a [Double] number.
     */
    override fun toDouble(unit: PrecipitationUnit): Double {
        return convertPrecipitationUnit(value.toDouble(), storageUnit, unit)
    }

    /** The value of this precipitation expressed as a [Double] number of micrometers. */
    val inMicrometers: Double
        get() = toDouble(PrecipitationUnit.MICROMETER)

    /** The value of this precipitation expressed as a [Double] number of millimeters. */
    val inMillimeters: Double
        get() = toDouble(PrecipitationUnit.MILLIMETER)

    /** The value of this precipitation expressed as a [Double] number of centimeters. */
    val inCentimeters: Double
        get() = toDouble(PrecipitationUnit.CENTIMETER)

    /** The value of this precipitation expressed as a [Double] number of inches. */
    val inInches: Double
        get() = toDouble(PrecipitationUnit.INCH)

    /** The value of this precipitation expressed as a [Double] number of liters per square meter. */
    val inLitersPerSquareMeter: Double
        get() = toDouble(PrecipitationUnit.LITER_PER_SQUARE_METER)

    /**
     * Returns a string representation of this precipitation value
     */
    override fun toString(): String {
        return toString(storageUnit)
    }

    /**
     * Returns a string representation of this precipitation value expressed in the given [unit]
     * and formatted with the specified [decimals] number of digits after decimal point.
     *
     * @param decimals the number of digits after decimal point to show. The value must be non-negative.
     * No more than [unit.decimals.max] decimals will be shown, even if a larger number is requested.
     *
     * @return the value of precipitation in the specified [unit] followed by that unit abbreviated name:
     * `microm`, `mm`, `cm`, `in`, `lpsqm`.
     *
     * @throws IllegalArgumentException if [decimals] is less than zero.
     */
    fun toString(unit: PrecipitationUnit, decimals: Int = unit.decimals.short): String {
        require(decimals >= 0) { "decimals must be not negative, but was $decimals" }
        return formatToExactDecimals(toDouble(unit), decimals.coerceAtMost(unit.decimals.long)) + unit.id
    }

    /**
     * Return null if the value is not between 0 mm and 1,000,000 μm (1,000 mm), otherwise this value
     */
    fun toValidHourlyOrNull(): Precipitation? {
        return if (rawValue in 0..1000000) this else null
    }

    /**
     * Return null if the value is not between 0 mm and 10,000,000 μm (10,000 mm), otherwise this value
     */
    fun toValidHalfDayOrNull(): Precipitation? {
        return if (rawValue in 0..10000000) this else null
    }

    /**
     * Special case of formatting:
     * mm/h instead of just mm
     */
    fun formatIntensity(
        context: Context,
        unit: PrecipitationUnit,
        valueWidth: UnitWidth = UnitWidth.SHORT,
        unitWidth: UnitWidth = UnitWidth.SHORT,
        locale: Locale = Locale.getDefault(),
        useNumberFormatter: Boolean = true,
        useMeasureFormat: Boolean = true,
    ): String {
        return unit.format(
            context = context,
            value = toDouble(unit),
            valueWidth = valueWidth,
            unitWidth = unitWidth,
            locale = locale,
            useNumberFormatter = useNumberFormatter,
            useMeasureFormat = useMeasureFormat
        )
    }

    fun formatIntensityWithAndroidTranslations(
        context: Context,
        unit: PrecipitationUnit,
        valueWidth: UnitWidth = UnitWidth.SHORT,
        unitWidth: UnitWidth = UnitWidth.SHORT,
        locale: Locale = Locale.getDefault(),
        useNumberFormatter: Boolean = true,
        useMeasureFormat: Boolean = true,
    ): String {
        return unit.formatWithAndroidTranslations(
            context = context,
            value = value,
            valueWidth = valueWidth,
            unitWidth = unitWidth,
            locale = locale,
            useNumberFormatter = useNumberFormatter,
            useMeasureFormat = useMeasureFormat
        )
    }
}

// constructing from number of units
// extension functions

/** Returns a [Precipitation] equal to this [Int] number of the specified [unit]. */
fun Int.toPrecipitation(unit: PrecipitationUnit): Precipitation {
    return toLong().toPrecipitation(unit)
}

/** Returns a [Precipitation] equal to this [Long] number of the specified [unit]. */
fun Long.toPrecipitation(unit: PrecipitationUnit): Precipitation {
    return precipitationOf(convertPrecipitationUnit(this.toDouble(), unit, PrecipitationUnit.MICROMETER).toLong())
}

/**
 * Returns a [Precipitation] equal to this [Double] number of the specified [unit].
 *
 * @throws IllegalArgumentException if this `Double` value is `NaN`.
 */
fun Double.toPrecipitation(unit: PrecipitationUnit): Precipitation {
    val valueInMicrom = convertPrecipitationUnit(this, unit, PrecipitationUnit.MICROMETER)
    require(!valueInMicrom.isNaN()) { "Precipitation value cannot be NaN." }
    return precipitationOf(valueInMicrom.roundToLong())
}

private fun parsePrecipitation(value: String): Precipitation {
    var length = value.length
    if (length == 0) throw IllegalArgumentException("The string is empty")

    TODO()
}

private fun precipitationOf(normalMeters: Long) = Precipitation(normalMeters)
