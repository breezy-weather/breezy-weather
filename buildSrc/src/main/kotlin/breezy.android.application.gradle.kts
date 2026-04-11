
import breezy.buildlogic.AndroidConfig
import breezy.buildlogic.configureAndroidApplication
import breezy.buildlogic.configureTest
import com.android.build.api.dsl.ApplicationExtension

plugins {
    id("com.android.application")

    id("breezy.code.lint")
}

configure<ApplicationExtension> {
    defaultConfig {
        targetSdk = AndroidConfig.TARGET_SDK
    }
    configureAndroidApplication()
    configureTest()
}
