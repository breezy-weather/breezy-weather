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

package org.breezyweather.option.appearance

import android.content.Context
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.breezyweather.common.basic.models.options.appearance.DailyTrendDisplay
import org.junit.jupiter.api.Test

class DailyTrendDisplayTest {
    @Test
    fun toDailyTrendDisplayList() = runTest {
        val value = "temperature&air_quality&wind&uv_index&precipitation"
        val list = DailyTrendDisplay.toDailyTrendDisplayList(value)
        list[0] shouldBe DailyTrendDisplay.TAG_TEMPERATURE
        list[1] shouldBe DailyTrendDisplay.TAG_AIR_QUALITY
        list[2] shouldBe DailyTrendDisplay.TAG_WIND
        list[3] shouldBe DailyTrendDisplay.TAG_UV_INDEX
        list[4] shouldBe DailyTrendDisplay.TAG_PRECIPITATION
    }

    @Test
    fun toValue() = runTest {
        val list = arrayListOf(
            DailyTrendDisplay.TAG_TEMPERATURE,
            DailyTrendDisplay.TAG_AIR_QUALITY,
            DailyTrendDisplay.TAG_WIND,
            DailyTrendDisplay.TAG_UV_INDEX,
            DailyTrendDisplay.TAG_PRECIPITATION
        )
        val value = "temperature&air_quality&wind&uv_index&precipitation"
        DailyTrendDisplay.toValue(list) shouldBe value
    }

    @Test
    fun getSummary() = runTest {
        val context = mockk<Context>()
        every { context.getString(any()) } returns "Name"
        val list = arrayListOf(
            DailyTrendDisplay.TAG_TEMPERATURE,
            DailyTrendDisplay.TAG_AIR_QUALITY,
            DailyTrendDisplay.TAG_WIND,
            DailyTrendDisplay.TAG_UV_INDEX,
            DailyTrendDisplay.TAG_PRECIPITATION
        )
        val value = "Name, Name, Name, Name, Name"
        DailyTrendDisplay.getSummary(context, list) shouldBe value
    }
}
