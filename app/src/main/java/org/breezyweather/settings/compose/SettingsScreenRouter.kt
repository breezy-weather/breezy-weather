package org.breezyweather.settings.compose

sealed class SettingsScreenRouter(val route: String) {
    object Root : SettingsScreenRouter("org.breezyweather.settings.root")
    object Appearance : SettingsScreenRouter("org.breezyweather.settings.appearance")
    object ServiceProvider : SettingsScreenRouter("org.breezyweather.settings.providers")
    object ServiceProviderAdvanced : SettingsScreenRouter("org.breezyweather.settings.advanced")
    object Unit : SettingsScreenRouter("org.breezyweather.settings.unit")
}