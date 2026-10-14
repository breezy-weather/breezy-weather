package breezy.buildlogic

import org.gradle.api.JavaVersion as GradleJavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget as KotlinJvmTarget

object AndroidConfig {
    const val COMPILE_SDK = 37
    const val MIN_SDK = 24
    const val TARGET_SDK = 37
    const val BUILD_TOOLS = "37.0.0"

    // https://youtrack.jetbrains.com/issue/KT-66995/JvmTarget-and-JavaVersion-compatibility-for-easier-JVM-version-setup
    val JavaVersion = GradleJavaVersion.VERSION_11
    val JvmTarget = KotlinJvmTarget.JVM_11
}
