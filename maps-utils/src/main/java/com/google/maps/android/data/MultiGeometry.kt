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

import com.google.maps.android.data.geojson.GeoJsonMultiLineString
import com.google.maps.android.data.geojson.GeoJsonMultiPoint
import com.google.maps.android.data.geojson.GeoJsonMultiPolygon

/**
 * An abstraction that shares the common properties of
 * [KmlMultiGeometry] and [GeoJsonMultiLineString], [GeoJsonMultiPoint] and [GeoJsonMultiPolygon]
 * @param geometries contains list of Polygons, Linestrings or Points
 */
open class MultiGeometry(
    geometries: List<Geometry<*>>,
) : Geometry<Any> {
    /**
     * Gets the type of geometry
     *
     * @return type of geometry
     */
    /**
     * Set the type of geometry
     *
     * @param type String describing type of geometry
     */
    override var geometryType = "MultiGeometry"
    private val mGeometries: List<Geometry<*>> = geometries

    /**
     * Gets the stored geometry object
     *
     * @return geometry object
     */
    override val geometryObject: Any
        get() = mGeometries

    override fun toString(): String {
        var typeString = "Geometries="
        if (geometryType == "MultiPoint") {
            typeString = "LineStrings="
        }
        if (geometryType == "MultiLineString") {
            typeString = "points="
        }
        if (geometryType == "MultiPolygon") {
            typeString = "Polygons="
        }
        return """$geometryType{ $typeString$geometryObject}"""
    }
}
