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
