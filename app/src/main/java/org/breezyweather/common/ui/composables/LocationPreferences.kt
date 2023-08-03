package org.breezyweather.common.ui.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.bus.EventBus
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.db.repositories.LocationEntityRepository
import org.breezyweather.main.MainActivity
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.preference.composables.ListPreferenceView
import org.breezyweather.settings.preference.composables.PreferenceView
import org.breezyweather.settings.preference.composables.SwitchPreferenceView
import org.breezyweather.sources.SourceManager

@Composable
fun LocationPreference(
    activity: MainActivity,
    location: Location,
    includeMainScreenSettings: Boolean = false,
    onClose: (() -> Unit)
) {
    Column {
        if (includeMainScreenSettings) {
            PreferenceView(
                titleId = R.string.settings_main,
                iconId = R.drawable.ic_home,
                summaryId = R.string.settings_main_summary,
                card = false
            ) {
                IntentHelper.startMainScreenSettingsActivity(activity)
                onClose()
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
                onClose()
            }
        } else {
            SwitchPreferenceView(
                title = stringResource(R.string.location_resident_location),
                iconId = R.drawable.ic_tag_plus,
                summary = { context, it ->

                    context.getString(if (it) {
                        R.string.location_resident_location_summaryOn
                    } else R.string.location_resident_location_summaryOff)
                        .replace("$", SettingsManager.getInstance(context).distanceUnit.getValueText(context, 20000f))
                },
                checked = location.isResidentPosition,
                card = false,
                onValueChanged = {
                    val newLocation = location.copy(
                        isResidentPosition = it
                        // Should we clean old weather data?
                    )
                    LocationEntityRepository.writeLocation(newLocation)
                    // TODO: Leads to "Updated in background" message
                    EventBus.instance
                        .with(Location::class.java)
                        .postValue(newLocation)
                },
            )
        }
        SecondarySourcesPreference(
            sourceManager = activity.sourceManager,
            location = location,
            onClose = onClose
        )
    }
}

@Composable
private fun SecondarySourcesPreference(
    sourceManager: SourceManager,
    location: Location,
    onClose: (() -> Unit)
) {
    val dialogOpenState = remember { mutableStateOf(false) }
    val hasChangedASource = remember { mutableStateOf(false) }
    val weatherSource = remember { mutableStateOf(location.weatherSource) }
    val airQualitySource = remember { mutableStateOf(location.airQualitySource ?: "") }
    val allergenSource = remember { mutableStateOf(location.allergenSource ?: "") }
    val minutelySource = remember { mutableStateOf(location.minutelySource ?: "") }
    val alertSource = remember { mutableStateOf(location.alertSource ?: "") }

    PreferenceView(
        titleId = R.string.settings_weather_sources,
        iconId = R.drawable.ic_factory,
        summaryId = R.string.settings_weather_sources_per_location_summary,
        card = false
    ) {
        dialogOpenState.value = true
    }

    if (dialogOpenState.value) {
        val weatherSources = sourceManager.getConfiguredMainWeatherSources()
        val secondarySources = sourceManager.getSecondaryWeatherSources()
        val compatibleAirQualitySources = secondarySources.filter {
            it.id != location.weatherSource &&
                    it.supportedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY) &&
                    it.isFeatureSupportedForLocation(
                        SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY, location
                    )
        }
        val compatibleAllergenSources = secondarySources.filter {
            it.id != location.weatherSource &&
                    it.supportedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALLERGEN)
                    && it.isFeatureSupportedForLocation(
                SecondaryWeatherSourceFeature.FEATURE_ALLERGEN, location
            )
        }
        val compatibleMinutelySources = secondarySources.filter {
            it.id != location.weatherSource &&
                    it.supportedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)
                    && it.isFeatureSupportedForLocation(
                SecondaryWeatherSourceFeature.FEATURE_MINUTELY, location
            )
        }
        val compatibleAlertSources = secondarySources.filter {
            it.id != location.weatherSource &&
                    it.supportedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)
                    && it.isFeatureSupportedForLocation(
                SecondaryWeatherSourceFeature.FEATURE_ALERT, location
            )
        }

        AlertDialog(
            onDismissRequest = { dialogOpenState.value = false },
            title = {
                Text(
                    text = stringResource(R.string.settings_weather_sources),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            text = {
                Column {
                    if (location.isCurrentPosition) {
                        SourceView(
                            title = stringResource(R.string.settings_weather_source_main),
                            selectedKey = weatherSource.value,
                            sourceList = weatherSources.associate { it.id to it.name },
                        ) { sourceId ->
                            weatherSource.value = sourceId
                            hasChangedASource.value = true
                        }
                    } else {
                        SourceView(
                            title = stringResource(R.string.settings_weather_source_main),
                            selectedKey = weatherSource.value,
                            enabled = false,
                            sourceList = weatherSources.associate { it.id to it.name },
                        ) { sourceId ->
                            // TODO: To be implemented
                        }
                    }
                    SourceView(
                        title = stringResource(R.string.air_quality),
                        selectedKey = airQualitySource.value,
                        sourceList = mapOf("" to stringResource(R.string.settings_weather_source_main)) +
                                compatibleAirQualitySources.associate { it.id to it.name },
                    ) { sourceId ->
                        airQualitySource.value = sourceId
                        hasChangedASource.value = true
                    }
                    SourceView(
                        title = stringResource(R.string.allergen),
                        selectedKey = allergenSource.value,
                        sourceList = mapOf("" to stringResource(R.string.settings_weather_source_main)) +
                                compatibleAllergenSources.associate { it.id to it.name },
                    ) { sourceId ->
                        allergenSource.value = sourceId
                        hasChangedASource.value = true
                    }
                    SourceView(
                        title = stringResource(R.string.minutely_forecast),
                        selectedKey = minutelySource.value,
                        sourceList = mapOf("" to stringResource(R.string.settings_weather_source_main)) +
                                compatibleMinutelySources.associate { it.id to it.name },
                    ) { sourceId ->
                        minutelySource.value = sourceId
                        hasChangedASource.value = true
                    }
                    SourceView(
                        title = stringResource(R.string.alerts),
                        selectedKey = alertSource.value,
                        sourceList = mapOf("" to stringResource(R.string.settings_weather_source_main)) +
                                compatibleAlertSources.associate { it.id to it.name },
                    ) { sourceId ->
                        alertSource.value = sourceId
                        hasChangedASource.value = true
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        dialogOpenState.value = false
                        if (hasChangedASource.value) {
                            val newLocation = location.copy(
                                weatherSource = weatherSource.value,
                                airQualitySource = airQualitySource.value,
                                allergenSource = allergenSource.value,
                                minutelySource = minutelySource.value,
                                alertSource = alertSource.value
                            )
                            LocationEntityRepository.writeLocation(newLocation)
                            EventBus.instance
                                .with(Location::class.java)
                                .postValue(newLocation)
                            onClose()
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
                    onClick = { dialogOpenState.value = false }
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