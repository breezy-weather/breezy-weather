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

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import org.breezyweather.R
import org.breezyweather.background.weather.WeatherUpdateJob
import org.breezyweather.common.basic.models.options.UpdateInterval
import org.breezyweather.common.extensions.formatTime
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.plus
import org.breezyweather.common.extensions.powerManager
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.generateCollapsedScrollBehavior
import org.breezyweather.ui.common.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.ui.settings.activities.WorkerInfoActivity
import org.breezyweather.ui.settings.preference.bottomInsetItem
import org.breezyweather.ui.settings.preference.clickablePreferenceItem
import org.breezyweather.ui.settings.preference.composables.ListPreferenceViewWithCard
import org.breezyweather.ui.settings.preference.composables.PreferenceScreen
import org.breezyweather.ui.settings.preference.composables.PreferenceViewWithCard
import org.breezyweather.ui.settings.preference.composables.SwitchPreferenceView
import org.breezyweather.ui.settings.preference.largeSeparatorItem
import org.breezyweather.ui.settings.preference.listPreferenceItem
import org.breezyweather.ui.settings.preference.sectionFooterItem
import org.breezyweather.ui.settings.preference.sectionHeaderItem
import org.breezyweather.ui.settings.preference.smallSeparatorItem
import org.breezyweather.ui.settings.preference.switchPreferenceItem
import org.breezyweather.unit.formatting.UnitWidth
import java.util.Date
import kotlin.time.DurationUnit

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
        PreferenceScreen(
            paddingValues = paddings.plus(PaddingValues(horizontal = dimensionResource(R.dimen.normal_margin)))
        ) {
            sectionHeaderItem(R.string.settings_background_updates_section_general)
            listPreferenceItem(R.string.settings_background_updates_refresh_title) { id ->
                val valueArray = stringArrayResource(R.array.automatic_refresh_rate_values)
                val nameArray = stringArrayResource(R.array.automatic_refresh_rates).mapIndexed { index, value ->
                    UpdateInterval.entries.firstOrNull { it.id == valueArray[index] }?.let { updateInterval ->
                        updateInterval.interval?.formatTime(
                            context = context,
                            smallestUnit = DurationUnit.MINUTES,
                            unitWidth = UnitWidth.LONG
                        )
                    } ?: value
                }.toTypedArray()
                val dialogNeverRefreshOpenState = remember { mutableStateOf(false) }
                ListPreferenceViewWithCard(
                    title = stringResource(id),
                    summary = { _, value ->
                        valueArray.indexOfFirst { it == value }.let { if (it == -1) nameArray[0] else nameArray[it] }
                    },
                    selectedKey = updateInterval.id,
                    valueArray = valueArray,
                    nameArray = nameArray,
                    withState = false,
                    isFirst = true,
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
                                Text(
                                    text = stringResource(
                                        R.string.settings_background_updates_refresh_never_warning2,
                                        5
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.normal_margin)))
                                Text(
                                    text = stringResource(R.string.settings_background_updates_refresh_never_warning3),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            smallSeparatorItem()
            switchPreferenceItem(R.string.settings_background_updates_refresh_skip_when_battery_low) { id ->
                SwitchPreferenceView(
                    titleId = id,
                    summaryOnId = R.string.settings_enabled,
                    summaryOffId = R.string.settings_disabled,
                    checked = SettingsManager.getInstance(context).ignoreUpdatesWhenBatteryLow,
                    enabled = updateInterval != UpdateInterval.INTERVAL_NEVER,
                    isLast = true,
                    onValueChanged = {
                        SettingsManager.getInstance(context).ignoreUpdatesWhenBatteryLow = it
                        WeatherUpdateJob.setupTask(context)
                    }
                )
            }
            sectionFooterItem(R.string.settings_background_updates_section_general)

            largeSeparatorItem()

            sectionHeaderItem(R.string.settings_background_updates_section_troubleshoot)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                clickablePreferenceItem(R.string.settings_background_updates_battery_optimization) { id ->
                    PreferenceViewWithCard(
                        titleId = id,
                        summaryId = R.string.settings_background_updates_battery_optimization_summary,
                        isFirst = true
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
            smallSeparatorItem()
            clickablePreferenceItem(R.string.settings_background_updates_dont_kill_my_app_title) { id ->
                PreferenceViewWithCard(
                    titleId = id,
                    summaryId = R.string.settings_background_updates_dont_kill_my_app_summary
                ) {
                    uriHandler.openUri("https://dontkillmyapp.com/")
                }
            }
            smallSeparatorItem()
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
                    },
                    isLast = true
                ) {
                    context.startActivity(Intent(context, WorkerInfoActivity::class.java))
                }
            }
            sectionFooterItem(R.string.settings_background_updates_section_troubleshoot)

            bottomInsetItem()
        }
    }
}
