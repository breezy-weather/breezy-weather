package breezy.buildlogic

import org.gradle.api.JavaVersion as GradleJavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget as KotlinJvmTarget

object AndroidConfig {
    const val COMPILE_SDK = 35
    const val MIN_SDK = 21
    const val TARGET_SDK = 35

    // https://youtrack.jetbrains.com/issue/KT-66995/JvmTarget-and-JavaVersion-compatibility-for-easier-JVM-version-setup
    val JavaVersion = GradleJavaVersion.VERSION_1_8
    val JvmTarget = KotlinJvmTarget.JVM_1_8
}
