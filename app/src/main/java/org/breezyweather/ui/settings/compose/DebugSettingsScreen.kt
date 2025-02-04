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

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.background.weather.WeatherUpdateJob
import org.breezyweather.common.utils.CrashLogUtils
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.ui.common.composables.AnimatedVisibilitySlideVertically
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.generateCollapsedScrollBehavior
import org.breezyweather.ui.common.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.ui.main.utils.RefreshErrorType
import org.breezyweather.ui.settings.activities.SettingsActivity
import org.breezyweather.ui.settings.preference.bottomInsetItem
import org.breezyweather.ui.settings.preference.clickablePreferenceItem
import org.breezyweather.ui.settings.preference.composables.PreferenceScreen
import org.breezyweather.ui.settings.preference.composables.PreferenceViewWithCard
import org.breezyweather.ui.settings.preference.listPreferenceItem
import org.breezyweather.ui.settings.preference.sectionFooterItem
import org.breezyweather.ui.settings.preference.sectionHeaderItem

@Composable
fun DebugSettingsScreen(
    context: SettingsActivity,
    onNavigateBack: () -> Unit,
    hasNotificationPermission: Boolean,
    postNotificationPermissionEnsurer: (succeedCallback: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val scrollBehavior = generateCollapsedScrollBehavior()

    Material3Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            FitStatusBarTopAppBar(
                title = stringResource(R.string.settings_debug),
                onBackPressed = onNavigateBack,
                actions = { AboutActivityIconButton(context) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddings ->
        PreferenceScreen(paddingValues = paddings) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listPreferenceItem(R.string.settings_notifications_permission) { title ->
                    AnimatedVisibilitySlideVertically(
                        visible = !hasNotificationPermission
                    ) {
                        PreferenceViewWithCard(
                            iconId = R.drawable.ic_about,
                            title = stringResource(title),
                            summary = stringResource(
                                R.string.settings_debug_notification_permission,
                                stringResource(R.string.action_grant_permission)
                            ),
                            onClick = {
                                postNotificationPermissionEnsurer { /* no callback */ }
                            }
                        )
                    }
                }
            }
            clickablePreferenceItem(R.string.settings_debug_dump_crash_logs_title) { id ->
                PreferenceViewWithCard(
                    titleId = id,
                    summaryId = R.string.settings_debug_dump_crash_logs_summary,
                    enabled = hasNotificationPermission
                ) {
                    scope.launch {
                        CrashLogUtils(context).dumpLogs()
                    }
                }
            }

            if (BreezyWeather.instance.debugMode) {
                clickablePreferenceItem(R.string.settings_debug_force_weather_update) { id ->
                    PreferenceViewWithCard(
                        title = stringResource(id),
                        summary = "Execute job for debugging purpose",
                        enabled = hasNotificationPermission
                    ) {
                        WeatherUpdateJob.startNow(context)
                    }
                }

                sectionHeaderItem(R.string.settings_debug_section_refresh_error)
                RefreshErrorType.entries.forEach { refreshError ->
                    clickablePreferenceItem(refreshError.shortMessage) { shortMessage ->
                        PreferenceViewWithCard(
                            titleId = shortMessage,
                            onClick = {
                                refreshError.showDialogAction?.let { showDialogAction ->
                                    SnackbarHelper.showSnackbar(
                                        content = context.getString(shortMessage),
                                        action = context.getString(refreshError.actionButtonMessage)
                                    ) {
                                        showDialogAction(context)
                                    }
                                } ?: SnackbarHelper.showSnackbar(context.getString(shortMessage))
                            }
                        )
                    }
                }
                sectionFooterItem(R.string.settings_debug_section_refresh_error)
            }

            bottomInsetItem()
        }
    }
}
