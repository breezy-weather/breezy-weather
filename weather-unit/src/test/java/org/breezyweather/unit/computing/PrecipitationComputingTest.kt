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

package org.breezyweather.unit.computing

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.breezyweather.unit.precipitation.Precipitation.Companion.centimeters
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import org.junit.jupiter.api.Test

class PrecipitationComputingTest {

    @Test
    fun computeTotalPrecipitationTest() = runTest {
        computeTotalPrecipitation(null, null, null, null) shouldBe null
        computeTotalPrecipitation(
            temperature = 2.0.celsius,
            rain = 20.0.millimeters,
            snow = 1.0.centimeters,
            ice = null
        ) shouldBe
            30.0.millimeters
        computeTotalPrecipitation(
            temperature = 0.0.celsius,
            rain = null,
            snow = 3.0.centimeters,
            ice = null
        ) shouldBe
            10.0.millimeters
        computeTotalPrecipitation(
            temperature = 0.0.celsius,
            rain = null,
            snow = null,
            ice = 2.0.centimeters
        ) shouldBe
            18.4.millimeters
        computeTotalPrecipitation(
            temperature = 0.0.celsius,
            rain = 50.0.millimeters,
            snow = 6.0.centimeters,
            ice = 1.0.centimeters
        ) shouldBe
            79.2.millimeters
        computeTotalPrecipitation(
            temperature = (-1.0).celsius,
            rain = null,
            snow = 5.0.centimeters,
            ice = null
        ) shouldBe
            10.0.millimeters
        computeTotalPrecipitation(
            temperature = (-6.0).celsius,
            rain = null,
            snow = 4.5.centimeters,
            ice = null
        ) shouldBe
            5.0.millimeters
    }
}
