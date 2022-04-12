package wangdaye.com.geometricweather.settings.compose

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
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
import wangdaye.com.geometricweather.remoteviews.config.*
import wangdaye.com.geometricweather.remoteviews.presenters.*
import wangdaye.com.geometricweather.remoteviews.presenters.notification.NormalNotificationIMP
import wangdaye.com.geometricweather.settings.SettingsManager
import wangdaye.com.geometricweather.theme.ThemeManager

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

@Composable
fun RootSettingsView(context: Context, navController: NavHostController) {
    LazyColumn(
        modifier = Modifier.fillMaxHeight()
    ) {
        // basic.
        item { SectionHeader(title = stringResource(R.string.settings_category_basic)) }
        item {
            CheckboxPreferenceView(
                title = stringResource(R.string.settings_title_background_free),
                summary = {
                    stringResource(
                        if (it) R.string.settings_summary_background_free_on
                        else R.string.settings_summary_background_free_off
                    )
                },
                checked = SettingsManager.getInstance(context).isBackgroundFree,
                onValueChanged = {
                    SettingsManager.getInstance(context).isBackgroundFree = it
                    
                    PollingManager.resetNormalBackgroundTask(context, false)
                    if (!it) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            showBlockNotificationGroupDialog(context)
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            showIgnoreBatteryOptimizationDialog(context)
                        }
                    }
                }
            )
        }
        item {
            CheckboxPreferenceView(
                title = stringResource(R.string.settings_title_alert_notification_switch),
                summary = { stringResource(if (it) R.string.on else R.string.off) },
                checked = SettingsManager.getInstance(context).isAlertPushEnabled,
                onValueChanged = {
                    SettingsManager.getInstance(context).isAlertPushEnabled = it
                    PollingManager.resetNormalBackgroundTask(context, false)
                }
            )
        }
        item {
            CheckboxPreferenceView(
                title = stringResource(R.string.settings_title_precipitation_notification_switch),
                summary = { stringResource(if (it) R.string.on else R.string.off) },
                checked = SettingsManager.getInstance(context).isPrecipitationPushEnabled,
                onValueChanged = {
                    SettingsManager.getInstance(context).isPrecipitationPushEnabled = it
                }
            )
        }
        item {
            val valueList = stringArrayResource(R.array.automatic_refresh_rate_values)
            val nameList = stringArrayResource(R.array.automatic_refresh_rates)
            ListPreferenceView(
                title = stringResource(R.string.settings_title_refresh_rate),
                summary = { key -> nameList[valueList.indexOfFirst { it == key }] },
                selectedKey = SettingsManager.getInstance(context).updateInterval.id,
                keyNamePairList = valueList.zip(nameList),
                onValueChanged = {
                    SettingsManager
                        .getInstance(context)
                        .updateInterval = UpdateInterval.getInstance(it)
                }
            )
        }
        item {
            val valueList = stringArrayResource(R.array.dark_mode_values)
            val nameList = stringArrayResource(R.array.dark_modes)
            ListPreferenceView(
                title = stringResource(R.string.settings_title_dark_mode),
                summary = { key -> nameList[valueList.indexOfFirst { it == key }] },
                selectedKey = SettingsManager.getInstance(context).darkMode.id,
                keyNamePairList = valueList.zip(nameList),
                onValueChanged = {
                    SettingsManager
                        .getInstance(context)
                        .darkMode = DarkMode.getInstance(it)

                    AsyncHelper.delayRunOnUI({
                        ThemeManager
                            .getInstance(context)
                            .update(darkMode = SettingsManager.getInstance(context).darkMode)
                    },300)
                }
            )
        }
        item {
            PreferenceView(
                title = stringResource(R.string.settings_title_live_wallpaper),
                summary = stringResource(R.string.settings_summary_live_wallpaper),
            ) {
                IntentHelper.startLiveWallpaperActivity(context)
            }
        }
        item {
            PreferenceView(
                title = stringResource(R.string.settings_title_service_provider),
                summary = stringResource(R.string.settings_summary_service_provider),
            ) {
                navController.navigate(SettingsScreenRouter.ServiceProvider.route)
            }
        }
        item {
            PreferenceView(
                title = stringResource(R.string.settings_title_unit),
                summary = stringResource(R.string.settings_summary_unit),
            ) {
                navController.navigate(SettingsScreenRouter.Unit.route)
            }
        }
        item {
            PreferenceView(
                title = stringResource(R.string.settings_title_appearance),
                summary = stringResource(R.string.settings_summary_appearance),
            ) {
                navController.navigate(SettingsScreenRouter.Appearance.route)
            }
        }
        item { SectionFooter() }

        // forecast.
        item { SectionHeader(title = stringResource(R.string.settings_category_forecast)) }
        item {
            val todayForecastEnabledState = remember {
                mutableStateOf(
                    SettingsManager
                        .getInstance(context)
                        .isTodayForecastEnabled
                )
            }
            CheckboxPreferenceView(
                title = stringResource(R.string.settings_title_forecast_today),
                summary = { stringResource(if (it) R.string.on else R.string.off) },
                checked = todayForecastEnabledState.value,
                onValueChanged = {
                    todayForecastEnabledState.value = it
                    PollingManager.resetNormalBackgroundTask(context, false)
                }
            )
            TimePickerPreferenceView(
                title = stringResource(R.string.settings_title_forecast_today_time),
                summary = { it },
                currentTime = SettingsManager.getInstance(context).todayForecastTime,
                enabled = todayForecastEnabledState.value,
                onValueChanged = {
                    SettingsManager.getInstance(context).todayForecastTime = it
                    PollingManager.resetTodayForecastBackgroundTask(
                        context,
                        false,
                        false
                    )
                }
            )
        }
        item {
            val tomorrowForecastEnabledState = remember {
                mutableStateOf(
                    SettingsManager
                        .getInstance(context)
                        .isTomorrowForecastEnabled
                )
            }
            CheckboxPreferenceView(
                title = stringResource(R.string.settings_title_forecast_tomorrow),
                summary = { stringResource(if (it) R.string.on else R.string.off) },
                checked = tomorrowForecastEnabledState.value,
                onValueChanged = {
                    tomorrowForecastEnabledState.value = it
                    PollingManager.resetNormalBackgroundTask(context, false)
                }
            )
            TimePickerPreferenceView(
                title = stringResource(R.string.settings_title_forecast_tomorrow_time),
                summary = { it },
                currentTime = SettingsManager.getInstance(context).tomorrowForecastTime,
                enabled = tomorrowForecastEnabledState.value,
                onValueChanged = {
                    SettingsManager.getInstance(context).tomorrowForecastTime = it
                    PollingManager.resetTodayForecastBackgroundTask(
                        context,
                        false,
                        true
                    )
                }
            )
        }
        item { SectionFooter() }

        // widget.
        item { SectionHeader(title = stringResource(R.string.settings_category_widget)) }
        item {
            val valueList = stringArrayResource(R.array.week_icon_mode_values)
            val nameList = stringArrayResource(R.array.week_icon_modes)
            ListPreferenceView(
                title = stringResource(R.string.settings_title_week_icon_mode),
                summary = { key -> nameList[valueList.indexOfFirst { it == key }] },
                selectedKey = SettingsManager.getInstance(context).widgetWeekIconMode.id,
                keyNamePairList = valueList.zip(nameList),
                onValueChanged = {
                    SettingsManager
                        .getInstance(context)
                        .widgetWeekIconMode = WidgetWeekIconMode.getInstance(it)
                    PollingManager.resetNormalBackgroundTask(context, true)
                }
            )
            CheckboxPreferenceView(
                title = stringResource(R.string.settings_title_minimal_icon),
                summary = { stringResource(if (it) R.string.on else R.string.off) },
                checked = SettingsManager.getInstance(context).isWidgetMinimalIconEnabled,
                onValueChanged = {
                    SettingsManager.getInstance(context).isWidgetMinimalIconEnabled = it
                    PollingManager.resetNormalBackgroundTask(context, true)
                }
            )
        }
        if (DayWidgetIMP.isEnable(context)) {
            item {
                PreferenceView(title = stringResource(R.string.key_widget_day)) {
                    context.startActivity(Intent(context, DayWidgetConfigActivity::class.java))
                }
            }
        }
        if (WeekWidgetIMP.isEnable(context)) {
            item {
                PreferenceView(title = stringResource(R.string.key_widget_week)) {
                    context.startActivity(Intent(context, WeekWidgetConfigActivity::class.java))
                }
            }
        }
        if (DayWeekWidgetIMP.isEnable(context)) {
            item {
                PreferenceView(title = stringResource(R.string.key_widget_day_week)) {
                    context.startActivity(Intent(context, DayWeekWidgetConfigActivity::class.java))
                }
            }
        }
        if (ClockDayHorizontalWidgetIMP.isEnable(context)) {
            item {
                PreferenceView(title = stringResource(R.string.key_widget_clock_day_horizontal)) {
                    context.startActivity(Intent(context, ClockDayHorizontalWidgetConfigActivity::class.java))
                }
            }
        }
        if (ClockDayDetailsWidgetIMP.isEnable(context)) {
            item {
                PreferenceView(title = stringResource(R.string.key_widget_clock_day_details)) {
                    context.startActivity(Intent(context, ClockDayDetailsWidgetConfigActivity::class.java))
                }
            }
        }
        if (ClockDayVerticalWidgetIMP.isEnable(context)) {
            item {
                PreferenceView(title = stringResource(R.string.key_widget_clock_day_vertical)) {
                    context.startActivity(Intent(context, ClockDayVerticalWidgetConfigActivity::class.java))
                }
            }
        }
        if (ClockDayWeekWidgetIMP.isEnable(context)) {
            item {
                PreferenceView(title = stringResource(R.string.key_widget_clock_day_week)) {
                    context.startActivity(Intent(context, ClockDayWeekWidgetConfigActivity::class.java))
                }
            }
        }
        if (TextWidgetIMP.isEnable(context)) {
            item {
                PreferenceView(title = stringResource(R.string.key_widget_text)) {
                    context.startActivity(Intent(context, TextWidgetConfigActivity::class.java))
                }
            }
        }
        if (DailyTrendWidgetIMP.isEnable(context)) {
            item {
                PreferenceView(title = stringResource(R.string.key_widget_trend_daily)) {
                    context.startActivity(Intent(context, DailyTrendWidgetConfigActivity::class.java))
                }
            }
        }
        if (HourlyTrendWidgetIMP.isEnable(context)) {
            item {
                PreferenceView(title = stringResource(R.string.key_widget_trend_hourly)) {
                    context.startActivity(Intent(context, HourlyTrendWidgetConfigActivity::class.java))
                }
            }
        }
        if (MultiCityWidgetIMP.isEnable(context)) {
            item {
                PreferenceView(title = stringResource(R.string.key_widget_multi_city)) {
                    context.startActivity(Intent(context, MultiCityWidgetConfigActivity::class.java))
                }
            }
        }
        item { SectionFooter() }

        // notification.
        item { SectionHeader(title = stringResource(R.string.settings_category_notification)) }
        item {
            val notificationEnabledState = remember {
                mutableStateOf(
                    SettingsManager
                        .getInstance(context)
                        .isNotificationEnabled
                )
            }

            CheckboxPreferenceView(
                title = stringResource(R.string.settings_title_notification),
                summary = { stringResource(if (it) R.string.on else R.string.off) },
                checked = notificationEnabledState.value,
                onValueChanged = {
                    SettingsManager.getInstance(context).isNotificationEnabled = it
                    notificationEnabledState.value = it

                    if (it) { // open notification.
                        PollingManager.resetNormalBackgroundTask(context, true)
                    } else { // close notification.
                        NormalNotificationIMP.cancelNotification(context)
                        PollingManager.resetNormalBackgroundTask(context, false)
                    }
                }
            )

            val valueList = stringArrayResource(R.array.notification_style_values)
            val nameList = stringArrayResource(R.array.notification_styles)
            ListPreferenceView(
                title = stringResource(R.string.settings_title_notification_style),
                summary = { key -> nameList[valueList.indexOfFirst { it == key }] },
                selectedKey = SettingsManager.getInstance(context).notificationStyle.id,
                keyNamePairList = valueList.zip(nameList),
                enabled = notificationEnabledState.value,
                onValueChanged = {
                    SettingsManager
                        .getInstance(context)
                        .notificationStyle = NotificationStyle.getInstance(it)
                    PollingManager.resetNormalBackgroundTask(context, true)
                }
            )

            CheckboxPreferenceView(
                title = stringResource(R.string.settings_title_notification_temp_icon),
                summary = { stringResource(if (it) R.string.on else R.string.off) },
                checked = SettingsManager
                    .getInstance(context)
                    .isNotificationTemperatureIconEnabled,
                enabled = notificationEnabledState.value,
                onValueChanged = {
                    SettingsManager
                        .getInstance(context)
                        .isNotificationTemperatureIconEnabled = it
                    PollingManager.resetNormalBackgroundTask(context, true)
                }
            )
            CheckboxPreferenceView(
                title = stringResource(R.string.settings_title_notification_can_be_cleared),
                summary = { stringResource(if (it) R.string.on else R.string.off) },
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
        item { SectionFooter() }
        item { Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)) }
    }
}