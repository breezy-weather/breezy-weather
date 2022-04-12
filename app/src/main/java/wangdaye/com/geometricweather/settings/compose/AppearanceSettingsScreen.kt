package wangdaye.com.geometricweather.settings.compose

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import wangdaye.com.geometricweather.GeometricWeather.Companion.instance
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.options.appearance.CardDisplay
import wangdaye.com.geometricweather.common.basic.models.options.appearance.DailyTrendDisplay
import wangdaye.com.geometricweather.common.basic.models.options.appearance.Language
import wangdaye.com.geometricweather.common.basic.models.weather.Temperature
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper
import wangdaye.com.geometricweather.common.utils.helpers.SnackbarHelper
import wangdaye.com.geometricweather.settings.SettingsManager
import wangdaye.com.geometricweather.settings.dialogs.ProvidersPreviewerDialog
import wangdaye.com.geometricweather.theme.resource.ResourcesProviderFactory

@Composable
fun AppearanceSettingsScreen(
    context: Context,
    cardDisplayList: List<CardDisplay>,
    dailyTrendDisplayList: List<DailyTrendDisplay>,
) {
    LazyColumn(
        modifier = Modifier.fillMaxHeight()
    ) {
        item {
            val iconProviderState = remember {
                mutableStateOf(
                    SettingsManager.getInstance(context).iconProvider
                )
            }
            PreferenceView(
                title = stringResource(R.string.settings_title_icon_provider),
                summary = ResourcesProviderFactory
                    .getNewInstance(iconProviderState.value)
                    .providerName
            ) {
                (context as? Activity)?.let { activity ->
                    ProvidersPreviewerDialog.show(activity) {
                        SettingsManager.getInstance(context).iconProvider = it
                        iconProviderState.value = it
                    }
                }
            }
        }
        item {
            PreferenceView(
                title = stringResource(R.string.settings_title_card_display),
                summary = CardDisplay.getSummary(context, cardDisplayList),
            ) {
                (context as? Activity)?.let {
                    IntentHelper.startCardDisplayManageActivity(it)
                }
            }
        }
        item {
            PreferenceView(
                title = stringResource(R.string.settings_title_daily_trend_display),
                summary = DailyTrendDisplay.getSummary(context, dailyTrendDisplayList),
            ) {
                (context as? Activity)?.let {
                    IntentHelper.startDailyTrendDisplayManageActivity(it)
                }
            }
        }
        item {
            CheckboxPreferenceView(
                title = stringResource(R.string.settings_title_trend_horizontal_line_switch),
                summary = { stringResource(if (it) R.string.on else R.string.off) },
                checked = SettingsManager.getInstance(context).isTrendHorizontalLinesEnabled,
                onValueChanged = {
                    SettingsManager.getInstance(context).isTrendHorizontalLinesEnabled = it
                }
            )
        }
        item {
            CheckboxPreferenceView(
                title = stringResource(R.string.settings_title_exchange_day_night_temp_switch),
                summary = {
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
                }
            )
        }
        item {
            CheckboxPreferenceView(
                title = stringResource(R.string.settings_title_gravity_sensor_switch),
                summary = { stringResource(if (it) R.string.on else R.string.off) },
                checked = SettingsManager.getInstance(context).isGravitySensorEnabled,
                onValueChanged = {
                    SettingsManager.getInstance(context).isGravitySensorEnabled = it
                }
            )
        }
        item {
            CheckboxPreferenceView(
                title = stringResource(R.string.settings_title_list_animation_switch),
                summary = { stringResource(if (it) R.string.on else R.string.off) },
                checked = SettingsManager.getInstance(context).isListAnimationEnabled,
                onValueChanged = {
                    SettingsManager.getInstance(context).isListAnimationEnabled = it
                }
            )
        }
        item {
            CheckboxPreferenceView(
                title = stringResource(R.string.settings_title_item_animation_switch),
                summary = { stringResource(if (it) R.string.on else R.string.off) },
                checked = SettingsManager.getInstance(context).isItemAnimationEnabled,
                onValueChanged = {
                    SettingsManager.getInstance(context).isItemAnimationEnabled = it
                }
            )
        }
        item {
            val valueList = stringArrayResource(R.array.language_values)
            val nameList = stringArrayResource(R.array.languages)
            ListPreferenceView(
                title = stringResource(R.string.settings_language),
                summary = { key -> nameList[valueList.indexOfFirst { it == key }] },
                selectedKey = SettingsManager.getInstance(context).language.id,
                keyNamePairList = valueList.zip(nameList),
                onValueChanged = {
                    SettingsManager.getInstance(context).language = Language.getInstance(it)

                    SnackbarHelper.showSnackbar(
                        context.getString(R.string.feedback_restart),
                        context.getString(R.string.restart)
                    ) {
                        instance.recreateAllActivities()
                    }
                }
            )
        }
        item { Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)) }
    }
}