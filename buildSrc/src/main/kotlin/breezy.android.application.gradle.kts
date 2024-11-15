import breezy.buildlogic.AndroidConfig
import breezy.buildlogic.configureAndroid
import breezy.buildlogic.configureTest

plugins {
    id("com.android.application")
    kotlin("android")

    id("breezy.code.lint")
}

android {
    defaultConfig {
        targetSdk = AndroidConfig.TARGET_SDK
    }
    configureAndroid(this)
    configureTest()
}
