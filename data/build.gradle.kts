plugins {
    id("breezy.library")
    kotlin("android")
    kotlin("plugin.serialization")
    id("app.cash.sqldelight")
}

android {
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

    api(libs.bundles.sqldelight)
}
