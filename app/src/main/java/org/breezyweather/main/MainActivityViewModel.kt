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
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import breezyweather.data.location.LocationRepository
import breezyweather.data.weather.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.breezyweather.common.basic.GeoViewModel
import org.breezyweather.common.basic.livedata.BusLiveData
import breezyweather.domain.location.model.Location
import org.breezyweather.common.extensions.hasPermission
import org.breezyweather.common.extensions.launchIO
import org.breezyweather.common.source.RefreshError
import org.breezyweather.common.utils.helpers.AsyncHelper
import org.breezyweather.main.utils.RefreshErrorType
import org.breezyweather.main.utils.StatementManager
import org.breezyweather.remoteviews.Gadgets
import org.breezyweather.remoteviews.Notifications
import org.breezyweather.remoteviews.Widgets
import org.breezyweather.settings.SettingsManager
import org.breezyweather.sources.RefreshHelper
import javax.inject.Inject

interface WeatherRequestCallback {
    fun onCompleted(
        location: Location,
        errors: List<RefreshError> = emptyList()
    )
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
    val statementManager: StatementManager,
    private val refreshHelper: RefreshHelper,
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository
) : GeoViewModel(application), WeatherRequestCallback {

    // flow
    private val _currentLocation: MutableStateFlow<DayNightLocation?> = MutableStateFlow(null)
    val currentLocation = _currentLocation.asStateFlow()
    private val _validLocationList = MutableStateFlow<Pair<List<Location>, String?>>(Pair(emptyList(), null))
    val validLocationList = _validLocationList.asStateFlow()

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

    private val _initCompleted = MutableStateFlow(false)
    val initCompleted = _initCompleted.asStateFlow()
    private var updating = false

    companion object {
        private const val KEY_FORMATTED_ID = "formatted_id"
    }

    // life cycle.
    fun init(formattedId: String? = null) {
        onCleared()

        var id = formattedId ?: savedStateHandle[KEY_FORMATTED_ID]

        // init live data.
        viewModelScope.launch {
            val validList = initLocations(formattedId = id)

            id = formattedId ?: validList.getOrNull(0)?.formattedId
            val current = validList.firstOrNull { item -> item.formattedId == id }

            current?.let {
                _currentLocation.value = DayNightLocation(location = it)
            }
            _validLocationList.value = Pair(validList, id)

            _loading.value = false
            _indicator.value = Indicator(
                total = validList.size,
                index = validList.indexOfFirst { it.formattedId == id }
            )

            locationPermissionsRequest.value = null
            snackbarError.setValue(null)

            // read weather caches.
            val newList = getWeatherCacheForLocations(
                oldList = validList,
                ignoredFormattedId = id,
            )

            _initCompleted.value = true
            if (newList.isNotEmpty()) { updateInnerData(newList) }
        }
    }

    // update inner data.
    private fun updateInnerData(location: Location, oldLocation: Location? = null) {
        val valid = ArrayList(validLocationList.value.first)

        for (i in valid.indices) {
            if (valid[i].formattedId == (oldLocation?.formattedId ?: location.formattedId)) {
                valid[i] = location
                break
            }
        }

        updateInnerData(valid)
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

    private fun updateInnerData(newValid: List<Location>) {
        // get valid locations and current index.
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
            if (initCompleted.value) {
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
                getWeather(
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
            locationPermissionList.addAll(getLocatePermissionList(getApplication())
                .filter { !(getApplication() as Application).hasPermission(it) }
                .toMutableList())
        }
        if (locationPermissionList.isEmpty()) {
            // already got all permissions -> request data directly.
            updating = true
            viewModelScope.launch {
                getWeather(
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
        if (!initCompleted.value) {
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
        if (validLocationList.value.first.firstOrNull {
            it.formattedId == location.formattedId
        } != null) {
            return false
        }

        val valid = ArrayList(validLocationList.value.first)
        valid.add(index ?: valid.size, location)

        updateInnerData(valid)
        writeLocationList(locationList = valid)

        return true
    }

    fun swapLocations(from: Int, to: Int) {
        /*val fromItem = _validLocationList.value.first[from]
        val toItem = _validLocationList.value.first[to]
        val newList = _validLocationList.value.first.toMutableList()
        newList[from] = toItem
        newList[to] = fromItem

        _validLocationList.value = Pair(newList, _validLocationList.value.second)*/
        if (from == to) {
            return
        }

        val valid = ArrayList(validLocationList.value.first)
        valid.add(to, valid.removeAt(from))

        updateInnerData(valid)

        writeLocationList(locationList = validLocationList.value.first)
    }

    fun updateLocation(newLocation: Location, oldLocation: Location?) {
        updateInnerData(newLocation, oldLocation)
        writeLocationList(locationList = validLocationList.value.first)
    }

    fun locationExists(location: Location): Boolean {
        return validLocationList.value.first
            .firstOrNull { item ->
                item.longitude == location.longitude &&
                item.latitude == location.latitude &&
                item.weatherSource == location.weatherSource
            } != null
    }

    fun deleteLocation(position: Int): Location {
        val valid = ArrayList(validLocationList.value.first)
        val location = valid.removeAt(position)

        updateInnerData(valid)
        deleteLocation(location = location)

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

    // Repository
    suspend fun initLocations(formattedId: String?): List<Location> {
        val list = locationRepository.getAllLocations().toMutableList()
        if (list.size == 0) return list

        if (formattedId != null) {
            for (i in list.indices) {
                if (list[i].formattedId == formattedId) {
                    list[i] = list[i].copy(weather = weatherRepository.getWeatherByLocationId(list[i].formattedId))
                    break
                }
            }
        } else {
            list[0] = list[0].copy(weather = weatherRepository.getWeatherByLocationId(list[0].formattedId))
        }

        return list
    }

    suspend fun getWeatherCacheForLocations(
        oldList: List<Location>,
        ignoredFormattedId: String?
    ): List<Location> {
        return oldList.map {
            if (it.formattedId == ignoredFormattedId) {
                it
            } else {
                it.copy(weather = weatherRepository.getWeatherByLocationId(it.formattedId))
            }
        }
    }

    fun writeLocationList(locationList: List<Location>) {
        viewModelScope.launch {
            locationRepository.addAll(locationList)
        }
    }

    fun deleteLocation(location: Location) {
        viewModelScope.launch {
            // Note: we will have a gap in the listOrder, but this doesn't cause any issue, and
            // it will fix by itself on next rewrite of the full location list
            locationRepository.delete(location.formattedId)
            // It cascades delete location parameters, weather and so on
        }
    }

    fun getLocatePermissionList(context: Context) = refreshHelper.getPermissions(context)

    suspend fun getWeather(
        context: Context,
        location: Location,
        callback: WeatherRequestCallback,
    ) {
        try {
            val locationResult = refreshHelper.getLocation(
                context, location, false
            )

            if (locationResult.location.isUsable
                && !locationResult.location.needsGeocodeRefresh) {
                val weatherResult = refreshHelper.getWeather(
                    context,
                    locationResult.location,
                    location.longitude != locationResult.location.longitude
                            || location.latitude != locationResult.location.latitude
                )
                callback.onCompleted(
                    locationResult.location.copy(weather = weatherResult.weather),
                    locationResult.errors + weatherResult.errors
                )
            } else {
                callback.onCompleted(
                    locationResult.location,
                    locationResult.errors
                )
            }
        } catch (e: Throwable) {
            // Should never happen
            e.printStackTrace()
            callback.onCompleted(
                location,
                listOf(RefreshError(RefreshErrorType.WEATHER_REQ_FAILED))
            )
        }
    }

    fun refreshBackgroundViews(context: Context, locationList: List<Location>?) {
        locationList?.let {
            if (it.isNotEmpty()) {
                viewModelScope.launchIO {
                    AsyncHelper.delayRunOnIO({
                        Widgets.updateWidgetIfNecessary(context, it[0])
                        Notifications.updateNotificationIfNecessary(context, it)
                        Widgets.updateWidgetIfNecessary(context, it)
                        Gadgets.updateGadgetIfNecessary(context, it[0])
                    }, 1000)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                        refreshHelper.refreshShortcuts(context, it)
                    }
                }
            }
        }
    }
}