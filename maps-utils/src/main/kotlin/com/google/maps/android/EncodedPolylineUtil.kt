/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android

import com.google.maps.android.model.LatLng

/**
 * Ported from https://github.com/googlemaps/js-polyline-codec/blob/main/src/index.ts
 * by Breezy Weather
 */
object EncodedPolylineUtil {

    fun decode(encodedPath: String): List<LatLng> {
        val factor = 1E5

        val coordinatesList = mutableListOf<LatLng>()
        var index = 0
        var lat = 0
        var lng = 0

        // This code has been profiled and optimized, so don't modify it without
        // measuring its performance.
        while (index < encodedPath.length) {
            // Fully unrolling the following loops speeds things up about 5%.
            var result = 1
            var shift = 0
            var b: Int
            do {
                // Invariant: "result" is current partial result plus (1 << shift).
                // The following line effectively clears this bit by decrementing "b".
                b = encodedPath[index++].code - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f) // See note above.
            lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1

            result = 1
            shift = 0
            do {
                b = encodedPath[index++].code - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f)
            lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1

            coordinatesList.add(
                LatLng(
                    lat.toDouble() / factor,
                    lng.toDouble() / factor
                )
            )
        }
        return coordinatesList
    }
}
