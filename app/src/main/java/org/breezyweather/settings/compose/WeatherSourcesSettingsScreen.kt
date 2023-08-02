package org.breezyweather.settings.compose

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import org.breezyweather.R
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.ListPreference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.LocationSource
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.preference.bottomInsetItem
import org.breezyweather.settings.preference.composables.EditTextPreferenceView
import org.breezyweather.settings.preference.composables.ListPreferenceView
import org.breezyweather.settings.preference.composables.PreferenceScreen
import org.breezyweather.settings.preference.composables.SectionFooter
import org.breezyweather.settings.preference.composables.SectionHeader
import org.breezyweather.settings.preference.editTextPreferenceItem
import org.breezyweather.settings.preference.listPreferenceItem
import org.breezyweather.settings.preference.sectionFooterItem
import org.breezyweather.settings.preference.sectionHeaderItem

@Composable
fun WeatherSourcesSettingsScreen(
    context: Context,
    configurableSources: List<ConfigurableSource>,
    locationSearchSources: List<LocationSearchSource>,
    paddingValues: PaddingValues,
) = PreferenceScreen(paddingValues = paddingValues) {
    sectionHeaderItem(R.string.settings_weather_providers_section_general)
    listPreferenceItem(R.string.settings_weather_sources_default_location_search) { id ->
        ListPreferenceView(
            title = context.getString(id),
            selectedKey = SettingsManager.getInstance(context).locationSearchSource,
            valueArray = locationSearchSources.map { it.id }.toTypedArray(),
            nameArray = locationSearchSources.map { it.name }.toTypedArray(),
            summary = { _, value -> locationSearchSources.firstOrNull { it.id == value }?.name },
            onValueChanged = { sourceId ->
                SettingsManager.getInstance(context).locationSearchSource = sourceId
            }
        )
    }
    sectionFooterItem(R.string.settings_weather_providers_section_general)

    configurableSources
        .filter { it !is LocationSource } // Exclude location sources configured in its own screen
        .sortedBy { it.name } // Sort by name because there are now a lot of sources
        .forEach { preferenceSource ->
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