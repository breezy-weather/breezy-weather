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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.ListPreference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.LocationSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.getName
import org.breezyweather.common.ui.composables.AlertDialogLink
import org.breezyweather.common.ui.composables.SourceView
import org.breezyweather.common.ui.widgets.Material3CardListItem
import org.breezyweather.common.ui.widgets.Material3Scaffold
import org.breezyweather.common.ui.widgets.generateCollapsedScrollBehavior
import org.breezyweather.common.ui.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.preference.bottomInsetItem
import org.breezyweather.settings.preference.clickablePreferenceItem
import org.breezyweather.settings.preference.composables.EditTextPreferenceView
import org.breezyweather.settings.preference.composables.ListPreferenceView
import org.breezyweather.settings.preference.composables.PreferenceScreen
import org.breezyweather.settings.preference.composables.SectionFooter
import org.breezyweather.settings.preference.composables.SectionHeader
import org.breezyweather.settings.preference.editTextPreferenceItem
import org.breezyweather.settings.preference.listPreferenceItem
import org.breezyweather.settings.preference.sectionFooterItem
import org.breezyweather.settings.preference.sectionHeaderItem
import org.breezyweather.theme.compose.DayNightTheme
import java.text.Collator

@Composable
fun WeatherSourcesSettingsScreen(
    context: Context,
    onNavigateBack: () -> Unit,
    configuredWorldwideSources: List<WeatherSource>,
    configurableSources: List<ConfigurableSource>,
) {
    val scrollBehavior = generateCollapsedScrollBehavior()

    Material3Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            FitStatusBarTopAppBar(
                title = stringResource(R.string.settings_weather_sources),
                onBackPressed = onNavigateBack,
                actions = { AboutActivityIconButton(context) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddings ->
        PreferenceScreen(paddingValues = paddings) {
            if (BuildConfig.FLAVOR == "freenet") {
                clickablePreferenceItem(R.string.settings_weather_source_freenet_disclaimer) { id ->
                    val dialogLinkOpenState = remember { mutableStateOf(false) }

                    Material3CardListItem(
                        modifier = Modifier.clickable {
                            dialogLinkOpenState.value = true
                        }
                    ) {
                        Text(
                            text = stringResource(id),
                            color = DayNightTheme.colors.bodyColor,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(dimensionResource(R.dimen.normal_margin))
                        )
                    }
                    if (dialogLinkOpenState.value) {
                        AlertDialogLink(
                            onClose = { dialogLinkOpenState.value = false },
                            linkToOpen = "https://github.com/breezy-weather/breezy-weather/blob/main/INSTALL.md"
                        )
                    }
                }
            }

            sectionHeaderItem(R.string.settings_weather_sources_section_general)
            listPreferenceItem(R.string.settings_weather_sources_default_source) { id ->
                val configuredWorldwideSourcesAssociated = configuredWorldwideSources.associate { it.id to it.name }
                val defaultWeatherSource = SettingsManager.getInstance(context).defaultForecastSource
                SourceView(
                    title = stringResource(id),
                    selectedKey = if (configuredWorldwideSourcesAssociated.contains(defaultWeatherSource)) {
                        defaultWeatherSource
                    } else {
                        "auto"
                    },
                    sourceList = buildList {
                        add(Triple("auto", stringResource(R.string.settings_automatic), true))
                        addAll(
                            configuredWorldwideSources.map {
                                Triple(it.id, it.getName(context), it !is ConfigurableSource || it.isConfigured)
                            }
                        )
                    },
                    card = true
                ) { defaultSource ->
                    SettingsManager.getInstance(context).defaultForecastSource = defaultSource
                }
            }
            sectionFooterItem(R.string.settings_weather_sources_section_general)

            configurableSources
                .filter {
                    // Exclude location sources configured in their own screen
                    it !is LocationSource && it.getPreferences(context).isNotEmpty()
                }
                .sortedWith { ws1, ws2 ->
                    // Sort by name because there are now a lot of sources
                    Collator.getInstance(context.currentLocale).compare(ws1.name, ws2.name)
                }
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
                                        onValueChanged = preference.onValueChanged
                                    )
                                }
                            }

                            is EditTextPreference -> {
                                editTextPreferenceItem(preference.titleId) { id ->
                                    EditTextPreferenceView(
                                        titleId = id,
                                        summary = preference.summary,
                                        content = preference.content,
                                        placeholder = preference.placeholder,
                                        regex = preference.regex,
                                        regexError = preference.regexError,
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
    }
}
