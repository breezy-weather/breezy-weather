package breezy.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val Project.libs get() = the<LibrariesForLibs>()

internal fun Project.configureAndroidApplication() {
    extensions.getByType(ApplicationExtension::class).apply {
        compileSdk = AndroidConfig.COMPILE_SDK
        buildToolsVersion = AndroidConfig.BUILD_TOOLS

        defaultConfig {
            minSdk = AndroidConfig.MIN_SDK
        }

        compileOptions {
            sourceCompatibility = AndroidConfig.JavaVersion
            targetCompatibility = AndroidConfig.JavaVersion
            // isCoreLibraryDesugaringEnabled = true
        }
    }

    configureKotlin()
}

internal fun Project.configureAndroidLibrary() {
    extensions.getByType(LibraryExtension::class).apply {
        compileSdk = AndroidConfig.COMPILE_SDK
        buildToolsVersion = AndroidConfig.BUILD_TOOLS

        defaultConfig {
            minSdk = AndroidConfig.MIN_SDK
        }

        compileOptions {
            sourceCompatibility = AndroidConfig.JavaVersion
            targetCompatibility = AndroidConfig.JavaVersion
            // isCoreLibraryDesugaringEnabled = true
        }
    }

    configureKotlin()
}

internal fun Project.configureKotlin() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(AndroidConfig.JvmTarget)
            freeCompilerArgs.addAll(
                "-Xcontext-receivers",
                "-opt-in=kotlin.RequiresOptIn",
            )

            // Treat all Kotlin warnings as errors (disabled by default)
            // Override by setting warningsAsErrors=true in your ~/.gradle/gradle.properties
            val warningsAsErrors: String? by project
            allWarningsAsErrors.set(warningsAsErrors.toBoolean())
        }
    }
}

internal fun Project.configureCompose() {
    pluginManager.apply(libs.plugins.compose.compiler.get().pluginId)

    extensions.getByType(ApplicationExtension::class).apply {
        buildFeatures {
            compose = true
        }
    }
}

internal fun Project.configureTest() {
    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }
    }
}
