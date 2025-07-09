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

import android.content.Context
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.breezyweather.common.basic.models.options.basic.Utils
import org.breezyweather.common.extensions.currentLocale
import org.junit.jupiter.api.Test
import java.util.Locale

class UnitUtilsTest {
    @Test
    fun formatFloat() = runTest {
        mockkStatic(Context::currentLocale)

        val context = mockk<Context>().apply {
            every { currentLocale } returns Locale("fr", "FR")
        }

        Utils.formatDouble(context, 14.34234) shouldBe "14,34"
        Utils.formatDouble(context, 14.34834) shouldBe "14,35"
        Utils.formatDouble(context, 14.34834, 3) shouldBe "14,348"
        Utils.formatDouble(context, 14.34864, 3) shouldBe "14,349"
    }

    @Test
    fun formatInt() = runTest {
        mockkStatic(Context::currentLocale)

        val context = mockk<Context>().apply {
            every { currentLocale } returns Locale("fr", "FR")
        }

        Utils.formatInt(context, 14) shouldBe "14"
        Utils.formatInt(context, 16) shouldBe "16"
    }
}
