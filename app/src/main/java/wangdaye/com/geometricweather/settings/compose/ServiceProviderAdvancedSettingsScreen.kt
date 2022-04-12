package wangdaye.com.geometricweather.settings.compose

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.settings.SettingsManager

@Composable
fun SettingsProviderAdvancedSettingsScreen(context: Context) {
    LazyColumn(
        modifier = Modifier.fillMaxHeight()
    ) {
        // accu.
        item { SectionHeader(title = stringResource(R.string.settings_provider_accu_weather)) }
        item {
            EditTextPreferenceView(
                title = stringResource(R.string.settings_provider_accu_weather_key),
                summary = { null },
                content = SettingsManager.getInstance(context).customAccuWeatherKey,
                onValueChanged = {
                    SettingsManager.getInstance(context).customAccuWeatherKey = it
                }
            )
            EditTextPreferenceView(
                title = stringResource(R.string.settings_provider_accu_current_key),
                summary = { null },
                content = SettingsManager.getInstance(context).customAccuCurrentKey,
                onValueChanged = {
                    SettingsManager.getInstance(context).customAccuCurrentKey = it
                }
            )
            EditTextPreferenceView(
                title = stringResource(R.string.settings_provider_accu_aqi_key),
                summary = { null },
                content = SettingsManager.getInstance(context).customAccuAqiKey,
                onValueChanged = {
                    SettingsManager.getInstance(context).customAccuAqiKey = it
                }
            )
        }
        item { SectionFooter() }

        // open weather map.
        item { SectionHeader(title = stringResource(R.string.settings_provider_owm)) }
        item {
            EditTextPreferenceView(
                title = stringResource(R.string.settings_provider_owm_key),
                summary = { null },
                content = SettingsManager.getInstance(context).customOwmKey,
                onValueChanged = {
                    SettingsManager.getInstance(context).customOwmKey = it
                }
            )
        }
        item { SectionFooter() }

        // baidu ip location.
        item { SectionHeader(title = stringResource(R.string.settings_provider_baidu_ip_location)) }
        item {
            EditTextPreferenceView(
                title = stringResource(R.string.settings_provider_baidu_ip_location_ak),
                summary = { null },
                content = SettingsManager.getInstance(context).customBaiduIpLocationAk,
                onValueChanged = {
                    SettingsManager.getInstance(context).customBaiduIpLocationAk = it
                }
            )
        }
        item { SectionFooter() }

        // meteo france.
        item { SectionHeader(title = stringResource(R.string.settings_provider_mf)) }
        item {
            EditTextPreferenceView(
                title = stringResource(R.string.settings_provider_mf_wsft_key),
                summary = { null },
                content = SettingsManager.getInstance(context).customMfWsftKey,
                onValueChanged = {
                    SettingsManager.getInstance(context).customMfWsftKey = it
                }
            )
            EditTextPreferenceView(
                title = stringResource(R.string.settings_provider_iqa_air_parif_key),
                summary = { null },
                content = SettingsManager.getInstance(context).customIqaAirParifKey,
                onValueChanged = {
                    SettingsManager.getInstance(context).customIqaAirParifKey = it
                }
            )
            EditTextPreferenceView(
                title = stringResource(R.string.settings_provider_iqa_atmo_aura_key),
                summary = { null },
                content = SettingsManager.getInstance(context).customIqaAtmoAuraKey,
                onValueChanged = {
                    SettingsManager.getInstance(context).customIqaAtmoAuraKey = it
                }
            )
        }
        item { SectionFooter() }

        item { Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)) }
    }
}