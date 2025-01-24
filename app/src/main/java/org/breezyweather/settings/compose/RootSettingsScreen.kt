/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.breezyweather.R
import org.breezyweather.common.ui.widgets.Material3Scaffold
import org.breezyweather.common.ui.widgets.generateCollapsedScrollBehavior
import org.breezyweather.common.ui.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.settings.preference.bottomInsetItem
import org.breezyweather.settings.preference.clickablePreferenceItem
import org.breezyweather.settings.preference.composables.PreferenceScreen
import org.breezyweather.settings.preference.composables.PreferenceViewWithCard

@Composable
fun RootSettingsView(
    onNavigateTo: (route: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = generateCollapsedScrollBehavior()

    Material3Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            FitStatusBarTopAppBar(
                title = stringResource(R.string.action_settings),
                onBackPressed = onNavigateBack,
                actions = { AboutActivityIconButton(LocalContext.current) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddings ->
        PreferenceScreen(paddingValues = paddings) {
            clickablePreferenceItem(R.string.settings_background_updates) { id ->
                PreferenceViewWithCard(
                    titleId = id,
                    iconId = R.drawable.ic_sync,
                    summaryId = R.string.settings_background_updates_summary
                ) {
                    onNavigateTo(SettingsScreenRouter.BackgroundUpdates.route)
                }
            }
            clickablePreferenceItem(R.string.settings_appearance) { id ->
                PreferenceViewWithCard(
                    titleId = id,
                    iconId = R.drawable.ic_palette,
                    summaryId = R.string.settings_appearance_summary
                ) {
                    onNavigateTo(SettingsScreenRouter.Appearance.route)
                }
            }
            clickablePreferenceItem(R.string.settings_main) { id ->
                PreferenceViewWithCard(
                    titleId = id,
                    iconId = R.drawable.ic_home,
                    summaryId = R.string.settings_main_summary
                ) {
                    onNavigateTo(SettingsScreenRouter.MainScreen.route)
                }
            }
            clickablePreferenceItem(R.string.settings_notifications) { id ->
                PreferenceViewWithCard(
                    titleId = id,
                    iconId = R.drawable.ic_notifications,
                    summaryId = R.string.settings_notifications_summary
                ) {
                    onNavigateTo(SettingsScreenRouter.Notifications.route)
                }
            }
            clickablePreferenceItem(R.string.settings_widgets) { id ->
                PreferenceViewWithCard(
                    titleId = id,
                    iconId = R.drawable.ic_widgets,
                    summaryId = R.string.settings_widgets_summary
                ) {
                    onNavigateTo(SettingsScreenRouter.Widgets.route)
                }
            }
            clickablePreferenceItem(R.string.settings_location) { id ->
                PreferenceViewWithCard(
                    titleId = id,
                    iconId = R.drawable.ic_location,
                    summaryId = R.string.settings_location_summary
                ) {
                    onNavigateTo(SettingsScreenRouter.Location.route)
                }
            }
            clickablePreferenceItem(R.string.settings_weather_sources) { id ->
                PreferenceViewWithCard(
                    titleId = id,
                    iconId = R.drawable.ic_factory,
                    summaryId = R.string.settings_weather_sources_summary
                ) {
                    onNavigateTo(SettingsScreenRouter.WeatherProviders.route)
                }
            }
            clickablePreferenceItem(R.string.settings_debug) { id ->
                PreferenceViewWithCard(
                    titleId = id,
                    iconId = R.drawable.ic_bug_report,
                    summaryId = R.string.settings_debug_summary
                ) {
                    onNavigateTo(SettingsScreenRouter.Debug.route)
                }
            }

            bottomInsetItem()
        }
    }
}
