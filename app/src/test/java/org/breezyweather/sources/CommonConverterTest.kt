package org.breezyweather.sources

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import org.junit.jupiter.api.Test

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
        computeHumidex(null, 13.0.celsius) shouldBe null
        computeHumidex(20.0.celsius, null) shouldBe null
        computeHumidex(20.0.celsius, 13.0.celsius)?.inCelsius shouldBe 22.8
        computeHumidex(39.0.celsius, 26.0.celsius)?.inCelsius shouldBe 52.5
    }
}
