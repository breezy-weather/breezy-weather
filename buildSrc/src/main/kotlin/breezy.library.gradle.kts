import breezy.buildlogic.configureAndroidLibrary
import breezy.buildlogic.configureTest
import com.android.build.api.dsl.LibraryExtension

plugins {
    id("com.android.library")

    id("breezy.code.lint")
}

configure<LibraryExtension> {
    configureAndroidLibrary()
    configureTest()
}
