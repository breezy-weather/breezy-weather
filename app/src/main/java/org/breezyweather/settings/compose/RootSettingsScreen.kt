package org.breezyweather.settings.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import org.breezyweather.R
import org.breezyweather.settings.preference.bottomInsetItem
import org.breezyweather.settings.preference.clickablePreferenceItem
import org.breezyweather.settings.preference.composables.PreferenceScreen
import org.breezyweather.settings.preference.composables.PreferenceView

@Composable
fun RootSettingsView(
    navController: NavHostController,
    paddingValues: PaddingValues,
) {
    PreferenceScreen(paddingValues = paddingValues) {
        clickablePreferenceItem(R.string.settings_background_updates) { id ->
            PreferenceView(
                titleId = id,
                iconId = R.drawable.ic_sync,
                summaryId = R.string.settings_background_updates_summary
            ) {
                navController.navigate(SettingsScreenRouter.BackgroundUpdates.route)
            }
        }
        clickablePreferenceItem(R.string.settings_providers) { id ->
            PreferenceView(
                titleId = id,
                iconId = R.drawable.ic_factory,
                summaryId = R.string.settings_providers_summary
            ) {
                navController.navigate(SettingsScreenRouter.DataProvider.route)
            }
        }
        clickablePreferenceItem(R.string.settings_appearance) { id ->
            PreferenceView(
                titleId = id,
                iconId = R.drawable.ic_palette,
                summaryId = R.string.settings_appearance_summary
            ) {
                navController.navigate(SettingsScreenRouter.Appearance.route)
            }
        }
        clickablePreferenceItem(R.string.settings_main) { id ->
            PreferenceView(
                titleId = id,
                iconId = R.drawable.ic_home,
                summaryId = R.string.settings_main_summary
            ) {
                navController.navigate(SettingsScreenRouter.MainScreen.route)
            }
        }
        clickablePreferenceItem(R.string.settings_notifications) { id ->
            PreferenceView(
                titleId = id,
                iconId = R.drawable.ic_notifications,
                summaryId = R.string.settings_notifications_summary
            ) {
                navController.navigate(SettingsScreenRouter.Notifications.route)
            }
        }
        clickablePreferenceItem(R.string.settings_widgets) { id ->
            PreferenceView(
                titleId = id,
                iconId = R.drawable.ic_widgets,
                summaryId = R.string.settings_widgets_summary
            ) {
                navController.navigate(SettingsScreenRouter.Widgets.route)
            }
        }

        bottomInsetItem()
    }
}