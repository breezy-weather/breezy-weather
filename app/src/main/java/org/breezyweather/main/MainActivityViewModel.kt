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

package org.breezyweather.main

import android.app.Application
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.breezyweather.common.basic.GeoViewModel
import org.breezyweather.common.basic.livedata.BusLiveData
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.extensions.hasPermission
import org.breezyweather.common.source.RefreshError
import org.breezyweather.main.utils.StatementManager
import org.breezyweather.settings.SettingsManager
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val repository: MainActivityRepository,
    val statementManager: StatementManager,
) : GeoViewModel(application),
    MainActivityRepository.WeatherRequestCallback {

    // flow
    private val _currentLocation: MutableStateFlow<DayNightLocation?> = MutableStateFlow(null)
    val currentLocation = _currentLocation.asStateFlow()
    private val _validLocationList = MutableStateFlow<Pair<List<Location>, String?>>(Pair(emptyList(), null))
    val validLocationList = _validLocationList.asStateFlow()
    private val _totalLocationList = MutableStateFlow<Pair<List<Location>, String?>>(Pair(emptyList(), null))
    val totalLocationList = _totalLocationList.asStateFlow()

    private val _dialogChooseWeatherSourcesOpen = MutableStateFlow(false)
    val dialogChooseWeatherSourcesOpen = _dialogChooseWeatherSourcesOpen.asStateFlow()

    private val _selectedLocation: MutableStateFlow<Location?> = MutableStateFlow(null)
    val selectedLocation = _selectedLocation.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()
    private val _indicator = MutableStateFlow(Indicator(total = 0, index = 0)) // Is overwritten on init
    val indicator = _indicator.asStateFlow()

    val locationPermissionsRequest: MutableStateFlow<PermissionsRequest?> = MutableStateFlow(null)
    val snackbarError = BusLiveData<RefreshError?>(Handler(Looper.getMainLooper()))

    // inner data.

    private var initCompleted = false
    private var updating = false

    companion object {
        private const val KEY_FORMATTED_ID = "formatted_id"
    }

    // life cycle.

    override fun onCleared() {
        super.onCleared()
    }

    fun init(formattedId: String? = null) {
        onCleared()

        var id = formattedId ?: savedStateHandle[KEY_FORMATTED_ID]

        // init live data.
        val totalList = repository.initLocations(formattedId = id)
        val validList = Location.excludeInvalidResidentLocation(getApplication(), totalList)

        id = formattedId ?: validList.getOrNull(0)?.formattedId
        val current = validList.firstOrNull { item -> item.formattedId == id }

        initCompleted = false

        current?.let {
            _currentLocation.value = DayNightLocation(location = it)
        }
        _validLocationList.value = Pair(validList, id)
        _totalLocationList.value = Pair(totalList, id)

        _loading.value = false
        _indicator.value = Indicator(
            total = validList.size,
            index = validList.indexOfFirst { it.formattedId == id }
        )

        locationPermissionsRequest.value = null
        snackbarError.setValue(null)

        // read weather caches.
        repository.getWeatherCacheForLocations(
            oldList = totalList,
            ignoredFormattedId = id,
        ) { newList, _ ->
            initCompleted = true
            if (newList.isNotEmpty()) { updateInnerData(newList) }
        }
    }

    // update inner data.
    private fun updateInnerData(location: Location, oldLocation: Location? = null) {
        val total = ArrayList(totalLocationList.value.first)

        for (i in total.indices) {
            if (total[i].formattedId == (oldLocation?.formattedId ?: location.formattedId)) {
                total[i] = location
                break
            }
        }

        updateInnerData(total)
    }

    /**
     * @param location: If null, will create a location of type currentPosition
     */
    fun openChooseWeatherSourcesDialog(location: Location?) {
        _selectedLocation.value = location
        _dialogChooseWeatherSourcesOpen.value = true
    }

    fun closeChooseWeatherSourcesDialog() {
        _dialogChooseWeatherSourcesOpen.value = false
        _selectedLocation.value = null
    }

    private fun updateInnerData(newTotal: List<Location>) {
        // get valid locations and current index.
        val newValid = Location.excludeInvalidResidentLocation(
            getApplication(),
            newTotal,
        )

        var index: Int? = null
        if (newValid.size != validLocationList.value.first.size) {
            if (newValid.size > validLocationList.value.first.size) {
                // New location added case
                index = newValid.size - 1
            } else {
                // Look for our location by formattedId in the new list (maybe the location deleted
                // is not the currently focused location!)
                for (i in newValid.indices) {
                    if (newValid[i].formattedId == currentLocation.value?.location?.formattedId) {
                        index = i
                        break
                    }
                }
            } // Deleted location case
        } else {
            // If the size didn't change, look for our location by formattedId in the new list
            for (i in newValid.indices) {
                if (newValid[i].formattedId == currentLocation.value?.location?.formattedId) {
                    index = i
                    break
                }
            }

            // If we didn't find it, it means formattedId changed (main weather source was changed)!
            // In that case, pick up latest known index for that location
            if (index == null) {
                for (i in validLocationList.value.first.indices) {
                    if (validLocationList.value.first[i].formattedId == currentLocation.value?.location?.formattedId) {
                        index = i
                        break
                    }
                }
            }
        }

        index = index ?: 0

        _indicator.value = Indicator(total = newValid.size, index = index)

        // update current location.
        setCurrentLocation(newValid[index])

        // check difference in valid locations.
        val diffInValidLocations = validLocationList.value.first != newValid
        if (diffInValidLocations || validLocationList.value.second != newValid[index].formattedId) {
            _validLocationList.value = Pair(newValid, newValid[index].formattedId)
        }

        // update total locations.
        _totalLocationList.value = Pair(newTotal, newValid[index].formattedId)
    }

    private fun setCurrentLocation(location: Location) {
        _currentLocation.value = DayNightLocation(location = location)
        savedStateHandle[KEY_FORMATTED_ID] = location.formattedId

        checkToUpdateCurrentLocation()
    }

    /**
     * Called on background thread
     */
    private fun onUpdateResult(
        location: Location,
        errors: List<RefreshError> = emptyList()
    ) {
        // Arbitrarily post only the first error, as we can only show one snackbar at a time
        snackbarError.postValue(errors.getOrNull(0))

        updateInnerData(location)

        _loading.value = false
        updating = false
    }

    private fun checkToUpdateCurrentLocation() {
        // is not loading
        if (!updating) {
            // if already valid, just return.
            if (currentLocationIsValid()) return

            // If we don't have any location yet, just return
            if (currentLocation.value?.location == null) {
                updating = false
                return
            }

            // if is not valid, we need:
            // update if init completed.
            // otherwise, mark a loading state and wait the init progress complete.
            if (initCompleted) {
                updateWithUpdatingChecking(
                    triggeredByUser = false,
                    checkPermissions = true,
                )
            } else {
                _loading.value = true
                updating = false
            }
            return
        }

        // is loading, do nothing.
    }

    private fun currentLocationIsValid() =
        currentLocation.value?.location?.weather?.isValid(
            SettingsManager
                .getInstance(getApplication())
                .updateInterval
                .validityInHour
        ) ?: false

    // update.
    fun updateWithUpdatingChecking(
        triggeredByUser: Boolean,
        checkPermissions: Boolean,
    ) {
        if (updating) return
        val locationToCheck = currentLocation.value?.location
        if (locationToCheck == null) {
            _loading.value = true
            _loading.value = false
            return
        }

        _loading.value = true

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || !checkPermissions) {
            updating = true
            viewModelScope.launch {
                repository.getWeather(
                    getApplication(),
                    locationToCheck,
                    this@MainActivityViewModel
                )
            }
            return
        }

        // check permissions.
        val locationPermissionList: MutableList<String> = mutableListOf()
        if (locationToCheck.isCurrentPosition) {
            locationPermissionList.addAll(repository
                .getLocatePermissionList(getApplication())
                .filter { !(getApplication() as Application).hasPermission(it) }
                .toMutableList())
        }
        if (locationPermissionList.isEmpty()) {
            // already got all permissions -> request data directly.
            updating = true
            viewModelScope.launch {
                repository.getWeather(
                    getApplication(),
                    locationToCheck,
                    this@MainActivityViewModel
                )
            }
        } else {
            updating = false
            locationPermissionsRequest.value = PermissionsRequest(
                locationPermissionList,
                locationToCheck,
                triggeredByUser
            )
        }
    }

    fun cancelRequest() {
        updating = false
        _loading.value = false
    }

    fun checkToUpdate() {
        checkToUpdateCurrentLocation()
    }

    fun updateLocationFromBackground(location: Location) {
        if (!initCompleted) {
            return
        }

        // Only updates are coming here (no location added or deleted)
        // If we don't find the formattedId in the current list, it means main source was changed
        // for currently focused location
        val oldLocation = validLocationList.value.first.firstOrNull {
            it.formattedId == location.formattedId
        } ?: currentLocation.value?.location

        if (currentLocation.value?.location?.formattedId == (oldLocation?.formattedId ?: location.formattedId)) {
            cancelRequest()
        }
        updateInnerData(location, oldLocation)
    }

    // set location.

    fun setLocation(index: Int) {
        validLocationList.value.first.let {
            setLocation(it[index].formattedId)
        }
    }

    fun setLocation(formattedId: String) {
        cancelRequest()

        validLocationList.value.first.let { locationList ->
            val index = locationList.indexOfFirst { it.formattedId == formattedId }

            if (index >= 0) {
                setCurrentLocation(locationList[index])

                _indicator.value = Indicator(total = locationList.size, index = index)

                _totalLocationList.value = Pair(totalLocationList.value.first, formattedId)
                _validLocationList.value = Pair(validLocationList.value.first, formattedId)
            }
        }
    }

    // return true if current location changed.
    fun offsetLocation(offset: Int): Boolean {
        cancelRequest()

        val oldFormattedId = currentLocation.value?.location?.formattedId ?: ""

        // ensure current index.
        var index = 0
        validLocationList.value.first.let {
            for (i in it.indices) {
                if (it[i].formattedId == currentLocation.value?.location?.formattedId) {
                    index = i
                    break
                }
            }
        }

        // update index.
        index = (index + offset + (validLocationList.value.first.size)) % (validLocationList.value.first.size)

        // update location.
        setCurrentLocation(validLocationList.value.first[index])

        _indicator.value = Indicator(total = validLocationList.value.first.size, index = index)

        _totalLocationList.value = Pair(totalLocationList.value.first, currentLocation.value?.location?.formattedId ?: "")
        _validLocationList.value = Pair(validLocationList.value.first, currentLocation.value?.location?.formattedId ?: "")

        return currentLocation.value?.location?.formattedId != oldFormattedId
    }

    // list.

    // return false if failed.
    fun addLocation(
        location: Location,
        index: Int? = null,
    ): Boolean {
        // do not add an existing location.
        if (totalLocationList.value.first.firstOrNull {
            it.formattedId == location.formattedId
        } != null) {
            return false
        }

        val total = ArrayList(totalLocationList.value.first)
        total.add(index ?: total.size, location)

        updateInnerData(total)
        repository.writeLocationList(locationList = total)

        return true
    }

    fun swapLocations(from: Int, to: Int) {
        /*val fromItem = _totalLocationList.value.first[from]
        val toItem = _totalLocationList.value.first[to]
        val newList = _totalLocationList.value.first.toMutableList()
        newList[from] = toItem
        newList[to] = fromItem

        _totalLocationList.value = Pair(newList, _totalLocationList.value.second)*/
        if (from == to) {
            return
        }

        val total = ArrayList(totalLocationList.value.first)
        total.add(to, total.removeAt(from))

        updateInnerData(total)

        repository.writeLocationList(
            locationList = totalLocationList.value.first
        )
    }

    fun updateLocation(newLocation: Location, oldLocation: Location) {
        updateInnerData(newLocation, oldLocation)
        repository.writeLocationList(
            locationList = totalLocationList.value.first,
        )
    }

    fun locationExists(location: Location): Boolean {
        return totalLocationList.value.first
            .firstOrNull { item ->
                item.longitude == location.longitude &&
                item.latitude == location.latitude &&
                item.weatherSource == location.weatherSource
            } != null
    }

    fun deleteLocation(position: Int): Location {
        val total = ArrayList(totalLocationList.value.first)
        val location = total.removeAt(position)

        updateInnerData(total)
        repository.deleteLocation(location = location)

        return location
    }

    // MARK: - getter.
    fun getValidLocation(offset: Int?): Location? {
        if (offset == null) return null
        // ensure current index.
        var index: Int? = null
        validLocationList.value.first.let {
            for (i in it.indices) {
                if (it[i].formattedId == currentLocation.value?.location?.formattedId) {
                    index = i
                    break
                }
            }
        }

        return index?.let {
            return validLocationList.value.first.getOrNull((it + offset + validLocationList.value.first.size) % validLocationList.value.first.size)
        }
    }

    // impl.

    override fun onCompleted(location: Location, errors: List<RefreshError>) {
        onUpdateResult(location, errors)
    }
}