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

package org.breezyweather.option.utils

import android.content.res.Resources
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.breezyweather.R
import org.breezyweather.common.utils.UnitUtils
import org.junit.jupiter.api.Test

class UtilsTest {
    @Test
    fun getNameByValue() = runTest {
        val res = mockk<Resources>()
        every { res.getStringArray(R.array.dark_modes) } returns
            arrayOf("Automatic", "Follow system", "Always light", "Always dark")
        every { res.getStringArray(R.array.dark_mode_values) } returns arrayOf("auto", "system", "light", "dark")
        UnitUtils.getNameByValue(res, "auto", R.array.dark_modes, R.array.dark_mode_values) shouldBe "Automatic"
    }
}
