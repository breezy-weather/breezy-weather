plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.android.gradle)
    implementation(libs.kotlin.gradle)
    implementation(libs.compose.compiler.gradle)
    implementation(libs.spotless.gradle)
    implementation(gradleApi())
    implementation(libs.javapoet)
    implementation(libs.json)

    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
}
