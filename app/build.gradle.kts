@file:Suppress("ChromeOsAbiSupport")

import breezy.buildlogic.Config
import breezy.buildlogic.getCommitCount
import breezy.buildlogic.getGitSha
import breezy.buildlogic.registerLocalesConfigTask
import java.util.Properties

plugins {
    id("breezy.android.application")
    id("breezy.android.application.compose")
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    kotlin("plugin.serialization")
    id("com.mikepenz.aboutlibraries.plugin.android")
}

val supportedAbi = setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")

android {
    namespace = "org.breezyweather"

    defaultConfig {
        applicationId = "org.breezyweather"
        versionCode = 60103
        versionName = "6.1.3"

        buildConfigField("String", "COMMIT_COUNT", "\"${getCommitCount()}\"")
        buildConfigField("String", "COMMIT_SHA", "\"${getGitSha()}\"")
        buildConfigField("boolean", "IS_BREEZY", "${Config.isBreezy}")

        multiDexEnabled = true
        ndk {
            abiFilters += supportedAbi
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables.useSupportLibrary = true
    }

    splits {
        abi {
            isEnable = true
            reset()
            include(*supportedAbi.toTypedArray())
            isUniversalApk = true
        }
    }

    buildTypes {
        named("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-r${getCommitCount()}"
        }
        named("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            isDebuggable = false
            isCrunchPngs = false // No need to do that, we already optimized them
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    val localProperties = Properties()
    if (project.rootProject.file("local.properties").canRead()) {
        localProperties.load(project.rootProject.file("local.properties").inputStream())
    }
    val globalProperties = Properties()
    if (project.rootProject.file("gradle.properties").canRead()) {
        globalProperties.load(project.rootProject.file("gradle.properties").inputStream())
    }
    buildTypes.forEach { it ->
        it.buildConfigField(
            "String",
            "REPORT_ISSUE",
            if (Config.isBreezy) {
                globalProperties.getProperty("breezy.report_issue")?.let { prop -> "\"$prop\"" }
                    ?: run {
                        logger.warn(
                            "Missing breezy.report_issue property! Some sources may not work without it. Please set this property in gradle.properties to a link or an email where issues can be reported."
                        )
                        "\"\""
                    }
            } else {
                globalProperties.getProperty("app.report_issue")?.let { prop -> "\"$prop\"" }
                    ?: run {
                        logger.warn(
                            "Missing app.report_issue property! Some sources may not work without it. Please set this property in gradle.properties to a link or an email where issues can be reported."
                        )
                        "\"\""
                    }
            }
        )
        it.buildConfigField(
            "String",
            "SOURCE_CODE_LINK",
            if (Config.isBreezy) {
                globalProperties.getProperty("breezy.source_code_link")?.takeIf { it.startsWith("https://") }
                    ?.let { prop -> "\"$prop\"" }
                    ?: run {
                        logger.error(
                            "Missing breezy.source_code_link property! Please set this property in gradle.properties to a link where the source code of your app can be viewed"
                        )
                        "\"\""
                    }
            } else {
                globalProperties.getProperty("app.source_code_link")?.takeIf { it.startsWith("https://") }
                    ?.let { prop -> "\"$prop\"" }
                    ?: run {
                        logger.error(
                            "Missing app.source_code_link property! Please set this property in gradle.properties to a link where the source code of your app can be viewed"
                        )
                        "\"\""
                    }
            }
        )
        it.buildConfigField(
            "String",
            "RELEASES_LINK",
            if (Config.isBreezy) {
                "\"${globalProperties.getProperty("breezy.releases_link") ?: ""}\""
            } else {
                "\"${globalProperties.getProperty("app.releases_link") ?: ""}\""
            }
        )
        it.buildConfigField(
            "String",
            "INSTALL_INSTRUCTIONS_LINK",
            if (Config.isBreezy) {
                globalProperties.getProperty("breezy.install_instructions_link")?.takeIf { it.startsWith("https://") }
                    ?.let { prop -> "\"$prop\"" }
                    ?: run {
                        logger.warn(
                            "Missing breezy.install_instructions_link property! Please set this property in gradle.properties to a link where installation instructions can be viewed"
                        )
                        "\"\""
                    }
            } else {
                globalProperties.getProperty("app.install_instructions_link")?.takeIf { it.startsWith("https://") }
                    ?.let { prop -> "\"$prop\"" }
                    ?: run {
                        logger.warn(
                            "Missing app.install_instructions_link property! Please set this property in gradle.properties to a link where installation instructions can be viewed"
                        )
                        "\"\""
                    }
            }
        )
        it.buildConfigField(
            "String",
            "ICON_PACKS_LINK",
            if (Config.isBreezy) {
                "\"${globalProperties.getProperty("breezy.icon_packs_link") ?: ""}\""
            } else {
                "\"${globalProperties.getProperty("app.icon_packs_link") ?: ""}\""
            }
        )
        it.buildConfigField(
            "String",
            "PRIVACY_POLICY_LINK",
            if (Config.isBreezy) {
                globalProperties.getProperty("breezy.privacy_policy_link")?.takeIf { it.startsWith("https://") }
                    ?.let { prop -> "\"$prop\"" }
                    ?: run {
                        logger.error(
                            "Missing breezy.privacy_policy_link property! Please set this property in gradle.properties to a link where the privacy policy of your app can be viewed"
                        )
                        "\"\""
                    }
            } else {
                globalProperties.getProperty("app.privacy_policy_link")?.takeIf { it.startsWith("https://") }
                    ?.let { prop -> "\"$prop\"" }
                    ?: run {
                        logger.error(
                            "Missing app.privacy_policy_link property! Please set this property in gradle.properties to a link where the privacy policy of your app can be viewed"
                        )
                        "\"\""
                    }
            }
        )
        it.buildConfigField(
            "String",
            "CONTACT_MATRIX",
            if (Config.isBreezy) {
                "\"${globalProperties.getProperty("breezy.matrix_link") ?: ""}\""
            } else {
                "\"${globalProperties.getProperty("app.matrix_link") ?: ""}\""
            }
        )
        it.buildConfigField(
            "String",
            "GITHUB_ORG",
            if (Config.isBreezy) {
                "\"${globalProperties.getProperty("breezy.github.org") ?: ""}\""
            } else {
                "\"${globalProperties.getProperty("app.github.org") ?: ""}\""
            }
        )
        it.buildConfigField(
            "String",
            "GITHUB_REPO",
            if (Config.isBreezy) {
                "\"${globalProperties.getProperty("breezy.github.repo") ?: ""}\""
            } else {
                "\"${globalProperties.getProperty("app.github.repo") ?: ""}\""
            }
        )
        it.buildConfigField(
            "String",
            "GITHUB_RELEASE_PREFIX",
            if (Config.isBreezy) {
                "\"${globalProperties.getProperty("breezy.github.release_prefix") ?: ""}\""
            } else {
                "\"${globalProperties.getProperty("app.github.release_prefix") ?: ""}\""
            }
        )
        it.buildConfigField(
            "String",
            "DEFAULT_LOCATION_SOURCE",
            "\"${localProperties.getProperty("breezy.source.default_location") ?: "native"}\""
        )
        it.buildConfigField(
            "String",
            "DEFAULT_LOCATION_SEARCH_SOURCE",
            "\"${localProperties.getProperty("breezy.source.default_location_search") ?: "openmeteo"}\""
        )
        it.buildConfigField(
            "String",
            "DEFAULT_GEOCODING_SOURCE",
            "\"${localProperties.getProperty("breezy.source.default_geocoding") ?: "naturalearth"}\""
        )
        it.buildConfigField(
            "String",
            "DEFAULT_FORECAST_SOURCE",
            "\"${localProperties.getProperty("breezy.source.default_weather") ?: "auto"}\""
        )
        it.buildConfigField(
            "String",
            "ACCU_WEATHER_KEY",
            "\"${localProperties.getProperty("breezy.accu.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "AEMET_KEY",
            "\"${localProperties.getProperty("breezy.aemet.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "ATMO_AURA_KEY",
            "\"${localProperties.getProperty("breezy.atmoaura.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "ATMO_FRANCE_KEY",
            "\"${localProperties.getProperty("breezy.atmofrance.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "ATMO_GRAND_EST_KEY",
            "\"${localProperties.getProperty("breezy.atmograndest.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "ATMO_HDF_KEY",
            "\"${localProperties.getProperty("breezy.atmohdf.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "ATMO_SUD_KEY",
            "\"${localProperties.getProperty("breezy.atmosud.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "BAIDU_IP_LOCATION_AK",
            "\"${localProperties.getProperty("breezy.baiduip.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "BMKG_KEY",
            "\"${localProperties.getProperty("breezy.bmkg.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "CWA_KEY",
            "\"${localProperties.getProperty("breezy.cwa.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "ECCC_KEY",
            "\"${localProperties.getProperty("breezy.eccc.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "GEO_NAMES_KEY",
            "\"${localProperties.getProperty("breezy.geonames.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "MET_IE_KEY",
            "\"${localProperties.getProperty("breezy.metie.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "MET_OFFICE_KEY",
            "\"${localProperties.getProperty("breezy.metoffice.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "MF_WSFT_JWT_KEY",
            "\"${localProperties.getProperty("breezy.mf.jwtKey") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "MF_WSFT_KEY",
            "\"${localProperties.getProperty("breezy.mf.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "OPEN_WEATHER_KEY",
            "\"${localProperties.getProperty("breezy.openweather.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "PIRATE_WEATHER_KEY",
            "\"${localProperties.getProperty("breezy.pirateweather.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "POLLENINFO_KEY",
            "\"${localProperties.getProperty("breezy.polleninfo.key") ?: ""}\""
        )
    }

    flavorDimensions.add("default")

    productFlavors {
        create("basic") {
            dimension = "default"
        }
        create("freenet") {
            dimension = "default"
            versionNameSuffix = "_freenet"
        }
    }

    sourceSets {
        getByName("basic") {
            java.srcDirs("src/src_nonfreenet")
            res.directories += "src/res_nonfreenet"
            if (Config.isBreezy) {
                res.directories += "src/res_breezy"
            } else {
                res.directories += "src/res_fork"
            }
        }
        getByName("freenet") {
            java.srcDirs("src/src_freenet")
            res.directories += "src/res_freenet"
            if (Config.isBreezy) {
                res.directories += "src/res_breezy"
            } else {
                res.directories += "src/res_fork"
            }
        }
    }

    packaging {
        resources.excludes.addAll(
            listOf(
                "kotlin-tooling-metadata.json",
                "LICENSE.txt",
                "META-INF/versions/9/OSGI-INF/MANIFEST.MF",
                "META-INF/**/*.properties",
                "META-INF/**/LICENSE.txt",
                "META-INF/*.properties",
                "META-INF/*.version",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/NOTICE",
                "META-INF/README.md"
            )
        )
    }

    dependenciesInfo {
        includeInApk = false
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true

        // Disable some unused things
        aidl = false
        renderScript = false
        shaders = false
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
        disable.addAll(listOf("MissingTranslation", "ExtraTranslation"))
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            unitTests.isReturnDefaultValues = true
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
            "-opt-in=com.google.accompanist.permissions.ExperimentalPermissionsApi",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-Xannotation-default-target=param-property"
        )
    }
}

aboutLibraries {
    offlineMode = true

    collect {
        // Define the path configuration files are located in. E.g. additional libraries, licenses to add to the target .json
        // Warning: Please do not use the parent folder of a module as path, as this can result in issues. More details: https://github.com/mikepenz/AboutLibraries/issues/936
        // The path provided is relative to the modules path (not project root)
        configPath = if (Config.isBreezy) {
            file("../config")
        } else {
            file("../config-fork") // TODO: Find a way to avoid duplicating files
        }
    }

    export {
        // Remove the "generated" timestamp to allow for reproducible builds
        excludeFields.add("generated")
    }
}

dependencies {
    implementation(projects.data)
    implementation(projects.domain)
    implementation(projects.mapsUtils)
    implementation(projects.uiWeatherView)
    implementation(projects.weatherUnit)
    implementation(libs.breezy.datasharing.lib)

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.core.splashscreen)

    implementation(libs.cardview)
    implementation(libs.swiperefreshlayout)

    implementation(platform(libs.compose.bom))
    implementation(libs.activity.compose)
    implementation(libs.compose.material.ripple)
    implementation(libs.compose.animation)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.util)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.navigation.compose)
    lintChecks(libs.compose.lint.checks)

    implementation(libs.accompanist.permissions)

    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.junit.platform)

    // preference.
    implementation(libs.preference.ktx)

    // db
    implementation(libs.bundles.sqlite)

    // work.
    implementation(libs.work.runtime)

    // lifecycle.
    implementation(libs.bundles.lifecycle)
    implementation(libs.recyclerview)

    // hilt.
    ksp(libs.kotlin.metadata.jvm)
    implementation(libs.dagger.hilt.core)
    ksp(libs.dagger.hilt.compiler)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler)

    // HTTP
    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.okhttp)
    // implementation(libs.kotlinx.serialization.csv) // Can be reenabled if needed (see also HttpModule.kt)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.xml.core)
    implementation(libs.kotlinx.serialization.xml)

    // data store
    // implementation(libs.datastore)

    // jwt - Only used by MF at the moment
    "basicImplementation"(libs.jjwt.api)
    "basicRuntimeOnly"(libs.jjwt.impl)
    "basicRuntimeOnly"(libs.jjwt.orgjson) {
        exclude("org.json", "json") // provided by Android natively
    }

    // rx java.
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.kotlinx.coroutines.rx3)

    // ui.
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.views)
    implementation(libs.adaptiveiconview)
    implementation(libs.activity)

    // utils.
    implementation(libs.suncalc)
    implementation(libs.aboutLibraries)

    // Allows reflection of the relative time class to pass Locale as parameter
    implementation(libs.restrictionBypass)

    // debugImplementation because LeakCanary should only run in debug builds.
    // debugImplementation(libs.leakcanary)
}

tasks {
    // May be too heavy to run, so let’s keep the generated file in Git
    // val naturalEarthConfigTask = registerNaturalEarthConfigTask(project)
    val localesConfigTask = registerLocalesConfigTask(project)

    // Duplicating Hebrew string assets due to some locale code issues on different devices
    val copyHebrewStrings by registering(Copy::class) {
        from("./src/main/res/values-he")
        into("./src/main/res/values-iw")
        include("**/*")
    }

    // Duplicating Indonesian string assets due to some locale code issues on different devices
    val copyIndonesianStrings by registering(Copy::class) {
        from("./src/main/res/values-id")
        into("./src/main/res/values-in")
        include("**/*")
    }

    preBuild {
        dependsOn(
            // naturalEarthConfigTask,
            copyHebrewStrings,
            copyIndonesianStrings,
            localesConfigTask
        )
    }
}

buildscript {
    dependencies {
        classpath(libs.kotlin.gradle)
    }
}
