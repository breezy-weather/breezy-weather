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
package com.google.maps.android.data

import com.google.maps.android.data.geojson.GeoJsonLineString
import com.google.maps.android.model.LatLng

/**
 * An abstraction that shares the common properties of
 * [KmlLineString] and
 * [GeoJsonLineString]
 * @param coordinates array of coordinates
 */
open class LineString(
    coordinates: List<LatLng>,
) : Geometry<List<LatLng>> {

    override val geometryType: String = GEOMETRY_TYPE

    /**
     * Gets the coordinates of the LineString
     *
     * @return coordinates of the LineString
     */
    override val geometryObject: List<LatLng> = coordinates

    override fun toString(): String {
        return """${GEOMETRY_TYPE}{ coordinates=$geometryObject}"""
    }

    companion object {
        const val GEOMETRY_TYPE = "LineString"
    }
}
