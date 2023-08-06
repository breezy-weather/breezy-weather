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

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.core.net.toUri
import org.breezyweather.R
import org.breezyweather.background.weather.WeatherUpdateJob
import org.breezyweather.common.basic.models.options.UpdateInterval
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.activities.WorkerInfoActivity
import org.breezyweather.settings.preference.*
import org.breezyweather.settings.preference.composables.ListPreferenceView
import org.breezyweather.settings.preference.composables.PreferenceScreen
import org.breezyweather.settings.preference.composables.PreferenceView
import org.breezyweather.common.extensions.powerManager
import org.breezyweather.settings.preference.composables.SwitchPreferenceView
import java.util.Date

@Composable
fun BackgroundSettingsScreen(
    context: Context,
    updateInterval: UpdateInterval,
    paddingValues: PaddingValues
) {
    val uriHandler = LocalUriHandler.current
    PreferenceScreen(paddingValues = paddingValues) {
        sectionHeaderItem(R.string.settings_background_updates_section_general)
        listPreferenceItem(R.string.settings_background_updates_refresh_title) { id ->
            ListPreferenceView(
                titleId = id,
                selectedKey = updateInterval.id,
                valueArrayId = R.array.automatic_refresh_rate_values,
                nameArrayId = R.array.automatic_refresh_rates,
                onValueChanged = {
                    SettingsManager
                        .getInstance(context)
                        .updateInterval = UpdateInterval.getInstance(it)
                    WeatherUpdateJob.setupTask(context)
                },
            )
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
                },
            )
        }
        sectionFooterItem(R.string.settings_background_updates_section_general)

        sectionHeaderItem(R.string.settings_background_updates_section_troubleshoot)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            clickablePreferenceItem(R.string.settings_background_updates_battery_optimization) { id ->
                PreferenceView(
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
                            SnackbarHelper.showSnackbar(context.getString(R.string.settings_background_updates_battery_optimization_activity_not_found))
                        }
                    } else {
                        SnackbarHelper.showSnackbar(context.getString(R.string.settings_background_updates_battery_optimization_disabled))
                    }
                }
            }
        }
        clickablePreferenceItem(R.string.settings_background_updates_dont_kill_my_app_title) { id ->
            PreferenceView(
                titleId = id,
                summaryId = R.string.settings_background_updates_dont_kill_my_app_summary
            ) {
                uriHandler.openUri("https://dontkillmyapp.com/")
            }
        }
        clickablePreferenceItem(R.string.settings_background_updates_worker_info_title) { id ->
            PreferenceView(
                title = context.getString(id),
                summary = if (SettingsManager.getInstance(context).weatherUpdateLastTimestamp > 0) {
                    context.getString(R.string.settings_background_updates_worker_info_summary)
                        .replace("$", Date(SettingsManager.getInstance(context).weatherUpdateLastTimestamp).getFormattedDate(pattern = "yyyy-MM-dd HH:mm"))
                } else null
            ) {
                context.startActivity(Intent(context, WorkerInfoActivity::class.java))
            }
        }
        sectionFooterItem(R.string.settings_background_updates_section_troubleshoot)

        bottomInsetItem()
    }
}
