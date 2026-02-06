/*
 * Copyright 2017 Google Inc.
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
package com.google.maps.android.data

import com.google.maps.android.data.geojson.GeoJsonPolygon
import com.google.maps.android.model.LatLng

/**
 * An interface containing the common properties of
 * [GeoJsonPolygon] and
 * [KmlPolygon][com.google.maps.android.data.kml.KmlPolygon]
 *
 * @param <T> the type of Polygon - GeoJsonPolygon or KmlPolygon
</T> */
interface DataPolygon<T> : Geometry<Any?> {
    /**
     * Gets an array of outer boundary coordinates
     *
     * @return array of outer boundary coordinates
     */
    val outerBoundaryCoordinates: List<LatLng?>?

    /**
     * Gets an array of arrays of inner boundary coordinates
     *
     * @return array of arrays of inner boundary coordinates
     */
    val innerBoundaryCoordinates: List<List<LatLng?>?>
}
