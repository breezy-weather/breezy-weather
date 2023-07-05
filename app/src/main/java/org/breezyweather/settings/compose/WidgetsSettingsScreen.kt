package org.breezyweather.settings.compose

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.breezyweather.R
import org.breezyweather.background.polling.PollingManager
import org.breezyweather.common.basic.models.options.NotificationStyle
import org.breezyweather.common.basic.models.options.WidgetWeekIconMode
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.remoteviews.config.*
import org.breezyweather.remoteviews.presenters.*
import org.breezyweather.remoteviews.presenters.notification.WidgetNotificationIMP
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.preference.*
import org.breezyweather.settings.preference.composables.*
import org.breezyweather.wallpaper.MaterialLiveWallpaperService

@Composable
fun WidgetsSettingsScreen(
    context: Context,
    notificationEnabled: Boolean,
    notificationTemperatureIconEnabled: Boolean,
    paddingValues: PaddingValues,
    postNotificationPermissionEnsurer: (succeedCallback: () -> Unit) -> Unit,
) = PreferenceScreen(paddingValues = paddingValues) {
    // widget.
    sectionHeaderItem(R.string.settings_widgets_section_general)
    clickablePreferenceItem(R.string.settings_widgets_live_wallpaper_title) { id ->
        PreferenceView(
            titleId = id,
            summaryId = R.string.settings_widgets_live_wallpaper_summary
        ) {
            try {
                context.startActivity(Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                    .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        ComponentName(context, MaterialLiveWallpaperService::class.java)
                    )
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (e: ActivityNotFoundException) {
                try {
                    context.startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                } catch (e2: ActivityNotFoundException) {
                    SnackbarHelper.showSnackbar(context.getString(R.string.settings_widgets_live_wallpaper_error))
                }
            }
        }
    }
    listPreferenceItem(R.string.settings_widgets_week_icon_mode_title) { id ->
        ListPreferenceView(
            titleId = id,
            selectedKey = SettingsManager.getInstance(context).widgetWeekIconMode.id,
            valueArrayId = R.array.week_icon_mode_values,
            nameArrayId = R.array.week_icon_modes,
            onValueChanged = {
                SettingsManager
                    .getInstance(context)
                    .widgetWeekIconMode = WidgetWeekIconMode.getInstance(it)
                PollingManager.resetNormalBackgroundTask(context, true)
            },
        )
    }
    switchPreferenceItem(R.string.settings_widgets_monochrome_icons_title) { id ->
        SwitchPreferenceView(
            titleId = id,
            summaryOnId = R.string.settings_enabled,
            summaryOffId = R.string.settings_disabled,
            checked = SettingsManager.getInstance(context).isWidgetUsingMonochromeIcons,
            onValueChanged = {
                SettingsManager.getInstance(context).isWidgetUsingMonochromeIcons = it
                PollingManager.resetNormalBackgroundTask(context, true)
            },
        )
    }
    switchPreferenceItem(R.string.settings_widgets_day_night_temp_reversed_title) { id ->
        SwitchPreferenceView(
            title = stringResource(id),
            summary = { context, it ->
                Temperature.getTrendTemperature(
                    context,
                    3,
                    7,
                    SettingsManager.getInstance(context).temperatureUnit,
                    it
                )
            },
            checked = SettingsManager.getInstance(context).isDayNightTempOrderReversed,
            onValueChanged = {
                SettingsManager.getInstance(context).isDayNightTempOrderReversed = it
            },
        )
    }
    sectionFooterItem(R.string.settings_widgets_section_general)

    if (DayWidgetIMP.isInUse(context) || WeekWidgetIMP.isInUse(context) || DayWeekWidgetIMP.isInUse(context) ||
        ClockDayHorizontalWidgetIMP.isInUse(context) || ClockDayDetailsWidgetIMP.isInUse(context) ||
        ClockDayVerticalWidgetIMP.isInUse(context) || ClockDayWeekWidgetIMP.isInUse(context) ||
        TextWidgetIMP.isInUse(context) || DailyTrendWidgetIMP.isInUse(context) ||
        HourlyTrendWidgetIMP.isInUse(context) || MultiCityWidgetIMP.isInUse(context)) {
        sectionHeaderItem(R.string.settings_widgets_section_widgets_in_use)
        if (DayWidgetIMP.isInUse(context)) {
            clickablePreferenceItem(R.string.widget_day) {
                PreferenceView(
                    title = stringResource(it),
                    summary = stringResource(R.string.settings_widgets_configure_widget_summary)
                ) {
                    context.startActivity(Intent(context, DayWidgetConfigActivity::class.java))
                }
            }
        }
        if (WeekWidgetIMP.isInUse(context)) {
            clickablePreferenceItem(R.string.widget_week) {
                PreferenceView(
                    title = stringResource(it),
                    summary = stringResource(R.string.settings_widgets_configure_widget_summary)
                ) {
                    context.startActivity(Intent(context, WeekWidgetConfigActivity::class.java))
                }
            }
        }
        if (DayWeekWidgetIMP.isInUse(context)) {
            clickablePreferenceItem(R.string.widget_day_week) {
                PreferenceView(
                    title = stringResource(it),
                    summary = stringResource(R.string.settings_widgets_configure_widget_summary)
                ) {
                    context.startActivity(Intent(context, DayWeekWidgetConfigActivity::class.java))
                }
            }
        }
        if (ClockDayHorizontalWidgetIMP.isInUse(context)) {
            clickablePreferenceItem(R.string.widget_clock_day_horizontal) {
                PreferenceView(
                    title = stringResource(it),
                    summary = stringResource(R.string.settings_widgets_configure_widget_summary)
                ) {
                    context.startActivity(Intent(context, ClockDayHorizontalWidgetConfigActivity::class.java))
                }
            }
        }
        if (ClockDayDetailsWidgetIMP.isInUse(context)) {
            clickablePreferenceItem(R.string.widget_clock_day_details) {
                PreferenceView(
                    title = stringResource(it),
                    summary = stringResource(R.string.settings_widgets_configure_widget_summary)
                ) {
                    context.startActivity(Intent(context, ClockDayDetailsWidgetConfigActivity::class.java))
                }
            }
        }
        if (ClockDayVerticalWidgetIMP.isInUse(context)) {
            clickablePreferenceItem(R.string.widget_clock_day_vertical) {
                PreferenceView(
                    title = stringResource(it),
                    summary = stringResource(R.string.settings_widgets_configure_widget_summary)
                ) {
                    context.startActivity(Intent(context, ClockDayVerticalWidgetConfigActivity::class.java))
                }
            }
        }
        if (ClockDayWeekWidgetIMP.isInUse(context)) {
            clickablePreferenceItem(R.string.widget_clock_day_week) {
                PreferenceView(
                    title = stringResource(it),
                    summary = stringResource(R.string.settings_widgets_configure_widget_summary)
                ) {
                    context.startActivity(Intent(context, ClockDayWeekWidgetConfigActivity::class.java))
                }
            }
        }
        if (TextWidgetIMP.isInUse(context)) {
            clickablePreferenceItem(R.string.widget_text) {
                PreferenceView(
                    title = stringResource(it),
                    summary = stringResource(R.string.settings_widgets_configure_widget_summary)
                ) {
                    context.startActivity(Intent(context, TextWidgetConfigActivity::class.java))
                }
            }
        }
        if (DailyTrendWidgetIMP.isInUse(context)) {
            clickablePreferenceItem(R.string.widget_trend_daily) {
                PreferenceView(
                    title = stringResource(it),
                    summary = stringResource(R.string.settings_widgets_configure_widget_summary)
                ) {
                    context.startActivity(Intent(context, DailyTrendWidgetConfigActivity::class.java))
                }
            }
        }
        if (HourlyTrendWidgetIMP.isInUse(context)) {
            clickablePreferenceItem(R.string.widget_trend_hourly) {
                PreferenceView(
                    title = stringResource(it),
                    summary = stringResource(R.string.settings_widgets_configure_widget_summary)
                ) {
                    context.startActivity(Intent(context, HourlyTrendWidgetConfigActivity::class.java))
                }
            }
        }
        if (MultiCityWidgetIMP.isInUse(context)) {
            clickablePreferenceItem(R.string.widget_multi_city) {
                PreferenceView(
                    title = stringResource(it),
                    summary = stringResource(R.string.settings_widgets_configure_widget_summary)
                ) {
                    context.startActivity(Intent(context, MultiCityWidgetConfigActivity::class.java))
                }
            }
        }
        sectionFooterItem(R.string.settings_widgets_section_widgets_in_use)
    }

    // notification.
    sectionHeaderItem(R.string.settings_widgets_section_notification_widget)
    switchPreferenceItem(R.string.settings_widgets_notification_widget_title) { id ->
        SwitchPreferenceView(
            titleId = id,
            summaryOnId = R.string.settings_enabled,
            summaryOffId = R.string.settings_disabled,
            checked = notificationEnabled,
            onValueChanged = {
                SettingsManager.getInstance(context).isWidgetNotificationEnabled = it
                if (it) { // open notification.
                    postNotificationPermissionEnsurer {
                        PollingManager.resetNormalBackgroundTask(context, true)
                    }
                } else { // close notification.
                    WidgetNotificationIMP.cancelNotification(context)
                    PollingManager.resetNormalBackgroundTask(context, false)
                }
            }
        )
    }
    switchPreferenceItem(R.string.settings_widgets_notification_persistent_switch) { id ->
        SwitchPreferenceView(
            titleId = id,
            summaryOnId = R.string.settings_enabled,
            summaryOffId = R.string.settings_disabled,
            checked = SettingsManager
                .getInstance(context)
                .isWidgetNotificationPersistent,
            enabled = notificationEnabled,
            onValueChanged = {
                SettingsManager
                    .getInstance(context)
                    .isWidgetNotificationPersistent = it
                PollingManager.resetNormalBackgroundTask(context, true)
            }
        )
    }
    listPreferenceItem(R.string.settings_widgets_notification_style_title) { id ->
        ListPreferenceView(
            titleId = id,
            selectedKey = SettingsManager.getInstance(context).widgetNotificationStyle.id,
            valueArrayId = R.array.notification_style_values,
            nameArrayId = R.array.notification_styles,
            enabled = notificationEnabled,
            onValueChanged = {
                SettingsManager
                    .getInstance(context)
                    .widgetNotificationStyle = NotificationStyle.getInstance(it)
                PollingManager.resetNormalBackgroundTask(context, true)
            },
        )
    }
    switchPreferenceItem(R.string.settings_widgets_notification_temp_icon_switch) { id ->
        SwitchPreferenceView(
            titleId = id,
            summaryOnId = R.string.settings_enabled,
            summaryOffId = R.string.settings_disabled,
            checked = SettingsManager
                .getInstance(context)
                .isWidgetNotificationTemperatureIconEnabled,
            enabled = notificationEnabled,
            onValueChanged = {
                SettingsManager
                    .getInstance(context)
                    .isWidgetNotificationTemperatureIconEnabled = it
                PollingManager.resetNormalBackgroundTask(context, true)
            }
        )
    }
    switchPreferenceItem(R.string.settings_widgets_notification_feels_like_switch) { id ->
        SwitchPreferenceView(
            titleId = id,
            summaryOnId = R.string.settings_enabled,
            summaryOffId = R.string.settings_disabled,
            checked = SettingsManager
                .getInstance(context)
                .isWidgetNotificationUsingFeelsLike,
            enabled = notificationEnabled && notificationTemperatureIconEnabled,
            onValueChanged = {
                SettingsManager
                    .getInstance(context)
                    .isWidgetNotificationUsingFeelsLike = it
                PollingManager.resetNormalBackgroundTask(context, true)
            }
        )
    }
    sectionFooterItem(R.string.settings_widgets_section_notification_widget)

    bottomInsetItem()
}