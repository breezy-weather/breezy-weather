package wangdaye.com.geometricweather.settings.compose

import android.content.Context
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.settings.SettingsManager
import wangdaye.com.geometricweather.settings.preference.*

@Composable
fun SettingsProviderAdvancedSettingsScreen(context: Context) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        // accu.
        sectionHeaderItem(R.string.settings_provider_accu_weather)
        preferenceItems(
            listOf(
                PreferenceModel.EditTextPreferenceModel(
                    titleId = R.string.settings_provider_accu_weather_key,
                    summaryGenerator = { _, _ -> null },
                    content = SettingsManager.getInstance(context).customAccuWeatherKey,
                    onValueChanged = {
                        SettingsManager.getInstance(context).customAccuWeatherKey = it
                    }
                ),
                PreferenceModel.EditTextPreferenceModel(
                    titleId = R.string.settings_provider_accu_current_key,
                    summaryGenerator = { _, _ -> null },
                    content = SettingsManager.getInstance(context).customAccuCurrentKey,
                    onValueChanged = {
                        SettingsManager.getInstance(context).customAccuCurrentKey = it
                    }
                ),
                PreferenceModel.EditTextPreferenceModel(
                    titleId = R.string.settings_provider_accu_aqi_key,
                    summaryGenerator = { _, _ -> null },
                    content = SettingsManager.getInstance(context).customAccuAqiKey,
                    onValueChanged = {
                        SettingsManager.getInstance(context).customAccuAqiKey = it
                    }
                ),
            ).map { mutableStateOf(it) }
        )
        sectionFooterItem(R.string.settings_provider_accu_weather)

        // open weather map.
        sectionHeaderItem(R.string.settings_provider_owm)
        preferenceItem(
            mutableStateOf(
                PreferenceModel.EditTextPreferenceModel(
                    titleId = R.string.settings_provider_owm_key,
                    summaryGenerator = { _, _ -> null },
                    content = SettingsManager.getInstance(context).customOwmKey,
                    onValueChanged = {
                        SettingsManager.getInstance(context).customOwmKey = it
                    }
                )
            )
        )
        sectionFooterItem(R.string.settings_provider_owm)

        // baidu ip location.
        sectionHeaderItem(R.string.settings_provider_baidu_ip_location)
        preferenceItem(
            mutableStateOf(
                PreferenceModel.EditTextPreferenceModel(
                    titleId = R.string.settings_provider_baidu_ip_location_ak,
                    summaryGenerator = { _, _ -> null },
                    content = SettingsManager.getInstance(context).customBaiduIpLocationAk,
                    onValueChanged = {
                        SettingsManager.getInstance(context).customBaiduIpLocationAk = it
                    }
                )
            )
        )
        sectionFooterItem(R.string.settings_provider_baidu_ip_location)

        // meteo france.
        sectionHeaderItem(R.string.settings_provider_mf)
        preferenceItems(
            listOf(
                PreferenceModel.EditTextPreferenceModel(
                    titleId = R.string.settings_provider_mf_wsft_key,
                    summaryGenerator = { _, _ -> null },
                    content = SettingsManager.getInstance(context).customMfWsftKey,
                    onValueChanged = {
                        SettingsManager.getInstance(context).customMfWsftKey = it
                    }
                ),
                PreferenceModel.EditTextPreferenceModel(
                    titleId = R.string.settings_provider_iqa_air_parif_key,
                    summaryGenerator = { _, _ -> null },
                    content = SettingsManager.getInstance(context).customIqaAirParifKey,
                    onValueChanged = {
                        SettingsManager.getInstance(context).customIqaAirParifKey = it
                    }
                ),
                PreferenceModel.EditTextPreferenceModel(
                    titleId = R.string.settings_provider_iqa_atmo_aura_key,
                    summaryGenerator = { _, _ -> null },
                    content = SettingsManager.getInstance(context).customIqaAtmoAuraKey,
                    onValueChanged = {
                        SettingsManager.getInstance(context).customIqaAtmoAuraKey = it
                    }
                )
            ).map { mutableStateOf(it) }
        )
        sectionFooterItem(R.string.settings_provider_mf)

        bottomInsetItem()
    }
}