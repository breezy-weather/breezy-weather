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

package org.breezyweather.option.appearance

import android.content.Context
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.breezyweather.common.options.appearance.CardDisplay
import org.junit.jupiter.api.Test

class CardDisplayTest {

    @Test
    fun toCardDisplayList() = runTest {
        val value = "nowcast&daily_forecast&hourly_forecast&precipitation&wind&air_quality&pollen" +
            "&humidity&uv&visibility&pressure&sun&moon"
        val list = CardDisplay.toCardDisplayList(value)
        list[0] shouldBe CardDisplay.CARD_NOWCAST
        list[1] shouldBe CardDisplay.CARD_DAILY_FORECAST
        list[2] shouldBe CardDisplay.CARD_HOURLY_FORECAST
        list[3] shouldBe CardDisplay.CARD_PRECIPITATION
        list[4] shouldBe CardDisplay.CARD_WIND
        list[5] shouldBe CardDisplay.CARD_AIR_QUALITY
        list[6] shouldBe CardDisplay.CARD_POLLEN
        list[7] shouldBe CardDisplay.CARD_HUMIDITY
        list[8] shouldBe CardDisplay.CARD_UV
        list[9] shouldBe CardDisplay.CARD_VISIBILITY
        list[10] shouldBe CardDisplay.CARD_PRESSURE
        list[11] shouldBe CardDisplay.CARD_SUN
        list[12] shouldBe CardDisplay.CARD_MOON
    }

    @Test
    fun toValue() = runTest {
        val list = arrayListOf(
            CardDisplay.CARD_NOWCAST,
            CardDisplay.CARD_DAILY_FORECAST,
            CardDisplay.CARD_HOURLY_FORECAST,
            CardDisplay.CARD_PRECIPITATION,
            CardDisplay.CARD_WIND,
            CardDisplay.CARD_AIR_QUALITY,
            CardDisplay.CARD_POLLEN,
            CardDisplay.CARD_HUMIDITY,
            CardDisplay.CARD_UV,
            CardDisplay.CARD_VISIBILITY,
            CardDisplay.CARD_PRESSURE,
            CardDisplay.CARD_SUN,
            CardDisplay.CARD_MOON
        )
        val value = "nowcast&daily_forecast&hourly_forecast&precipitation&wind&air_quality&pollen" +
            "&humidity&uv&visibility&pressure&sun&moon"
        CardDisplay.toValue(list) shouldBe value
    }

    @Test
    fun getSummary() = runTest {
        val context = mockk<Context>().apply {
            every { getString(any()) } returns "Name"
            every { getString(org.breezyweather.unit.R.string.locale_separator) } returns ", "
        }
        val list = arrayListOf(
            CardDisplay.CARD_NOWCAST,
            CardDisplay.CARD_DAILY_FORECAST,
            CardDisplay.CARD_HOURLY_FORECAST,
            CardDisplay.CARD_PRECIPITATION,
            CardDisplay.CARD_WIND,
            CardDisplay.CARD_AIR_QUALITY,
            CardDisplay.CARD_POLLEN,
            CardDisplay.CARD_HUMIDITY,
            CardDisplay.CARD_UV,
            CardDisplay.CARD_VISIBILITY,
            CardDisplay.CARD_PRESSURE,
            CardDisplay.CARD_SUN,
            CardDisplay.CARD_MOON
        )
        val value = "Name, Name, Name, Name, Name, Name, Name, Name, Name, Name, Name, Name, Name"
        CardDisplay.getSummary(context, list) shouldBe value
    }
}
