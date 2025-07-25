package org.breezyweather.sources

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.math.roundToInt

/**
 * To be completed
 */
class CommonConverterTest {

    @Test
    fun getWindDegreeTest() = runTest {
        getWindDegree(null) shouldBe null
        getWindDegree("E") shouldBe 90.0
        getWindDegree("SSO") shouldBe 202.5
        getWindDegree("VR") shouldBe -1.0
    }

    @Test
    fun computeHumidexTest() = runTest {
        computeHumidex(null, null) shouldBe null
        computeHumidex(null, 13.0) shouldBe null
        computeHumidex(20.0, null) shouldBe null
        computeHumidex(20.0, 13.0)?.roundToInt() shouldBe 23
        computeHumidex(39.0, 26.0)?.roundToInt() shouldBe 52
    }
}
