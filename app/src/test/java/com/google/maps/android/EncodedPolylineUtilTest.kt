package com.google.maps.android

import com.google.maps.android.model.LatLng
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class EncodedPolylineUtilTest {

    @Test
    fun decode() = runTest {
        val encoded = "_p~iF~ps|U_ulLnnqC_mqNvxq`@"
        val decodedLatLng = EncodedPolylineUtil.decode(encoded)

        println(decodedLatLng.toString())

        decodedLatLng.size shouldBe 3
        decodedLatLng[0] shouldBe LatLng(38.5, -120.2)
        decodedLatLng[1] shouldBe LatLng(40.7, -120.95)
        decodedLatLng[2] shouldBe LatLng(43.252, -126.453)
        /*
            decodedLatLng[0].latitude shouldBe 38.5
            decodedLatLng[0].longitude shouldBe -120.2
            decodedLatLng[1].latitude shouldBe 40.7
            decodedLatLng[1].longitude shouldBe -120.95
            decodedLatLng[2].latitude shouldBe 43.252
            decodedLatLng[2].longitude shouldBe -126.453
         */
    }

}