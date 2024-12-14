package breezy.buildlogic

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.TaskContainerScope
import org.json.JSONObject

/**
 * Performs a few corrections and optimize by removing unused properties
 * on the original Natural Earth file already converted to JSON
 *
 * TODO: Make this task convert shapefile to json instead of relying on external tools
 * TODO: Add missing Penghu and Matsu islands to Taiwan geometry as they are supported by the Taiwanese CWA source
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
                    if (features is JSONObject && features.has("properties")) {
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
                                    if (!k.startsWith("NAME_") && k != "ISO_A2") {
                                        propertiesToRemove.add(k)
                                    }
                                }
                                propertiesToRemove.forEach { p ->
                                    properties.remove(p)
                                }
                                properties
                            }
                        )
                    } else {
                        features
                    }
                }
            )

            val localeFile = file("$projectDir/src/main/res/raw/ne_50m_admin_0_countries.json")
            localeFile.parentFile.mkdirs()
            localeFile.writeText(json.toString())
        }
    }
}
