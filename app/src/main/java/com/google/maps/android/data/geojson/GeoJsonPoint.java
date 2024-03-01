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

import com.google.maps.android.model.LatLng;
import com.google.maps.android.data.Point;

/**
 * A GeoJsonPoint geometry contains a single {@link com.google.maps.android.model.LatLng}.
 */
public class GeoJsonPoint extends Point {
    private final Double mAltitude;

    /**
     * Creates a new GeoJsonPoint
     *
     * @param coordinates coordinates of GeoJsonPoint to store
     */
    public GeoJsonPoint(LatLng coordinates) {
        this(coordinates, null);
    }

    /**
     * Creates a new GeoJsonPoint
     *
     * @param coordinates coordinates of the KmlPoint
     * @param altitude    altitude of the KmlPoint
     */
    public GeoJsonPoint(LatLng coordinates, Double altitude) {
        super(coordinates);

        this.mAltitude = altitude;
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
     * Gets the coordinates of the GeoJsonPoint
     *
     * @return coordinates of the GeoJsonPoint
     */
    public LatLng getCoordinates() {
        return getGeometryObject();
    }

    /**
     * Gets the altitude of the GeoJsonPoint
     *
     * @return altitude of the GeoJsonPoint
     */
    public Double getAltitude() {
        return mAltitude;
    }
}
