package org.breezyweather.settings.compose

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import org.breezyweather.R
import org.breezyweather.weather.openweather.preferences.OpenWeatherOneCallVersion
import org.breezyweather.common.basic.models.options.provider.WeatherSource
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
fun WeatherProvidersSettingsScreen(
    context: Context,
    paddingValues: PaddingValues,
) = PreferenceScreen(paddingValues = paddingValues) {
    sectionHeaderItem(R.string.settings_weather_providers_section_general)
    listPreferenceItem(R.string.settings_weather_providers_current_location) { id ->
        ListPreferenceView(
            titleId = id,
            valueArrayId = R.array.weather_source_values,
            nameArrayId = R.array.weather_sources,
            selectedKey = SettingsManager.getInstance(context).weatherSource.id,
            onValueChanged = { sourceId ->
                SettingsManager
                    .getInstance(context)
                    .weatherSource = WeatherSource.getInstance(sourceId)

                val locationList = LocationEntityRepository.readLocationList(context).toMutableList()
                val index = locationList.indexOfFirst { it.isCurrentPosition }
                if (index >= 0) {
                    locationList[index] = locationList[index].copy(
                        weather = null,
                        weatherSource = SettingsManager.getInstance(context).weatherSource
                    )
                    WeatherEntityRepository.deleteWeather(locationList[index])
                    LocationEntityRepository.writeLocationList(locationList)
                }
            }
        )
    }
    sectionFooterItem(R.string.settings_weather_providers_section_general)

    sectionHeaderItem(R.string.weather_source_accuweather)
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
    editTextPreferenceItem(R.string.settings_weather_provider_accu_weather_key) { id ->
        EditTextPreferenceView(
            titleId = id,
            summary = { context, content ->
                content.ifEmpty {
                    context.getString(R.string.settings_weather_provider_default_value)
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
    sectionFooterItem(R.string.weather_source_accuweather)

    sectionHeaderItem(R.string.weather_source_openweather)
    editTextPreferenceItem(R.string.settings_weather_provider_open_weather_key) { id ->
        EditTextPreferenceView(
            titleId = id,
            summary = { context, content ->
                content.ifEmpty {
                    context.getString(R.string.settings_weather_provider_default_value)
                }
            },
            content = SettingsManager.getInstance(context).customOpenWeatherKey,
            onValueChanged = {
                SettingsManager.getInstance(context).customOpenWeatherKey = it
            }
        )
    }
    listPreferenceItem(R.string.settings_weather_provider_open_weather_one_call_version) { id ->
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
    sectionFooterItem(R.string.weather_source_openweather)

    sectionHeaderItem(R.string.weather_source_mf)
    editTextPreferenceItem(R.string.settings_weather_provider_mf_wsft_key) { id ->
        EditTextPreferenceView(
            titleId = id,
            summary = { context, content ->
                content.ifEmpty {
                    context.getString(R.string.settings_weather_provider_default_value)
                }
            },
            content = SettingsManager.getInstance(context).customMfWsftKey,
            onValueChanged = {
                SettingsManager.getInstance(context).customMfWsftKey = it
            }
        )
    }
    editTextPreferenceItem(R.string.settings_weather_provider_iqa_atmo_aura_key) { id ->
        EditTextPreferenceView(
            titleId = id,
            summary = { context, content ->
                content.ifEmpty {
                    context.getString(R.string.settings_weather_provider_default_value)
                }
            },
            content = SettingsManager.getInstance(context).customIqaAtmoAuraKey,
            onValueChanged = {
                SettingsManager.getInstance(context).customIqaAtmoAuraKey = it
            }
        )
    }
    sectionFooterItem(R.string.weather_source_mf)

    bottomInsetItem()
}