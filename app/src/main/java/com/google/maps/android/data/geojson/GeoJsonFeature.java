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
package com.google.maps.android.data.geojson;

import com.google.maps.android.model.LatLngBounds;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.Geometry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import androidx.annotation.NonNull;

/**
 * A GeoJsonFeature has a geometry, bounding box, id and set of properties. Styles are also stored
 * in this class.
 */
public class GeoJsonFeature extends Feature implements Observer {

    private final LatLngBounds mBoundingBox;

    /**
     * Creates a new GeoJsonFeature object
     *
     * @param geometry    type of geometry to assign to the feature
     * @param id          common identifier of the feature
     * @param properties  hashmap of containing properties related to the feature
     * @param boundingBox bounding box of the feature
     */
    public GeoJsonFeature(Geometry geometry, String id,
                          HashMap<String, String> properties, LatLngBounds boundingBox) {
        super(geometry, id, properties);
        mId = id;
        mBoundingBox = boundingBox;
    }

    /**
     * Store a new property key and value
     *
     * @param property      key of the property to store
     * @param propertyValue value of the property to store
     * @return previous value with the same key, otherwise null if the key didn't exist
     */
    public String setProperty(String property, String propertyValue) {
        return super.setProperty(property, propertyValue);
    }

    /**
     * Removes a given property
     *
     * @param property key of the property to remove
     * @return value of the removed property or null if there was no corresponding key
     */
    public String removeProperty(String property) {
        return super.removeProperty(property);
    }

    /**
     * Checks whether the new style that was set requires the feature to be redrawn. If the
     * geometry and the style that was set match, then the feature is redrawn.
     *
     * @param style style to check if a redraw is needed
     */
    private void checkRedrawFeature(GeoJsonStyle style) {
        if (hasGeometry() && Arrays.asList(style.getGeometryType())
                .contains(getGeometry().getGeometryType())) {
            // Don't redraw objects that aren't on the map
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Sets the stored Geometry and redraws it on the layer if it has already been added
     *
     * @param geometry Geometry to set
     */
    public void setGeometry(Geometry geometry) {
        super.setGeometry(geometry);
        setChanged();
        notifyObservers();
    }

    /**
     * Gets the array containing the coordinates of the bounding box for the feature. If
     * the feature did not have a bounding box then null will be returned.
     *
     * @return LatLngBounds containing bounding box of the feature, null if no bounding box
     */
    public LatLngBounds getBoundingBox() {
        return mBoundingBox;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Feature{");
        sb.append("\n bounding box=").append(mBoundingBox);
        sb.append(",\n geometry=").append(getGeometry());
        sb.append(",\n id=").append(mId);
        sb.append(",\n properties=").append(getProperties());
        sb.append("\n}\n");
        return sb.toString();
    }

    /**
     * Update is called if the developer modifies a style that is stored in this feature
     *
     * @param observable GeoJsonStyle object
     * @param data       null, no extra argument is passed through the notifyObservers method
     */
    @Override
    public void update(Observable observable, Object data) {
        if (observable instanceof GeoJsonStyle) {
            checkRedrawFeature((GeoJsonStyle) observable);
        }
    }
}
