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

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import org.breezyweather.R
import org.breezyweather.background.weather.WeatherUpdateJob
import org.breezyweather.common.basic.models.options.UpdateInterval
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.powerManager
import org.breezyweather.common.ui.widgets.Material3Scaffold
import org.breezyweather.common.ui.widgets.generateCollapsedScrollBehavior
import org.breezyweather.common.ui.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.activities.WorkerInfoActivity
import org.breezyweather.settings.preference.bottomInsetItem
import org.breezyweather.settings.preference.clickablePreferenceItem
import org.breezyweather.settings.preference.composables.ListPreferenceView
import org.breezyweather.settings.preference.composables.PreferenceScreen
import org.breezyweather.settings.preference.composables.PreferenceViewWithCard
import org.breezyweather.settings.preference.composables.SwitchPreferenceView
import org.breezyweather.settings.preference.listPreferenceItem
import org.breezyweather.settings.preference.sectionFooterItem
import org.breezyweather.settings.preference.sectionHeaderItem
import org.breezyweather.settings.preference.switchPreferenceItem
import org.breezyweather.theme.compose.DayNightTheme
import java.util.Date

@Composable
fun BackgroundSettingsScreen(
    context: Context,
    updateInterval: UpdateInterval,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val scrollBehavior = generateCollapsedScrollBehavior()

    Material3Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            FitStatusBarTopAppBar(
                title = stringResource(R.string.settings_background_updates),
                onBackPressed = onNavigateBack,
                actions = { AboutActivityIconButton(context) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddings ->
        PreferenceScreen(paddingValues = paddings) {
            sectionHeaderItem(R.string.settings_background_updates_section_general)
            listPreferenceItem(R.string.settings_background_updates_refresh_title) { id ->
                val dialogNeverRefreshOpenState = remember { mutableStateOf(false) }
                ListPreferenceView(
                    titleId = id,
                    selectedKey = updateInterval.id,
                    valueArrayId = R.array.automatic_refresh_rate_values,
                    nameArrayId = R.array.automatic_refresh_rates,
                    withState = false,
                    card = true,
                    onValueChanged = {
                        val newValue = UpdateInterval.getInstance(it)
                        if (newValue == UpdateInterval.INTERVAL_NEVER) {
                            dialogNeverRefreshOpenState.value = true
                        } else {
                            SettingsManager
                                .getInstance(context)
                                .updateInterval = UpdateInterval.getInstance(it)
                            WeatherUpdateJob.setupTask(context)
                        }
                    }
                )
                if (dialogNeverRefreshOpenState.value) {
                    AlertDialog(
                        onDismissRequest = { dialogNeverRefreshOpenState.value = false },
                        text = {
                            Column(
                                modifier = Modifier
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_background_updates_refresh_never_warning1),
                                    color = DayNightTheme.colors.bodyColor,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
                                Text(
                                    text = stringResource(
                                        R.string.settings_background_updates_refresh_never_warning2,
                                        5
                                    ),
                                    color = DayNightTheme.colors.bodyColor,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
                                Text(
                                    text = stringResource(R.string.settings_background_updates_refresh_never_warning3),
                                    color = DayNightTheme.colors.bodyColor,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    dialogNeverRefreshOpenState.value = false
                                    SettingsManager
                                        .getInstance(context)
                                        .updateInterval = UpdateInterval.INTERVAL_NEVER
                                    WeatherUpdateJob.setupTask(context)
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.action_continue),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    dialogNeverRefreshOpenState.value = false
                                }
                            ) {
                                Text(
                                    text = stringResource(android.R.string.cancel),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    )
                }
            }
            switchPreferenceItem(R.string.settings_background_updates_refresh_ignore_when_battery_low) { id ->
                SwitchPreferenceView(
                    titleId = id,
                    summaryOnId = R.string.settings_enabled,
                    summaryOffId = R.string.settings_disabled,
                    checked = SettingsManager.getInstance(context).ignoreUpdatesWhenBatteryLow,
                    enabled = updateInterval != UpdateInterval.INTERVAL_NEVER,
                    onValueChanged = {
                        SettingsManager.getInstance(context).ignoreUpdatesWhenBatteryLow = it
                        WeatherUpdateJob.setupTask(context)
                    }
                )
            }
            sectionFooterItem(R.string.settings_background_updates_section_general)

            sectionHeaderItem(R.string.settings_background_updates_section_troubleshoot)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                clickablePreferenceItem(R.string.settings_background_updates_battery_optimization) { id ->
                    PreferenceViewWithCard(
                        titleId = id,
                        summaryId = R.string.settings_background_updates_battery_optimization_summary
                    ) {
                        val packageName: String = context.packageName
                        if (!context.powerManager.isIgnoringBatteryOptimizations(packageName)) {
                            try {
                                @SuppressLint("BatteryLife")
                                val intent = Intent().apply {
                                    action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                    data = "package:$packageName".toUri()
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                SnackbarHelper.showSnackbar(
                                    context.getString(
                                        R.string.settings_background_updates_battery_optimization_activity_not_found
                                    )
                                )
                            }
                        } else {
                            SnackbarHelper.showSnackbar(
                                context.getString(R.string.settings_background_updates_battery_optimization_disabled)
                            )
                        }
                    }
                }
            }
            clickablePreferenceItem(R.string.settings_background_updates_dont_kill_my_app_title) { id ->
                PreferenceViewWithCard(
                    titleId = id,
                    summaryId = R.string.settings_background_updates_dont_kill_my_app_summary
                ) {
                    uriHandler.openUri("https://dontkillmyapp.com/")
                }
            }
            clickablePreferenceItem(R.string.settings_background_updates_worker_info_title) { id ->
                PreferenceViewWithCard(
                    title = context.getString(id),
                    summary = if (SettingsManager.getInstance(context).weatherUpdateLastTimestamp > 0) {
                        context.getString(
                            R.string.settings_background_updates_worker_info_summary,
                            Date(SettingsManager.getInstance(context).weatherUpdateLastTimestamp)
                                .getFormattedDate("yyyy-MM-dd HH:mm")
                        )
                    } else {
                        null
                    }
                ) {
                    context.startActivity(Intent(context, WorkerInfoActivity::class.java))
                }
            }
            sectionFooterItem(R.string.settings_background_updates_section_troubleshoot)

            bottomInsetItem()
        }
    }
}
