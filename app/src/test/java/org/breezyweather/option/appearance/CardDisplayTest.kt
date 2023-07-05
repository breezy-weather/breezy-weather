package org.breezyweather.option.appearance

import android.content.Context
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.breezyweather.common.basic.models.options.appearance.CardDisplay
import org.junit.jupiter.api.Test

class CardDisplayTest {

    @Test
    fun toCardDisplayList() = runTest {
        val value = "daily_overview&hourly_overview&air_quality&allergen&sunrise_sunset&live"
        val list = CardDisplay.toCardDisplayList(value)
        list[0] shouldBe CardDisplay.CARD_DAILY_OVERVIEW
        list[1] shouldBe CardDisplay.CARD_HOURLY_OVERVIEW
        list[2] shouldBe CardDisplay.CARD_AIR_QUALITY
        list[3] shouldBe CardDisplay.CARD_ALLERGEN
        list[4] shouldBe CardDisplay.CARD_SUNRISE_SUNSET
        list[5] shouldBe CardDisplay.CARD_LIVE
    }

    @Test
    fun toValue() = runTest {
        val list = arrayListOf(
            CardDisplay.CARD_DAILY_OVERVIEW,
            CardDisplay.CARD_HOURLY_OVERVIEW,
            CardDisplay.CARD_AIR_QUALITY,
            CardDisplay.CARD_ALLERGEN,
            CardDisplay.CARD_SUNRISE_SUNSET,
            CardDisplay.CARD_LIVE
        )
        val value = "daily_overview&hourly_overview&air_quality&allergen&sunrise_sunset&live"
        CardDisplay.toValue(list) shouldBe value
    }

    @Test
    fun getSummary() = runTest {
        val context = mockk<Context>()
        every { context.getString(any()) } returns "Name"
        val list = arrayListOf(
            CardDisplay.CARD_DAILY_OVERVIEW,
            CardDisplay.CARD_HOURLY_OVERVIEW,
            CardDisplay.CARD_AIR_QUALITY,
            CardDisplay.CARD_ALLERGEN,
            CardDisplay.CARD_SUNRISE_SUNSET,
            CardDisplay.CARD_LIVE
        )
        val value = "Name, Name, Name, Name, Name, Name"
        CardDisplay.getSummary(context, list) shouldBe value
    }
}
