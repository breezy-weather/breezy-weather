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

import com.google.maps.android.data.geojson.GeoJsonPoint
import com.google.maps.android.model.LatLng

/**
 * An abstraction that shares the common properties of
 * [KmlPoint] and
 * [GeoJsonPoint]
 */
open class Point(
    coordinates: LatLng,
) : Geometry<Any> {
    private val mCoordinates: LatLng

    /*
     * Creates a new Point object
     *
     * @param coordinates coordinates of Point to store
     */
    init {
        mCoordinates = coordinates
    }

    /**
     * Gets the coordinates of the Point
     *
     * @return coordinates of the Point
     */
    override val geometryObject: Any
        get() = mCoordinates

    override fun toString(): String {
        return """$geometryType{ coordinates=$mCoordinates}"""
    }

    override val geometryType = "Point"
}
