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

package org.breezyweather.ui.common.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.getName
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.source.resourceName
import org.breezyweather.sources.SourceManager
import org.breezyweather.ui.common.widgets.Material3ExpressiveCardListItem
import org.breezyweather.ui.main.MainActivity
import org.breezyweather.ui.settings.preference.composables.EditTextPreferenceView
import org.breezyweather.ui.settings.preference.composables.ListPreferenceView
import org.breezyweather.ui.settings.preference.composables.ListPreferenceViewWithCard
import org.breezyweather.ui.settings.preference.composables.ListPreferenceWithGroupsView
import org.breezyweather.ui.settings.preference.composables.PreferenceView
import org.breezyweather.ui.settings.preference.composables.SectionFooter
import org.breezyweather.ui.settings.preference.composables.SectionHeader
import java.text.Collator

@Composable
fun LocationPreference(
    activity: MainActivity,
    location: Location,
    onClose: ((location: Location?) -> Unit),
    locationExists: ((location: Location) -> Boolean),
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val dialogWeatherSourcesOpenState = remember { mutableStateOf(false) }
    val dialogAdditionalLocationPreferencesOpenState = remember { mutableStateOf(false) }
    val sourcesWithPreferencesScreen = remember { activity.sourceManager.sourcesWithPreferencesScreen(location) }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        if (location.isCurrentPosition) {
            val locationSources = activity.sourceManager.getLocationSources()
            SourceView(
                title = stringResource(R.string.settings_location_service),
                iconId = R.drawable.ic_location,
                selectedKey = SettingsManager.getInstance(activity).locationSource,
                sourceList = locationSources.map {
                    Triple(it.id, it.getName(context), it !is ConfigurableSource || it.isConfigured)
                }.toImmutableList(),
                colors = ListItemDefaults.colors(AlertDialogDefaults.containerColor)
            ) { sourceId ->
                SettingsManager.getInstance(activity).locationSource = sourceId
                onClose(null)
            }
        }
        PreferenceView(
            titleId = R.string.settings_weather_sources,
            iconId = R.drawable.ic_factory,
            summaryId = R.string.settings_weather_sources_per_location_summary,
            colors = ListItemDefaults.colors(containerColor = AlertDialogDefaults.containerColor)
        ) {
            dialogWeatherSourcesOpenState.value = true
        }
        if (!location.isCurrentPosition || sourcesWithPreferencesScreen.isNotEmpty()) {
            PreferenceView(
                titleId = R.string.settings_per_location,
                iconId = R.drawable.ic_settings,
                summaryId = R.string.settings_per_location_summary,
                colors = ListItemDefaults.colors(containerColor = AlertDialogDefaults.containerColor)
            ) {
                dialogAdditionalLocationPreferencesOpenState.value = true
            }
        }
        PreferenceView(
            titleId = R.string.settings_global,
            iconId = R.drawable.ic_home,
            summaryId = R.string.settings_main_summary,
            colors = ListItemDefaults.colors(containerColor = AlertDialogDefaults.containerColor)
        ) {
            IntentHelper.startMainScreenSettingsActivity(activity)
            onClose(null)
        }

        if (dialogWeatherSourcesOpenState.value) {
            SecondarySourcesPreference(
                sourceManager = activity.sourceManager,
                location = location,
                onClose = { newLocation ->
                    if (newLocation != null) {
                        if (location.forecastSource != newLocation.forecastSource) {
                            // Don't save if matches an existing location/forecast source
                            if (locationExists(newLocation)) {
                                SnackbarHelper.showSnackbar(
                                    activity.getString(R.string.location_message_already_exists)
                                )
                                dialogWeatherSourcesOpenState.value = false
                                onClose(null)
                            } else {
                                dialogWeatherSourcesOpenState.value = false
                                onClose(newLocation)
                            }
                        } else {
                            dialogWeatherSourcesOpenState.value = false
                            onClose(newLocation)
                        }
                    } else {
                        dialogWeatherSourcesOpenState.value = false
                    }
                },
                locationExists = locationExists
            )
        }

        if (dialogAdditionalLocationPreferencesOpenState.value) {
            val hasChangedPreferences = remember { mutableStateOf(false) }
            val customName = remember {
                mutableStateOf(location.customName)
            }
            AlertDialogNoPadding(
                title = {
                    Text(
                        text = stringResource(R.string.settings_per_location),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (!location.isCurrentPosition) {
                            SectionHeader(title = stringResource(R.string.settings_section_general))
                            EditTextPreferenceView(
                                title = stringResource(R.string.location_custom_name),
                                summary = { _, value ->
                                    value.ifEmpty { location.cityAndDistrict }
                                },
                                content = location.customName.let {
                                    if (it.isNullOrEmpty()) location.cityAndDistrict else it
                                },
                                onValueChanged = {
                                    if (it != location.cityAndDistrict && it != location.customName) {
                                        customName.value = it
                                        hasChangedPreferences.value = true
                                    } else if ((it == location.cityAndDistrict || it.isEmpty()) &&
                                        !location.customName.isNullOrEmpty()
                                    ) {
                                        customName.value = ""
                                        hasChangedPreferences.value = true
                                    }
                                }
                            )
                            SectionFooter()
                        }

                        sourcesWithPreferencesScreen.forEach { preferenceSource ->
                            SectionHeader(title = preferenceSource.name)
                            preferenceSource.PerLocationPreferences(
                                context = activity,
                                location = location,
                                features = persistentListOf() // TODO
                            ) {
                                val newParameters = location.parameters.toMutableMap()
                                newParameters[preferenceSource.id] = buildMap {
                                    if (newParameters.getOrElse(preferenceSource.id) { null } != null) {
                                        putAll(newParameters[preferenceSource.id]!!)
                                    }
                                    putAll(it)
                                }
                                dialogAdditionalLocationPreferencesOpenState.value = false
                                dialogWeatherSourcesOpenState.value = false
                                onClose(
                                    location.copy(
                                        parameters = newParameters,
                                        // TODO: Will trigger a full refresh which we should avoid
                                        // if we only change a secondary weather source
                                        weather = location.weather?.let { weather ->
                                            weather.copy(
                                                base = weather.base.copy(
                                                    refreshTime = null,
                                                    forecastUpdateTime = null,
                                                    currentUpdateTime = null,
                                                    airQualityUpdateTime = null,
                                                    pollenUpdateTime = null,
                                                    minutelyUpdateTime = null,
                                                    alertsUpdateTime = null,
                                                    normalsUpdateTime = null
                                                )
                                            )
                                        }
                                    )
                                )
                            }
                            SectionFooter()
                        }
                    }
                },
                onDismissRequest = {
                    dialogAdditionalLocationPreferencesOpenState.value = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (hasChangedPreferences.value) {
                                dialogAdditionalLocationPreferencesOpenState.value = false
                                dialogWeatherSourcesOpenState.value = false
                                onClose(location.copy(customName = customName.value))
                            } else {
                                dialogAdditionalLocationPreferencesOpenState.value = false
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.action_close),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun SecondarySourcesPreference(
    sourceManager: SourceManager,
    location: Location,
    onClose: ((location: Location?) -> Unit),
    modifier: Modifier = Modifier,
    locationExists: ((location: Location) -> Boolean)? = null,
) {
    val context = LocalContext.current
    val continentComparator = Comparator<SourceContinent?> { va1, va2 ->
        if (va1 == null && va2 == null) {
            0
        } else if (va1 == null) {
            -1
        } else if (va2 == null) {
            1
        } else if (va1 == SourceContinent.WORLDWIDE && va2 == SourceContinent.WORLDWIDE) {
            0
        } else if (va1 == SourceContinent.WORLDWIDE) {
            -1
        } else if (va2 == SourceContinent.WORLDWIDE) {
            1
        } else {
            Collator.getInstance(context.currentLocale)
                .compare(
                    context.getString(va1.resourceName!!),
                    context.getString(va2.resourceName!!)
                )
        }
    }

    val dialogLinkOpenState = remember { mutableStateOf(false) }
    val hasChangedReverseGeocodingSource = remember { mutableStateOf(false) }
    val hasChangedASource = remember { mutableStateOf(false) }
    val forecastSource = remember { mutableStateOf(location.forecastSource) }
    val isLocationDuplicate = remember { mutableStateOf(false) }
    val currentSource = remember { mutableStateOf(location.currentSource ?: "") }
    val airQualitySource = remember { mutableStateOf(location.airQualitySource ?: "") }
    val pollenSource = remember { mutableStateOf(location.pollenSource ?: "") }
    val minutelySource = remember { mutableStateOf(location.minutelySource ?: "") }
    val alertSource = remember { mutableStateOf(location.alertSource ?: "") }
    val normalsSource = remember { mutableStateOf(location.normalsSource ?: "") }
    val reverseGeocodingSource = remember { mutableStateOf(location.reverseGeocodingSource ?: "") }

    /**
     * In the filter condition, we always take the current source, even if no longer compatible
     * That way, we can show it later as "unavailable" if no longer compatible instead of not having it in the list
     */
    val compatibleForecastSources = sourceManager
        .getSupportedWeatherSources(SourceFeature.FORECAST, location, forecastSource.value)
        .groupBy { if (it is HttpSource) it.continent else SourceContinent.WORLDWIDE }
        .toSortedMap(continentComparator)
        .mapValues { m ->
            m.value.map {
                Triple(
                    it.id,
                    it.getName(context, SourceFeature.FORECAST, location),
                    (it !is ConfigurableSource || it.isConfigured) &&
                        it.isFeatureSupportedForLocation(location, SourceFeature.FORECAST)
                )
            }.toImmutableList()
        }.toImmutableMap()

    val compatibleCurrentSources = sourceManager
        .getSupportedWeatherSources(SourceFeature.CURRENT, location, currentSource.value)
        .groupBy { if (it is HttpSource) it.continent else SourceContinent.WORLDWIDE }
        .toSortedMap(continentComparator)
        .mapValues { m ->
            m.value.map {
                Triple(
                    it.id,
                    it.getName(context, SourceFeature.CURRENT, location),
                    (it !is ConfigurableSource || it.isConfigured) &&
                        it.isFeatureSupportedForLocation(location, SourceFeature.CURRENT)
                )
            }.toImmutableList()
        }.toImmutableMap()
    val compatibleAirQualitySources = sourceManager
        .getSupportedWeatherSources(SourceFeature.AIR_QUALITY, location, airQualitySource.value)
        .groupBy { if (it is HttpSource) it.continent else SourceContinent.WORLDWIDE }
        .toSortedMap(continentComparator)
        .mapValues { m ->
            m.value.map {
                Triple(
                    it.id,
                    it.getName(context, SourceFeature.AIR_QUALITY, location),
                    (it !is ConfigurableSource || it.isConfigured) &&
                        it.isFeatureSupportedForLocation(location, SourceFeature.AIR_QUALITY)
                )
            }.toImmutableList()
        }.toImmutableMap()
    val compatiblePollenSources = sourceManager
        .getSupportedWeatherSources(SourceFeature.POLLEN, location, pollenSource.value)
        .groupBy { if (it is HttpSource) it.continent else SourceContinent.WORLDWIDE }
        .toSortedMap(continentComparator)
        .mapValues { m ->
            m.value.map {
                Triple(
                    it.id,
                    it.getName(context, SourceFeature.POLLEN, location),
                    (it !is ConfigurableSource || it.isConfigured) &&
                        it.isFeatureSupportedForLocation(location, SourceFeature.POLLEN)
                )
            }.toImmutableList()
        }.toImmutableMap()
    val compatibleMinutelySources = sourceManager
        .getSupportedWeatherSources(SourceFeature.MINUTELY, location, minutelySource.value)
        .groupBy { if (it is HttpSource) it.continent else SourceContinent.WORLDWIDE }
        .toSortedMap(continentComparator)
        .mapValues { m ->
            m.value.map {
                Triple(
                    it.id,
                    it.getName(context, SourceFeature.MINUTELY, location),
                    (it !is ConfigurableSource || it.isConfigured) &&
                        it.isFeatureSupportedForLocation(location, SourceFeature.MINUTELY)
                )
            }.toImmutableList()
        }.toImmutableMap()
    val compatibleAlertSources = sourceManager
        .getSupportedWeatherSources(SourceFeature.ALERT, location, alertSource.value)
        .groupBy { if (it is HttpSource) it.continent else SourceContinent.WORLDWIDE }
        .toSortedMap(continentComparator)
        .mapValues { m ->
            m.value.map {
                Triple(
                    it.id,
                    it.getName(context, SourceFeature.ALERT, location),
                    (it !is ConfigurableSource || it.isConfigured) &&
                        it.isFeatureSupportedForLocation(location, SourceFeature.ALERT)
                )
            }.toImmutableList()
        }.toImmutableMap()
    val compatibleNormalsSources = sourceManager
        .getSupportedWeatherSources(SourceFeature.NORMALS, location, normalsSource.value)
        .groupBy { if (it is HttpSource) it.continent else SourceContinent.WORLDWIDE }
        .toSortedMap(continentComparator)
        .mapValues { m ->
            m.value.map {
                Triple(
                    it.id,
                    it.getName(context, SourceFeature.NORMALS, location),
                    (it !is ConfigurableSource || it.isConfigured) &&
                        it.isFeatureSupportedForLocation(location, SourceFeature.NORMALS)
                )
            }.toImmutableList()
        }.toImmutableMap()

    val compatibleReverseGeocodingSources = sourceManager
        .getSupportedReverseGeocodingSources(location)
        .groupBy { if (it is HttpSource) it.continent else SourceContinent.WORLDWIDE }
        .toSortedMap(continentComparator)
        .mapValues { m ->
            m.value.map {
                Triple(
                    it.id,
                    it.getName(context, SourceFeature.REVERSE_GEOCODING, location),
                    (it !is ConfigurableSource || it.isConfigured) &&
                        it.isReverseGeocodingSupportedForLocation(location)
                )
            }.toImmutableList()
        }.toImmutableMap()

    AlertDialogNoPadding(
        modifier = modifier,
        onDismissRequest = {
            onClose(null)
        },
        title = {
            Text(
                text = stringResource(R.string.settings_weather_sources),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                if (BuildConfig.FLAVOR == "freenet") {
                    Material3ExpressiveCardListItem(
                        surface = MaterialTheme.colorScheme.secondaryContainer,
                        onSurface = MaterialTheme.colorScheme.onSecondaryContainer,
                        isFirst = true,
                        isLast = true,
                        modifier = Modifier
                            .padding(horizontal = dimensionResource(R.dimen.small_margin))
                            .clickable { dialogLinkOpenState.value = true }
                    ) {
                        Text(
                            text = stringResource(R.string.settings_weather_source_freenet_disclaimer),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(dimensionResource(R.dimen.normal_margin))
                        )
                    }
                }
                if (location.isCurrentPosition && !location.isUsable) {
                    Material3ExpressiveCardListItem(
                        surface = MaterialTheme.colorScheme.secondaryContainer,
                        onSurface = MaterialTheme.colorScheme.onSecondaryContainer,
                        isFirst = true,
                        isLast = true,
                        modifier = Modifier
                            .padding(horizontal = dimensionResource(R.dimen.small_margin))
                            .clickable { dialogLinkOpenState.value = true }
                    ) {
                        Text(
                            text = stringResource(R.string.settings_weather_source_current_position_disclaimer),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(dimensionResource(R.dimen.normal_margin))
                        )
                    }
                }
                SourceViewWithContinents(
                    title = stringResource(SourceFeature.FORECAST.resourceName),
                    selectedKey = forecastSource.value,
                    sourceList = buildMap {
                        if (
                            forecastSource.value.isNotEmpty() &&
                            !compatibleForecastSources.values.any { l ->
                                l.any { it.first == forecastSource.value }
                            }
                        ) {
                            put(
                                null,
                                buildList {
                                    add(
                                        Triple(
                                            forecastSource.value,
                                            stringResource(
                                                R.string.settings_weather_source_unavailable,
                                                forecastSource.value
                                            ),
                                            false
                                        )
                                    )
                                }.toImmutableList()
                            )
                        }
                        putAll(compatibleForecastSources)
                    }.toImmutableMap(),
                    withState = false
                ) { sourceId ->
                    if (locationExists != null) {
                        if (sourceId != location.forecastSource) {
                            isLocationDuplicate.value = locationExists(
                                location.copy(
                                    forecastSource = sourceId
                                )
                            )
                        } else {
                            isLocationDuplicate.value = false
                        }
                    }
                    forecastSource.value = sourceId
                    hasChangedASource.value = true
                }
                if (isLocationDuplicate.value) {
                    Text(
                        text = stringResource(R.string.location_message_already_exists),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.normal_margin))
                    )
                }
                SourceViewWithContinents(
                    title = stringResource(SourceFeature.CURRENT.resourceName),
                    selectedKey = currentSource.value,
                    sourceList = buildMap {
                        put(
                            null,
                            buildList {
                                if (currentSource.value.isNotEmpty() &&
                                    !compatibleCurrentSources.values.any { l ->
                                        l.any { it.first == currentSource.value }
                                    }
                                ) {
                                    add(
                                        Triple(
                                            currentSource.value,
                                            stringResource(
                                                R.string.settings_weather_source_unavailable,
                                                currentSource.value
                                            ),
                                            false
                                        )
                                    )
                                }
                                add(
                                    Triple(
                                        "",
                                        stringResource(R.string.forecast),
                                        true
                                    )
                                )
                            }.toImmutableList()
                        )
                        putAll(compatibleCurrentSources)
                    }.toImmutableMap(),
                    withState = false
                ) { sourceId ->
                    currentSource.value = sourceId
                    hasChangedASource.value = true
                }
                SourceViewWithContinents(
                    title = stringResource(SourceFeature.AIR_QUALITY.resourceName),
                    selectedKey = airQualitySource.value,
                    sourceList = buildMap {
                        put(
                            null,
                            buildList {
                                if (airQualitySource.value.isNotEmpty() &&
                                    !compatibleAirQualitySources.values.any { l ->
                                        l.any { it.first == airQualitySource.value }
                                    }
                                ) {
                                    add(
                                        Triple(
                                            airQualitySource.value,
                                            stringResource(
                                                R.string.settings_weather_source_unavailable,
                                                airQualitySource.value
                                            ),
                                            false
                                        )
                                    )
                                }
                                add(
                                    Triple(
                                        "",
                                        stringResource(R.string.settings_weather_source_none),
                                        true
                                    )
                                )
                            }.toImmutableList()
                        )
                        putAll(compatibleAirQualitySources)
                    }.toImmutableMap(),
                    withState = false
                ) { sourceId ->
                    airQualitySource.value = sourceId
                    hasChangedASource.value = true
                }
                SourceViewWithContinents(
                    title = stringResource(SourceFeature.POLLEN.resourceName),
                    selectedKey = pollenSource.value,
                    sourceList = buildMap {
                        put(
                            null,
                            buildList {
                                if (pollenSource.value.isNotEmpty() &&
                                    !compatiblePollenSources.values.any { l ->
                                        l.any { it.first == pollenSource.value }
                                    }
                                ) {
                                    add(
                                        Triple(
                                            pollenSource.value,
                                            stringResource(
                                                R.string.settings_weather_source_unavailable,
                                                pollenSource.value
                                            ),
                                            false
                                        )
                                    )
                                }
                                add(
                                    Triple(
                                        "",
                                        stringResource(R.string.settings_weather_source_none),
                                        true
                                    )
                                )
                            }.toImmutableList()
                        )
                        putAll(compatiblePollenSources)
                    }.toImmutableMap(),
                    withState = false
                ) { sourceId ->
                    pollenSource.value = sourceId
                    hasChangedASource.value = true
                }
                SourceViewWithContinents(
                    title = stringResource(SourceFeature.MINUTELY.resourceName),
                    selectedKey = minutelySource.value,
                    sourceList = buildMap {
                        put(
                            null,
                            buildList {
                                if (minutelySource.value.isNotEmpty() &&
                                    !compatibleMinutelySources.values.any { l ->
                                        l.any { it.first == minutelySource.value }
                                    }
                                ) {
                                    add(
                                        Triple(
                                            minutelySource.value,
                                            stringResource(
                                                R.string.settings_weather_source_unavailable,
                                                minutelySource.value
                                            ),
                                            false
                                        )
                                    )
                                }
                                add(
                                    Triple(
                                        "",
                                        stringResource(R.string.settings_weather_source_none),
                                        true
                                    )
                                )
                            }.toImmutableList()
                        )
                        putAll(compatibleMinutelySources)
                    }.toImmutableMap(),
                    withState = false
                ) { sourceId ->
                    minutelySource.value = sourceId
                    hasChangedASource.value = true
                }
                SourceViewWithContinents(
                    title = stringResource(SourceFeature.ALERT.resourceName),
                    selectedKey = alertSource.value,
                    sourceList = buildMap {
                        put(
                            null,
                            buildList {
                                if (alertSource.value.isNotEmpty() &&
                                    !compatibleAlertSources.values.any { l ->
                                        l.any { it.first == alertSource.value }
                                    }
                                ) {
                                    add(
                                        Triple(
                                            alertSource.value,
                                            stringResource(
                                                R.string.settings_weather_source_unavailable,
                                                alertSource.value
                                            ),
                                            false
                                        )
                                    )
                                }
                                add(
                                    Triple(
                                        "",
                                        stringResource(R.string.settings_weather_source_none),
                                        true
                                    )
                                )
                            }.toImmutableList()
                        )
                        putAll(compatibleAlertSources)
                    }.toImmutableMap(),
                    withState = false
                ) { sourceId ->
                    alertSource.value = sourceId
                    hasChangedASource.value = true
                }
                SourceViewWithContinents(
                    title = stringResource(SourceFeature.NORMALS.resourceName),
                    selectedKey = normalsSource.value,
                    sourceList = buildMap {
                        put(
                            null,
                            buildList {
                                if (normalsSource.value.isNotEmpty() &&
                                    !compatibleNormalsSources.values.any { l ->
                                        l.any { it.first == normalsSource.value }
                                    }
                                ) {
                                    add(
                                        Triple(
                                            normalsSource.value,
                                            stringResource(
                                                R.string.settings_weather_source_unavailable,
                                                normalsSource.value
                                            ),
                                            false
                                        )
                                    )
                                }
                                add(
                                    Triple(
                                        "",
                                        stringResource(R.string.settings_weather_source_none),
                                        true
                                    )
                                )
                            }.toImmutableList()
                        )
                        putAll(compatibleNormalsSources)
                    }.toImmutableMap(),
                    withState = false
                ) { sourceId ->
                    normalsSource.value = sourceId
                    hasChangedASource.value = true
                }
                if (location.isCurrentPosition) {
                    SourceViewWithContinents(
                        title = stringResource(SourceFeature.REVERSE_GEOCODING.resourceName),
                        selectedKey = reverseGeocodingSource.value,
                        sourceList = buildMap {
                            put(
                                null,
                                buildList {
                                    if (reverseGeocodingSource.value.isNotEmpty() &&
                                        !compatibleReverseGeocodingSources.values.any { l ->
                                            l.any { it.first == reverseGeocodingSource.value }
                                        }
                                    ) {
                                        add(
                                            Triple(
                                                reverseGeocodingSource.value,
                                                stringResource(
                                                    R.string.settings_weather_source_unavailable,
                                                    reverseGeocodingSource.value
                                                ),
                                                false
                                            )
                                        )
                                    }
                                    add(
                                        Triple(
                                            "",
                                            stringResource(R.string.settings_weather_source_none),
                                            true
                                        )
                                    )
                                }.toImmutableList()
                            )
                            putAll(compatibleReverseGeocodingSources)
                        }.toImmutableMap(),
                        withState = false
                    ) { sourceId ->
                        reverseGeocodingSource.value = sourceId
                        hasChangedReverseGeocodingSource.value = true
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isLocationDuplicate.value,
                onClick = {
                    if (hasChangedReverseGeocodingSource.value || hasChangedASource.value) {
                        val newLocation = location.copy(
                            // Reset cityId as they differ from one reverse geocoding source to another
                            cityId = if (hasChangedReverseGeocodingSource.value) "" else location.cityId,
                            forecastSource = forecastSource.value,
                            currentSource = currentSource.value,
                            airQualitySource = airQualitySource.value,
                            pollenSource = pollenSource.value,
                            minutelySource = minutelySource.value,
                            alertSource = alertSource.value,
                            normalsSource = normalsSource.value,
                            reverseGeocodingSource = reverseGeocodingSource.value,
                            needsGeocodeRefresh = hasChangedReverseGeocodingSource.value && location.isCurrentPosition,
                            // TODO: Will trigger a full refresh which we should avoid
                            // if we only change a secondary weather source
                            weather = location.weather?.let { weather ->
                                weather.copy(
                                    base = weather.base.copy(
                                        refreshTime = null,
                                        forecastUpdateTime = null,
                                        currentUpdateTime = null,
                                        airQualityUpdateTime = null,
                                        pollenUpdateTime = null,
                                        minutelyUpdateTime = null,
                                        alertsUpdateTime = null,
                                        normalsUpdateTime = null
                                    )
                                )
                            }
                        )
                        onClose(newLocation)
                    } else {
                        onClose(location)
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.action_save),
                    color = if (!isLocationDuplicate.value) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onClose(null)
                }
            ) {
                Text(
                    text = stringResource(android.R.string.cancel),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )

    if (dialogLinkOpenState.value) {
        AlertDialogLink(
            onClose = { dialogLinkOpenState.value = false },
            linkToOpen = "https://github.com/breezy-weather/breezy-weather/blob/main/INSTALL.md"
        )
    }
}

@Composable
fun SourceView(
    title: String,
    selectedKey: String,
    sourceList: ImmutableList<Triple<String, String, Boolean>>,
    @DrawableRes iconId: Int? = null,
    enabled: Boolean = true,
    card: Boolean = false,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    colors: ListItemColors = ListItemDefaults.colors(),
    withState: Boolean = true,
    onValueChanged: (String) -> Unit,
) {
    val dialogLinkOpenState = remember { mutableStateOf(false) }

    // TODO: Reduce redundancy
    if (card) {
        ListPreferenceViewWithCard(
            title = title,
            iconId = iconId,
            selectedKey = selectedKey,
            valueArray = sourceList.map { it.first }.toTypedArray(),
            nameArray = sourceList.map { it.second }.toTypedArray(),
            enableArray = sourceList.map { it.third }.toTypedArray(),
            summary = { _, value ->
                sourceList.firstOrNull { it.first == value }?.second
            },
            onValueChanged = { sourceId ->
                onValueChanged(sourceId)
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        dialogLinkOpenState.value = true
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_help_me_choose),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            enabled = enabled,
            colors = colors,
            isFirst = isFirst,
            isLast = isLast,
            withState = withState
        )
    } else {
        ListPreferenceView(
            title = title,
            iconId = iconId,
            selectedKey = selectedKey,
            valueArray = sourceList.map { it.first }.toTypedArray(),
            nameArray = sourceList.map { it.second }.toTypedArray(),
            enableArray = sourceList.map { it.third }.toTypedArray(),
            summary = { _, value ->
                sourceList.firstOrNull { it.first == value }?.second
            },
            onValueChanged = { sourceId ->
                onValueChanged(sourceId)
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        dialogLinkOpenState.value = true
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_help_me_choose),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            enabled = enabled,
            colors = colors,
            withState = withState
        )
    }

    if (dialogLinkOpenState.value) {
        AlertDialogLink(
            onClose = { dialogLinkOpenState.value = false },
            linkToOpen = "https://github.com/breezy-weather/breezy-weather/blob/main/docs/SOURCES.md"
        )
    }
}

@Composable
fun SourceViewWithContinents(
    title: String,
    selectedKey: String,
    sourceList: ImmutableMap<SourceContinent?, ImmutableList<Triple<String, String, Boolean>>>,
    modifier: Modifier = Modifier,
    @DrawableRes iconId: Int? = null,
    enabled: Boolean = true,
    colors: ListItemColors = ListItemDefaults.colors(AlertDialogDefaults.containerColor),
    withState: Boolean = true,
    onValueChanged: (String) -> Unit,
) {
    val dialogLinkOpenState = remember { mutableStateOf(false) }
    val context = LocalContext.current

    ListPreferenceWithGroupsView(
        modifier = modifier,
        title = title,
        iconId = iconId,
        selectedKey = selectedKey,
        values = sourceList.mapKeys {
            it.key?.let { k -> context.getString(k.resourceName!!) }
        }.toImmutableMap(),
        summary = { _, value ->
            sourceList.values.firstOrNull { c ->
                c.firstOrNull { it.first == value } != null
            }?.firstOrNull { it.first == value }?.second
        },
        onValueChanged = { sourceId ->
            onValueChanged(sourceId)
        },
        dismissButton = {
            TextButton(
                onClick = {
                    dialogLinkOpenState.value = true
                }
            ) {
                Text(
                    text = stringResource(R.string.action_help_me_choose),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        enabled = enabled,
        colors = colors,
        withState = withState
    )

    if (dialogLinkOpenState.value) {
        AlertDialogLink(
            onClose = { dialogLinkOpenState.value = false },
            linkToOpen = "https://github.com/breezy-weather/breezy-weather/blob/main/docs/SOURCES.md"
        )
    }
}
