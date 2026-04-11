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
package com.google.maps.android.data.geojson

import com.google.maps.android.data.DataPolygon
import com.google.maps.android.model.LatLng

/**
 * A GeoJsonPolygon geometry contains an array of arrays of [LatLng]s.
 * The first array is the polygon exterior boundary. Subsequent arrays are holes.
 */
class GeoJsonPolygon(
    /**
     * list of a list of coordinates of the GeoJsonPolygons
     */
    val coordinates: List<List<LatLng>>,
) : DataPolygon<Any?> {

    /**
     * Gets the stored geometry object
     *
     * @return geometry object
     */
    override val geometryObject: Any
        get() = coordinates

    /**
     * Gets the type of geometry
     *
     * @return type of geometry
     */
    override val geometryType: String
        get() = TYPE

    /**
     * Gets an array of outer boundary coordinates
     *
     * @return array of outer boundary coordinates
     */
    override val outerBoundaryCoordinates: List<LatLng?>
        get() = // First array of coordinates are the outline
            coordinates[POLYGON_OUTER_COORDINATE_INDEX] as MutableList<LatLng>

    /**
     * Gets an array of arrays of inner boundary coordinates
     *
     * @return array of arrays of inner boundary coordinates
     */
    override val innerBoundaryCoordinates: List<List<LatLng?>?>
        get() {
            // Following arrays are holes
            val innerBoundary = mutableListOf<MutableList<LatLng>>()
            for (i in POLYGON_INNER_COORDINATE_INDEX until coordinates.size) {
                innerBoundary.add(coordinates[i] as MutableList<LatLng>)
            }
            return innerBoundary
        }

    override fun toString(): String {
        return """$TYPE{ coordinates=$coordinates}"""
    }

    companion object {
        const val TYPE = "Polygon"

        private const val POLYGON_OUTER_COORDINATE_INDEX = 0
        private const val POLYGON_INNER_COORDINATE_INDEX = 1
    }
}
