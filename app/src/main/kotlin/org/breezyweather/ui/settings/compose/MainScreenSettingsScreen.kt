/*
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

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.common.extensions.isMotionReduced
import org.breezyweather.common.extensions.plus
import org.breezyweather.common.options.appearance.BackgroundAnimationMode
import org.breezyweather.common.options.appearance.CardDisplay
import org.breezyweather.common.options.appearance.DailyTrendDisplay
import org.breezyweather.common.options.appearance.HourlyTrendDisplay
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.generateCollapsedScrollBehavior
import org.breezyweather.ui.common.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.ui.settings.preference.bottomInsetItem
import org.breezyweather.ui.settings.preference.clickablePreferenceItem
import org.breezyweather.ui.settings.preference.composables.ListPreferenceView
import org.breezyweather.ui.settings.preference.composables.PreferenceScreen
import org.breezyweather.ui.settings.preference.composables.PreferenceViewWithCard
import org.breezyweather.ui.settings.preference.composables.SwitchPreferenceView
import org.breezyweather.ui.settings.preference.largeSeparatorItem
import org.breezyweather.ui.settings.preference.listPreferenceItem
import org.breezyweather.ui.settings.preference.sectionFooterItem
import org.breezyweather.ui.settings.preference.sectionHeaderItem
import org.breezyweather.ui.settings.preference.smallSeparatorItem
import org.breezyweather.ui.settings.preference.switchPreferenceItem

@Composable
fun MainScreenSettingsScreen(
    context: Context,
    onNavigateBack: () -> Unit,
    cardDisplayList: ImmutableList<CardDisplay>,
    dailyTrendDisplayList: ImmutableList<DailyTrendDisplay>,
    hourlyTrendDisplayList: ImmutableList<HourlyTrendDisplay>,
    updateWidgetIfNecessary: (Context) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = generateCollapsedScrollBehavior()

    Material3Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            FitStatusBarTopAppBar(
                title = stringResource(R.string.settings_main),
                onBackPressed = onNavigateBack,
                actions = { AboutActivityIconButton(context) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddings ->
        PreferenceScreen(
            paddingValues = paddings.plus(PaddingValues(horizontal = dimensionResource(R.dimen.normal_margin)))
        ) {
            sectionHeaderItem(R.string.settings_main_section_displayed_data)
            clickablePreferenceItem(
                R.string.settings_main_cards_title
            ) {
                PreferenceViewWithCard(
                    title = stringResource(it),
                    summary = CardDisplay.getSummary(context, cardDisplayList),
                    isFirst = true
                ) {
                    (context as? Activity)?.let { a ->
                        IntentHelper.startCardDisplayManageActivity(a)
                    }
                }
            }
            smallSeparatorItem()
            clickablePreferenceItem(
                R.string.settings_main_daily_trends_title
            ) {
                PreferenceViewWithCard(
                    title = stringResource(it),
                    summary = DailyTrendDisplay.getSummary(context, dailyTrendDisplayList)
                ) {
                    (context as? Activity)?.let { a ->
                        IntentHelper.startDailyTrendDisplayManageActivity(a)
                    }
                }
            }
            smallSeparatorItem()
            clickablePreferenceItem(
                R.string.settings_main_hourly_trends_title
            ) {
                PreferenceViewWithCard(
                    title = stringResource(it),
                    summary = HourlyTrendDisplay.getSummary(context, hourlyTrendDisplayList),
                    isLast = true
                ) {
                    (context as? Activity)?.let { a ->
                        IntentHelper.startHourlyTrendDisplayManageActivity(a)
                    }
                }
            }
            sectionFooterItem(R.string.settings_main_section_displayed_data)

            largeSeparatorItem()

            sectionHeaderItem(R.string.settings_main_section_options)
            switchPreferenceItem(R.string.settings_main_threshold_lines_on_charts) { id ->
                SwitchPreferenceView(
                    titleId = id,
                    summaryOnId = R.string.settings_enabled,
                    summaryOffId = R.string.settings_disabled,
                    checked = SettingsManager.getInstance(context).isTrendHorizontalLinesEnabled,
                    isFirst = true,
                    isLast = true,
                    onValueChanged = {
                        SettingsManager.getInstance(context).isTrendHorizontalLinesEnabled = it
                        updateWidgetIfNecessary(context) // Has some widgets with it

                        // FIXME: Doesn't work without restart on nowcasting chart
                        SnackbarHelper.showSnackbar(
                            content = context.getString(R.string.settings_changes_apply_after_restart),
                            action = context.getString(R.string.action_restart)
                        ) {
                            BreezyWeather.instance.recreateAllActivities()
                        }
                    }
                )
            }
            sectionFooterItem(R.string.settings_main_section_options)

            largeSeparatorItem()

            sectionHeaderItem(R.string.settings_main_section_animations)
            listPreferenceItem(R.string.settings_main_background_animation_title) { id ->
                ListPreferenceView(
                    titleId = id,
                    selectedKey = SettingsManager.getInstance(context).backgroundAnimationMode.id,
                    valueArrayId = R.array.background_animation_values,
                    nameArrayId = R.array.background_animation,
                    card = true,
                    isFirst = true,
                    onValueChanged = {
                        SettingsManager
                            .getInstance(context)
                            .backgroundAnimationMode = BackgroundAnimationMode.getInstance(it)

                        SnackbarHelper.showSnackbar(
                            content = context.getString(R.string.settings_changes_apply_after_restart),
                            action = context.getString(R.string.action_restart)
                        ) {
                            BreezyWeather.instance.recreateAllActivities()
                        }
                    }
                )
            }
            smallSeparatorItem()
            switchPreferenceItem(R.string.settings_main_gravity_sensor_switch) { id ->
                SwitchPreferenceView(
                    titleId = id,
                    summaryOnId = R.string.settings_enabled,
                    summaryOffId = R.string.settings_disabled,
                    checked = SettingsManager.getInstance(context).isGravitySensorEnabled,
                    onValueChanged = {
                        SettingsManager.getInstance(context).isGravitySensorEnabled = it

                        SnackbarHelper.showSnackbar(
                            content = context.getString(R.string.settings_changes_apply_after_restart),
                            action = context.getString(R.string.action_restart)
                        ) {
                            BreezyWeather.instance.recreateAllActivities()
                        }
                    }
                )
            }
            smallSeparatorItem()

            val animationsEnabled = !context.isMotionReduced
            switchPreferenceItem(R.string.settings_main_cards_fade_in_switch) { id ->
                SwitchPreferenceView(
                    titleId = id,
                    summaryOnId = if (animationsEnabled) {
                        R.string.settings_enabled
                    } else {
                        R.string.settings_unavailable_no_animations
                    },
                    summaryOffId = if (animationsEnabled) {
                        R.string.settings_disabled
                    } else {
                        R.string.settings_unavailable_no_animations
                    },
                    checked = SettingsManager.getInstance(context).isCardsFadeInEnabled && animationsEnabled,
                    onValueChanged = {
                        SettingsManager.getInstance(context).isCardsFadeInEnabled = it
                    },
                    enabled = animationsEnabled
                )
            }
            smallSeparatorItem()
            switchPreferenceItem(R.string.settings_main_cards_other_element_animations_switch) { id ->
                SwitchPreferenceView(
                    titleId = id,
                    summaryOnId = if (animationsEnabled) {
                        R.string.settings_enabled
                    } else {
                        R.string.settings_unavailable_no_animations
                    },
                    summaryOffId = if (animationsEnabled) {
                        R.string.settings_disabled
                    } else {
                        R.string.settings_unavailable_no_animations
                    },
                    enabled = animationsEnabled,
                    checked = SettingsManager.getInstance(context).isElementsAnimationEnabled && animationsEnabled,
                    isLast = true,
                    onValueChanged = {
                        SettingsManager.getInstance(context).isElementsAnimationEnabled = it
                    }
                )
            }
            sectionFooterItem(R.string.settings_main_section_animations)

            bottomInsetItem()
        }
    }
}
