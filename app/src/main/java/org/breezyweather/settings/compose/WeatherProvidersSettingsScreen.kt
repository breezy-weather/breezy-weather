package org.breezyweather.settings.compose

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.ListPreference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.settings.preference.*
import org.breezyweather.settings.preference.composables.EditTextPreferenceView
import org.breezyweather.settings.preference.composables.ListPreferenceView
import org.breezyweather.settings.preference.composables.PreferenceScreen
import org.breezyweather.settings.preference.composables.SectionFooter
import org.breezyweather.settings.preference.composables.SectionHeader

@Composable
fun WeatherProvidersSettingsScreen(
    context: Context,
    mainWeatherSources: List<MainWeatherSource>,
    paddingValues: PaddingValues,
) = PreferenceScreen(paddingValues = paddingValues) {
    mainWeatherSources.filterIsInstance<ConfigurableSource>().forEach { preferenceSource ->
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