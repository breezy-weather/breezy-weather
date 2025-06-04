plugins {
    id("breezy.library")
    kotlin("android")
}

android {
    namespace = "org.breezyweather.ui.theme.weatherView"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(libs.core.ktx)
}
