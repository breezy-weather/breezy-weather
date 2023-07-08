package org.breezyweather.option._utils

import android.content.res.Resources
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.Utils
import org.junit.jupiter.api.Test

class UtilsTest {
    @Test
    fun getNameByValue() = runTest {
        val res = mockk<Resources>()
        every { res.getStringArray(R.array.dark_modes) } returns arrayOf("Automatic", "Follow system", "Always light", "Always dark")
        every { res.getStringArray(R.array.dark_mode_values) } returns arrayOf("auto", "system", "light", "dark")
        Utils.getNameByValue(res, "auto", R.array.dark_modes, R.array.dark_mode_values) shouldBe "Automatic"
    }
}
