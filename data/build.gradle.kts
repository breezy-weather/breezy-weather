import com.android.build.api.dsl.LibraryExtension

plugins {
    id("breezy.library")
    kotlin("plugin.serialization")
    id("app.cash.sqldelight")
}

configure<LibraryExtension> {
    namespace = "breezyweather.data"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

    sqldelight {
        databases {
            create("Database") {
                packageName.set("breezyweather.data")
                dialect(libs.sqldelight.dialects.sql)
                schemaOutputDirectory.set(project.file("./src/main/sqldelight"))
            }
        }
    }
}

dependencies {
    implementation(projects.domain)
    implementation(projects.weatherUnit)

    api(libs.bundles.sqldelight)
}
