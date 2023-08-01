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
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.bus.EventBus
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.common.source.Source
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.common.utils.helpers.SnackbarHelper
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
            val weatherSources = activity.sourceManager.getConfiguredWeatherSources()
            SourceView(
                title = stringResource(R.string.settings_location_service),
                iconId = R.drawable.ic_location,
                selectedKey = SettingsManager.getInstance(activity).locationSource,
                sourceList = locationSources,
                helpMeChoose = null
            ) { sourceId ->
                SettingsManager.getInstance(activity).locationSource = sourceId
                onClose()
            }
            SourceView(
                title = stringResource(R.string.settings_weather_source),
                iconId = R.drawable.ic_factory,
                selectedKey = location.weatherSource,
                sourceList = weatherSources,
            ) { sourceId ->
                val newLocation = location.copy(
                    weatherSource = sourceId
                    // Should we clean old weather data?
                )
                LocationEntityRepository.writeLocation(newLocation)
                // TODO: Leads to "Updated in background" message
                EventBus.instance
                    .with(Location::class.java)
                    .postValue(newLocation)
                onClose()
            }
        } else {
            SwitchPreferenceView(
                titleId = R.string.location_resident_location,
                iconId = R.drawable.ic_tag_plus,
                summaryOnId = R.string.location_resident_location_summaryOn,
                summaryOffId = R.string.location_resident_location_summaryOff,
                checked = location.isResidentPosition,
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
                card = false
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
    val airQualitySource = remember { mutableStateOf(location.airQualitySource ?: location.weatherSource) }
    val allergenSource = remember { mutableStateOf(location.allergenSource ?: location.weatherSource) }
    val minutelySource = remember { mutableStateOf(location.minutelySource ?: location.weatherSource) }
    val alertSource = remember { mutableStateOf(location.alertSource ?: location.weatherSource) }

    PreferenceView(
        titleId = R.string.settings_weather_sources,
        iconId = R.drawable.ic_factory,
        summaryId = R.string.settings_weather_sources_per_location_summary,
        card = false
    ) {
        dialogOpenState.value = true
    }

    if (dialogOpenState.value) {
        val mainWeatherSource = sourceManager.getWeatherSource(location.weatherSource)!!
        val secondarySources = sourceManager.getSecondaryWeatherSources()
        val compatibleAirQualitySources = listOf(mainWeatherSource) +
                secondarySources.filter {
                    it.id != location.weatherSource &&
                            it.supportedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY) &&
                            it.isFeatureSupportedForLocation(
                                SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY, location
                            )
                }
        val compatibleAllergenSources = listOf(mainWeatherSource) +
                secondarySources.filter {
                    it.id != location.weatherSource &&
                            it.supportedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALLERGEN)
                            && it.isFeatureSupportedForLocation(
                        SecondaryWeatherSourceFeature.FEATURE_ALLERGEN, location
                    )
                }
        val compatibleMinutelySources = listOf(mainWeatherSource) +
                secondarySources.filter {
                    it.id != location.weatherSource &&
                            it.supportedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)
                            && it.isFeatureSupportedForLocation(
                        SecondaryWeatherSourceFeature.FEATURE_MINUTELY, location
                    )
                }
        val compatibleAlertSources = listOf(mainWeatherSource) +
                secondarySources.filter {
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
                    if (location.isCurrentPosition) { // TODO: Allow for others as well
                        val weatherSources = sourceManager.getConfiguredWeatherSources()
                        SourceView(
                            title = stringResource(R.string.settings_weather_source_main),
                            selectedKey = weatherSource.value,
                            sourceList = weatherSources,
                        ) { sourceId ->
                            weatherSource.value = sourceId
                            hasChangedASource.value = true
                        }
                    }
                    SourceView(
                        title = stringResource(R.string.air_quality),
                        selectedKey = airQualitySource.value,
                        sourceList = compatibleAirQualitySources,
                    ) { sourceId ->
                        airQualitySource.value = sourceId
                        hasChangedASource.value = true
                    }
                    SourceView(
                        title = stringResource(R.string.allergen),
                        selectedKey = allergenSource.value,
                        sourceList = compatibleAllergenSources,
                    ) { sourceId ->
                        allergenSource.value = sourceId
                        hasChangedASource.value = true
                    }
                    SourceView(
                        title = stringResource(R.string.minutely_forecast),
                        selectedKey = minutelySource.value,
                        sourceList = compatibleMinutelySources,
                    ) { sourceId ->
                        minutelySource.value = sourceId
                        hasChangedASource.value = true
                    }
                    SourceView(
                        title = stringResource(R.string.alerts),
                        selectedKey = alertSource.value,
                        sourceList = compatibleAlertSources,
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
                            // TODO: Leads to "Updated in background" message
                            EventBus.instance
                                .with(Location::class.java)
                                .postValue(newLocation)
                            // TODO: Doesn’t work without a full restart of the app
                            /*SnackbarHelper.showSnackbar(
                                content = context.getString(R.string.settings_changes_apply_after_restart),
                                action = context.getString(R.string.action_restart)
                            ) {
                                BreezyWeather.instance.recreateAllActivities()
                            }*/
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
    sourceList: List<Source>,
    helpMeChoose: String? = "https://github.com/breezy-weather/breezy-weather/blob/main/docs/SOURCES.md",
    onValueChanged: (String) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    ListPreferenceView(
        title = title,
        iconId = iconId,
        selectedKey = selectedKey,
        valueArray = sourceList.map { it.id }.toTypedArray(),
        nameArray = sourceList.map { it.name }.toTypedArray(),
        summary = { _, value ->
            sourceList
                .firstOrNull { it.id == value }
                ?.name
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
        card = false
    )
}