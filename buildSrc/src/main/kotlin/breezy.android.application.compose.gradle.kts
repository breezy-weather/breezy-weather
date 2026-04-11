
import breezy.buildlogic.configureCompose
import com.android.build.api.dsl.ApplicationExtension

plugins {
    id("com.android.application")

    id("breezy.code.lint")
}

configure<ApplicationExtension> {
    configureCompose()
}
