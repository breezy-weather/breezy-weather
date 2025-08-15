plugins {
    id("breezy.library")
    kotlin("android")
}

android {
    namespace = "org.breezyweather.unit"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(libs.annotation.jvm)
    implementation(libs.core.ktx)
}
