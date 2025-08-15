package org.breezyweather.unit.temperature

import org.breezyweather.unit.WeatherValue
import org.breezyweather.unit.formatting.UnitDecimals.Companion.formatToExactDecimals
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
 * Represents the temperature.
 *
 * To construct a temperature, use either the extension function [toTemperature],
 * or the extension properties [pascals], [hectopascals], [millibars], and so on,
 * available on [Int], [Long], and [Double] numeric types.
 *
 * To get the value of this temperature expressed in a particular [temperature unit][TemperatureUnit]
 * use the functions [toInt], [toLong], and [toDouble]
 * or the properties [inPascals], [inHectopascals], [inMillibars], [inAtmospheres], and so on.
 */
@JvmInline
value class Temperature internal constructor(
    private val rawValue: Long,
) : Comparable<Temperature>, WeatherValue<TemperatureUnit> {
    val value: Long get() = rawValue
    private val storageUnit get() = TemperatureUnit.DECI_CELSIUS

    companion object {
        /** Returns a [Temperature] equal to this [Int] number of decidegrees Celsius. */
        inline val Int.deciCelsius: Temperature get() = toTemperature(TemperatureUnit.DECI_CELSIUS)

        /** Returns a [Temperature] equal to this [Long] number of decidegrees Celsius. */
        inline val Long.deciCelsius: Temperature get() = toTemperature(TemperatureUnit.DECI_CELSIUS)

        /** Returns a [Temperature] equal to this [Double] number of decidegrees Celsius. */
        inline val Double.deciCelsius: Temperature get() = toTemperature(TemperatureUnit.DECI_CELSIUS)

        /** Returns a [Temperature] equal to this [Int] number of degrees Celsius. */
        inline val Int.celsius: Temperature get() = toTemperature(TemperatureUnit.CELSIUS)

        /** Returns a [Temperature] equal to this [Long] number of degrees Celsius. */
        inline val Long.celsius: Temperature get() = toTemperature(TemperatureUnit.CELSIUS)

        /** Returns a [Temperature] equal to this [Double] number of degrees Celsius. */
        inline val Double.celsius: Temperature get() = toTemperature(TemperatureUnit.CELSIUS)

        /** Returns a [Temperature] equal to this [Int] number of degrees Fahrenheit. */
        inline val Int.fahrenheit: Temperature get() = toTemperature(TemperatureUnit.FAHRENHEIT)

        /** Returns a [Temperature] equal to this [Long] number of degrees Fahrenheit. */
        inline val Long.fahrenheit: Temperature get() = toTemperature(TemperatureUnit.FAHRENHEIT)

        /** Returns a [Temperature] equal to this [Double] number of degrees Fahrenheit. */
        inline val Double.fahrenheit: Temperature get() = toTemperature(TemperatureUnit.FAHRENHEIT)

        /** Returns a [Temperature] equal to this [Int] number of kelvins. */
        inline val Int.kelvin: Temperature get() = toTemperature(TemperatureUnit.KELVIN)

        /** Returns a [Temperature] equal to this [Long] number of kelvins. */
        inline val Long.kelvin: Temperature get() = toTemperature(TemperatureUnit.KELVIN)

        /** Returns a [Temperature] equal to this [Double] number of kelvins. */
        inline val Double.kelvin: Temperature get() = toTemperature(TemperatureUnit.KELVIN)

        /**
         * Parses a string that represents a temperature and returns the parsed [Temperature] value.
         *
         * The following format is accepted:
         *
         * - The format of string returned by the default [Temperature.toString] and `toString` in a specific unit,
         *   e.g. `1013.25hpa` or `29.95inhg`.
         *
         * @throws IllegalArgumentException if the string doesn't represent a temperature in any of the supported formats.
         */
        fun parse(value: String): Temperature = try {
            parseTemperature(value)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid temperature string format: '$value'.", e)
        }

        /**
         * Parses a string that represents a temperature and returns the parsed [Temperature] value,
         * or `null` if the string doesn't represent a temperature in any of the supported formats.
         *
         * The following formats is accepted:
         *
         * - The format of string returned by the default [Temperature.toString] and `toString` in a specific unit,
         *   e.g. `1013.25hpa` or `29.95inhg`.
         */
        fun parseOrNull(value: String): Temperature? = try {
            parseTemperature(value)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    override fun compareTo(other: Temperature): Int {
        return this.rawValue.compareTo(other.rawValue)
    }

    // conversion to units

    /**
     * Returns the value of this temperature expressed as a [Double] number of the specified [unit].
     *
     * The operation may involve rounding when the result cannot be represented exactly with a [Double] number.
     */
    override fun toDouble(unit: TemperatureUnit): Double {
        return convertTemperatureUnit(value.toDouble(), storageUnit, unit)
    }

    /**
     * Returns the value of this temperature deviation expressed as a [Double] number of the specified [unit].
     * Concretely, this means that 1 °C will be converted to 1.8 °F and not as 33.8 °F
     *
     * The operation may involve rounding when the result cannot be represented exactly with a [Double] number.
     */
    fun toDoubleDeviation(unit: TemperatureUnit): Double {
        return convertTemperatureUnitDeviation(value.toDouble(), storageUnit, unit)
    }

    /** The value of this temperature expressed as a [Double] number of decidegrees Celsius. */
    val inDeciCelsius: Double
        get() = toDouble(TemperatureUnit.DECI_CELSIUS)

    /** The value of this temperature expressed as a [Double] number of degrees Celsius. */
    val inCelsius: Double
        get() = toDouble(TemperatureUnit.CELSIUS)

    /** The value of this temperature expressed as a [Double] number of degrees Fahrenheit. */
    val inFahrenheit: Double
        get() = toDouble(TemperatureUnit.FAHRENHEIT)

    /** The value of this temperature expressed as a [Double] number of kelvins. */
    val inKelvins: Double
        get() = toDouble(TemperatureUnit.KELVIN)

    /**
     * Returns a string representation of this temperature value
     */
    override fun toString(): String {
        return toString(storageUnit)
    }

    /**
     * Returns a string representation of this temperature value expressed in the given [unit]
     * and formatted with the specified [decimals] number of digits after decimal point.
     *
     * @param decimals the number of digits after decimal point to show. The value must be non-negative.
     * No more than [unit.decimals.max] decimals will be shown, even if a larger number is requested.
     *
     * @return the value of temperature in the specified [unit] followed by that unit abbreviated name:
     * `pa`, `hpa`, `mb`, `atm`, `mmhg`, `inhg`.
     *
     * @throws IllegalArgumentException if [decimals] is less than zero.
     */
    fun toString(unit: TemperatureUnit, decimals: Int = unit.decimals.short): String {
        require(decimals >= 0) { "decimals must be not negative, but was $decimals" }
        return formatToExactDecimals(toDouble(unit), decimals.coerceAtMost(unit.decimals.long)) + unit.id
    }

    /**
     * Return null if the value is not between -100 °C and 100 °C, otherwise this value
     */
    fun toValidOrNull(): Temperature? {
        return takeIf { rawValue in -1000..1000 }
    }
}

// constructing from number of units
// extension functions

/** Returns a [Temperature] equal to this [Int] number of the specified [unit]. */
fun Int.toTemperature(unit: TemperatureUnit): Temperature {
    return toLong().toTemperature(unit)
}

/** Returns a [Temperature] equal to this [Long] number of the specified [unit]. */
fun Long.toTemperature(unit: TemperatureUnit): Temperature {
    return temperatureOf(convertTemperatureUnit(this.toDouble(), unit, TemperatureUnit.DECI_CELSIUS).toLong())
}

/**
 * Returns a [Temperature] equal to this [Double] number of the specified [unit].
 *
 * Depending on its magnitude, the value is rounded to an integer number of nanoseconds or milliseconds.
 *
 * @throws IllegalArgumentException if this `Double` value is `NaN`.
 */
fun Double.toTemperature(unit: TemperatureUnit): Temperature {
    val valueInDC = convertTemperatureUnit(this, unit, TemperatureUnit.DECI_CELSIUS)
    require(!valueInDC.isNaN()) { "Temperature value cannot be NaN." }
    return temperatureOf(valueInDC.roundToLong())
}

private fun parseTemperature(value: String): Temperature {
    var length = value.length
    if (length == 0) throw IllegalArgumentException("The string is empty")

    TODO()
}

private fun temperatureOf(normalDeciCelsius: Long) = Temperature(normalDeciCelsius)
