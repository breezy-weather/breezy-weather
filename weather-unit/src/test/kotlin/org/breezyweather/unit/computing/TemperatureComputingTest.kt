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
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import org.junit.jupiter.api.Test

/**
 * TODO: To be completed
 */
class TemperatureComputingTest {

    @Test
    fun computeHumidexTest() = runTest {
        computeHumidex(null, null) shouldBe null
        computeHumidex(null, 13.0.celsius) shouldBe null
        computeHumidex(20.0.celsius, null) shouldBe null
        computeHumidex(20.0.celsius, 13.0.celsius)?.inCelsius shouldBe 22.8
        computeHumidex(39.0.celsius, 26.0.celsius)?.inCelsius shouldBe 52.5
    }
}
