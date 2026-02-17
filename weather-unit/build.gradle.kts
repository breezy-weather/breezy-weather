import com.android.build.api.dsl.LibraryExtension

plugins {
    id("breezy.library")
}

configure<LibraryExtension> {
    namespace = "org.breezyweather.unit"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(libs.annotation.jvm)
    implementation(libs.core.ktx)

    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.junit.platform)
}
