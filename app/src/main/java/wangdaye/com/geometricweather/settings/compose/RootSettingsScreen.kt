package wangdaye.com.geometricweather.settings.compose

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.background.polling.PollingManager
import wangdaye.com.geometricweather.common.basic.models.options.DarkMode
import wangdaye.com.geometricweather.common.basic.models.options.NotificationStyle
import wangdaye.com.geometricweather.common.basic.models.options.UpdateInterval
import wangdaye.com.geometricweather.common.basic.models.options.WidgetWeekIconMode
import wangdaye.com.geometricweather.common.utils.helpers.AsyncHelper
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper
import wangdaye.com.geometricweather.remoteviews.config.ClockDayDetailsWidgetConfigActivity
import wangdaye.com.geometricweather.remoteviews.config.ClockDayHorizontalWidgetConfigActivity
import wangdaye.com.geometricweather.remoteviews.config.ClockDayVerticalWidgetConfigActivity
import wangdaye.com.geometricweather.remoteviews.config.ClockDayWeekWidgetConfigActivity
import wangdaye.com.geometricweather.remoteviews.config.DailyTrendWidgetConfigActivity
import wangdaye.com.geometricweather.remoteviews.config.DayWeekWidgetConfigActivity
import wangdaye.com.geometricweather.remoteviews.config.DayWidgetConfigActivity
import wangdaye.com.geometricweather.remoteviews.config.HourlyTrendWidgetConfigActivity
import wangdaye.com.geometricweather.remoteviews.config.MultiCityWidgetConfigActivity
import wangdaye.com.geometricweather.remoteviews.config.TextWidgetConfigActivity
import wangdaye.com.geometricweather.remoteviews.config.WeekWidgetConfigActivity
import wangdaye.com.geometricweather.remoteviews.presenters.ClockDayDetailsWidgetIMP
import wangdaye.com.geometricweather.remoteviews.presenters.ClockDayHorizontalWidgetIMP
import wangdaye.com.geometricweather.remoteviews.presenters.ClockDayVerticalWidgetIMP
import wangdaye.com.geometricweather.remoteviews.presenters.ClockDayWeekWidgetIMP
import wangdaye.com.geometricweather.remoteviews.presenters.DailyTrendWidgetIMP
import wangdaye.com.geometricweather.remoteviews.presenters.DayWeekWidgetIMP
import wangdaye.com.geometricweather.remoteviews.presenters.DayWidgetIMP
import wangdaye.com.geometricweather.remoteviews.presenters.HourlyTrendWidgetIMP
import wangdaye.com.geometricweather.remoteviews.presenters.MultiCityWidgetIMP
import wangdaye.com.geometricweather.remoteviews.presenters.TextWidgetIMP
import wangdaye.com.geometricweather.remoteviews.presenters.WeekWidgetIMP
import wangdaye.com.geometricweather.remoteviews.presenters.notification.NormalNotificationIMP
import wangdaye.com.geometricweather.settings.SettingsManager
import wangdaye.com.geometricweather.settings.preference.bottomInsetItem
import wangdaye.com.geometricweather.settings.preference.switchPreferenceItem
import wangdaye.com.geometricweather.settings.preference.clickablePreferenceItem
import wangdaye.com.geometricweather.settings.preference.composables.ListPreferenceView
import wangdaye.com.geometricweather.settings.preference.composables.PreferenceScreen
import wangdaye.com.geometricweather.settings.preference.composables.PreferenceView
import wangdaye.com.geometricweather.settings.preference.composables.SwitchPreferenceView
import wangdaye.com.geometricweather.settings.preference.composables.TimePickerPreferenceView
import wangdaye.com.geometricweather.settings.preference.listPreferenceItem
import wangdaye.com.geometricweather.settings.preference.sectionFooterItem
import wangdaye.com.geometricweather.settings.preference.sectionHeaderItem
import wangdaye.com.geometricweather.settings.preference.timePickerPreferenceItem
import wangdaye.com.geometricweather.theme.ThemeManager

