package org.breezyweather.settings.compose

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import org.breezyweather.BuildConfig
import org.breezyweather.BreezyWeather.Companion.instance
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.provider.LocationProvider
import org.breezyweather.common.basic.models.options.provider.OpenWeatherOneCallVersion
import org.breezyweather.common.basic.models.options.provider.WeatherSource
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.db.repositories.LocationEntityRepository
import org.breezyweather.db.repositories.WeatherEntityRepository
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.preference.*
import org.breezyweather.settings.preference.composables.EditTextPreferenceView
import org.breezyweather.settings.preference.composables.ListPreferenceView
import org.breezyweather.settings.preference.composables.PreferenceScreen
import org.breezyweather.weather.accu.preferences.AccuDaysPreference
import org.breezyweather.weather.accu.preferences.AccuHoursPreference
import org.breezyweather.weather.accu.preferences.AccuPortalPreference

@Composable
fun ServiceProviderSettingsScreen(
    context: Context,
    paddingValues: PaddingValues,
) = PreferenceScreen(paddingValues = paddingValues) {
    sectionHeaderItem(R.string.settings_providers_section_general)
    listPreferenceItem(R.string.settings_providers_weather_provider_current_location) { id ->
        ListPreferenceView(
            titleId = id,
            valueArrayId = R.array.weather_source_values,
            nameArrayId = R.array.weather_sources,
            selectedKey = SettingsManager.getInstance(context).weatherSource.id,
            onValueChanged = { sourceId ->
                SettingsManager
                    .getInstance(context)
                    .weatherSource = WeatherSource.getInstance(sourceId)

                val locationList = LocationEntityRepository.readLocationList(context)
                val index = locationList.indexOfFirst { it.isCurrentPosition }
                if (index >= 0) {
                    locationList[index] = locationList[index].copy(
                        weather = null,
                        weatherSource = SettingsManager.getInstance(context).weatherSource
                    ).copy()
                    WeatherEntityRepository.deleteWeather(locationList[index])
                    LocationEntityRepository.writeLocationList(locationList)
                }
            }
        )
    }
    listPreferenceItem(R.string.settings_providers_location_service) { id ->
        // read configuration from data store.
        var currentSelectedKey = SettingsManager.getInstance(context).locationProvider.id
        var valueList = stringArrayResource(R.array.location_service_values)
        var nameList = stringArrayResource(R.array.location_services)

        // clear invalid config by build flavor.
        if (BuildConfig.FLAVOR.contains("fdroid")
            || BuildConfig.FLAVOR.contains("gplay")) {
            valueList = arrayOf(valueList[1], valueList[3])
            nameList = arrayOf(nameList[1], nameList[3])
        }
        if (!valueList.contains(currentSelectedKey)) {
            currentSelectedKey = LocationProvider.NATIVE.id
        }

        ListPreferenceView(
            title = stringResource(id),
            summary = { _, key -> nameList[valueList.indexOfFirst { it == key }] },
            selectedKey = currentSelectedKey,
            valueArray = valueList,
            nameArray = nameList,
            onValueChanged = { sourceId ->
                SettingsManager
                    .getInstance(context)
                    .locationProvider = LocationProvider.getInstance(sourceId)

                SnackbarHelper.showSnackbar(
                    context.getString(R.string.settings_changes_apply_after_restart),
                    context.getString(R.string.action_restart)
                ) {
                    instance.recreateAllActivities()
                }
            }
        )
    }
    sectionFooterItem(R.string.settings_providers_section_general)

    sectionHeaderItem(R.string.settings_provider_accu_weather)
    listPreferenceItem(R.string.weather_source_accu_preference_portal) { id ->
        ListPreferenceView(
            titleId = id,
            selectedKey = SettingsManager.getInstance(context).customAccuPortal.id,
            valueArrayId = R.array.accu_preference_portal_values,
            nameArrayId = R.array.accu_preference_portal,
            onValueChanged = {
                SettingsManager
                    .getInstance(context)
                    .customAccuPortal = AccuPortalPreference.getInstance(it)
            },
        )
    }
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
    listPreferenceItem(R.string.weather_source_accu_preference_days) { id ->
        ListPreferenceView(
            titleId = id,
            selectedKey = SettingsManager.getInstance(context).customAccuDays.id,
            valueArrayId = R.array.accu_preference_day_values,
            nameArrayId = R.array.accu_preference_days,
            onValueChanged = {
                SettingsManager
                    .getInstance(context)
                    .customAccuDays = AccuDaysPreference.getInstance(it)
            },
        )
    }
    listPreferenceItem(R.string.weather_source_accu_preference_hours) { id ->
        ListPreferenceView(
            titleId = id,
            selectedKey = SettingsManager.getInstance(context).customAccuHours.id,
            valueArrayId = R.array.accu_preference_hour_values,
            nameArrayId = R.array.accu_preference_hours,
            onValueChanged = {
                SettingsManager
                    .getInstance(context)
                    .customAccuHours = AccuHoursPreference.getInstance(it)
            },
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