/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

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
        clickablePreferenceItem(R.string.settings_location) { id ->
            PreferenceView(
                titleId = id,
                iconId = R.drawable.ic_location,
                summaryId = R.string.settings_location_summary
            ) {
                navController.navigate(SettingsScreenRouter.Location.route)
            }
        }
        clickablePreferenceItem(R.string.settings_weather_sources) { id ->
            PreferenceView(
                titleId = id,
                iconId = R.drawable.ic_factory,
                summaryId = R.string.settings_weather_sources_summary
            ) {
                navController.navigate(SettingsScreenRouter.WeatherProviders.route)
            }
        }
        clickablePreferenceItem(R.string.settings_debug) { id ->
            PreferenceView(
                titleId = id,
                iconId = R.drawable.ic_bug_report,
                summaryId = R.string.settings_debug_summary
            ) {
                navController.navigate(SettingsScreenRouter.Debug.route)
            }
        }

        bottomInsetItem()
    }
}