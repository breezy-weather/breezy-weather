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

package org.breezyweather.common.ui.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import org.breezyweather.R
import breezyweather.domain.location.model.Location
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.main.MainActivity
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.preference.composables.ListPreferenceView
import org.breezyweather.settings.preference.composables.PreferenceView
import org.breezyweather.sources.SourceManager

@Composable
fun LocationPreference(
    activity: MainActivity,
    location: Location,
    includeMainScreenSettings: Boolean = false,
    onClose: ((location: Location?) -> Unit)
) {
    val dialogWeatherSourcesOpenState = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        if (includeMainScreenSettings) {
            PreferenceView(
                titleId = R.string.settings_main,
                iconId = R.drawable.ic_home,
                summaryId = R.string.settings_main_summary,
                card = false
            ) {
                IntentHelper.startMainScreenSettingsActivity(activity)
                onClose(null)
            }
        }
        if (location.isCurrentPosition) {
            val locationSources = activity.sourceManager.getLocationSources()
            SourceView(
                title = stringResource(R.string.settings_location_service),
                iconId = R.drawable.ic_location,
                selectedKey = SettingsManager.getInstance(activity).locationSource,
                sourceList = locationSources.associate { it.id to it.name },
                helpMeChoose = null
            ) { sourceId ->
                SettingsManager.getInstance(activity).locationSource = sourceId
                onClose(null)
            }
        }
        PreferenceView(
            titleId = R.string.settings_weather_sources,
            iconId = R.drawable.ic_factory,
            summaryId = R.string.settings_weather_sources_per_location_summary,
            card = false
        ) {
            dialogWeatherSourcesOpenState.value = true
        }

        if (dialogWeatherSourcesOpenState.value) {
            SecondarySourcesPreference(
                sourceManager = activity.sourceManager,
                location = location
            ) { newLocation ->
                if (newLocation != null) {
                    if (location.weatherSource != newLocation.weatherSource) {
                        // TODO: Don't save if match an existing location/main weather source
                        val locationExists = false
                        if (locationExists) {
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
                    onClose(null)
                }
            }
        }
    }
}

@Composable
fun SecondarySourcesPreference(
    sourceManager: SourceManager,
    location: Location,
    onClose: ((location: Location?) -> Unit)
) {
    val hasChangedMainSource = remember { mutableStateOf(false) }
    val hasChangedASecondarySource = remember { mutableStateOf(false) }
    val weatherSource = remember { mutableStateOf(location.weatherSource) }
    val airQualitySource = remember { mutableStateOf(location.airQualitySource ?: "") }
    val pollenSource = remember { mutableStateOf(location.pollenSource ?: "") }
    val minutelySource = remember { mutableStateOf(location.minutelySource ?: "") }
    val alertSource = remember { mutableStateOf(location.alertSource ?: "") }
    val normalsSource = remember { mutableStateOf(location.normalsSource ?: "") }
    val weatherSources = sourceManager.getConfiguredMainWeatherSources()
    val secondarySources = sourceManager.getSecondaryWeatherSources()
    val mainSource = sourceManager.getMainWeatherSource(weatherSource.value)
    val compatibleAirQualitySources = secondarySources.filter {
        it.id != weatherSource.value &&
                it.supportedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY) &&
                it.isFeatureSupportedForLocation(
                    SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY, location
                )
    }
    val compatiblePollenSources = secondarySources.filter {
        it.id != weatherSource.value &&
                it.supportedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_POLLEN)
                && it.isFeatureSupportedForLocation(
            SecondaryWeatherSourceFeature.FEATURE_POLLEN, location
        )
    }
    val compatibleMinutelySources = secondarySources.filter {
        it.id != weatherSource.value &&
                it.supportedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)
                && it.isFeatureSupportedForLocation(
            SecondaryWeatherSourceFeature.FEATURE_MINUTELY, location
        )
    }
    val compatibleAlertSources = secondarySources.filter {
        it.id != weatherSource.value &&
                it.supportedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)
                && it.isFeatureSupportedForLocation(
            SecondaryWeatherSourceFeature.FEATURE_ALERT, location
        )
    }
    val compatibleNormalsSources = secondarySources.filter {
        it.id != weatherSource.value &&
                it.supportedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_NORMALS)
                && it.isFeatureSupportedForLocation(
            SecondaryWeatherSourceFeature.FEATURE_NORMALS, location
        )
    }

    AlertDialog(
        onDismissRequest = {
            onClose(null)
        },
        title = {
            Text(
                text = stringResource(R.string.settings_weather_sources),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                SourceView(
                    title = stringResource(R.string.settings_weather_source_main),
                    selectedKey = weatherSource.value,
                    sourceList = weatherSources.associate { it.id to it.name },
                ) { sourceId ->
                    if (airQualitySource.value == sourceId) {
                        airQualitySource.value = ""
                    }
                    if (pollenSource.value == sourceId) {
                        pollenSource.value = ""
                    }
                    if (minutelySource.value == sourceId) {
                        minutelySource.value = ""
                    }
                    if (alertSource.value == sourceId) {
                        alertSource.value = ""
                    }
                    if (normalsSource.value == sourceId) {
                        normalsSource.value = ""
                    }
                    weatherSource.value = sourceId
                    hasChangedMainSource.value = true
                }
                SourceView(
                    title = stringResource(R.string.air_quality),
                    selectedKey = airQualitySource.value,
                    sourceList = mapOf(
                        "" to stringResource(
                            if (mainSource!!.supportedFeaturesInMain
                                    .contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)
                            ) {
                                R.string.settings_weather_source_main
                            } else R.string.settings_weather_source_none)
                    ) + compatibleAirQualitySources.associate { it.id to it.name }
                ) { sourceId ->
                    airQualitySource.value = sourceId
                    hasChangedASecondarySource.value = true
                }
                SourceView(
                    title = stringResource(R.string.pollen),
                    selectedKey = pollenSource.value,
                    sourceList = mapOf(
                        "" to stringResource(
                            if (mainSource.supportedFeaturesInMain
                                    .contains(SecondaryWeatherSourceFeature.FEATURE_POLLEN)
                            ) {
                                R.string.settings_weather_source_main
                            } else R.string.settings_weather_source_none)
                    ) + compatiblePollenSources.associate { it.id to it.name }
                ) { sourceId ->
                    pollenSource.value = sourceId
                    hasChangedASecondarySource.value = true
                }
                SourceView(
                    title = stringResource(R.string.minutely_precipitations),
                    selectedKey = minutelySource.value,
                    sourceList = mapOf(
                        "" to stringResource(
                            if (mainSource.supportedFeaturesInMain
                                    .contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)
                            ) {
                                R.string.settings_weather_source_main
                            } else R.string.settings_weather_source_none)
                    ) + compatibleMinutelySources.associate { it.id to it.name }
                ) { sourceId ->
                    minutelySource.value = sourceId
                    hasChangedASecondarySource.value = true
                }
                SourceView(
                    title = stringResource(R.string.alerts),
                    selectedKey = alertSource.value,
                    sourceList = mapOf(
                        "" to stringResource(
                            if (mainSource.supportedFeaturesInMain
                                    .contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)
                            ) {
                                R.string.settings_weather_source_main
                            } else R.string.settings_weather_source_none)
                    ) + compatibleAlertSources.associate { it.id to it.name }
                ) { sourceId ->
                    alertSource.value = sourceId
                    hasChangedASecondarySource.value = true
                }
                SourceView(
                    title = stringResource(R.string.temperature_normals),
                    selectedKey = normalsSource.value,
                    sourceList = mapOf(
                        "" to stringResource(
                            if (mainSource.supportedFeaturesInMain
                                    .contains(SecondaryWeatherSourceFeature.FEATURE_NORMALS)
                            ) {
                                R.string.settings_weather_source_main
                            } else R.string.settings_weather_source_none)
                    ) + compatibleNormalsSources.associate { it.id to it.name }
                ) { sourceId ->
                    normalsSource.value = sourceId
                    hasChangedASecondarySource.value = true
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (hasChangedMainSource.value || hasChangedASecondarySource.value) {
                        val newLocation = location.copy(
                            // Reset cityId when changing source as they are linked to source
                            // Also reset secondary weather sources which are the new main source
                            cityId = if (hasChangedMainSource.value) "" else location.cityId,
                            weatherSource = weatherSource.value,
                            airQualitySource = if (hasChangedMainSource.value && airQualitySource.value == weatherSource.value) "" else airQualitySource.value,
                            pollenSource = if (hasChangedMainSource.value && pollenSource.value == weatherSource.value) "" else pollenSource.value,
                            minutelySource = if (hasChangedMainSource.value && minutelySource.value == weatherSource.value) "" else minutelySource.value,
                            alertSource = if (hasChangedMainSource.value && alertSource.value == weatherSource.value) "" else alertSource.value,
                            normalsSource = if (hasChangedMainSource.value && normalsSource.value == weatherSource.value) "" else normalsSource.value,
                            needsGeocodeRefresh = hasChangedMainSource.value,
                            // TODO: Will trigger a full refresh which we should avoid
                            // if we only change a secondary weather source
                            weather = location.weather?.let {
                                it.copy(
                                    base = it.base.copy(
                                        refreshTime = null,
                                        mainUpdateTime = null,
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
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
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
                    text = stringResource(R.string.action_cancel),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    )
}

@Composable
private fun SourceView(
    title: String,
    @DrawableRes iconId: Int? = null,
    selectedKey: String,
    enabled: Boolean = true,
    sourceList: Map<String, String>,
    helpMeChoose: String? = "https://github.com/breezy-weather/breezy-weather/blob/main/docs/SOURCES.md",
    onValueChanged: (String) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    ListPreferenceView(
        title = title,
        iconId = iconId,
        selectedKey = selectedKey,
        valueArray = sourceList.map { it.key }.toTypedArray(),
        nameArray = sourceList.map { it.value }.toTypedArray(),
        summary = { _, value ->
            sourceList.getOrElse(value) { null }
        },
        onValueChanged = { sourceId ->
            onValueChanged(sourceId)
        },
        dismissButton = if (!helpMeChoose.isNullOrEmpty()) {
            {
                TextButton(
                    onClick = { uriHandler.openUri(helpMeChoose) }
                ) {
                    Text(
                        text = stringResource(R.string.action_help_me_choose),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        } else null,
        enabled = enabled,
        card = false
    )
}