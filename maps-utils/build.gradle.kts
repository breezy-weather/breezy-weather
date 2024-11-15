plugins {
    id("breezy.library")
    kotlin("android")
}

android {
    namespace = "com.google.maps.android"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}
