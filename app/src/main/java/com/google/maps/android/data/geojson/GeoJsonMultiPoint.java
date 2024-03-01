/*
 * Copyright 2020 Google Inc.
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

import com.google.maps.android.data.Geometry;
import com.google.maps.android.data.MultiGeometry;

import java.util.ArrayList;
import java.util.List;

/**
 * A GeoJsonMultiPoint geometry contains a number of {@link GeoJsonPoint}s.
 */
public class GeoJsonMultiPoint extends MultiGeometry {
    /**
     * Creates a GeoJsonMultiPoint object
     *
     * @param geoJsonPoints list of GeoJsonPoints to store
     */
    public GeoJsonMultiPoint(List<GeoJsonPoint> geoJsonPoints) {
        super(geoJsonPoints);
        setGeometryType("MultiPoint");
    }

    /**
     * Gets the type of geometry. The type of geometry conforms to the GeoJSON 'type'
     * specification.
     *
     * @return type of geometry
     */
    public String getType() {
        return getGeometryType();
    }

    /**
     * Gets a list of GeoJsonPoints
     *
     * @return list of GeoJsonPoints
     */
    public List<GeoJsonPoint> getPoints() {
        //convert list of Geometry types to list of GeoJsonPoint types
        List<Geometry> geometryList = getGeometryObject();
        ArrayList<GeoJsonPoint> geoJsonPoints = new ArrayList<GeoJsonPoint>();
        for (Geometry geometry : geometryList) {
            GeoJsonPoint point = (GeoJsonPoint) geometry;
            geoJsonPoints.add(point);
        }
        return geoJsonPoints;
    }
}
