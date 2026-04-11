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

import com.google.maps.android.data.geojson.GeoJsonFeature

/**
 * An abstraction that shares the common properties of
 * [KmlPlacemark][com.google.maps.android.data.kml.KmlPlacemark] and
 * [GeoJsonFeature]
 * @param featureGeometry type of geometry to assign to the feature
 * @param id              common identifier of the feature
 * @param properties      map containing properties related to the feature
 */
open class Feature(
    /*
     * Sets the stored Geometry and redraws it on the layer if it has already been added
     *
     * @param geometry Geometry to set
     */
    /**
     * Sets the stored Geometry and redraws it on the layer if it has already been added
     *
     * @param geometry Geometry to set
     */
    var geometry: Geometry<*>?,
    /**
     * Gets the id of the feature
     *
     * @return id
     */
    id: String?,
    properties: MutableMap<String, String?>?,
) {
    var mId: String? = id
        protected set
    private var mProperties: MutableMap<String, String?> = properties ?: mutableMapOf()
    /*
     * Gets the geometry object
     *
     * @return geometry object
     */

    /**
     * Returns all the stored property keys
     *
     * @return iterable of property keys
     */
    val propertyKeys: Iterable<String>
        get() = mProperties.keys

    /**
     * Gets the property entry set
     *
     * @return property entry set
     */
    val properties: Iterable<*>
        get() = mProperties.entries

    /**
     * Gets the value for a stored property
     *
     * @param property key of the property
     * @return value of the property if its key exists, otherwise null
     */
    fun getProperty(property: String): String? {
        return mProperties[property]
    }

    /**
     * Checks whether the given property key exists
     *
     * @param property key of the property to check
     * @return true if property key exists, false otherwise
     */
    fun hasProperty(property: String): Boolean {
        return mProperties.containsKey(property)
    }

    /**
     * Gets whether the placemark has properties
     *
     * @return true if there are properties in the properties map, false otherwise
     */
    fun hasProperties(): Boolean {
        return mProperties.isNotEmpty()
    }

    /**
     * Checks if the geometry is assigned
     *
     * @return true if feature contains geometry object, otherwise null
     */
    fun hasGeometry(): Boolean {
        return geometry != null
    }

    /**
     * Store a new property key and value
     *
     * @param property      key of the property to store
     * @param propertyValue value of the property to store
     * @return previous value with the same key, otherwise null if the key didn't exist
     */
    protected open fun setProperty(property: String, propertyValue: String?): String? {
        return mProperties.put(property, propertyValue)
    }

    /**
     * Removes a given property
     *
     * @param property key of the property to remove
     * @return value of the removed property or null if there was no corresponding key
     */
    protected open fun removeProperty(property: String): String? {
        return mProperties.remove(property)
    }
}
