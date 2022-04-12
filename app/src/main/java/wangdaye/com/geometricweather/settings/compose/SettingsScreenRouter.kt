package wangdaye.com.geometricweather.settings.compose

sealed class SettingsScreenRouter(val route: String) {
    object Root : SettingsScreenRouter("wangdaye.com.geometricweather.settings.root")
    object Appearance : SettingsScreenRouter("wangdaye.com.geometricweather.settings.appearance")
    object ServiceProvider : SettingsScreenRouter("wangdaye.com.geometricweather.settings.providers")
    object ServiceProviderAdvanced : SettingsScreenRouter("wangdaye.com.geometricweather.settings.advanced")
    object Unit : SettingsScreenRouter("wangdaye.com.geometricweather.settings.unit")
}