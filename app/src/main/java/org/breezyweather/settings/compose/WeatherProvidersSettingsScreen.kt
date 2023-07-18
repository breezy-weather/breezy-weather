package org.breezyweather.settings.compose

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import org.breezyweather.R
import org.breezyweather.background.weather.WeatherUpdateJob
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.ListPreference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.db.repositories.LocationEntityRepository
import org.breezyweather.db.repositories.WeatherEntityRepository
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.preference.*
import org.breezyweather.settings.preference.composables.EditTextPreferenceView
import org.breezyweather.settings.preference.composables.ListPreferenceView
import org.breezyweather.settings.preference.composables.PreferenceScreen
import org.breezyweather.settings.preference.composables.SectionFooter
import org.breezyweather.settings.preference.composables.SectionHeader

@Composable
fun WeatherProvidersSettingsScreen(
    context: Context,
    weatherSources: List<WeatherSource>,
    paddingValues: PaddingValues,
) = PreferenceScreen(paddingValues = paddingValues) {
    sectionHeaderItem(R.string.settings_weather_providers_section_general)
    listPreferenceItem(R.string.settings_weather_providers_current_location) { id ->
        ListPreferenceView(
            title = context.getString(id),
            selectedKey = SettingsManager.getInstance(context).weatherSource,
            valueArray = weatherSources.map { it.id }.toTypedArray(),
            nameArray = weatherSources.map { it.name }.toTypedArray(),
            summary = { _, value -> weatherSources.firstOrNull { it.id == value }?.name },
            onValueChanged = { sourceId ->
                SettingsManager.getInstance(context).weatherSource = sourceId

                val locationList = LocationEntityRepository.readLocationList().toMutableList()
                val index = locationList.indexOfFirst { it.isCurrentPosition }
                if (index >= 0) {
                    locationList[index] = locationList[index].copy(
                        weather = null,
                        // FIXME: This won't work as it will keep older version if null, however as we delete in database, this shouldn’t change anything
                        weatherSource = sourceId
                    )
                    WeatherEntityRepository.deleteWeather(locationList[index])
                    LocationEntityRepository.writeLocationList(locationList)
                    // Trigger refresh of the current location
                    WeatherUpdateJob.startNow(context, locationList[index])
                }
            }
        )
    }
    sectionFooterItem(R.string.settings_weather_providers_section_general)

    weatherSources.filterIsInstance<ConfigurableSource>().forEach { preferenceSource ->
        item(key = "header_${preferenceSource.id}") {
            SectionHeader(title = preferenceSource.name)
        }
        preferenceSource.getPreferences(context).forEach { preference ->
            when (preference) {
                is ListPreference -> {
                    listPreferenceItem(preference.titleId) { id ->
                        ListPreferenceView(
                            titleId = id,
                            selectedKey = preference.selectedKey,
                            valueArrayId = preference.valueArrayId,
                            nameArrayId = preference.nameArrayId,
                            onValueChanged = preference.onValueChanged,
                        )
                    }
                }
                is EditTextPreference -> {
                    editTextPreferenceItem(preference.titleId) { id ->
                        EditTextPreferenceView(
                            titleId = id,
                            summary = preference.summary,
                            content = preference.content,
                            onValueChanged = preference.onValueChanged
                        )
                    }
                }
            }
        }
        item(key = "footer_${preferenceSource.id}") {
            SectionFooter()
        }
    }

    bottomInsetItem()
}