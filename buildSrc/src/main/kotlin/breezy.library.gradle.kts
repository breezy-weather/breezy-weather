import breezy.buildlogic.configureAndroid
import breezy.buildlogic.configureTest

plugins {
    id("com.android.library")

    id("breezy.code.lint")
}

android {
    configureAndroid(this)
    configureTest()
}
