package org.breezyweather.sources

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
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
}
