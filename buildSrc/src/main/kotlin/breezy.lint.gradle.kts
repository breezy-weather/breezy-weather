import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("com.diffplug.spotless")
}

val libs = the<LibrariesForLibs>()

val xmlFormatExclude = buildList(2) {
    add("**/build/**/*.xml")

    projectDir
        .resolve("src/main/res/")
        .takeIf { it.isDirectory &&
            (!it.name.startsWith("values-") ||
                it.name.endsWith("hdpi") ||
                it.name.endsWith("v29") ||
                it.name.endsWith("v31")
            )
        }?.let(::fileTree)
        ?.let(::add)
}.toTypedArray()

spotless {
    kotlin {
        target("**/*.kt", "**/*.kts")
        targetExclude(
            "**/build/**/*.kt",
            // TODO:
            "src/main/java/org/breezyweather/domain/weather/index/PollenIndex.kt",
            "src/main/java/org/breezyweather/remoteviews/config/AbstractWidgetConfigActivity.kt",
            "src/main/java/org/breezyweather/remoteviews/presenters/AbstractRemoteViewsPresenter.kt",
            "src/main/java/org/breezyweather/search/SearchActivityRepository.kt",
            "src/main/java/org/breezyweather/sources/CommonConverter.kt",
            "src/src_nonfreenet/org/breezyweather/sources/eccc/json/EcccResult.kt",
            "src/src_nonfreenet/org/breezyweather/sources/geosphereat/json/GeoSphereAtTimeseriesResult.kt",
            "src/src_nonfreenet/org/breezyweather/sources/meteoam/json/MeteoAmForecastDatasets.kt",
            "src/src_nonfreenet/org/breezyweather/sources/meteoam/json/MeteoAmForecastResult.kt",
            "src/src_nonfreenet/org/breezyweather/sources/meteoam/json/MeteoAmObservationResult.kt",
            "src/src_nonfreenet/org/breezyweather/sources/metno/json/MetNoAlertWhen.kt",
            "src/src_nonfreenet/org/breezyweather/sources/wmosevereweather/WmoSevereWeatherService.kt"
        )
        ktlint(libs.ktlint.core.get().version)
            .editorConfigOverride(
                mapOf(
                    "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                    "ktlint_standard_class-signature" to "disabled",
                    "ktlint_standard_discouraged-comment-location" to "disabled",
                    "ktlint_standard_function-expression-body" to "disabled",
                    "ktlint_standard_function-signature" to "disabled"
                )
            )
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("xml") {
        target("**/*.xml")
        targetExclude(*xmlFormatExclude)
        trimTrailingWhitespace()
        endWithNewline()
    }
}
