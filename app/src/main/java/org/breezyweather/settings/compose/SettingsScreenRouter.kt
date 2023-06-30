package org.breezyweather.settings.compose

sealed class SettingsScreenRouter(val route: String) {
    object Root : SettingsScreenRouter("org.breezyweather.settings.root")
    object BackgroundUpdates : SettingsScreenRouter("org.breezyweather.settings.background")
    object Location : SettingsScreenRouter("org.breezyweather.settings.location")
    object WeatherProviders : SettingsScreenRouter("org.breezyweather.settings.providers")
    object Appearance : SettingsScreenRouter("org.breezyweather.settings.appearance")
    object MainScreen : SettingsScreenRouter("org.breezyweather.settings.main")
    object Notifications : SettingsScreenRouter("org.breezyweather.settings.notifications")
    object Unit : SettingsScreenRouter("org.breezyweather.settings.unit")
    object Widgets : SettingsScreenRouter("org.breezyweather.settings.widgets")
    object Debug : SettingsScreenRouter("org.breezyweather.settings.debug")
}