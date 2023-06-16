package org.breezyweather.settings.compose

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.breezyweather.BreezyWeather.Companion.instance
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.appearance.BackgroundAnimationMode
import org.breezyweather.common.basic.models.options.appearance.CardDisplay
import org.breezyweather.common.basic.models.options.appearance.DailyTrendDisplay
import org.breezyweather.common.basic.models.options.appearance.HourlyTrendDisplay
import org.breezyweather.common.basic.models.options.appearance.Language
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.dialogs.ProvidersPreviewerDialog
import org.breezyweather.settings.preference.bottomInsetItem
import org.breezyweather.settings.preference.switchPreferenceItem
import org.breezyweather.settings.preference.clickablePreferenceItem
import org.breezyweather.settings.preference.composables.ListPreferenceView
import org.breezyweather.settings.preference.composables.PreferenceView
import org.breezyweather.settings.preference.composables.SwitchPreferenceView
import org.breezyweather.settings.preference.listPreferenceItem
import org.breezyweather.theme.resource.ResourcesProviderFactory

@Composable
fun AppearanceSettingsScreen(
    context: Context,
    cardDisplayList: List<CardDisplay>,
    dailyTrendDisplayList: List<DailyTrendDisplay>,
    hourlyTrendDisplayList: List<HourlyTrendDisplay>,
    paddingValues: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentPadding = paddingValues,
    ) {
        clickablePreferenceItem(
            R.string.settings_title_icon_provider
        ) {
            val iconProviderState = remember {
                mutableStateOf(
                    SettingsManager.getInstance(context).iconProvider
                )
            }

            PreferenceView(
                title = stringResource(it),
                summary = ResourcesProviderFactory
                    .getNewInstance(iconProviderState.value)
                    .providerName
            ) {
                (context as? Activity)?.let { activity ->
                    ProvidersPreviewerDialog.show(activity) { packageName ->
                        SettingsManager.getInstance(context).iconProvider = packageName
                        iconProviderState.value = packageName
                    }
                }
            }
        }
        clickablePreferenceItem(
            R.string.settings_title_card_display
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
            R.string.settings_title_daily_trend_display
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
            R.string.settings_title_hourly_trend_display
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
        switchPreferenceItem(R.string.settings_title_trend_horizontal_line_switch) { id ->
            SwitchPreferenceView(
                titleId = id,
                summaryOnId = R.string.on,
                summaryOffId = R.string.off,
                checked = SettingsManager.getInstance(context).isTrendHorizontalLinesEnabled,
                onValueChanged = {
                    SettingsManager.getInstance(context).isTrendHorizontalLinesEnabled = it
                },
            )
        }
        switchPreferenceItem(R.string.settings_title_exchange_day_night_temp_switch) { id ->
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
                checked = SettingsManager.getInstance(context).isExchangeDayNightTempEnabled,
                onValueChanged = {
                    SettingsManager.getInstance(context).isExchangeDayNightTempEnabled = it
                },
            )
        }
        listPreferenceItem(R.string.settings_title_background_animation) { id ->
            ListPreferenceView(
                titleId = id,
                selectedKey = SettingsManager.getInstance(context).backgroundAnimationMode.id,
                valueArrayId = R.array.background_animation_values,
                nameArrayId = R.array.background_animation,
                onValueChanged = {
                    SettingsManager
                        .getInstance(context)
                        .backgroundAnimationMode = BackgroundAnimationMode.getInstance(it)
                },
            )
        }
        switchPreferenceItem(R.string.settings_title_gravity_sensor_switch) { id ->
            SwitchPreferenceView(
                titleId = id,
                summaryOnId = R.string.on,
                summaryOffId = R.string.off,
                checked = SettingsManager.getInstance(context).isGravitySensorEnabled,
                onValueChanged = {
                    SettingsManager.getInstance(context).isGravitySensorEnabled = it
                },
            )
        }
        switchPreferenceItem(R.string.settings_title_list_animation_switch) { id ->
            SwitchPreferenceView(
                titleId = id,
                summaryOnId = R.string.on,
                summaryOffId = R.string.off,
                checked = SettingsManager.getInstance(context).isListAnimationEnabled,
                onValueChanged = {
                    SettingsManager.getInstance(context).isListAnimationEnabled = it
                },
            )
        }
        switchPreferenceItem(R.string.settings_title_item_animation_switch) { id ->
            SwitchPreferenceView(
                titleId = id,
                summaryOnId = R.string.on,
                summaryOffId = R.string.off,
                checked = SettingsManager.getInstance(context).isItemAnimationEnabled,
                onValueChanged = {
                    SettingsManager.getInstance(context).isItemAnimationEnabled = it
                },
            )
        }
        listPreferenceItem(R.string.settings_language) { id ->
            ListPreferenceView(
                titleId = id,
                valueArrayId = R.array.language_values,
                nameArrayId = R.array.languages,
                selectedKey = SettingsManager.getInstance(context).language.id,
                onValueChanged = {
                    SettingsManager.getInstance(context).language = Language.getInstance(it)

                    SnackbarHelper.showSnackbar(
                        context.getString(R.string.feedback_restart),
                        context.getString(R.string.restart)
                    ) {
                        instance.recreateAllActivities()
                    }
                },
            )
        }

        bottomInsetItem()
    }
}