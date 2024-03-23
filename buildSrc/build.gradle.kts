plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.android.gradle)
    implementation(libs.kotlin.gradle)
    implementation(libs.ktlint)
    implementation(gradleApi())
    implementation(libs.javapoet)
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
}