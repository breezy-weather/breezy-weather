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

package org.breezyweather.unit.temperature

import android.content.Context
import androidx.core.text.util.LocalePreferences
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import org.breezyweather.unit.temperature.Temperature.Companion.deciCelsius
import org.breezyweather.unit.temperature.Temperature.Companion.fahrenheit
import org.junit.jupiter.api.Test
import java.util.Locale

/**
 * TODO:
 *  - Deviation conversion
 *  - Instrumented tests formatting with Android 16 (currently, SDK is always 0)
 */
class TemperatureTest {

    @Test
    fun `convert from reference unit to Fahrenheit`() = runTest {
        0.deciCelsius.inFahrenheit shouldBe 32.0
        100.deciCelsius.inFahrenheit shouldBe 50.0
    }

    @Test
    fun `convert from Fahrenheit to reference unit`() = runTest {
        32.0.fahrenheit.value shouldBe 0L
        50.0.fahrenheit.value shouldBe 100L
    }

    @Test
    fun `convert from Celsius to Fahrenheit`() = runTest {
        0.celsius.inFahrenheit shouldBe 32.0
        10.celsius.inFahrenheit shouldBe 50.0
    }

    @Test
    fun `convert from Celsius to Kelvin`() = runTest {
        0.celsius.inKelvins shouldBe 273.15
        10.celsius.inKelvins shouldBe 283.15
    }

    @Test
    fun `format Celsius with narrow unit width with Android translations`() = runTest {
        val formattedNumberSlot = slot<String>()
        val context = mockk<Context>().apply {
            every { getString(any()) } returns "FAILED"
            every {
                getString(
                    org.breezyweather.unit.R.string.temperature_c_nominative_narrow,
                    capture(formattedNumberSlot)
                )
            } answers { "${formattedNumberSlot.captured}°" }
            every {
                getString(
                    org.breezyweather.unit.R.string.temperature_k_nominative_narrow,
                    capture(formattedNumberSlot)
                )
            } answers { "${formattedNumberSlot.captured}K" }
        }
        101.4.deciCelsius.format(
            context = context,
            unit = TemperatureUnit.CELSIUS,
            valueWidth = UnitWidth.NARROW,
            unitWidth = UnitWidth.NARROW,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build()
        ) shouldBe "10°"
        101.4.deciCelsius.format(
            context = context,
            unit = TemperatureUnit.CELSIUS,
            valueWidth = UnitWidth.SHORT,
            unitWidth = UnitWidth.NARROW,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build()
        ) shouldBe "10,1°"
        0.deciCelsius.format(
            context = context,
            unit = TemperatureUnit.KELVIN,
            valueWidth = UnitWidth.LONG,
            unitWidth = UnitWidth.NARROW,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build()
        ) shouldBe "273,15K"
    }

    @Test
    fun `format Celsius with short unit width with Android translations`() = runTest {
        val formattedNumberSlot = slot<String>()
        val context = mockk<Context>().apply {
            every { getString(any()) } returns "FAILED"
            every {
                getString(
                    org.breezyweather.unit.R.string.temperature_c_nominative_short,
                    capture(formattedNumberSlot)
                )
            } answers { "${formattedNumberSlot.captured} °C" }
            every {
                getString(
                    org.breezyweather.unit.R.string.temperature_k_nominative_short,
                    capture(formattedNumberSlot)
                )
            } answers { "${formattedNumberSlot.captured} K" }
        }
        101.4.deciCelsius.format(
            context = context,
            unit = TemperatureUnit.CELSIUS,
            valueWidth = UnitWidth.NARROW,
            unitWidth = UnitWidth.SHORT,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build()
        ) shouldBe "10 °C"
        101.4.deciCelsius.format(
            context = context,
            unit = TemperatureUnit.CELSIUS,
            valueWidth = UnitWidth.SHORT,
            unitWidth = UnitWidth.SHORT,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build()
        ) shouldBe "10,1 °C"
        0.deciCelsius.format(
            context = context,
            unit = TemperatureUnit.KELVIN,
            valueWidth = UnitWidth.LONG,
            unitWidth = UnitWidth.SHORT,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build()
        ) shouldBe "273,15 K"
    }

    @Test
    fun `format Celsius with long unit width with Android translations`() = runTest {
        val formattedNumberSlot = slot<String>()
        val context = mockk<Context>().apply {
            every { getString(any()) } returns "FAILED"
            every {
                getString(
                    org.breezyweather.unit.R.string.temperature_c_nominative_long,
                    capture(formattedNumberSlot)
                )
            } answers { "${formattedNumberSlot.captured} degrés Celsius" }
            every {
                getString(
                    org.breezyweather.unit.R.string.temperature_k_nominative_long,
                    capture(formattedNumberSlot)
                )
            } answers { "${formattedNumberSlot.captured} kelvins" }
        }
        101.4.deciCelsius.format(
            context = context,
            unit = TemperatureUnit.CELSIUS,
            valueWidth = UnitWidth.NARROW,
            unitWidth = UnitWidth.LONG,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build()
        ) shouldBe "10 degrés Celsius"
        101.4.deciCelsius.format(
            context = context,
            unit = TemperatureUnit.CELSIUS,
            valueWidth = UnitWidth.SHORT,
            unitWidth = UnitWidth.LONG,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build()
        ) shouldBe "10,1 degrés Celsius"
        0.deciCelsius.format(
            context = context,
            unit = TemperatureUnit.KELVIN,
            valueWidth = UnitWidth.LONG,
            unitWidth = UnitWidth.LONG,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build()
        ) shouldBe "273,15 kelvins"
    }

    @Test
    fun `get temperature display name`() = runTest {
        val context = mockk<Context>().apply {
            every { getString(any()) } returns "FAILED"
            every {
                getString(org.breezyweather.unit.R.string.temperature_f_display_name_short)
            } answers { "°F" }
            every {
                getString(org.breezyweather.unit.R.string.temperature_f_display_name_long)
            } returns "degrés Fahrenheit"
        }
        TemperatureUnit.FAHRENHEIT.getDisplayName(
            context = context,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build(),
            width = UnitWidth.SHORT
        ) shouldBe "°F"
        TemperatureUnit.FAHRENHEIT.getDisplayName(
            context = context,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build(),
            width = UnitWidth.LONG
        ) shouldBe "degrés Fahrenheit"
    }

    @Test
    fun `default temperature unit with locale preference set`() = runTest {
        mockkStatic(LocalePreferences::class)
        every { LocalePreferences.getTemperatureUnit() } returns LocalePreferences.TemperatureUnit.KELVIN
        TemperatureUnit.getDefaultUnit(
            Locale.Builder().setLanguage("en").setRegion("US").build()
        ) shouldBe TemperatureUnit.KELVIN
    }

    @Test
    fun `default temperature unit for country`() = runTest {
        mockkStatic(LocalePreferences::class)
        every { LocalePreferences.getTemperatureUnit() } returns ""
        TemperatureUnit.getDefaultUnit(
            Locale.Builder().setLanguage("en").setRegion("US").build()
        ) shouldBe TemperatureUnit.FAHRENHEIT
    }

    @Test
    fun `get TemperatureUnit from id`() = runTest {
        TemperatureUnit.getUnit("c") shouldBe TemperatureUnit.CELSIUS
        TemperatureUnit.getUnit("f") shouldBe TemperatureUnit.FAHRENHEIT
        TemperatureUnit.getUnit("k") shouldBe TemperatureUnit.KELVIN
    }
}
