import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("com.diffplug.spotless")
}

val libs = the<LibrariesForLibs>()

val xmlFormatExclude = buildList(2) {
    add("**/build/**/*.xml")

    projectDir
        .resolve("src/main/res/")
        .takeIf {
            it.isDirectory &&
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
        targetExclude("**/build/**/*.kt")
        ktlint(libs.ktlint.core.get().version)
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
