/*
 * Copyright 2020 Google Inc.
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

import android.util.Log
import com.google.maps.android.data.Geometry
import com.google.maps.android.model.LatLng
import com.google.maps.android.model.LatLngBounds
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Parses a JSONObject and places data into their appropriate GeoJsonFeature objects. Returns an
 * array of
 * GeoJsonFeature objects parsed from the GeoJSON file.
 */
class GeoJsonParser(
    private val mGeoJsonFile: JSONObject,
) {
    /**
     * Gets the array of GeoJsonFeature objects
     *
     * @return array of GeoJsonFeatures
     */
    val features = mutableListOf<GeoJsonFeature>()
    private var mBoundingBox: LatLngBounds? = null

    /**
     * Internal helper class to store latLng and altitude in a single object.
     * This allows to parse [lon,lat,altitude] tuples in GeoJson files more efficiently.
     * Note that altitudes are generally optional so they can be null.
     */
    private class LatLngAlt internal constructor(val latLng: LatLng, val altitude: Double?)

    /**
     * Creates a new GeoJsonParser
     *
     * @param geoJsonFile GeoJSON file to parse
     */
    init {
        parseGeoJson()
    }

    /**
     * Parses the GeoJSON file by type and adds the generated GeoJsonFeature objects to the
     * mFeatures array. Supported GeoJSON types include feature, feature collection and geometry.
     */
    private fun parseGeoJson() {
        try {
            val feature: GeoJsonFeature?
            val type = mGeoJsonFile.getString("type")
            if (type == FEATURE) {
                feature = parseFeature(mGeoJsonFile)
                if (feature != null) {
                    features.add(feature)
                }
            } else if (type == FEATURE_COLLECTION) {
                features.addAll(parseFeatureCollection(mGeoJsonFile))
            } else if (isGeometry(type)) {
                feature = parseGeometryToFeature(mGeoJsonFile)
                if (feature != null) {
                    // Don't add null features
                    features.add(feature)
                }
            } else {
                Log.w(LOG_TAG, "GeoJSON file could not be parsed.")
            }
        } catch (e: JSONException) {
            Log.w(LOG_TAG, "GeoJSON file could not be parsed.")
        }
    }

    /**
     * Parses the array of GeoJSON features in a given GeoJSON feature collection. Also parses the
     * bounding box member of the feature collection if it exists.
     *
     * @param geoJsonFeatureCollection feature collection to parse
     * @return array of GeoJsonFeature objects
     */
    private fun parseFeatureCollection(geoJsonFeatureCollection: JSONObject): List<GeoJsonFeature> {
        val geoJsonFeatures: JSONArray
        val features = mutableListOf<GeoJsonFeature>()
        try {
            geoJsonFeatures = geoJsonFeatureCollection.getJSONArray(FEATURE_COLLECTION_ARRAY)
            if (geoJsonFeatureCollection.has(BOUNDING_BOX)) {
                mBoundingBox = parseBoundingBox(
                    geoJsonFeatureCollection.getJSONArray(BOUNDING_BOX)
                )
            }
        } catch (e: JSONException) {
            Log.w(LOG_TAG, "Feature Collection could not be created.")
            return features
        }
        for (i in 0 until geoJsonFeatures.length()) {
            try {
                val feature = geoJsonFeatures.getJSONObject(i)
                if (feature.getString("type") == FEATURE) {
                    val parsedFeature = parseFeature(feature)
                    if (parsedFeature != null) {
                        // Don't add null features
                        features.add(parsedFeature)
                    } else {
                        Log.w(
                            LOG_TAG,
                            "Index of Feature in Feature Collection that could not be created: $i"
                        )
                    }
                }
            } catch (e: JSONException) {
                Log.w(
                    LOG_TAG,
                    "Index of Feature in Feature Collection that could not be created: $i"
                )
            }
        }
        return features
    }

    companion object {
        private const val LOG_TAG = "GeoJsonParser"

        // Feature object type
        private const val FEATURE = "Feature"

        // Feature object geometry member
        private const val FEATURE_GEOMETRY = "geometry"

        // Feature object id member
        private const val FEATURE_ID = "id"

        // FeatureCollection type
        private const val FEATURE_COLLECTION = "FeatureCollection"

        // FeatureCollection features array member
        private const val FEATURE_COLLECTION_ARRAY = "features"

        // Geometry coordinates member
        private const val GEOMETRY_COORDINATES_ARRAY = "coordinates"

        // GeometryCollection type
        private const val GEOMETRY_COLLECTION = "GeometryCollection"

        // GeometryCollection geometries array member
        private const val GEOMETRY_COLLECTION_ARRAY = "geometries"

        // Coordinates for bbox
        private const val BOUNDING_BOX = "bbox"
        private const val PROPERTIES = "properties"
        private const val POINT = "Point"
        private const val MULTIPOINT = "MultiPoint"
        private const val LINESTRING = "LineString"
        private const val MULTILINESTRING = "MultiLineString"
        private const val POLYGON = "Polygon"
        private const val MULTIPOLYGON = "MultiPolygon"
        private fun isGeometry(type: String): Boolean {
            return type.matches(
                (
                    POINT + "|" + MULTIPOINT + "|" + LINESTRING + "|" + MULTILINESTRING +
                        "|" + POLYGON + "|" + MULTIPOLYGON + "|" + GEOMETRY_COLLECTION
                    ).toRegex()
            )
        }

        /**
         * Parses a single GeoJSON feature which contains a geometry and properties member both of
         * which can be null. Also parses the bounding box and id members of the feature if they exist.
         *
         * @param geoJsonFeature feature to parse
         * @return GeoJsonFeature object
         */
        private fun parseFeature(geoJsonFeature: JSONObject): GeoJsonFeature? {
            var id: String? = null
            var boundingBox: LatLngBounds? = null
            var geometry: Geometry<*>? = null
            var properties = HashMap<String, String?>()
            try {
                if (geoJsonFeature.has(FEATURE_ID)) {
                    id = geoJsonFeature.getString(FEATURE_ID)
                }
                if (geoJsonFeature.has(BOUNDING_BOX)) {
                    boundingBox = parseBoundingBox(geoJsonFeature.getJSONArray(BOUNDING_BOX))
                }
                if (geoJsonFeature.has(FEATURE_GEOMETRY) && !geoJsonFeature.isNull(FEATURE_GEOMETRY)) {
                    geometry = parseGeometry(geoJsonFeature.getJSONObject(FEATURE_GEOMETRY))
                }
                if (geoJsonFeature.has(PROPERTIES) && !geoJsonFeature.isNull(PROPERTIES)) {
                    properties = parseProperties(geoJsonFeature.getJSONObject("properties"))
                }
            } catch (e: JSONException) {
                Log.w(LOG_TAG, "Feature could not be successfully parsed $geoJsonFeature")
                return null
            }
            return GeoJsonFeature(geometry, id, properties, boundingBox)
        }

        /**
         * Parses a bounding box given as a JSONArray of 4 elements in the order of lowest values for
         * all axes followed by highest values. Axes order of a bounding box follows the axes order of
         * geometries.
         *
         * @param coordinates array of 4 coordinates
         * @return LatLngBounds containing the coordinates of the bounding box
         * @throws JSONException if the bounding box could not be parsed
         */
        @Throws(JSONException::class)
        private fun parseBoundingBox(coordinates: JSONArray): LatLngBounds {
            // Lowest values for all axes
            val southWestCorner = LatLng(coordinates.getDouble(1), coordinates.getDouble(0))
            // Highest value for all axes
            val northEastCorner = LatLng(coordinates.getDouble(3), coordinates.getDouble(2))
            return LatLngBounds(southWestCorner, northEastCorner)
        }

        /**
         * Parses a single GeoJSON geometry object containing a coordinates array or a geometries array
         * if it has type GeometryCollection. FeatureCollections, styles, bounding boxes, and properties
         * are not processed by this method. If you want to parse GeoJSON including FeatureCollections,
         * styles, bounding boxes, and properties into an array of [GeoJsonFeature]s then
         * instantiate [GeoJsonParser] and call [GeoJsonParser.getFeatures].
         *
         * @param geoJsonGeometry geometry object to parse
         * @return Geometry object
         */
        fun parseGeometry(geoJsonGeometry: JSONObject): Geometry<*>? {
            return try {
                val geometryType = geoJsonGeometry.getString("type")
                val geometryArray: JSONArray
                geometryArray = if (geometryType == GEOMETRY_COLLECTION) {
                    // GeometryCollection
                    geoJsonGeometry.getJSONArray(GEOMETRY_COLLECTION_ARRAY)
                } else if (isGeometry(geometryType)) {
                    geoJsonGeometry.getJSONArray(GEOMETRY_COORDINATES_ARRAY)
                } else {
                    // No geometries or coordinates array
                    return null
                }
                createGeometry(geometryType, geometryArray)
            } catch (e: JSONException) {
                null
            }
        }

        /**
         * Converts a Geometry object into a GeoJsonFeature object. A geometry object has no ID,
         * properties or bounding box so it is set to null.
         *
         * @param geoJsonGeometry Geometry object to convert into a Feature object
         * @return new Feature object
         */
        private fun parseGeometryToFeature(geoJsonGeometry: JSONObject): GeoJsonFeature? {
            val geometry = parseGeometry(geoJsonGeometry)
            if (geometry != null) {
                return GeoJsonFeature(geometry, null, HashMap(), null)
            }
            Log.w(LOG_TAG, "Geometry could not be parsed")
            return null
        }

        /**
         * Parses the properties of a GeoJSON feature into a hashmap
         *
         * @param properties GeoJSON properties member
         * @return hashmap containing property values
         * @throws JSONException if the properties could not be parsed
         */
        @Throws(JSONException::class)
        private fun parseProperties(properties: JSONObject): HashMap<String, String?> {
            val propertiesMap = HashMap<String, String?>()
            val propertyKeys: Iterator<*> = properties.keys()
            while (propertyKeys.hasNext()) {
                val key = propertyKeys.next() as String
                propertiesMap[key] = if (properties.isNull(key)) null else properties.getString(key)
            }
            return propertiesMap
        }

        /**
         * Creates a Geometry object from the given type of geometry and its coordinates or
         * geometries array
         *
         * @param geometryType  type of geometry
         * @param geometryArray coordinates or geometries of the geometry
         * @return Geometry object
         * @throws JSONException if the coordinates or geometries could be parsed
         */
        @Throws(JSONException::class)
        private fun createGeometry(geometryType: String, geometryArray: JSONArray): Geometry<*>? {
            when (geometryType) {
                POINT -> return createPoint(geometryArray)
                MULTIPOINT -> return createMultiPoint(geometryArray)
                LINESTRING -> return createLineString(geometryArray)
                MULTILINESTRING -> return createMultiLineString(geometryArray)
                POLYGON -> return createPolygon(geometryArray)
                MULTIPOLYGON -> return createMultiPolygon(geometryArray)
                GEOMETRY_COLLECTION -> return createGeometryCollection(geometryArray)
            }
            return null
        }

        /**
         * Creates a new GeoJsonPoint object
         *
         * @param coordinates array containing the coordinates for the GeoJsonPoint
         * @return GeoJsonPoint object
         * @throws JSONException if coordinates cannot be parsed
         */
        @Throws(JSONException::class)
        private fun createPoint(coordinates: JSONArray): GeoJsonPoint {
            val latLngAlt = parseCoordinate(coordinates)
            return GeoJsonPoint(latLngAlt.latLng)
        }

        /**
         * Creates a new GeoJsonMultiPoint object containing an array of GeoJsonPoint objects
         *
         * @param coordinates array containing the coordinates for the GeoJsonMultiPoint
         * @return GeoJsonMultiPoint object
         * @throws JSONException if coordinates cannot be parsed
         */
        @Throws(JSONException::class)
        private fun createMultiPoint(coordinates: JSONArray): GeoJsonMultiPoint {
            val geoJsonPoints = mutableListOf<GeoJsonPoint>()
            for (i in 0 until coordinates.length()) {
                geoJsonPoints.add(createPoint(coordinates.getJSONArray(i)))
            }
            return GeoJsonMultiPoint(geoJsonPoints)
        }

        /**
         * Creates a new GeoJsonLineString object
         *
         * @param coordinates array containing the coordinates for the GeoJsonLineString
         * @return GeoJsonLineString object
         * @throws JSONException if coordinates cannot be parsed
         */
        @Throws(JSONException::class)
        private fun createLineString(coordinates: JSONArray): GeoJsonLineString {
            val latLngAlts = parseCoordinatesArray(coordinates)
            val latLngs = mutableListOf<LatLng>()
            for (latLngAlt in latLngAlts) {
                latLngs.add(latLngAlt.latLng)
            }
            return GeoJsonLineString(latLngs)
        }

        /**
         * Creates a new GeoJsonMultiLineString object containing an array of GeoJsonLineString objects
         *
         * @param coordinates array containing the coordinates for the GeoJsonMultiLineString
         * @return GeoJsonMultiLineString object
         * @throws JSONException if coordinates cannot be parsed
         */
        @Throws(JSONException::class)
        private fun createMultiLineString(coordinates: JSONArray): GeoJsonMultiLineString {
            val geoJsonLineStrings = mutableListOf<GeoJsonLineString>()
            for (i in 0 until coordinates.length()) {
                geoJsonLineStrings.add(createLineString(coordinates.getJSONArray(i)))
            }
            return GeoJsonMultiLineString(geoJsonLineStrings)
        }

        /**
         * Creates a new GeoJsonPolygon object
         *
         * @param coordinates array containing the coordinates for the GeoJsonPolygon
         * @return GeoJsonPolygon object
         * @throws JSONException if coordinates cannot be parsed
         */
        @Throws(JSONException::class)
        private fun createPolygon(coordinates: JSONArray): GeoJsonPolygon {
            return GeoJsonPolygon(parseCoordinatesArrays(coordinates))
        }

        /**
         * Creates a new GeoJsonMultiPolygon object containing an array of GeoJsonPolygon objects
         *
         * @param coordinates array containing the coordinates for the GeoJsonMultiPolygon
         * @return GeoJsonPolygon object
         * @throws JSONException if coordinates cannot be parsed
         */
        @Throws(JSONException::class)
        private fun createMultiPolygon(coordinates: JSONArray): GeoJsonMultiPolygon {
            val geoJsonPolygons = mutableListOf<GeoJsonPolygon>()
            for (i in 0 until coordinates.length()) {
                geoJsonPolygons.add(createPolygon(coordinates.getJSONArray(i)))
            }
            return GeoJsonMultiPolygon(geoJsonPolygons)
        }

        /**
         * Creates a new GeoJsonGeometryCollection object containing an array of Geometry
         * objects
         *
         * @param geometries array containing the geometries for the GeoJsonGeometryCollection
         * @return GeoJsonGeometryCollection object
         * @throws JSONException if geometries cannot be parsed
         */
        @Throws(JSONException::class)
        private fun createGeometryCollection(geometries: JSONArray): GeoJsonGeometryCollection {
            val geometryCollectionElements = mutableListOf<Geometry<*>>()
            for (i in 0 until geometries.length()) {
                val geometryElement = geometries.getJSONObject(i)
                val geometry = parseGeometry(geometryElement)
                if (geometry != null) {
                    // Do not add geometries that could not be parsed
                    geometryCollectionElements.add(geometry)
                }
            }
            return GeoJsonGeometryCollection(geometryCollectionElements)
        }

        /**
         * Parses an array containing a coordinate into a LatLngAlt object
         *
         * @param coordinates array containing the GeoJSON coordinate
         * @return LatLngAlt object
         * @throws JSONException if coordinate cannot be parsed
         */
        @Throws(JSONException::class)
        private fun parseCoordinate(coordinates: JSONArray): LatLngAlt {
            // GeoJSON stores coordinates as Lng, Lat so we need to reverse
            val latLng = LatLng(coordinates.getDouble(1), coordinates.getDouble(0))
            val altitude = if (coordinates.length() < 3) null else coordinates.getDouble(2)
            return LatLngAlt(latLng, altitude)
        }

        /**
         * Parses an array containing coordinates into a List of LatLng objects
         *
         * @param coordinates array containing the GeoJSON coordinates
         * @return List of LatLng objects
         * @throws JSONException if coordinates cannot be parsed
         */
        @Throws(JSONException::class)
        private fun parseCoordinatesArray(coordinates: JSONArray): List<LatLngAlt> {
            val coordinatesArray = mutableListOf<LatLngAlt>()
            for (i in 0 until coordinates.length()) {
                coordinatesArray.add(parseCoordinate(coordinates.getJSONArray(i)))
            }
            return coordinatesArray
        }

        /**
         * Parses an array of arrays containing coordinates into a List of a List of LatLng
         * objects
         *
         * @param coordinates array of an array containing the GeoJSON coordinates
         * @return List of a List of LatLng objects
         * @throws JSONException if coordinates cannot be parsed
         */
        @Throws(JSONException::class)
        private fun parseCoordinatesArrays(coordinates: JSONArray): List<List<LatLng>> {
            val coordinatesArray = mutableListOf<MutableList<LatLng>>()
            for (i in 0 until coordinates.length()) {
                val latLngAlts = parseCoordinatesArray(coordinates.getJSONArray(i))
                // this method is called for polygons, which do not have altitude values
                val latLngs = mutableListOf<LatLng>()
                for (latLngAlt in latLngAlts) {
                    latLngs.add(latLngAlt.latLng)
                }
                coordinatesArray.add(latLngs)
            }
            return coordinatesArray
        }
    }
}
