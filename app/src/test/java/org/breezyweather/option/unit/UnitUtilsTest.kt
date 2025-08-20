/**
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

package org.breezyweather.option.unit

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.breezyweather.unit.formatting.format
import org.junit.jupiter.api.Test
import java.util.Locale

/**
 * TODO: Move to unit lib
 * TODO: Add sign testing
 */
class UnitUtilsTest {
    @Test
    fun formatDouble() = runTest {
        7.00646.format(
            decimals = 2,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build()
        ) shouldBe "7,01"
        7.00246.format(
            decimals = 2,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build()
        ) shouldBe "7"
        14.34234.format(
            decimals = 2,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build()
        ) shouldBe "14,34"
        14.34834.format(
            decimals = 2,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build()
        ) shouldBe "14,35"
        14.34834.format(
            decimals = 3,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build()
        ) shouldBe "14,348"
        14.34864.format(
            decimals = 3,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build()
        ) shouldBe "14,349"
    }

    @Test
    fun formatInt() = runTest {
        14.format(
            decimals = 0,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build()
        ) shouldBe "14"
        16.format(
            decimals = 0,
            locale = Locale.Builder().setLanguage("fr").setRegion("FR").build()
        ) shouldBe "16"
    }
}
