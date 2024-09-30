import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

plugins {
    id("com.android.application")
    id("com.mikepenz.aboutlibraries.plugin")
    kotlin("android")
    alias(libs.plugins.kotlinCompose)
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    kotlin("plugin.serialization")
}

val SUPPORTED_ABIS = setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")

android {
    namespace = "org.breezyweather"

    defaultConfig {
        applicationId = "org.breezyweather"
        versionCode = 50209
        versionName = "5.2.9"

        buildConfigField("String", "COMMIT_COUNT", "\"${getCommitCount()}\"")
        buildConfigField("String", "COMMIT_SHA", "\"${getGitSha()}\"")

        multiDexEnabled = true
        ndk {
            abiFilters += SUPPORTED_ABIS
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables.useSupportLibrary = true
    }

    splits {
        abi {
            isEnable = true
            reset()
            include(*SUPPORTED_ABIS.toTypedArray())
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
        it.buildConfigField("String", "DEFAULT_LOCATION_SOURCE", "\"${properties.getProperty("breezy.source.default_location") ?: "native"}\"")
        it.buildConfigField("String", "DEFAULT_LOCATION_SEARCH_SOURCE", "\"${properties.getProperty("breezy.source.default_location_search") ?: "openmeteo"}\"")
        it.buildConfigField("String", "DEFAULT_GEOCODING_SOURCE", "\"${properties.getProperty("breezy.source.default_geocoding") ?: "naturalearth"}\"")
        it.buildConfigField("String", "DEFAULT_WEATHER_SOURCE", "\"${properties.getProperty("breezy.source.default_weather") ?: "auto"}\"")
        it.buildConfigField("String", "ACCU_WEATHER_KEY", "\"${properties.getProperty("breezy.accu.key") ?: ""}\"")
        it.buildConfigField("String", "ATMO_AURA_KEY", "\"${properties.getProperty("breezy.atmoaura.key") ?: ""}\"")
        it.buildConfigField("String", "BAIDU_IP_LOCATION_AK", "\"${properties.getProperty("breezy.baiduip.key") ?: ""}\"")
        it.buildConfigField("String", "CWA_KEY", "\"${properties.getProperty("breezy.cwa.key") ?: ""}\"")
        it.buildConfigField("String", "GEO_NAMES_KEY", "\"${properties.getProperty("breezy.geonames.key") ?: ""}\"")
        it.buildConfigField("String", "HERE_KEY", "\"${properties.getProperty("breezy.here.key") ?: ""}\"")
        it.buildConfigField("String", "MF_WSFT_JWT_KEY", "\"${properties.getProperty("breezy.mf.jwtKey") ?: ""}\"")
        it.buildConfigField("String", "MF_WSFT_KEY", "\"${properties.getProperty("breezy.mf.key") ?: ""}\"")
        it.buildConfigField("String", "OPEN_WEATHER_KEY", "\"${properties.getProperty("breezy.openweather.key") ?: ""}\"")
        it.buildConfigField("String", "PIRATE_WEATHER_KEY", "\"${properties.getProperty("breezy.pirateweather.key") ?: ""}\"")
        it.buildConfigField("String", "QWEATHER_KEY", "\"${properties.getProperty("breezy.qweather.key") ?: ""}\"")
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
        }
        getByName("freenet") {
            java.srcDirs("src/src_freenet")
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
        compose = true
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

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}

aboutLibraries {
    // Define the path configuration files are located in. E.g. additional libraries, licenses to add to the target .json
    // Warning: Please do not use the parent folder of a module as path, as this can result in issues. More details: https://github.com/mikepenz/AboutLibraries/issues/936
    configPath = "config"

    // Remove the "generated" timestamp to allow for reproducible builds
    excludeFields = arrayOf("generated")
}

dependencies {
    implementation(projects.data)
    implementation(projects.domain)
    implementation(projects.mapsUtils)

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)

    implementation(libs.cardview)
    implementation(libs.swiperefreshlayout)

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
    kapt(libs.dagger.hilt.compiler)
    implementation(libs.hilt.work)
    kapt(libs.hilt.compiler)

    // HTTP
    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.okhttp)
    implementation(libs.kotlinx.serialization.json)
    //implementation(libs.kotlinx.serialization.xml)

    // data store
    //implementation(libs.datastore)

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
    implementation(libs.adaptiveiconview)
    implementation(libs.activity)
    implementation(libs.expandabletextcompose)

    // utils.
    implementation(libs.suncalc)
    implementation(libs.aboutLibraries)

    // Allows reflection of the relative time class to pass Locale as parameter
    implementation(libs.restrictionBypass)

    // debugImplementation because LeakCanary should only run in debug builds.
    //debugImplementation(libs.leakcanary)
}

tasks {
    val localesConfigTask = registerLocalesConfigTask(project)

    // Duplicating Hebrew string assets due to some locale code issues on different devices
    val copyHebrewStrings by registering(Copy::class) {
        from("./src/main/res/values-he")
        into("./src/main/res/values-iw")
        include("**/*")
    }

    preBuild {
        dependsOn(copyHebrewStrings, localesConfigTask)
    }

    withType<KotlinCompile> {
        compilerOptions.freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
        compilerOptions.freeCompilerArgs.add("-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi")
        compilerOptions.freeCompilerArgs.add("-opt-in=kotlinx.serialization.ExperimentalSerializationApi")
        compilerOptions.freeCompilerArgs.add("-opt-in=com.google.accompanist.permissions.ExperimentalPermissionsApi")
    }
}

buildscript {
    dependencies {
        classpath(libs.kotlin.gradle)
    }
}
