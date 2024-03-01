/*
 * Copyright 2023 Google Inc.
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
package com.google.maps.android.data.geojson;

import com.google.maps.android.model.LatLng;
import com.google.maps.android.data.DataPolygon;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * A GeoJsonPolygon geometry contains an array of arrays of {@link LatLng}s.
 * The first array is the polygon exterior boundary. Subsequent arrays are holes.
 */

public class GeoJsonPolygon implements DataPolygon {

    private final static String GEOMETRY_TYPE = "Polygon";

    private final List<? extends List<LatLng>> mCoordinates;

    private final static int POLYGON_OUTER_COORDINATE_INDEX = 0;

    private final static int POLYGON_INNER_COORDINATE_INDEX = 1;

    /**
     * Creates a new GeoJsonPolygon object
     *
     * @param coordinates list of list of coordinates of GeoJsonPolygon to store
     */
    public GeoJsonPolygon(
            List<? extends List<LatLng>> coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        mCoordinates = coordinates;
    }

    /**
     * Gets the type of geometry. The type of geometry conforms to the GeoJSON 'type'
     * specification.
     *
     * @return type of geometry
     */
    public String getType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets a list of a list of coordinates of the GeoJsonPolygons
     *
     * @return list of a list of coordinates of the GeoJsonPolygon
     */
    public List<? extends List<LatLng>> getCoordinates() {
        return mCoordinates;
    }

    /**
     * Gets the stored geometry object
     *
     * @return geometry object
     */
    public List<? extends List<LatLng>> getGeometryObject() {
        return getCoordinates();
    }

    /**
     * Gets the type of geometry
     *
     * @return type of geometry
     */
    public String getGeometryType() {
        return getType();
    }


    /**
     * Gets an array of outer boundary coordinates
     *
     * @return array of outer boundary coordinates
     */
    public ArrayList<LatLng> getOuterBoundaryCoordinates() {
        // First array of coordinates are the outline
        return (ArrayList<LatLng>) getCoordinates().get(POLYGON_OUTER_COORDINATE_INDEX);
    }

    /**
     * Gets an array of arrays of inner boundary coordinates
     *
     * @return array of arrays of inner boundary coordinates
     */
    public ArrayList<ArrayList<LatLng>> getInnerBoundaryCoordinates() {
        // Following arrays are holes
        ArrayList<ArrayList<LatLng>> innerBoundary = new ArrayList<>();
        for (int i = POLYGON_INNER_COORDINATE_INDEX; i < getCoordinates().size();
             i++) {
            innerBoundary.add((ArrayList<LatLng>) getCoordinates().get(i));
        }
        return innerBoundary;
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
