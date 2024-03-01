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

package com.google.maps.android;

import com.google.maps.android.model.LatLng;

import static com.google.maps.android.MathUtil.mercator;
import static com.google.maps.android.MathUtil.wrap;
import static java.lang.Math.PI;
import static java.lang.Math.sin;
import static java.lang.Math.tan;
import static java.lang.Math.toRadians;

import java.util.List;

public class PolyUtil {

    private PolyUtil() {
    }

    /**
     * Returns tan(latitude-at-lng3) on the great circle (lat1, lng1) to (lat2, lng2). lng1==0.
     * See http://williams.best.vwh.net/avform.htm .
     */
    private static double tanLatGC(double lat1, double lat2, double lng2, double lng3) {
        return (tan(lat1) * sin(lng2 - lng3) + tan(lat2) * sin(lng3)) / sin(lng2);
    }

    /**
     * Returns mercator(latitude-at-lng3) on the Rhumb line (lat1, lng1) to (lat2, lng2). lng1==0.
     */
    private static double mercatorLatRhumb(double lat1, double lat2, double lng2, double lng3) {
        return (mercator(lat1) * (lng2 - lng3) + mercator(lat2) * lng3) / lng2;
    }

    /**
     * Computes whether the vertical segment (lat3, lng3) to South Pole intersects the segment
     * (lat1, lng1) to (lat2, lng2).
     * Longitudes are offset by -lng1; the implicit lng1 becomes 0.
     */
    private static boolean intersects(double lat1, double lat2, double lng2,
                                      double lat3, double lng3, boolean geodesic) {
        // Both ends on the same side of lng3.
        if ((lng3 >= 0 && lng3 >= lng2) || (lng3 < 0 && lng3 < lng2)) {
            return false;
        }
        // Point is South Pole.
        if (lat3 <= -PI / 2) {
            return false;
        }
        // Any segment end is a pole.
        if (lat1 <= -PI / 2 || lat2 <= -PI / 2 || lat1 >= PI / 2 || lat2 >= PI / 2) {
            return false;
        }
        if (lng2 <= -PI) {
            return false;
        }
        double linearLat = (lat1 * (lng2 - lng3) + lat2 * lng3) / lng2;
        // Northern hemisphere and point under lat-lng line.
        if (lat1 >= 0 && lat2 >= 0 && lat3 < linearLat) {
            return false;
        }
        // Southern hemisphere and point above lat-lng line.
        if (lat1 <= 0 && lat2 <= 0 && lat3 >= linearLat) {
            return true;
        }
        // North Pole.
        if (lat3 >= PI / 2) {
            return true;
        }
        // Compare lat3 with latitude on the GC/Rhumb segment corresponding to lng3.
        // Compare through a strictly-increasing function (tan() or mercator()) as convenient.
        return geodesic ?
                tan(lat3) >= tanLatGC(lat1, lat2, lng2, lng3) :
                mercator(lat3) >= mercatorLatRhumb(lat1, lat2, lng2, lng3);
    }

    public static boolean containsLocation(LatLng point, List<LatLng> polygon, boolean geodesic) {
        return containsLocation(point.getLatitude(), point.getLongitude(), polygon, geodesic);
    }

    /**
     * Computes whether the given point lies inside the specified polygon.
     * The polygon is always considered closed, regardless of whether the last point equals
     * the first or not.
     * Inside is defined as not containing the South Pole -- the South Pole is always outside.
     * The polygon is formed of great circle segments if geodesic is true, and of rhumb
     * (loxodromic) segments otherwise.
     */
    public static boolean containsLocation(double latitude, double longitude, List<LatLng> polygon, boolean geodesic) {
        final int size = polygon.size();
        if (size == 0) {
            return false;
        }
        double lat3 = toRadians(latitude);
        double lng3 = toRadians(longitude);
        LatLng prev = polygon.get(size - 1);
        double lat1 = toRadians(prev.getLatitude());
        double lng1 = toRadians(prev.getLongitude());
        int nIntersect = 0;
        for (LatLng point2 : polygon) {
            double dLng3 = wrap(lng3 - lng1, -PI, PI);
            // Special case: point equal to vertex is inside.
            if (lat3 == lat1 && dLng3 == 0) {
                return true;
            }
            double lat2 = toRadians(point2.getLatitude());
            double lng2 = toRadians(point2.getLongitude());
            // Offset longitudes by -lng1.
            if (intersects(lat1, lat2, wrap(lng2 - lng1, -PI, PI), lat3, dLng3, geodesic)) {
                ++nIntersect;
            }
            lat1 = lat2;
            lng1 = lng2;
        }
        return (nIntersect & 1) != 0;
    }

}
