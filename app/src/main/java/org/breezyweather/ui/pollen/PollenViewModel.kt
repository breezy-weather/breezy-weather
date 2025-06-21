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

package org.breezyweather.ui.pollen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import breezyweather.data.location.LocationRepository
import breezyweather.data.weather.WeatherRepository
import breezyweather.domain.location.model.Location
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.breezyweather.sources.SourceManager
import javax.inject.Inject

@HiltViewModel
class PollenViewModel @Inject constructor(
    val locationRepository: LocationRepository,
    val weatherRepository: WeatherRepository,
    val sourceManager: SourceManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val formattedId: String? = savedStateHandle.get<String>(
        PollenActivity.KEY_POLLEN_ACTIVITY_LOCATION_FORMATTED_ID
    )

    private val _uiState = MutableStateFlow(PollenUiState())
    val uiState: StateFlow<PollenUiState> = _uiState.asStateFlow()

    init {
        reloadLocation()
    }

    private fun reloadLocation() {
        viewModelScope.launch {
            var locationC: Location? = null
            if (!formattedId.isNullOrEmpty()) {
                locationC = locationRepository.getLocation(formattedId, withParameters = false)
            }
            if (locationC == null) {
                locationC = locationRepository.getFirstLocation(withParameters = false)
            }
            if (locationC == null) {
                // The database is empty; we should never have entered pollen screen
                return@launch
            }

            // Daily weather data is needed to check if the sun is still up or if it has set when
            // day/night mode per location is enabled.
            val weather = weatherRepository.getWeatherByLocationId(
                locationC.formattedId,
                withDaily = true,
                withHourly = false,
                withMinutely = false,
                withAlerts = false
            )
            if (weather == null) {
                // There is no weather for this location; we should never have entered pollen screen
                return@launch
            }

            _uiState.value = PollenUiState(
                location = locationC.copy(weather = weather),
                pollenIndexSource = locationC.pollenSource?.let {
                    sourceManager.getPollenIndexSource(it)
                }
            )
        }
    }
}
