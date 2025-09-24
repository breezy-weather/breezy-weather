@file:Suppress("ChromeOsAbiSupport")

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
    id("com.mikepenz.aboutlibraries.plugin")
}

val supportedAbi = setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")

android {
    namespace = "org.breezyweather"

    defaultConfig {
        applicationId = "org.breezyweather"
        versionCode = 60012
        versionName = "6.0.12-rc"

        buildConfigField("String", "COMMIT_COUNT", "\"${getCommitCount()}\"")
        buildConfigField("String", "COMMIT_SHA", "\"${getGitSha()}\"")

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

    val properties = Properties()
    if (project.rootProject.file("local.properties").canRead()) {
        properties.load(project.rootProject.file("local.properties").inputStream())
    }
    buildTypes.forEach {
        it.buildConfigField(
            "String",
            "DEFAULT_LOCATION_SOURCE",
            "\"${properties.getProperty("breezy.source.default_location") ?: "native"}\""
        )
        it.buildConfigField(
            "String",
            "DEFAULT_LOCATION_SEARCH_SOURCE",
            "\"${properties.getProperty("breezy.source.default_location_search") ?: "openmeteo"}\""
        )
        it.buildConfigField(
            "String",
            "DEFAULT_GEOCODING_SOURCE",
            "\"${properties.getProperty("breezy.source.default_geocoding") ?: "naturalearth"}\""
        )
        it.buildConfigField(
            "String",
            "DEFAULT_FORECAST_SOURCE",
            "\"${properties.getProperty("breezy.source.default_weather") ?: "auto"}\""
        )
        it.buildConfigField(
            "String",
            "ACCU_WEATHER_KEY",
            "\"${properties.getProperty("breezy.accu.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "AEMET_KEY",
            "\"${properties.getProperty("breezy.aemet.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "ATMO_AURA_KEY",
            "\"${properties.getProperty("breezy.atmoaura.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "ATMO_FRANCE_KEY",
            "\"${properties.getProperty("breezy.atmofrance.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "ATMO_GRAND_EST_KEY",
            "\"${properties.getProperty("breezy.atmograndest.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "ATMO_HDF_KEY",
            "\"${properties.getProperty("breezy.atmohdf.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "ATMO_SUD_KEY",
            "\"${properties.getProperty("breezy.atmosud.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "BAIDU_IP_LOCATION_AK",
            "\"${properties.getProperty("breezy.baiduip.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "BMKG_KEY",
            "\"${properties.getProperty("breezy.bmkg.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "CWA_KEY",
            "\"${properties.getProperty("breezy.cwa.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "ECCC_KEY",
            "\"${properties.getProperty("breezy.eccc.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "GEO_NAMES_KEY",
            "\"${properties.getProperty("breezy.geonames.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "HERE_KEY",
            "\"${properties.getProperty("breezy.here.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "MF_WSFT_JWT_KEY",
            "\"${properties.getProperty("breezy.mf.jwtKey") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "MF_WSFT_KEY",
            "\"${properties.getProperty("breezy.mf.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "OPEN_WEATHER_KEY",
            "\"${properties.getProperty("breezy.openweather.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "PIRATE_WEATHER_KEY",
            "\"${properties.getProperty("breezy.pirateweather.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "MET_OFFICE_KEY",
            "\"${properties.getProperty("breezy.metoffice.key") ?: ""}\""
        )
        it.buildConfigField(
            "String",
            "POLLENINFO_KEY",
            "\"${properties.getProperty("breezy.polleninfo.key") ?: ""}\""
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
            res.srcDirs("src/res_nonfreenet")
        }
        getByName("freenet") {
            java.srcDirs("src/src_freenet")
            res.srcDirs("src/res_freenet")
        }
    }

    packaging {
        resources.excludes.addAll(
            listOf(
                "META-INF/DEPENDENCIES",
                "LICENSE.txt",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/README.md",
                "META-INF/NOTICE",
                "META-INF/*.kotlin_module"
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
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
            "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-opt-in=com.google.accompanist.permissions.ExperimentalPermissionsApi"
        )
    }
}

aboutLibraries {
    offlineMode = true

    collect {
        // Define the path configuration files are located in. E.g. additional libraries, licenses to add to the target .json
        // Warning: Please do not use the parent folder of a module as path, as this can result in issues. More details: https://github.com/mikepenz/AboutLibraries/issues/936
        // The path provided is relative to the modules path (not project root)
        configPath = file("../config")
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
