package org.breezyweather.common.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.bus.EventBus
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.db.repositories.LocationEntityRepository
import org.breezyweather.main.MainActivity
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.preference.composables.ListPreferenceView
import org.breezyweather.settings.preference.composables.PreferenceView
import org.breezyweather.settings.preference.composables.SwitchPreferenceView

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
            val weatherSources = activity.sourceManager.getWeatherSources()
            val uriHandler = LocalUriHandler.current
            ListPreferenceView(
                title = stringResource(R.string.settings_location_service),
                iconId = R.drawable.ic_location,
                selectedKey = SettingsManager.getInstance(activity).locationSource,
                valueArray = locationSources.map { it.id }.toTypedArray(),
                nameArray = locationSources.map { it.name }.toTypedArray(),
                summary = { _, value -> locationSources.firstOrNull { it.id == value }?.name },
                onValueChanged = { sourceId ->
                    SettingsManager.getInstance(activity).locationSource = sourceId
                    onClose()
                },
                card = false
            )
            ListPreferenceView(
                title = stringResource(R.string.settings_weather_source),
                iconId = R.drawable.ic_factory,
                selectedKey = location.weatherSource,
                valueArray = weatherSources.map { it.id }.toTypedArray(),
                nameArray = weatherSources.map { it.name }.toTypedArray(),
                summary = { _, value -> weatherSources.firstOrNull { it.id == value }?.name },
                onValueChanged = { sourceId ->
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
                },
                dismissButton = {
                    TextButton(
                        onClick = { uriHandler.openUri("https://github.com/breezy-weather/breezy-weather/blob/main/docs/SOURCES.md") }
                    ) {
                        Text(
                            text = stringResource(R.string.action_help_me_choose),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                },
                card = false
            )
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
    }
}