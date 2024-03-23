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

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import org.breezyweather.R
import org.breezyweather.background.forecast.TodayForecastNotificationJob
import org.breezyweather.background.forecast.TomorrowForecastNotificationJob
import org.breezyweather.common.basic.models.options.UpdateInterval
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.preference.bottomInsetItem
import org.breezyweather.settings.preference.composables.PreferenceScreen
import org.breezyweather.settings.preference.composables.SwitchPreferenceView
import org.breezyweather.settings.preference.composables.TimePickerPreferenceView
import org.breezyweather.settings.preference.sectionFooterItem
import org.breezyweather.settings.preference.sectionHeaderItem
import org.breezyweather.settings.preference.switchPreferenceItem
import org.breezyweather.settings.preference.timePickerPreferenceItem

@Composable
fun NotificationsSettingsScreen(
    context: Context,
    todayForecastEnabled: Boolean,
    tomorrowForecastEnabled: Boolean,
    paddingValues: PaddingValues,
    postNotificationPermissionEnsurer: (succeedCallback: () -> Unit) -> Unit
) = PreferenceScreen(paddingValues = paddingValues) {
    sectionHeaderItem(R.string.settings_notifications_section_general)
    switchPreferenceItem(R.string.settings_notifications_alerts_title) { id ->
        SwitchPreferenceView(
            titleId = id,
            summaryOnId = R.string.settings_enabled,
            summaryOffId = if (SettingsManager.getInstance(context).updateInterval != UpdateInterval.INTERVAL_NEVER) {
                R.string.settings_disabled
            } else R.string.settings_unavailable_no_background_updates,
            checked = SettingsManager.getInstance(context).isAlertPushEnabled &&
                SettingsManager.getInstance(context).updateInterval != UpdateInterval.INTERVAL_NEVER,
            enabled = SettingsManager.getInstance(context).updateInterval != UpdateInterval.INTERVAL_NEVER,
            onValueChanged = {
                SettingsManager.getInstance(context).isAlertPushEnabled = it
                if (it) {
                    postNotificationPermissionEnsurer {
                        // Do nothing
                    }
                }
            },
        )
    }
    switchPreferenceItem(R.string.settings_notifications_precipitations_title) { id ->
        SwitchPreferenceView(
            titleId = id,
            summaryOnId = R.string.settings_enabled,
            summaryOffId = if (SettingsManager.getInstance(context).updateInterval != UpdateInterval.INTERVAL_NEVER) {
                R.string.settings_disabled
            } else R.string.settings_unavailable_no_background_updates,
            checked = SettingsManager.getInstance(context).isPrecipitationPushEnabled &&
                SettingsManager.getInstance(context).updateInterval != UpdateInterval.INTERVAL_NEVER,
            enabled = SettingsManager.getInstance(context).updateInterval != UpdateInterval.INTERVAL_NEVER,
            onValueChanged = {
                SettingsManager.getInstance(context).isPrecipitationPushEnabled = it
                if (it) {
                    postNotificationPermissionEnsurer {
                        // Do nothing
                    }
                }
            },
        )
    }
    sectionFooterItem(R.string.settings_notifications_section_general)

    // forecast.
    sectionHeaderItem(R.string.settings_notifications_section_forecast)
    switchPreferenceItem(R.string.settings_notifications_forecast_today_title) { id ->
        SwitchPreferenceView(
            titleId = id,
            summaryOnId = R.string.settings_enabled,
            summaryOffId = R.string.settings_disabled,
            checked = todayForecastEnabled,
            onValueChanged = {
                SettingsManager.getInstance(context).isTodayForecastEnabled = it
                TodayForecastNotificationJob.setupTask(context, false)
            },
        )
    }
    timePickerPreferenceItem(R.string.settings_notifications_forecast_time_today_title) { id ->
        TimePickerPreferenceView(
            titleId = id,
            currentTime = SettingsManager.getInstance(context).todayForecastTime,
            enabled = todayForecastEnabled,
            onValueChanged = {
                SettingsManager.getInstance(context).todayForecastTime = it
                TodayForecastNotificationJob.setupTask(context, false)
            },
        )
    }
    switchPreferenceItem(R.string.settings_notifications_forecast_tomorrow_title) { id ->
        SwitchPreferenceView(
            titleId = id,
            summaryOnId = R.string.settings_enabled,
            summaryOffId = R.string.settings_disabled,
            checked = tomorrowForecastEnabled,
            onValueChanged = {
                SettingsManager.getInstance(context).isTomorrowForecastEnabled = it
                TomorrowForecastNotificationJob.setupTask(context, false)
            },
        )
    }
    timePickerPreferenceItem(R.string.settings_notifications_forecast_time_tomorrow_title) { id ->
        TimePickerPreferenceView(
            titleId = id,
            currentTime = SettingsManager.getInstance(context).tomorrowForecastTime,
            enabled = tomorrowForecastEnabled,
            onValueChanged = {
                SettingsManager.getInstance(context).tomorrowForecastTime = it
                TomorrowForecastNotificationJob.setupTask(context, false)
            },
        )
    }
    sectionFooterItem(R.string.settings_notifications_section_forecast)

    bottomInsetItem()
}
