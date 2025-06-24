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

package org.breezyweather.ui.daily

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
import org.breezyweather.common.basic.models.options.appearance.ChartDisplay
import org.breezyweather.common.source.PollenIndexSource
import org.breezyweather.sources.SourceManager
import javax.inject.Inject

@HiltViewModel
class DailyViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
    private val sourceManager: SourceManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val formattedId: String? = savedStateHandle.get<String>(DailyActivity.KEY_FORMATTED_LOCATION_ID)
    private val dailyIndex: Int? = savedStateHandle.get<Int>(DailyActivity.KEY_CURRENT_DAILY_INDEX)
    private val selectedChart: ChartDisplay = ChartDisplay.entries.firstOrNull {
        it.id == savedStateHandle.get<String>(DailyActivity.KEY_CURRENT_PAGE)
    } ?: ChartDisplay.TAG_CONDITIONS

    private val _uiState = MutableStateFlow(DailyUiState())
    val uiState: StateFlow<DailyUiState> = _uiState.asStateFlow()

    init {
        reloadLocation()
    }

    fun getPollenIndexSource(location: Location): PollenIndexSource? {
        return sourceManager.getPollenIndexSource(
            if (!location.pollenSource.isNullOrEmpty()) {
                location.pollenSource!!
            } else {
                location.forecastSource
            }
        )
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
                // The database is empty; we should never have entered daily screen
                return@launch
            }

            val weather = weatherRepository.getWeatherByLocationId(
                locationC.formattedId,
                withDaily = true,
                withHourly = true, // 24-hour charts
                withMinutely = false,
                withAlerts = false
            )
            if (weather?.dailyForecast.isNullOrEmpty()) {
                // There is no weather for this location; we should never have entered daily screen
                return@launch
            }

            _uiState.value = DailyUiState(
                location = locationC.copy(weather = weather),
                selectedChart = selectedChart,
                initialIndex = dailyIndex.let {
                    if (it == null || it == -1 || it >= weather!!.dailyForecast.size) {
                        weather!!.todayIndex ?: 0
                    } else {
                        it
                    }
                }
            )
        }
    }

    fun setSelectedChart(chartDisplay: ChartDisplay) {
        _uiState.value = _uiState.value.copy(
            selectedChart = chartDisplay
        )
    }
}
