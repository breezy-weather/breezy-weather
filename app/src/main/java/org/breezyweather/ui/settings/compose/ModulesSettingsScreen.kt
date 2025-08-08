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

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.NotificationStyle
import org.breezyweather.common.basic.models.options.WidgetWeekIconMode
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.plus
import org.breezyweather.common.source.BroadcastSource
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.settings.SourceConfigStore
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
import org.breezyweather.ui.common.composables.AnimatedVisibilitySlideVertically
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.generateCollapsedScrollBehavior
import org.breezyweather.ui.common.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.ui.settings.preference.bottomInsetItem
import org.breezyweather.ui.settings.preference.clickablePreferenceItem
import org.breezyweather.ui.settings.preference.composables.ListPreferenceView
import org.breezyweather.ui.settings.preference.composables.PackagePreferenceView
import org.breezyweather.ui.settings.preference.composables.PreferenceScreen
import org.breezyweather.ui.settings.preference.composables.PreferenceViewWithCard
import org.breezyweather.ui.settings.preference.composables.SwitchPreferenceView
import org.breezyweather.ui.settings.preference.largeSeparatorItem
import org.breezyweather.ui.settings.preference.listPreferenceItem
import org.breezyweather.ui.settings.preference.sectionFooterItem
import org.breezyweather.ui.settings.preference.sectionHeaderItem
import org.breezyweather.ui.settings.preference.smallSeparatorItem
import org.breezyweather.ui.settings.preference.switchPreferenceItem
import org.breezyweather.wallpaper.MaterialLiveWallpaperService
import java.text.Collator

