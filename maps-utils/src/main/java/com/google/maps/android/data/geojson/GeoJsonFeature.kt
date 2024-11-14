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

import com.google.maps.android.data.Feature
import com.google.maps.android.data.Geometry
import com.google.maps.android.model.LatLngBounds

/**
 * A GeoJsonFeature has a geometry, bounding box, id and set of properties. Styles are also stored
 * in this class.
 */
class GeoJsonFeature(
    geometry: Geometry<*>?,
    id: String?,
    properties: HashMap<String, String?>?,
    boundingBox: LatLngBounds?,
) : Feature(geometry, id, properties) {
    private val mBoundingBox: LatLngBounds?

    /**
     * Creates a new GeoJsonFeature object
     *
     * @param geometry    type of geometry to assign to the feature
     * @param id          common identifier of the feature
     * @param properties  hashmap of containing properties related to the feature
     * @param boundingBox bounding box of the feature
     */
    init {
        mId = id
        mBoundingBox = boundingBox
    }

    /**
     * Store a new property key and value
     *
     * @param property      key of the property to store
     * @param propertyValue value of the property to store
     * @return previous value with the same key, otherwise null if the key didn't exist
     */
    public override fun setProperty(property: String, propertyValue: String?): String? {
        return super.setProperty(property, propertyValue)
    }

    /**
     * Removes a given property
     *
     * @param property key of the property to remove
     * @return value of the removed property or null if there was no corresponding key
     */
    public override fun removeProperty(property: String): String? {
        return super.removeProperty(property)
    }

    override fun toString(): String {
        return """Feature{ bounding box=$mBoundingBox, geometry=$geometry, id=$mId, properties=$properties}"""
    }
}
