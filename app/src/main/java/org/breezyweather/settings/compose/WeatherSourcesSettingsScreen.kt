/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.settings.compose

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.ListPreference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.LocationSource
import org.breezyweather.settings.preference.bottomInsetItem
import org.breezyweather.settings.preference.composables.EditTextPreferenceView
import org.breezyweather.settings.preference.composables.ListPreferenceView
import org.breezyweather.settings.preference.composables.PreferenceScreen
import org.breezyweather.settings.preference.composables.SectionFooter
import org.breezyweather.settings.preference.composables.SectionHeader
import org.breezyweather.settings.preference.editTextPreferenceItem
import org.breezyweather.settings.preference.listPreferenceItem

@Composable
fun WeatherSourcesSettingsScreen(
    context: Context,
    configurableSources: List<ConfigurableSource>,
    paddingValues: PaddingValues,
) = PreferenceScreen(paddingValues = paddingValues) {
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