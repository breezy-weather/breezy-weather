buildscript {
    dependencies {
        classpath(libs.aboutLibraries.gradle)
        classpath(libs.dagger.hilt.gradle)
        classpath(libs.sqldelight.gradle)
    }
}

plugins {
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.aboutLibraries) apply false
    alias(libs.plugins.sqldelight) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
