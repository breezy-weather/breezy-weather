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

package org.breezyweather.ui.settings.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import org.breezyweather.R
import org.breezyweather.common.extensions.plus
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.generateCollapsedScrollBehavior
import org.breezyweather.ui.common.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.ui.settings.preference.bottomInsetItem
import org.breezyweather.ui.settings.preference.clickablePreferenceItem
import org.breezyweather.ui.settings.preference.composables.PreferenceScreen
import org.breezyweather.ui.settings.preference.composables.PreferenceViewWithCard
import org.breezyweather.ui.settings.preference.largeSeparatorItem
import org.breezyweather.ui.settings.preference.smallSeparatorItem

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
        PreferenceScreen(
            paddingValues = paddings.plus(PaddingValues(horizontal = dimensionResource(R.dimen.normal_margin)))
        ) {
            clickablePreferenceItem(R.string.settings_background_updates) { id ->
                PreferenceViewWithCard(
                    titleId = id,
                    iconId = R.drawable.ic_sync,
                    summaryId = R.string.settings_background_updates_summary,
                    isFirst = true,
                    isLast = true
                ) {
                    onNavigateTo(SettingsScreenRouter.BackgroundUpdates.route)
                }
            }
            largeSeparatorItem()
            clickablePreferenceItem(R.string.settings_appearance) { id ->
                PreferenceViewWithCard(
                    titleId = id,
                    iconId = R.drawable.ic_palette,
                    summaryId = R.string.settings_appearance_summary,
                    isFirst = true
                ) {
                    onNavigateTo(SettingsScreenRouter.Appearance.route)
                }
            }
            smallSeparatorItem()
            clickablePreferenceItem(R.string.settings_main) { id ->
                PreferenceViewWithCard(
                    titleId = id,
                    iconId = R.drawable.ic_home,
                    summaryId = R.string.settings_main_summary,
                    isLast = true
                ) {
                    onNavigateTo(SettingsScreenRouter.MainScreen.route)
                }
            }
            largeSeparatorItem()
            clickablePreferenceItem(R.string.settings_notifications) { id ->
                PreferenceViewWithCard(
                    titleId = id,
                    iconId = R.drawable.ic_notifications,
                    summaryId = R.string.settings_notifications_summary,
                    isFirst = true
                ) {
                    onNavigateTo(SettingsScreenRouter.Notifications.route)
                }
            }
            smallSeparatorItem()
            clickablePreferenceItem(R.string.settings_modules) { id ->
                PreferenceViewWithCard(
                    titleId = id,
                    iconId = R.drawable.ic_widgets,
                    summaryId = R.string.settings_modules_summary,
                    isLast = true
                ) {
                    onNavigateTo(SettingsScreenRouter.Widgets.route)
                }
            }
            largeSeparatorItem()
            clickablePreferenceItem(R.string.settings_location) { id ->
                PreferenceViewWithCard(
                    titleId = id,
                    iconId = R.drawable.ic_location,
                    summaryId = R.string.settings_location_summary,
                    isFirst = true
                ) {
                    onNavigateTo(SettingsScreenRouter.Location.route)
                }
            }
            smallSeparatorItem()
            clickablePreferenceItem(R.string.settings_weather_sources) { id ->
                PreferenceViewWithCard(
                    titleId = id,
                    iconId = R.drawable.ic_factory,
                    summaryId = R.string.settings_weather_sources_summary,
                    isLast = true
                ) {
                    onNavigateTo(SettingsScreenRouter.WeatherProviders.route)
                }
            }
            largeSeparatorItem()
            clickablePreferenceItem(R.string.settings_debug) { id ->
                PreferenceViewWithCard(
                    titleId = id,
                    iconId = R.drawable.ic_bug_report,
                    summaryId = R.string.settings_debug_summary,
                    isFirst = true,
                    isLast = true
                ) {
                    onNavigateTo(SettingsScreenRouter.Debug.route)
                }
            }

            bottomInsetItem()
        }
    }
}
