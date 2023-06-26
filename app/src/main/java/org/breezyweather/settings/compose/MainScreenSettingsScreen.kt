package org.breezyweather.settings.compose

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.breezyweather.BreezyWeather.Companion.instance
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.appearance.*
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.preference.*
import org.breezyweather.settings.preference.composables.ListPreferenceView
import org.breezyweather.settings.preference.composables.PreferenceView
import org.breezyweather.settings.preference.composables.SwitchPreferenceView

@Composable
fun MainScreenSettingsScreen(
    context: Context,
    cardDisplayList: List<CardDisplay>,
    dailyTrendDisplayList: List<DailyTrendDisplay>,
    hourlyTrendDisplayList: List<HourlyTrendDisplay>,
    detailDisplayList: List<DetailDisplay>,
    paddingValues: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentPadding = paddingValues,
    ) {
        sectionHeaderItem(R.string.settings_main_section_displayed_data)
        clickablePreferenceItem(
            R.string.settings_main_cards_title
        ) {
            PreferenceView(
                title = stringResource(it),
                summary = CardDisplay.getSummary(context, cardDisplayList),
            ) {
                (context as? Activity)?.let { a ->
                    IntentHelper.startCardDisplayManageActivity(a)
                }
            }
        }
        clickablePreferenceItem(
            R.string.settings_main_daily_trends_title
        ) {
            PreferenceView(
                title = stringResource(it),
                summary = DailyTrendDisplay.getSummary(context, dailyTrendDisplayList),
            ) {
                (context as? Activity)?.let { a ->
                    IntentHelper.startDailyTrendDisplayManageActivity(a)
                }
            }
        }
        clickablePreferenceItem(
            R.string.settings_main_hourly_trends_title
        ) {
            PreferenceView(
                title = stringResource(it),
                summary = HourlyTrendDisplay.getSummary(context, hourlyTrendDisplayList),
            ) {
                (context as? Activity)?.let { a ->
                    IntentHelper.startHourlyTrendDisplayManageActivityForResult(a)
                }
            }
        }
        clickablePreferenceItem(
            R.string.settings_main_header_details_title
        ) {
            PreferenceView(
                title = stringResource(it),
                summary = DetailDisplay.getSummary(context, detailDisplayList),
            ) {
                (context as? Activity)?.let { a ->
                    IntentHelper.startDetailDisplayManageActivity(a)
                }
            }
        }
        sectionFooterItem(R.string.settings_main_section_displayed_data)

        sectionHeaderItem(R.string.settings_main_section_options)
        switchPreferenceItem(R.string.settings_main_yesterday_alert_lines_in_trends) { id ->
            SwitchPreferenceView(
                titleId = id,
                summaryOnId = R.string.settings_enabled,
                summaryOffId = R.string.settings_disabled,
                checked = SettingsManager.getInstance(context).isTrendHorizontalLinesEnabled,
                onValueChanged = {
                    SettingsManager.getInstance(context).isTrendHorizontalLinesEnabled = it
                },
            )
        }
        sectionFooterItem(R.string.settings_main_section_options)

        sectionHeaderItem(R.string.settings_main_section_animations)
        listPreferenceItem(R.string.settings_main_background_animation_title) { id ->
            ListPreferenceView(
                titleId = id,
                selectedKey = SettingsManager.getInstance(context).backgroundAnimationMode.id,
                valueArrayId = R.array.background_animation_values,
                nameArrayId = R.array.background_animation,
                onValueChanged = {
                    SettingsManager
                        .getInstance(context)
                        .backgroundAnimationMode = BackgroundAnimationMode.getInstance(it)

                    SnackbarHelper.showSnackbar(
                        context.getString(R.string.settings_changes_apply_after_restart),
                        context.getString(R.string.action_restart)
                    ) {
                        instance.recreateAllActivities()
                    }
                },
            )
        }
        switchPreferenceItem(R.string.settings_main_gravity_sensor_switch) { id ->
            SwitchPreferenceView(
                titleId = id,
                summaryOnId = R.string.settings_enabled,
                summaryOffId = R.string.settings_disabled,
                checked = SettingsManager.getInstance(context).isGravitySensorEnabled,
                onValueChanged = {
                    SettingsManager.getInstance(context).isGravitySensorEnabled = it

                    SnackbarHelper.showSnackbar(
                        context.getString(R.string.settings_changes_apply_after_restart),
                        context.getString(R.string.action_restart)
                    ) {
                        instance.recreateAllActivities()
                    }
                },
            )
        }
        switchPreferenceItem(R.string.settings_main_cards_fade_in_switch) { id ->
            SwitchPreferenceView(
                titleId = id,
                summaryOnId = R.string.settings_enabled,
                summaryOffId = R.string.settings_disabled,
                checked = SettingsManager.getInstance(context).isCardsFadeInEnabled,
                onValueChanged = {
                    SettingsManager.getInstance(context).isCardsFadeInEnabled = it
                },
            )
        }
        switchPreferenceItem(R.string.settings_main_cards_other_element_animations_switch) { id ->
            SwitchPreferenceView(
                titleId = id,
                summaryOnId = R.string.settings_enabled,
                summaryOffId = R.string.settings_disabled,
                checked = SettingsManager.getInstance(context).isElementsAnimationEnabled,
                onValueChanged = {
                    SettingsManager.getInstance(context).isElementsAnimationEnabled = it
                },
            )
        }
        sectionFooterItem(R.string.settings_main_section_animations)

        bottomInsetItem()
    }
}