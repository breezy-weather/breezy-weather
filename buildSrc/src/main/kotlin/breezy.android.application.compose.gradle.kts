import breezy.buildlogic.configureCompose

plugins {
    id("com.android.application")
    kotlin("android")

    id("breezy.code.lint")
}

android {
    configureCompose(this)
}
