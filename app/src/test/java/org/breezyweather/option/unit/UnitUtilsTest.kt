/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
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

import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.breezyweather.common.basic.models.options._basic.Utils
import org.junit.jupiter.api.Test

class UnitUtilsTest {
    @Test
    fun formatFloat() = runTest {
        Utils.formatFloat(14.34234f) shouldBeIn arrayOf("14.34", "14,34")
        Utils.formatFloat(14.34834f) shouldBeIn arrayOf("14.35", "14,35")
        Utils.formatFloat(14.34834f, 3) shouldBeIn arrayOf("14.348", "14,348")
        Utils.formatFloat(14.34864f, 3) shouldBeIn arrayOf("14.349", "14,349")
    }

    @Test
    fun formatInt() = runTest {
        Utils.formatInt(14) shouldBe "14"
        Utils.formatInt(16) shouldBe "16"
    }
}
