/*
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

package org.breezyweather.ui.settings.compose

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.plus
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.ListPreference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.FeatureSource
import org.breezyweather.common.source.LocationSource
import org.breezyweather.common.source.NonFreeNetSource
import org.breezyweather.common.source.getName
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.composables.AlertDialogLink
import org.breezyweather.ui.common.composables.SourceView
import org.breezyweather.ui.common.widgets.Material3ExpressiveCardListItem
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.generateCollapsedScrollBehavior
import org.breezyweather.ui.common.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.ui.settings.preference.bottomInsetItem
import org.breezyweather.ui.settings.preference.clickablePreferenceItem
import org.breezyweather.ui.settings.preference.composables.EditTextPreferenceViewWithCard
import org.breezyweather.ui.settings.preference.composables.ListPreferenceView
import org.breezyweather.ui.settings.preference.composables.PreferenceScreen
import org.breezyweather.ui.settings.preference.composables.SectionFooter
import org.breezyweather.ui.settings.preference.composables.SectionHeader
import org.breezyweather.ui.settings.preference.editTextPreferenceItem
import org.breezyweather.ui.settings.preference.largeSeparatorItem
import org.breezyweather.ui.settings.preference.listPreferenceItem
import org.breezyweather.ui.settings.preference.sectionFooterItem
import org.breezyweather.ui.settings.preference.sectionHeaderItem
import org.breezyweather.ui.settings.preference.smallSeparatorItem
import java.text.Collator

@Composable
fun WeatherSourcesSettingsScreen(
    context: Context,
    onNavigateBack: () -> Unit,
    worldwideSources: ImmutableList<FeatureSource>,
    configurableSources: ImmutableList<ConfigurableSource>,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = generateCollapsedScrollBehavior()

    Material3Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            FitStatusBarTopAppBar(
                title = stringResource(R.string.settings_weather_sources),
                onBackPressed = onNavigateBack,
                actions = { AboutActivityIconButton(context) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddings ->
        PreferenceScreen(
            paddingValues = paddings.plus(PaddingValues(horizontal = dimensionResource(R.dimen.normal_margin)))
        ) {
            if (BuildConfig.FLAVOR == "freenet") {
                clickablePreferenceItem(R.string.settings_weather_source_freenet_disclaimer) { id ->
                    val dialogLinkOpenState = remember { mutableStateOf(false) }

                    Material3ExpressiveCardListItem(
                        surface = MaterialTheme.colorScheme.secondaryContainer,
                        onSurface = MaterialTheme.colorScheme.onSecondaryContainer,
                        isFirst = true,
                        isLast = true,
                        modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.small_margin))
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                top = dimensionResource(R.dimen.normal_margin),
                                start = dimensionResource(R.dimen.normal_margin),
                                end = dimensionResource(R.dimen.normal_margin)
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.settings_weather_source_freenet_disclaimer),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            TextButton(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                onClick = {
                                    dialogLinkOpenState.value = true
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.action_learn_more)
                                )
                            }
                        }
                    }
                    if (dialogLinkOpenState.value) {
                        AlertDialogLink(
                            onClose = { dialogLinkOpenState.value = false },
                            linkToOpen = "https://github.com/breezy-weather/breezy-weather/blob/main/INSTALL.md"
                        )
                    }
                }
                largeSeparatorItem()
            }

            sectionHeaderItem(R.string.settings_weather_sources_section_general)
            listPreferenceItem(R.string.settings_weather_sources_default_source) { id ->
                val worldwideSourcesAssociated = worldwideSources.associate { it.id to it.name }
                val defaultWeatherSource = SettingsManager.getInstance(context).defaultForecastSource
                SourceView(
                    title = stringResource(id),
                    selectedKey = if (worldwideSourcesAssociated.contains(defaultWeatherSource)) {
                        defaultWeatherSource
                    } else {
                        "auto"
                    },
                    sourceList = buildList {
                        add(Triple("auto", stringResource(R.string.settings_automatic), true))
                        addAll(
                            worldwideSources.map {
                                Triple(
                                    it.id,
                                    it.getName(context),
                                    (it !is ConfigurableSource || it.isConfigured) &&
                                        (BuildConfig.FLAVOR != "freenet" || it !is NonFreeNetSource)
                                )
                            }
                        )
                    }.toImmutableList(),
                    card = true,
                    isFirst = true,
                    isLast = true
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
                    largeSeparatorItem()
                    item(key = "header_${preferenceSource.id}") {
                        SectionHeader(title = preferenceSource.name)
                    }
                    preferenceSource.getPreferences(context).forEachIndexed { index, preference ->
                        when (preference) {
                            is ListPreference -> {
                                listPreferenceItem(preference.titleId) { id ->
                                    ListPreferenceView(
                                        titleId = id,
                                        selectedKey = preference.selectedKey,
                                        valueArrayId = preference.valueArrayId,
                                        nameArrayId = preference.nameArrayId,
                                        card = true,
                                        isFirst = index == 0,
                                        isLast = index == preferenceSource.getPreferences(context).lastIndex,
                                        onValueChanged = preference.onValueChanged
                                    )
                                }
                            }

                            is EditTextPreference -> {
                                editTextPreferenceItem(preference.titleId) { id ->
                                    EditTextPreferenceViewWithCard(
                                        titleId = id,
                                        summary = preference.summary,
                                        content = preference.content,
                                        placeholder = preference.placeholder,
                                        regex = preference.regex,
                                        regexError = preference.regexError,
                                        keyboardType = preference.keyboardType,
                                        isFirst = index == 0,
                                        isLast = index == preferenceSource.getPreferences(context).lastIndex,
                                        onValueChanged = preference.onValueChanged
                                    )
                                }
                            }
                        }
                        if (index != preferenceSource.getPreferences(context).lastIndex) {
                            smallSeparatorItem()
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
