package breezy.buildlogic

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.TaskContainerScope
import org.json.JSONArray
import org.json.JSONObject
import java.math.RoundingMode

/**
 * 4 is ~10 m
 * 5 is ~1 m
 * 6 is ~10 cm
 */
const val COORDINATES_PRECISION = 5

/**
 * Performs a few corrections and optimize by removing unused properties
 * on the original Natural Earth file already converted to JSON
 *
 * TODO: Make this task convert shapefile to json instead of relying on external tools
 */
fun TaskContainerScope.registerNaturalEarthConfigTask(project: Project): TaskProvider<Task> {
    return with(project) {
        register("generateNaturalEarthConfig") {
            val originalFileContents = file("$projectDir/work/ne_50m_admin_0_countries.json")
                .bufferedReader().use { it.readText() }
            val json = JSONObject(originalFileContents)

            json.put(
                "features",
                (json.getJSONArray("features")).map { features ->
                    if (features is JSONObject) {
                        // Geometry
                        if (features.has("geometry")) {
                            features.put(
                                "geometry",
                                features.getJSONObject("geometry").let { geometry ->
                                    if (geometry.has("type")) {
                                        when (geometry.getString("type")) {
                                            "MultiPolygon", "Polygon" -> {
                                                geometry.put(
                                                    "coordinates",
                                                    reduceDecimalPrecision(geometry.getJSONArray("coordinates"))
                                                )
                                            }
                                            // No other cases in this file
                                            else -> {}
                                        }
                                    }
                                    geometry
                                }
                            )
                        }

                        // Properties
                        if (features.has("properties")) {
                            features.put(
                                "properties",
                                features.getJSONObject("properties").let { properties ->
                                    // Fix Taiwan inexisting ISO A2 code
                                    // Fix a few countries with no code, such as France and Norway
                                    if (properties.has("ISO_A2")) {
                                        when (properties.getString("ISO_A2")) {
                                            "CN-TW" -> properties.put("ISO_A2", "TW")
                                            "-99" -> properties.put(
                                                "ISO_A2",
                                                properties.getString("ISO_A2_EH").takeIf { it != "-99" } ?: ""
                                            )
                                            else -> {}
                                        }
                                    }

                                    // Must be stored to avoid ConcurrentModificationException
                                    val propertiesToRemove = mutableSetOf(
                                        "NAME_CIAWF",
                                        "NAME_SORT",
                                        "NAME_ALT",
                                        "NAME_LEN"
                                    )
                                    properties.keys().forEach { k ->
                                        if (k != "ISO_A2") {
                                            if (!k.startsWith("NAME_")) {
                                                // Remove everything we don't need
                                                propertiesToRemove.add(k)
                                            } else if (!properties.getString("ISO_A2").isNullOrEmpty()) {
                                                // Remove every name as Android already provides it
                                                // Unless it's an unknown country (with no ISO A2)
                                                propertiesToRemove.add(k)
                                            }
                                        }
                                    }
                                    propertiesToRemove.forEach { p ->
                                        properties.remove(p)
                                    }
                                    properties
                                }
                            )
                        }
                    }
                    features
                }
            )

            val localeFile = file("$projectDir/src/main/res/raw/ne_50m_admin_0_countries.json")
            localeFile.parentFile.mkdirs()
            localeFile.writeText(json.toString())
        }
    }
}

private fun reduceDecimalPrecision(coordinates: JSONArray): JSONArray {
    if (coordinates.get(0) is Number) {
        coordinates.put(
            0,
            coordinates.getBigDecimal(0)
                .setScale(COORDINATES_PRECISION, RoundingMode.HALF_UP)
        )
        coordinates.put(
            1,
            coordinates.getBigDecimal(1)
                .setScale(COORDINATES_PRECISION, RoundingMode.HALF_UP)
        )
    }

    if (coordinates.get(0) is JSONArray) {
        coordinates.map {
            reduceDecimalPrecision(it as JSONArray)
        }
    }
    return coordinates
}
