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

package org.breezyweather.option.appearance

import android.content.Context
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.appearance.CardDisplay
import org.junit.jupiter.api.Test

class CardDisplayTest {

    @Test
    fun toCardDisplayList() = runTest {
        val value = "precipitation_nowcast&daily_overview&hourly_overview&air_quality&pollen&sunrise_sunset&live"
        val list = CardDisplay.toCardDisplayList(value)
        list[0] shouldBe CardDisplay.CARD_PRECIPITATION_NOWCAST
        list[1] shouldBe CardDisplay.CARD_DAILY_OVERVIEW
        list[2] shouldBe CardDisplay.CARD_HOURLY_OVERVIEW
        list[3] shouldBe CardDisplay.CARD_AIR_QUALITY
        list[4] shouldBe CardDisplay.CARD_POLLEN
        list[5] shouldBe CardDisplay.CARD_SUNRISE_SUNSET
        list[6] shouldBe CardDisplay.CARD_LIVE
    }

    @Test
    fun toValue() = runTest {
        val list = arrayListOf(
            CardDisplay.CARD_PRECIPITATION_NOWCAST,
            CardDisplay.CARD_DAILY_OVERVIEW,
            CardDisplay.CARD_HOURLY_OVERVIEW,
            CardDisplay.CARD_AIR_QUALITY,
            CardDisplay.CARD_POLLEN,
            CardDisplay.CARD_SUNRISE_SUNSET,
            CardDisplay.CARD_LIVE
        )
        val value = "precipitation_nowcast&daily_overview&hourly_overview&air_quality&pollen&sunrise_sunset&live"
        CardDisplay.toValue(list) shouldBe value
    }

    @Test
    fun getSummary() = runTest {
        val context = mockk<Context>().apply {
            every { getString(any()) } returns "Name"
            every { getString(R.string.comma_separator) } returns ", "
        }
        val list = arrayListOf(
            CardDisplay.CARD_DAILY_OVERVIEW,
            CardDisplay.CARD_HOURLY_OVERVIEW,
            CardDisplay.CARD_AIR_QUALITY,
            CardDisplay.CARD_POLLEN,
            CardDisplay.CARD_SUNRISE_SUNSET,
            CardDisplay.CARD_LIVE
        )
        val value = "Name, Name, Name, Name, Name, Name"
        CardDisplay.getSummary(context, list) shouldBe value
    }
}
