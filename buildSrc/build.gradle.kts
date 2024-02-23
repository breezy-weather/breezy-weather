plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.android.gradle)
    implementation(libs.kotlin.gradle)
    implementation(gradleApi())
    implementation(libs.javapoet)
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
}