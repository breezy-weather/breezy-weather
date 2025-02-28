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
package com.google.maps.android.data.geojson

import com.google.maps.android.data.MultiGeometry

/**
 * A GeoJsonMultiPolygon geometry contains a number of [GeoJsonPolygon]s.
 *
 * @param geoJsonPolygons list of GeoJsonPolygons to store
 */
class GeoJsonMultiPolygon(
    geoJsonPolygons: List<GeoJsonPolygon>,
) : MultiGeometry(geoJsonPolygons) {
    /**
     * Creates a new GeoJsonMultiPolygon
     */
    init {
        geometryType = "MultiPolygon"
    }

    /**
     * Gets the type of geometry. The type of geometry conforms to the GeoJSON 'type'
     * specification.
     *
     * @return type of geometry
     */
    val type: String
        get() = geometryType

    /**
     * Gets a list of GeoJsonPolygons
     *
     * @return list of GeoJsonPolygons
     */
    val polygons: List<GeoJsonPolygon>
        get() {
            // convert list of Geometry types to list of GeoJsonPolygon types
            val geometryList = geometryObject
            val geoJsonPolygon = mutableListOf<GeoJsonPolygon>()
            for (geometry in geometryList as List<GeoJsonPolygon>) {
                geoJsonPolygon.add(geometry)
            }
            return geoJsonPolygon
        }
}
