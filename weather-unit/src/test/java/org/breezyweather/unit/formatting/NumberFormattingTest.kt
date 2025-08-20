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

package org.breezyweather.unit.formatting

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.util.Locale

/**
 * TODO: Currently, NumberFormatter and NumberFormat never works, as SDK is always 0
 * TODO: Thousand separator tests
 */
class NumberFormattingTest {
    @Test
    fun `Format double with decimals and French-style number formatting, using NumberFormatter`() = runTest {
        formatDoubleWithDecimalsAndFrenchLocale(useNumberFormatter = true, useNumberFormat = false)
    }

    @Test
    fun `Format double with decimals and French-style number formatting, using NumberFormat`() = runTest {
        formatDoubleWithDecimalsAndFrenchLocale(useNumberFormatter = false, useNumberFormat = true)
    }

    @Test
    fun `Format double with decimals and French-style number formatting, using DecimalFormat`() = runTest {
        formatDoubleWithDecimalsAndFrenchLocale(useNumberFormatter = false, useNumberFormat = false)
    }

    fun formatDoubleWithDecimalsAndFrenchLocale(
        useNumberFormatter: Boolean,
        useNumberFormat: Boolean,
    ) {
        7.00646.format(
            decimals = 2,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build(),
            showSign = false,
            useNumberFormatter = useNumberFormatter,
            useNumberFormat = useNumberFormat
        ) shouldBe "7,01"
        14.34234.format(
            decimals = 2,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build(),
            showSign = false,
            useNumberFormatter = useNumberFormatter,
            useNumberFormat = useNumberFormat
        ) shouldBe "14,34"
        14.34834.format(
            decimals = 2,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build(),
            showSign = false,
            useNumberFormatter = useNumberFormatter,
            useNumberFormat = useNumberFormat
        ) shouldBe "14,35"
        14.34834.format(
            decimals = 3,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build(),
            showSign = false,
            useNumberFormatter = useNumberFormatter,
            useNumberFormat = useNumberFormat
        ) shouldBe "14,348"
        14.34864.format(
            decimals = 3,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build(),
            showSign = false,
            useNumberFormatter = useNumberFormatter,
            useNumberFormat = useNumberFormat
        ) shouldBe "14,349"
    }

    @Test
    fun `Format double without values ending with a 0`() = runTest {
        7.00246.format(
            decimals = 2,
            locale = Locale.Builder().setLanguage("en").setRegion("US").build(),
            showSign = false,
            useNumberFormatter = true,
            useNumberFormat = false
        ) shouldBe "7"
        7.00246.format(
            decimals = 2,
            locale = Locale.Builder().setLanguage("en").setRegion("US").build(),
            showSign = false,
            useNumberFormatter = false,
            useNumberFormat = true
        ) shouldBe "7"
        7.00246.format(
            decimals = 2,
            locale = Locale.Builder().setLanguage("en").setRegion("US").build(),
            showSign = false,
            useNumberFormatter = false,
            useNumberFormat = false
        ) shouldBe "7"
    }

    @Test
    fun `Format int`() = runTest {
        14.format(
            decimals = 0,
            locale = Locale.Builder().setLanguage("en").setRegion("US").build(),
            showSign = false,
            useNumberFormatter = true,
            useNumberFormat = false
        ) shouldBe "14"
        14.format(
            decimals = 0,
            locale = Locale.Builder().setLanguage("en").setRegion("US").build(),
            showSign = false,
            useNumberFormatter = false,
            useNumberFormat = true
        ) shouldBe "14"
        14.format(
            decimals = 0,
            locale = Locale.Builder().setLanguage("en").setRegion("US").build(),
            showSign = false,
            useNumberFormatter = false,
            useNumberFormat = false
        ) shouldBe "14"
    }

    @Test
    fun `Format with leading sign`() = runTest {
        7.3.format(
            decimals = 1,
            locale = Locale.Builder().setLanguage("en").setRegion("US").build(),
            showSign = true,
            useNumberFormatter = true,
            useNumberFormat = false
        ) shouldBe "+7.3"
        7.3.format(
            decimals = 1,
            locale = Locale.Builder().setLanguage("en").setRegion("US").build(),
            showSign = true,
            useNumberFormatter = false,
            useNumberFormat = true
        ) shouldBe "+7.3"
        7.3.format(
            decimals = 1,
            locale = Locale.Builder().setLanguage("en").setRegion("US").build(),
            showSign = true,
            useNumberFormatter = false,
            useNumberFormat = false
        ) shouldBe "+7.3"
    }
}
