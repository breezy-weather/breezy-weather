import com.android.build.api.dsl.LibraryExtension

plugins {
    id("breezy.library")
}

configure<LibraryExtension> {
    namespace = "com.google.maps.android"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.junit.platform)
}
