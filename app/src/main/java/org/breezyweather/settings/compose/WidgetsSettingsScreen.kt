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

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.NotificationStyle
import org.breezyweather.common.basic.models.options.WidgetWeekIconMode
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.BroadcastSource
import org.breezyweather.common.ui.widgets.Material3Scaffold
import org.breezyweather.common.ui.widgets.generateCollapsedScrollBehavior
import org.breezyweather.common.ui.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.remoteviews.config.ClockDayDetailsWidgetConfigActivity
import org.breezyweather.remoteviews.config.ClockDayHorizontalWidgetConfigActivity
import org.breezyweather.remoteviews.config.ClockDayVerticalWidgetConfigActivity
import org.breezyweather.remoteviews.config.ClockDayWeekWidgetConfigActivity
import org.breezyweather.remoteviews.config.DailyTrendWidgetConfigActivity
import org.breezyweather.remoteviews.config.DayWeekWidgetConfigActivity
import org.breezyweather.remoteviews.config.DayWidgetConfigActivity
import org.breezyweather.remoteviews.config.HourlyTrendWidgetConfigActivity
import org.breezyweather.remoteviews.config.MultiCityWidgetConfigActivity
import org.breezyweather.remoteviews.config.TextWidgetConfigActivity
import org.breezyweather.remoteviews.config.WeekWidgetConfigActivity
import org.breezyweather.remoteviews.presenters.ClockDayDetailsWidgetIMP
import org.breezyweather.remoteviews.presenters.ClockDayHorizontalWidgetIMP
import org.breezyweather.remoteviews.presenters.ClockDayVerticalWidgetIMP
import org.breezyweather.remoteviews.presenters.ClockDayWeekWidgetIMP
import org.breezyweather.remoteviews.presenters.DailyTrendWidgetIMP
import org.breezyweather.remoteviews.presenters.DayWeekWidgetIMP
import org.breezyweather.remoteviews.presenters.DayWidgetIMP
import org.breezyweather.remoteviews.presenters.HourlyTrendWidgetIMP
import org.breezyweather.remoteviews.presenters.MultiCityWidgetIMP
import org.breezyweather.remoteviews.presenters.TextWidgetIMP
import org.breezyweather.remoteviews.presenters.WeekWidgetIMP
import org.breezyweather.remoteviews.presenters.notification.WidgetNotificationIMP
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.settings.preference.bottomInsetItem
import org.breezyweather.settings.preference.clickablePreferenceItem
import org.breezyweather.settings.preference.composables.ListPreferenceView
import org.breezyweather.settings.preference.composables.PackagePreferenceView
import org.breezyweather.settings.preference.composables.PreferenceScreen
import org.breezyweather.settings.preference.composables.PreferenceView
import org.breezyweather.settings.preference.composables.SwitchPreferenceView
import org.breezyweather.settings.preference.listPreferenceItem
import org.breezyweather.settings.preference.sectionFooterItem
import org.breezyweather.settings.preference.sectionHeaderItem
import org.breezyweather.settings.preference.switchPreferenceItem
import org.breezyweather.wallpaper.MaterialLiveWallpaperService
import java.text.Collator

@Composable
fun WidgetsSettingsScreen(
    context: Context,
    onNavigateBack: () -> Unit,
    notificationEnabled: Boolean,
    notificationTemperatureIconEnabled: Boolean,
    postNotificationPermissionEnsurer: (succeedCallback: () -> Unit) -> Unit,
    updateWidgetIfNecessary: (Context) -> Unit,
    updateNotificationIfNecessary: (Context) -> Unit,
    broadcastDataIfNecessary: (Context, String) -> Unit,
    broadcastSources: List<BroadcastSource>
) {
    val scrollBehavior = generateCollapsedScrollBehavior()

    Material3Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            FitStatusBarTopAppBar(
                title = stringResource(R.string.settings_widgets),
                onBackPressed = onNavigateBack,
                actions = { AboutActivityIconButton(context) },
                scrollBehavior = scrollBehavior
            )
        },
    ) { paddings ->
        PreferenceScreen(paddingValues = paddings) {
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
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS))
                    } catch (e: ActivityNotFoundException) {
                        try {
                    context.startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS))
                        } catch (e2: ActivityNotFoundException) {
                            SnackbarHelper.showSnackbar(
                                context.getString(
                                    R.string.settings_widgets_live_wallpaper_error,
                                    context.getString(R.string.breezy_weather)
                                )
                            )
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
                        updateWidgetIfNecessary(context)
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
                        updateWidgetIfNecessary(context)
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
                                updateNotificationIfNecessary(context)
                            }
                        } else { // close notification.
                            WidgetNotificationIMP.cancelNotification(context)
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
                        updateNotificationIfNecessary(context)
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
                        updateNotificationIfNecessary(context)
                    },
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                            updateNotificationIfNecessary(context)
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
                            updateNotificationIfNecessary(context)
                        }
                    )
                }
            }
            sectionFooterItem(R.string.settings_widgets_section_notification_widget)

            sectionHeaderItem(R.string.settings_widgets_broadcast_title)
            broadcastSources
                .sortedWith { ws1, ws2 ->
                    Collator.getInstance(context.currentLocale).compare(ws1.name, ws2.name)
                }
                .forEach { broadcastSource ->
                    item(key = broadcastSource.id) {
                        val config = SourceConfigStore(context, broadcastSource.id)
                        val enabledPackages = (config.getString("packages", null) ?: "").let {
                            if (it.isNotEmpty()) {
                                it.split(",")
                            } else emptyList()
                        }
                        PackagePreferenceView(
                    title = stringResource(R.string.settings_widgets_broadcast_send_data_title, broadcastSource.name),
                            intent = broadcastSource.intentAction,
                            selectedKeys = enabledPackages
                        ) {
                            config.edit().putString("packages", it.joinToString(",")).apply()
                            broadcastDataIfNecessary(context, broadcastSource.id)
                        }
                    }
                }
            sectionFooterItem(R.string.settings_widgets_broadcast_title)

            bottomInsetItem()
        }
    }
}
