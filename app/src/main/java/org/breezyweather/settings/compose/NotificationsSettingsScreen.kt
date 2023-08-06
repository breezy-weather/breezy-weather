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

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import org.breezyweather.R
import org.breezyweather.background.forecast.TodayForecastNotificationJob
import org.breezyweather.background.forecast.TomorrowForecastNotificationJob
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.preference.*
import org.breezyweather.settings.preference.composables.*

@Composable
fun NotificationsSettingsScreen(
    context: Context,
    todayForecastEnabled: Boolean,
    tomorrowForecastEnabled: Boolean,
    paddingValues: PaddingValues,
    postNotificationPermissionEnsurer: (succeedCallback: () -> Unit) -> Unit,
) = PreferenceScreen(paddingValues = paddingValues) {
    sectionHeaderItem(R.string.settings_notifications_section_general)
    switchPreferenceItem(R.string.settings_notifications_alerts_title) { id ->
        SwitchPreferenceView(
            titleId = id,
            summaryOnId = R.string.settings_enabled,
            summaryOffId = R.string.settings_disabled,
            checked = SettingsManager.getInstance(context).isAlertPushEnabled,
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
            summaryOffId = R.string.settings_disabled,
            checked = SettingsManager.getInstance(context).isPrecipitationPushEnabled,
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