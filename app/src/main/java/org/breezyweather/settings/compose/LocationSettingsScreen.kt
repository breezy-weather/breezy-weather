package org.breezyweather.settings.compose

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.provider.LocationProvider
import org.breezyweather.weather.openweather.preferences.OpenWeatherOneCallVersion
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
fun LocationSettingsScreen(
    context: Context,
    paddingValues: PaddingValues,
) = PreferenceScreen(paddingValues = paddingValues) {
    sectionHeaderItem(R.string.settings_location_section_general)
    listPreferenceItem(R.string.settings_location_service) { id ->
        ListPreferenceView(
            titleId = id,
            selectedKey = SettingsManager.getInstance(context).locationProvider.id,
            valueArrayId = R.array.location_service_values,
            nameArrayId = R.array.location_services,
            onValueChanged = { sourceId ->
                SettingsManager
                    .getInstance(context)
                    .locationProvider = LocationProvider.getInstance(sourceId)

                SnackbarHelper.showSnackbar(
                    context.getString(R.string.settings_changes_apply_after_restart),
                    context.getString(R.string.action_restart)
                ) {
                    BreezyWeather.instance.recreateAllActivities()
                }
            }
        )
    }
    sectionFooterItem(R.string.settings_location_section_general)

    sectionHeaderItem(R.string.location_service_baidu_ip)
    editTextPreferenceItem(R.string.settings_location_baidu_ip_location_ak) { id ->
        EditTextPreferenceView(
            titleId = id,
            summary = { context, content ->
                content.ifEmpty {
                    context.getString(R.string.settings_weather_provider_default_value)
                }
            },
            content = SettingsManager.getInstance(context).customBaiduIpLocationAk,
            onValueChanged = {
                SettingsManager.getInstance(context).customBaiduIpLocationAk = it
            }
        )
    }
    sectionFooterItem(R.string.location_service_baidu_ip)

    bottomInsetItem()
}