@Composable
fun RootSettingsView(
    context: Context,
    navController: NavHostController,
    paddingValues: PaddingValues,
    postNotificationPermissionEnsurer: (succeedCallback: () -> Unit) -> Unit,
) {
    val todayForecastEnabledState = remember {
        mutableStateOf(
            SettingsManager
                .getInstance(context)
                .isTodayForecastEnabled
        )
    }
    val tomorrowForecastEnabledState = remember {
        mutableStateOf(
            SettingsManager
                .getInstance(context)
                .isTomorrowForecastEnabled
        )
    }
    val notificationEnabledState = remember {
        mutableStateOf(
            SettingsManager
                .getInstance(context)
                .isNotificationEnabled
        )
    }
    val notificationTemperatureIconEnabledState = remember {
        mutableStateOf(
            SettingsManager
                .getInstance(context)
                .isNotificationTemperatureIconEnabled
        )
    }

    PreferenceScreen(paddingValues = paddingValues) {
        // basic.
        sectionHeaderItem(R.string.settings_category_basic)
        switchPreferenceItem(R.string.settings_title_background_free) { id ->
            SwitchPreferenceView(
                titleId = id,
                summaryOnId = R.string.settings_summary_background_free_on,
                summaryOffId = R.string.settings_summary_background_free_off,
                checked = SettingsManager.getInstance(context).isBackgroundFree,
                onValueChanged = {
                    SettingsManager.getInstance(context).isBackgroundFree = it
                    if (!it) {
                        postNotificationPermissionEnsurer {
                            PollingManager.resetNormalBackgroundTask(context, false)

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                showBlockNotificationGroupDialog(context)
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                showIgnoreBatteryOptimizationDialog(context)
                            }
                        }
                    }
                },
            )
        }
        switchPreferenceItem(R.string.settings_title_alert_notification_switch) { id ->
            SwitchPreferenceView(
                titleId = id,
                summaryOnId = R.string.on,
                summaryOffId = R.string.off,
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
        switchPreferenceItem(R.string.settings_title_precipitation_notification_switch) { id ->
            SwitchPreferenceView(
                titleId = id,
                summaryOnId = R.string.on,
                summaryOffId = R.string.off,
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
        listPreferenceItem(R.string.settings_title_refresh_rate) { id ->
            ListPreferenceView(
                titleId = id,
                selectedKey = SettingsManager.getInstance(context).updateInterval.id,
                valueArrayId = R.array.automatic_refresh_rate_values,
                nameArrayId = R.array.automatic_refresh_rates,
                onValueChanged = {
                    SettingsManager
                        .getInstance(context)
                        .updateInterval = UpdateInterval.getInstance(it)
                },
            )
        }
        listPreferenceItem(R.string.settings_title_dark_mode) { id ->
            ListPreferenceView(
                titleId = id,
                selectedKey = SettingsManager.getInstance(context).darkMode.id,
                valueArrayId = R.array.dark_mode_values,
                nameArrayId = R.array.dark_modes,
                onValueChanged = {
                    SettingsManager
                        .getInstance(context)
                        .darkMode = DarkMode.getInstance(it)

                    AsyncHelper.delayRunOnUI({
                        ThemeManager
                            .getInstance(context)
                            .update(darkMode = SettingsManager.getInstance(context).darkMode)
                    },300)
                },
            )
        }
        clickablePreferenceItem(R.string.settings_title_live_wallpaper) { id ->
            PreferenceView(
                titleId = id,
                summaryId = R.string.settings_summary_live_wallpaper
            ) {
                IntentHelper.startLiveWallpaperActivity(context)
            }
        }
        clickablePreferenceItem(R.string.settings_title_service_provider) { id ->
            PreferenceView(
                titleId = id,
                summaryId = R.string.settings_summary_service_provider
            ) {
                navController.navigate(SettingsScreenRouter.ServiceProvider.route)
            }
        }
        clickablePreferenceItem(R.string.settings_title_unit) { id ->
            PreferenceView(
                titleId = id,
                summaryId = R.string.settings_summary_unit
            ) {
                navController.navigate(SettingsScreenRouter.Unit.route)
            }
        }
        clickablePreferenceItem(R.string.settings_title_appearance) { id ->
            PreferenceView(
                titleId = id,
                summaryId = R.string.settings_summary_appearance
            ) {
                navController.navigate(SettingsScreenRouter.Appearance.route)
            }
        }
        sectionFooterItem(R.string.settings_category_basic)

        // forecast.
        sectionHeaderItem(R.string.settings_category_forecast)
        switchPreferenceItem(R.string.settings_title_forecast_today) { id ->
            SwitchPreferenceView(
                titleId = id,
                summaryOnId = R.string.on,
                summaryOffId = R.string.off,
                checked = todayForecastEnabledState.value,
                onValueChanged = {
                    todayForecastEnabledState.value = it
                    SettingsManager.getInstance(context).isTodayForecastEnabled = it
                    PollingManager.resetNormalBackgroundTask(context, false)
                },
            )
        }
        timePickerPreferenceItem(R.string.settings_title_forecast_today_time) { id ->
            TimePickerPreferenceView(
                titleId = id,
                currentTime = SettingsManager.getInstance(context).todayForecastTime,
                enabled = todayForecastEnabledState.value,
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
        switchPreferenceItem(R.string.settings_title_forecast_tomorrow) { id ->
            SwitchPreferenceView(
                titleId = id,
                summaryOnId = R.string.on,
                summaryOffId = R.string.off,
                checked = tomorrowForecastEnabledState.value,
                onValueChanged = {
                    tomorrowForecastEnabledState.value = it
                    SettingsManager.getInstance(context).isTomorrowForecastEnabled = it
                    PollingManager.resetNormalBackgroundTask(context, false)
                },
            )
        }
        timePickerPreferenceItem(R.string.settings_title_forecast_tomorrow_time) { id ->
            TimePickerPreferenceView(
                titleId = id,
                currentTime = SettingsManager.getInstance(context).tomorrowForecastTime,
                enabled = tomorrowForecastEnabledState.value,
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
        sectionFooterItem(R.string.settings_category_forecast)

        // widget.
        sectionHeaderItem(R.string.settings_category_widget)
        listPreferenceItem(R.string.settings_title_week_icon_mode) { id ->
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
        switchPreferenceItem(R.string.settings_title_minimal_icon) { id ->
            SwitchPreferenceView(
                titleId = id,
                summaryOnId = R.string.on,
                summaryOffId = R.string.off,
                checked = SettingsManager.getInstance(context).isWidgetMinimalIconEnabled,
                onValueChanged = {
                    SettingsManager.getInstance(context).isWidgetMinimalIconEnabled = it
                    PollingManager.resetNormalBackgroundTask(context, true)
                },
            )
        }
        if (DayWidgetIMP.isEnable(context)) {
            clickablePreferenceItem(R.string.widget_day) {
                PreferenceView(title = stringResource(it)) {
                    context.startActivity(Intent(context, DayWidgetConfigActivity::class.java))
                }
            }
        }
        if (WeekWidgetIMP.isEnable(context)) {
            clickablePreferenceItem(R.string.widget_week) {
                PreferenceView(title = stringResource(it)) {
                    context.startActivity(Intent(context, WeekWidgetConfigActivity::class.java))
                }
            }
        }
        if (DayWeekWidgetIMP.isEnable(context)) {
            clickablePreferenceItem(R.string.widget_day_week) {
                PreferenceView(title = stringResource(it)) {
                    context.startActivity(Intent(context, DayWeekWidgetConfigActivity::class.java))
                }
            }
        }
        if (ClockDayHorizontalWidgetIMP.isEnable(context)) {
            clickablePreferenceItem(R.string.widget_clock_day_horizontal) {
                PreferenceView(title = stringResource(it)) {
                    context.startActivity(Intent(context, ClockDayHorizontalWidgetConfigActivity::class.java))
                }
            }
        }
        if (ClockDayDetailsWidgetIMP.isEnable(context)) {
            clickablePreferenceItem(R.string.widget_clock_day_details) {
                PreferenceView(title = stringResource(it)) {
                    context.startActivity(Intent(context, ClockDayDetailsWidgetConfigActivity::class.java))
                }
            }
        }
        if (ClockDayVerticalWidgetIMP.isEnable(context)) {
            clickablePreferenceItem(R.string.widget_clock_day_vertical) {
                PreferenceView(title = stringResource(it)) {
                    context.startActivity(Intent(context, ClockDayVerticalWidgetConfigActivity::class.java))
                }
            }
        }
        if (ClockDayWeekWidgetIMP.isEnable(context)) {
            clickablePreferenceItem(R.string.widget_clock_day_week) {
                PreferenceView(title = stringResource(it)) {
                    context.startActivity(Intent(context, ClockDayWeekWidgetConfigActivity::class.java))
                }
            }
        }
        if (TextWidgetIMP.isEnable(context)) {
            clickablePreferenceItem(R.string.widget_text) {
                PreferenceView(title = stringResource(it)) {
                    context.startActivity(Intent(context, TextWidgetConfigActivity::class.java))
                }
            }
        }
        if (DailyTrendWidgetIMP.isEnable(context)) {
            clickablePreferenceItem(R.string.widget_trend_daily) {
                PreferenceView(title = stringResource(it)) {
                    context.startActivity(Intent(context, DailyTrendWidgetConfigActivity::class.java))
                }
            }
        }
        if (HourlyTrendWidgetIMP.isEnable(context)) {
            clickablePreferenceItem(R.string.widget_trend_hourly) {
                PreferenceView(title = stringResource(it)) {
                    context.startActivity(Intent(context, HourlyTrendWidgetConfigActivity::class.java))
                }
            }
        }
        if (MultiCityWidgetIMP.isEnable(context)) {
            clickablePreferenceItem(R.string.widget_multi_city) {
                PreferenceView(title = stringResource(it)) {
                    context.startActivity(Intent(context, MultiCityWidgetConfigActivity::class.java))
                }
            }
        }
        sectionFooterItem(R.string.settings_category_widget)

        // notification.
        sectionHeaderItem(R.string.settings_category_notification)
        switchPreferenceItem(R.string.settings_title_notification) { id ->
            SwitchPreferenceView(
                titleId = id,
                summaryOnId = R.string.on,
                summaryOffId = R.string.off,
                checked = notificationEnabledState.value,
                onValueChanged = {
                    SettingsManager.getInstance(context).isNotificationEnabled = it
                    notificationEnabledState.value = it

                    if (it) { // open notification.
                        postNotificationPermissionEnsurer {
                            PollingManager.resetNormalBackgroundTask(context, true)
                        }
                    } else { // close notification.
                        NormalNotificationIMP.cancelNotification(context)
                        PollingManager.resetNormalBackgroundTask(context, false)
                    }
                }
            )
        }
        listPreferenceItem(R.string.settings_title_notification_style) { id ->
            ListPreferenceView(
                titleId = id,
                selectedKey = SettingsManager.getInstance(context).notificationStyle.id,
                valueArrayId = R.array.notification_style_values,
                nameArrayId = R.array.notification_styles,
                enabled = notificationEnabledState.value,
                onValueChanged = {
                    SettingsManager
                        .getInstance(context)
                        .notificationStyle = NotificationStyle.getInstance(it)
                    PollingManager.resetNormalBackgroundTask(context, true)
                },
            )
        }
        switchPreferenceItem(R.string.settings_title_notification_temp_icon) { id ->
            SwitchPreferenceView(
                titleId = id,
                summaryOnId = R.string.on,
                summaryOffId = R.string.off,
                checked = SettingsManager
                    .getInstance(context)
                    .isNotificationTemperatureIconEnabled,
                enabled = notificationEnabledState.value,
                onValueChanged = {
                    SettingsManager
                        .getInstance(context)
                        .isNotificationTemperatureIconEnabled = it
                    notificationTemperatureIconEnabledState.value = it

                    PollingManager.resetNormalBackgroundTask(context, true)
                }
            )
        }
        switchPreferenceItem(R.string.settings_title_notification_feels_like) { id ->
            SwitchPreferenceView(
                titleId = id,
                summaryOnId = R.string.on,
                summaryOffId = R.string.off,
                checked = SettingsManager
                    .getInstance(context)
                    .isNotificationFeelsLike,
                enabled = notificationEnabledState.value
                        && notificationTemperatureIconEnabledState.value,
                onValueChanged = {
                    SettingsManager
                        .getInstance(context)
                        .isNotificationFeelsLike = it
                    PollingManager.resetNormalBackgroundTask(context, true)
                }
            )
        }
        switchPreferenceItem(R.string.settings_title_notification_can_be_cleared) { id ->
            SwitchPreferenceView(
                titleId = id,
                summaryOnId = R.string.on,
                summaryOffId = R.string.off,
                checked = SettingsManager
                    .getInstance(context)
                    .isNotificationCanBeClearedEnabled,
                enabled = notificationEnabledState.value,
                onValueChanged = {
                    SettingsManager
                        .getInstance(context)
                        .isNotificationCanBeClearedEnabled = it
                    PollingManager.resetNormalBackgroundTask(context, true)
                }
            )
        }
        sectionFooterItem(R.string.settings_category_notification)

        bottomInsetItem()
    }
}

@RequiresApi(api = Build.VERSION_CODES.O)
private fun showBlockNotificationGroupDialog(context: Context) {
    MaterialAlertDialogBuilder(context)
        .setTitle(R.string.feedback_interpret_notification_group_title)
        .setMessage(R.string.feedback_interpret_notification_group_content)
        .setPositiveButton(R.string.go_to_set) { _, _ ->
            val intent = Intent()
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(
                Settings.EXTRA_APP_PACKAGE,
                context.packageName
            )
            context.startActivity(intent)
            showIgnoreBatteryOptimizationDialog(context)
        }
        .setNeutralButton(
            R.string.done
        ) { _, _ ->
            showIgnoreBatteryOptimizationDialog(context)
        }
        .setCancelable(false)
        .show()
}

@RequiresApi(api = Build.VERSION_CODES.M)
private fun showIgnoreBatteryOptimizationDialog(context: Context) {
    MaterialAlertDialogBuilder(context)
        .setTitle(R.string.feedback_ignore_battery_optimizations_title)
        .setMessage(R.string.feedback_ignore_battery_optimizations_content)
        .setPositiveButton(
            R.string.go_to_set
        ) { _, _ ->
            IntentHelper.startBatteryOptimizationActivity(context)
        }
        .setNeutralButton(R.string.done) { _, _ -> }
        .setCancelable(false)
        .show()
}
