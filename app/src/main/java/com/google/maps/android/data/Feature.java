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

package com.google.maps.android.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/**
 * An abstraction that shares the common properties of
 * {@link com.google.maps.android.data.kml.KmlPlacemark KmlPlacemark} and
 * {@link com.google.maps.android.data.geojson.GeoJsonFeature GeoJsonFeature}
 */
public class Feature extends Observable {

    protected String mId;

    private final Map<String, String> mProperties;

    private Geometry mGeometry;

    /**
     * Creates a new Feature object
     *
     * @param featureGeometry type of geometry to assign to the feature
     * @param id              common identifier of the feature
     * @param properties      map containing properties related to the feature
     */
    public Feature(Geometry featureGeometry, String id,
                   Map<String, String> properties) {
        mGeometry = featureGeometry;
        mId = id;
        if (properties == null) {
            mProperties = new HashMap<>();
        } else {
            mProperties = properties;
        }
    }

    /**
     * Returns all the stored property keys
     *
     * @return iterable of property keys
     */
    public Iterable<String> getPropertyKeys() {
        return mProperties.keySet();
    }

    /**
     * Gets the property entry set
     *
     * @return property entry set
     */
    public Iterable getProperties() {
        return mProperties.entrySet();
    }

    /**
     * Gets the value for a stored property
     *
     * @param property key of the property
     * @return value of the property if its key exists, otherwise null
     */
    public String getProperty(String property) {
        return mProperties.get(property);
    }

    /**
     * Gets the id of the feature
     *
     * @return id
     */
    public String getId() {
        return mId;
    }

    /**
     * Checks whether the given property key exists
     *
     * @param property key of the property to check
     * @return true if property key exists, false otherwise
     */
    public boolean hasProperty(String property) {
        return mProperties.containsKey(property);
    }

    /**
     * Gets the geometry object
     *
     * @return geometry object
     */
    public Geometry getGeometry() {
        return mGeometry;
    }

    /**
     * Gets whether the placemark has properties
     *
     * @return true if there are properties in the properties map, false otherwise
     */
    public boolean hasProperties() {
        return mProperties.size() > 0;
    }

    /**
     * Checks if the geometry is assigned
     *
     * @return true if feature contains geometry object, otherwise null
     */
    public boolean hasGeometry() {
        return (mGeometry != null);
    }

    /**
     * Store a new property key and value
     *
     * @param property      key of the property to store
     * @param propertyValue value of the property to store
     * @return previous value with the same key, otherwise null if the key didn't exist
     */
    protected String setProperty(String property, String propertyValue) {
        return mProperties.put(property, propertyValue);
    }

    /**
     * Removes a given property
     *
     * @param property key of the property to remove
     * @return value of the removed property or null if there was no corresponding key
     */
    protected String removeProperty(String property) {
        return mProperties.remove(property);
    }

    /**
     * Sets the stored Geometry and redraws it on the layer if it has already been added
     *
     * @param geometry Geometry to set
     */
    protected void setGeometry(Geometry geometry) {
        mGeometry = geometry;
    }
}
