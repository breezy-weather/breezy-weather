package wangdaye.com.geometricweather.settings.compose

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.options.provider.OpenWeatherOneCallVersion
import wangdaye.com.geometricweather.settings.SettingsManager
import wangdaye.com.geometricweather.settings.preference.*
import wangdaye.com.geometricweather.settings.preference.composables.EditTextPreferenceView
import wangdaye.com.geometricweather.settings.preference.composables.ListPreferenceView
import wangdaye.com.geometricweather.settings.preference.composables.PreferenceScreen

@Composable
fun SettingsProviderAdvancedSettingsScreen(
    context: Context,
    paddingValues: PaddingValues,
) = PreferenceScreen(paddingValues = paddingValues) {
    sectionHeaderItem(R.string.settings_provider_accu_weather)
    editTextPreferenceItem(R.string.settings_provider_accu_weather_key) { id ->
        EditTextPreferenceView(
            titleId = id,
            summary = { context, content ->
                content.ifEmpty {
                    context.getString(R.string.settings_provider_default_value)
                }
            },
            content = SettingsManager.getInstance(context).customAccuWeatherKey,
            onValueChanged = {
                SettingsManager.getInstance(context).customAccuWeatherKey = it
            }
        )
    }
    editTextPreferenceItem(R.string.settings_provider_accu_current_key) { id ->
        EditTextPreferenceView(
            titleId = id,
            summary = { context, content ->
                content.ifEmpty {
                    context.getString(R.string.settings_provider_default_value)
                }
            },
            content = SettingsManager.getInstance(context).customAccuCurrentKey,
            onValueChanged = {
                SettingsManager.getInstance(context).customAccuCurrentKey = it
            }
        )
    }
    editTextPreferenceItem(R.string.settings_provider_accu_aqi_key) { id ->
        EditTextPreferenceView(
            titleId = id,
            summary = { context, content ->
                content.ifEmpty {
                    context.getString(R.string.settings_provider_default_value)
                }
            },
            content = SettingsManager.getInstance(context).customAccuAqiKey,
            onValueChanged = {
                SettingsManager.getInstance(context).customAccuAqiKey = it
            }
        )
    }
    sectionFooterItem(R.string.settings_provider_accu_weather)

    sectionHeaderItem(R.string.settings_provider_open_weather)
    editTextPreferenceItem(R.string.settings_provider_open_weather_key) { id ->
        EditTextPreferenceView(
            titleId = id,
            summary = { context, content ->
                content.ifEmpty {
                    context.getString(R.string.settings_provider_default_value)
                }
            },
            content = SettingsManager.getInstance(context).customOpenWeatherKey,
            onValueChanged = {
                SettingsManager.getInstance(context).customOpenWeatherKey = it
            }
        )
    }
    listPreferenceItem(R.string.settings_provider_open_weather_one_call_version) { id ->
        ListPreferenceView(
            titleId = id,
            selectedKey = SettingsManager.getInstance(context).customOpenWeatherOneCallVersion.id,
            valueArrayId = R.array.open_weather_one_call_version_values,
            nameArrayId = R.array.open_weather_one_call_version,
            onValueChanged = {
                SettingsManager
                    .getInstance(context)
                    .customOpenWeatherOneCallVersion = OpenWeatherOneCallVersion.getInstance(it)
            },
        )
    }
    sectionFooterItem(R.string.settings_provider_open_weather)

    sectionHeaderItem(R.string.settings_provider_baidu_ip_location)
    editTextPreferenceItem(R.string.settings_provider_baidu_ip_location) { id ->
        EditTextPreferenceView(
            titleId = id,
            summary = { context, content ->
                content.ifEmpty {
                    context.getString(R.string.settings_provider_default_value)
                }
            },
            content = SettingsManager.getInstance(context).customBaiduIpLocationAk,
            onValueChanged = {
                SettingsManager.getInstance(context).customBaiduIpLocationAk = it
            }
        )
    }
    sectionFooterItem(R.string.settings_provider_baidu_ip_location)

    sectionHeaderItem(R.string.settings_provider_mf)
    editTextPreferenceItem(R.string.settings_provider_mf_wsft_key) { id ->
        EditTextPreferenceView(
            titleId = id,
            summary = { context, content ->
                content.ifEmpty {
                    context.getString(R.string.settings_provider_default_value)
                }
            },
            content = SettingsManager.getInstance(context).customMfWsftKey,
            onValueChanged = {
                SettingsManager.getInstance(context).customMfWsftKey = it
            }
        )
    }
    editTextPreferenceItem(R.string.settings_provider_iqa_atmo_aura_key) { id ->
        EditTextPreferenceView(
            titleId = id,
            summary = { context, content ->
                content.ifEmpty {
                    context.getString(R.string.settings_provider_default_value)
                }
            },
            content = SettingsManager.getInstance(context).customIqaAtmoAuraKey,
            onValueChanged = {
                SettingsManager.getInstance(context).customIqaAtmoAuraKey = it
            }
        )
    }
    sectionFooterItem(R.string.settings_provider_mf)

    bottomInsetItem()
}