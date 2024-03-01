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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

/**
 * Extension of HashMap that provides two main features. Firstly it allows reverse lookup
 * for a key given a value, by storing a second HashMap internally which maps values to keys.
 * Secondly, it supports Collection values, in which case, each item in the collection is
 * used as a key in the internal reverse HashMap. It's therefore up to the caller to ensure
 * the overall set of values, and collection values, are unique.
 * <p>
 * Used by GeoJsonRenderer to store GeoJsonFeature instances mapped to corresponding Marker,
 * Polyline, and Polygon map objects. We want to look these up in reverse to provide access
 * to GeoJsonFeature instances when map objects are clicked.
 */
public class BiMultiMap<K> extends HashMap<K, Object> {

    private final Map<Object, K> mValuesToKeys = new HashMap<>();

    @Override
    public void putAll(Map<? extends K, ?> map) {
        // put() manages the reverse map, so call it on each entry.
        for (Entry<? extends K, ?> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Object put(K key, Object value) {
        // Store value/key in the reverse map.
        if (value instanceof Collection) {
            return put(key, (Collection) value);
        } else {
            mValuesToKeys.put(value, key);
            return super.put(key, value);
        }
    }

    public Object put(K key, Collection values) {
        // Store values/key in the reverse map.
        for (Object value : values) {
            mValuesToKeys.put(value, key);
        }
        return super.put(key, values);
    }

    @Override
    public Object remove(Object key) {
        Object value = super.remove(key);
        // Also remove the value(s) and key from the reverse map.
        if (value instanceof Collection) {
            for (Object valueItem : (Collection) value) {
                mValuesToKeys.remove(valueItem);
            }
        } else {
            mValuesToKeys.remove(value);
        }
        return value;
    }

    @Override
    public void clear() {
        super.clear();
        mValuesToKeys.clear();
    }

    @NonNull
    @SuppressWarnings("unchecked")
    @Override
    public BiMultiMap<K> clone() {
        BiMultiMap<K> cloned = new BiMultiMap<>();
        cloned.putAll((Map<K, Object>) super.clone());
        return cloned;
    }

    /**
     * Reverse lookup of key by value.
     *
     * @param value Value to lookup
     * @return Key for the given value
     */
    public K getKey(Object value) {
        return mValuesToKeys.get(value);
    }

}
