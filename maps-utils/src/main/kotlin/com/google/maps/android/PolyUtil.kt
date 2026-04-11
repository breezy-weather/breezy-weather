/*
 * Copyright 2008, 2013 Google Inc.
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

import com.google.maps.android.MathUtil.wrap
import com.google.maps.android.model.LatLng
import kotlin.math.sin
import kotlin.math.tan

object PolyUtil {
    /**
     * Returns tan(latitude-at-lng3) on the great circle (lat1, lng1) to (lat2, lng2). lng1==0.
     * See http://williams.best.vwh.net/avform.htm .
     */
    private fun tanLatGC(lat1: Double, lat2: Double, lng2: Double, lng3: Double): Double {
        return (tan(lat1) * sin(lng2 - lng3) + tan(lat2) * sin(lng3)) / sin(lng2)
    }

    /**
     * Returns mercator(latitude-at-lng3) on the Rhumb line (lat1, lng1) to (lat2, lng2). lng1==0.
     */
    private fun mercatorLatRhumb(lat1: Double, lat2: Double, lng2: Double, lng3: Double): Double {
        return (MathUtil.mercator(lat1) * (lng2 - lng3) + MathUtil.mercator(lat2) * lng3) / lng2
    }

    /**
     * Computes whether the vertical segment (lat3, lng3) to South Pole intersects the segment
     * (lat1, lng1) to (lat2, lng2).
     * Longitudes are offset by -lng1; the implicit lng1 becomes 0.
     */
    private fun intersects(
        lat1: Double,
        lat2: Double,
        lng2: Double,
        lat3: Double,
        lng3: Double,
        geodesic: Boolean,
    ): Boolean {
        // Both ends on the same side of lng3.
        if ((lng3 >= 0 && lng3 >= lng2) || (lng3 < 0 && lng3 < lng2)) {
            return false
        }
        // Point is South Pole.
        if (lat3 <= -Math.PI / 2) {
            return false
        }
        // Any segment end is a pole.
        if (lat1 <= -Math.PI / 2 || lat2 <= -Math.PI / 2 || lat1 >= Math.PI / 2 || lat2 >= Math.PI / 2) {
            return false
        }
        if (lng2 <= -Math.PI) {
            return false
        }
        val linearLat = (lat1 * (lng2 - lng3) + lat2 * lng3) / lng2
        // Northern hemisphere and point under lat-lng line.
        if (lat1 >= 0 && lat2 >= 0 && lat3 < linearLat) {
            return false
        }
        // Southern hemisphere and point above lat-lng line.
        if (lat1 <= 0 && lat2 <= 0 && lat3 >= linearLat) {
            return true
        }
        // North Pole.
        if (lat3 >= Math.PI / 2) {
            return true
        }
        // Compare lat3 with latitude on the GC/Rhumb segment corresponding to lng3.
        // Compare through a strictly-increasing function (tan() or mercator()) as convenient.
        return if (geodesic) {
            tan(lat3) >= tanLatGC(lat1, lat2, lng2, lng3)
        } else {
            MathUtil.mercator(lat3) >= mercatorLatRhumb(lat1, lat2, lng2, lng3)
        }
    }

    fun containsLocation(point: LatLng, polygon: List<LatLng>, geodesic: Boolean): Boolean {
        return containsLocation(point.latitude, point.longitude, polygon, geodesic)
    }

    /**
     * Computes whether the given point lies inside the specified polygon.
     * The polygon is always considered closed, regardless of whether the last point equals
     * the first or not.
     * Inside is defined as not containing the South Pole -- the South Pole is always outside.
     * The polygon is formed of great circle segments if geodesic is true, and of rhumb
     * (loxodromic) segments otherwise.
     */
    fun containsLocation(
        latitude: Double,
        longitude: Double,
        polygon: List<LatLng>,
        geodesic: Boolean,
    ): Boolean {
        val size = polygon.size
        if (size == 0) {
            return false
        }
        val lat3 = Math.toRadians(latitude)
        val lng3 = Math.toRadians(longitude)
        val prev = polygon[size - 1]
        var lat1 = Math.toRadians(prev.latitude)
        var lng1 = Math.toRadians(prev.longitude)
        var nIntersect = 0
        for (point2 in polygon) {
            val dLng3 = wrap(lng3 - lng1, -Math.PI, Math.PI)
            // Special case: point equal to vertex is inside.
            if (lat3 == lat1 && dLng3 == 0.0) {
                return true
            }
            val lat2 = Math.toRadians(point2.latitude)
            val lng2 = Math.toRadians(point2.longitude)
            // Offset longitudes by -lng1.
            if (
                intersects(
                    lat1,
                    lat2,
                    wrap(lng2 - lng1, -Math.PI, Math.PI),
                    lat3,
                    dLng3,
                    geodesic
                )
            ) {
                ++nIntersect
            }
            lat1 = lat2
            lng1 = lng2
        }
        return nIntersect and 1 != 0
    }
}
