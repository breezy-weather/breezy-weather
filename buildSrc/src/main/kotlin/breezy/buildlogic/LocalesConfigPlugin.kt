package breezy.buildlogic

/**
 * Taken from Mihon
 * Apache License, Version 2.0
 *
 * https://github.com/mihonapp/mihon/blob/290efb0283145d81290972991047064c1d905c9c/buildSrc/src/main/kotlin/LocalesConfigPlugin.kt
 */

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.TaskContainerScope

private val emptyResourcesElement = "<resources>\\s*</resources>|<resources/>".toRegex()
private val valuesPrefix = "values(-(b\\+)?)?".toRegex()

fun TaskContainerScope.registerLocalesConfigTask(project: Project): TaskProvider<Task> {
    return with(project) {
        register("generateLocalesConfig") {
            val languages = fileTree("$projectDir/src/main/res/")
                .matching { include("**/strings.xml") }
                .filterNot { it.readText().contains(emptyResourcesElement) }
                .map { it.parentFile.name }
                .sorted()
                .joinToString(separator = "\n") {
                    val language = it
                        .replace(valuesPrefix, "")
                        .replace("-r", "-")
                        .replace("+", "-")
                        .takeIf(String::isNotBlank) ?: "en"
                    "|    <locale android:name=\"$language\" />"
                }

            val content = """
            |<?xml version="1.0" encoding="utf-8"?>
            |<locale-config xmlns:android="http://schemas.android.com/apk/res/android">
            $languages
            |</locale-config>
            """.trimMargin()

            val localeFile = file("$projectDir/src/main/res/xml/locales_config.xml")
            localeFile.parentFile.mkdirs()
            localeFile.writeText(content)
        }
    }
}