@Composable
fun ModulesSettingsScreen(
    context: Context,
    onNavigateBack: () -> Unit,
    hasNotificationPermission: Boolean,
    notificationEnabled: Boolean,
    notificationTemperatureIconEnabled: Boolean,
    postNotificationPermissionEnsurer: (succeedCallback: () -> Unit) -> Unit,
    updateWidgetIfNecessary: (Context) -> Unit,
    updateNotificationIfNecessary: (Context) -> Unit,
    broadcastDataIfNecessary: (Context, String) -> Unit,
    broadcastSources: ImmutableList<BroadcastSource>,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = generateCollapsedScrollBehavior()

    Material3Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            FitStatusBarTopAppBar(
                title = stringResource(R.string.settings_modules),
                onBackPressed = onNavigateBack,
                actions = { AboutActivityIconButton(context) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddings ->
        PreferenceScreen(
            paddingValues = paddings.plus(PaddingValues(horizontal = dimensionResource(R.dimen.normal_margin)))
        ) {
            // widget.
            sectionHeaderItem(R.string.settings_modules_section_general)
            clickablePreferenceItem(R.string.settings_modules_live_wallpaper_title) { id ->
                PreferenceViewWithCard(
                    titleId = id,
                    summaryId = R.string.settings_modules_live_wallpaper_summary,
                    isFirst = true
                ) {
                    try {
                        context.startActivity(
                            Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                                .putExtra(
                                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                                    ComponentName(context, MaterialLiveWallpaperService::class.java)
                                )
                                .addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                                )
                        )
                    } catch (e: ActivityNotFoundException) {
                        try {
                            context.startActivity(
                                Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                                    .addFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                                    )
                            )
                        } catch (e2: ActivityNotFoundException) {
                            SnackbarHelper.showSnackbar(
                                context.getString(
                                    R.string.settings_modules_live_wallpaper_error,
                                    context.getString(R.string.breezy_weather)
                                )
                            )
                        }
                    }
                }
            }
            smallSeparatorItem()
            listPreferenceItem(R.string.settings_modules_week_icon_mode_title) { id ->
                ListPreferenceView(
                    titleId = id,
                    selectedKey = SettingsManager.getInstance(context).widgetWeekIconMode.id,
                    valueArrayId = R.array.week_icon_mode_values,
                    nameArrayId = R.array.week_icon_modes,
                    card = true,
                    onValueChanged = {
                        SettingsManager
                            .getInstance(context)
                            .widgetWeekIconMode = WidgetWeekIconMode.getInstance(it)
                        updateWidgetIfNecessary(context)
                    }
                )
            }
            smallSeparatorItem()
            switchPreferenceItem(R.string.settings_modules_monochrome_icons_title) { id ->
                SwitchPreferenceView(
                    titleId = id,
                    summaryOnId = R.string.settings_enabled,
                    summaryOffId = R.string.settings_disabled,
                    checked = SettingsManager.getInstance(context).isWidgetUsingMonochromeIcons,
                    isLast = true,
                    onValueChanged = {
                        SettingsManager.getInstance(context).isWidgetUsingMonochromeIcons = it
                        updateWidgetIfNecessary(context)
                    }
                )
            }
            smallSeparatorItem()
            sectionFooterItem(R.string.settings_modules_section_general)

            val widgetsInUse = buildList {
                if (DayWidgetIMP.isInUse(context)) {
                    add(Pair(R.string.widget_day, DayWidgetConfigActivity::class.java))
                }
                if (WeekWidgetIMP.isInUse(context)) {
                    add(Pair(R.string.widget_week, WeekWidgetConfigActivity::class.java))
                }
                if (DayWeekWidgetIMP.isInUse(context)) {
                    add(Pair(R.string.widget_day_week, DayWeekWidgetConfigActivity::class.java))
                }
                if (ClockDayHorizontalWidgetIMP.isInUse(context)) {
                    add(Pair(R.string.widget_clock_day_horizontal, ClockDayHorizontalWidgetConfigActivity::class.java))
                }
                if (ClockDayDetailsWidgetIMP.isInUse(context)) {
                    add(Pair(R.string.widget_clock_day_details, ClockDayDetailsWidgetConfigActivity::class.java))
                }
                if (ClockDayVerticalWidgetIMP.isInUse(context)) {
                    add(Pair(R.string.widget_clock_day_vertical, ClockDayVerticalWidgetConfigActivity::class.java))
                }
                if (ClockDayWeekWidgetIMP.isInUse(context)) {
                    add(Pair(R.string.widget_clock_day_week, ClockDayWeekWidgetConfigActivity::class.java))
                }
                if (TextWidgetIMP.isInUse(context)) {
                    add(Pair(R.string.widget_text, TextWidgetConfigActivity::class.java))
                }
                if (DailyTrendWidgetIMP.isInUse(context)) {
                    add(Pair(R.string.widget_trend_daily, DailyTrendWidgetConfigActivity::class.java))
                }
                if (HourlyTrendWidgetIMP.isInUse(context)) {
                    add(Pair(R.string.widget_trend_hourly, HourlyTrendWidgetConfigActivity::class.java))
                }
                if (MultiCityWidgetIMP.isInUse(context)) {
                    add(Pair(R.string.widget_multi_city, MultiCityWidgetConfigActivity::class.java))
                }
            }
            if (widgetsInUse.isNotEmpty()) {
                largeSeparatorItem()
                sectionHeaderItem(R.string.settings_modules_section_widgets_in_use)
                widgetsInUse.forEachIndexed { index, widget ->
                    clickablePreferenceItem(widget.first) {
                        PreferenceViewWithCard(
                            title = stringResource(it),
                            summary = stringResource(R.string.settings_modules_configure_widget_summary),
                            isFirst = index == 0,
                            isLast = index == widgetsInUse.lastIndex
                        ) {
                            context.startActivity(Intent(context, widget.second))
                        }
                    }
                    if (index != widgetsInUse.lastIndex) {
                        smallSeparatorItem()
                    }
                }
                sectionFooterItem(R.string.settings_modules_section_widgets_in_use)
            }

            largeSeparatorItem()

            // notification.
            sectionHeaderItem(R.string.settings_modules_section_notification_widget)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listPreferenceItem(R.string.settings_notifications_permission) { title ->
                    AnimatedVisibilitySlideVertically(
                        visible = !hasNotificationPermission
                    ) {
                        PreferenceViewWithCard(
                            iconId = R.drawable.ic_about,
                            title = stringResource(title),
                            summary = stringResource(
                                R.string.settings_modules_notification_permission_summary,
                                stringResource(R.string.action_grant_permission)
                            ),
                            surface = MaterialTheme.colorScheme.primaryContainer,
                            onSurface = MaterialTheme.colorScheme.onPrimaryContainer,
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            ),
                            isFirst = true,
                            isLast = true,
                            onClick = {
                                postNotificationPermissionEnsurer {
                                    updateNotificationIfNecessary(context)
                                }
                            }
                        )
                        largeSeparatorItem()
                    }
                }
            }
            switchPreferenceItem(R.string.settings_modules_notification_widget_title) { id ->
                SwitchPreferenceView(
                    titleId = id,
                    summaryOnId = R.string.settings_enabled,
                    summaryOffId = R.string.settings_disabled,
                    checked = notificationEnabled,
                    withState = false,
                    enabled = hasNotificationPermission,
                    isFirst = true,
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
            smallSeparatorItem()
            switchPreferenceItem(R.string.settings_modules_notification_persistent_switch) { id ->
                SwitchPreferenceView(
                    titleId = id,
                    summaryOnId = R.string.settings_enabled,
                    summaryOffId = R.string.settings_disabled,
                    checked = SettingsManager
                        .getInstance(context)
                        .isWidgetNotificationPersistent,
                    enabled = notificationEnabled && hasNotificationPermission,
                    onValueChanged = {
                        SettingsManager
                            .getInstance(context)
                            .isWidgetNotificationPersistent = it
                        updateNotificationIfNecessary(context)
                    }
                )
            }
            smallSeparatorItem()
            listPreferenceItem(R.string.settings_modules_notification_style_title) { id ->
                ListPreferenceView(
                    titleId = id,
                    selectedKey = SettingsManager.getInstance(context).widgetNotificationStyle.id,
                    valueArrayId = R.array.notification_style_values,
                    nameArrayId = R.array.notification_styles,
                    enabled = notificationEnabled && hasNotificationPermission,
                    card = true,
                    isLast = Build.VERSION.SDK_INT < Build.VERSION_CODES.M,
                    onValueChanged = {
                        SettingsManager
                            .getInstance(context)
                            .widgetNotificationStyle = NotificationStyle.getInstance(it)
                        updateNotificationIfNecessary(context)
                    }
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                smallSeparatorItem()
                switchPreferenceItem(R.string.settings_modules_notification_temp_icon_switch) { id ->
                    SwitchPreferenceView(
                        titleId = id,
                        summaryOnId = R.string.settings_enabled,
                        summaryOffId = R.string.settings_disabled,
                        checked = SettingsManager
                            .getInstance(context)
                            .isWidgetNotificationTemperatureIconEnabled,
                        enabled = notificationEnabled && hasNotificationPermission,
                        onValueChanged = {
                            SettingsManager
                                .getInstance(context)
                                .isWidgetNotificationTemperatureIconEnabled = it
                            updateNotificationIfNecessary(context)
                        }
                    )
                }
                smallSeparatorItem()
                switchPreferenceItem(R.string.settings_modules_notification_feels_like_switch) { id ->
                    SwitchPreferenceView(
                        titleId = id,
                        summaryOnId = R.string.settings_enabled,
                        summaryOffId = R.string.settings_disabled,
                        checked = SettingsManager
                            .getInstance(context)
                            .isWidgetNotificationUsingFeelsLike,
                        enabled = notificationEnabled &&
                            hasNotificationPermission &&
                            notificationTemperatureIconEnabled,
                        isLast = true,
                        onValueChanged = {
                            SettingsManager
                                .getInstance(context)
                                .isWidgetNotificationUsingFeelsLike = it
                            updateNotificationIfNecessary(context)
                        }
                    )
                }
            }
            sectionFooterItem(R.string.settings_modules_section_notification_widget)

            largeSeparatorItem()

            sectionHeaderItem(R.string.settings_modules_broadcast_title)
            broadcastSources
                .sortedWith { ws1, ws2 ->
                    Collator.getInstance(context.currentLocale).compare(ws1.name, ws2.name)
                }
                .forEachIndexed { index, broadcastSource ->
                    item(key = broadcastSource.id) {
                        val config = SourceConfigStore(context, broadcastSource.id)
                        val enabledPackages = (config.getString("packages", null) ?: "").let {
                            if (it.isNotEmpty()) {
                                it.split(",").toImmutableList()
                            } else {
                                persistentListOf()
                            }
                        }
                        PackagePreferenceView(
                            title = stringResource(
                                R.string.settings_modules_broadcast_send_data_title,
                                broadcastSource.name
                            ),
                            intent = broadcastSource.intentAction,
                            selectedKeys = enabledPackages,
                            isFirst = index == 0,
                            isLast = index == broadcastSources.lastIndex
                        ) {
                            config.edit().putString("packages", it.joinToString(",")).apply()
                            broadcastDataIfNecessary(context, broadcastSource.id)
                        }
                    }
                    if (index != broadcastSources.lastIndex) {
                        smallSeparatorItem()
                    }
                }
            sectionFooterItem(R.string.settings_modules_broadcast_title)

            bottomInsetItem()
        }
    }
}
