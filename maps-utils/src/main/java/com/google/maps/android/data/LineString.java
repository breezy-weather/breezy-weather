/*
 * Copyright 2023 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.data;

import com.google.maps.android.data.geojson.GeoJsonLineString;
import com.google.maps.android.model.LatLng;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * An abstraction that shares the common properties of
 * {@link com.google.maps.android.data.kml.KmlLineString KmlLineString} and
 * {@link GeoJsonLineString GeoJsonLineString}
 */
public class LineString implements Geometry<List<LatLng>> {

    private static final String GEOMETRY_TYPE = "LineString";

    private final List<LatLng> mCoordinates;

    /**
     * Creates a new LineString object
     *
     * @param coordinates array of coordinates
     */
    public LineString(List<LatLng> coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        mCoordinates = coordinates;
    }

    /**
     * Gets the type of geometry
     *
     * @return type of geometry
     */
    public String getGeometryType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets the coordinates of the LineString
     *
     * @return coordinates of the LineString
     */
    public List<LatLng> getGeometryObject() {
        return mCoordinates;
    }

    @NonNull
    @Override
    public String toString() {
        String sb = GEOMETRY_TYPE + "{" +
                "\n coordinates=" + mCoordinates +
                "\n}\n";
        return sb;
    }

}
