package org.breezyweather.settings.compose

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import org.breezyweather.R
import org.breezyweather.background.polling.PollingManager
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
                        PollingManager.resetNormalBackgroundTask(context, false)
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
                        PollingManager.resetNormalBackgroundTask(context, false)
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
                PollingManager.resetNormalBackgroundTask(context, false)
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
                PollingManager.resetTodayForecastBackgroundTask(
                    context,
                    false,
                    false
                )
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
                PollingManager.resetNormalBackgroundTask(context, false)
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
                PollingManager.resetTomorrowForecastBackgroundTask(
                    context,
                    false,
                    false
                )
            },
        )
    }
    sectionFooterItem(R.string.settings_notifications_section_forecast)

    bottomInsetItem()
}