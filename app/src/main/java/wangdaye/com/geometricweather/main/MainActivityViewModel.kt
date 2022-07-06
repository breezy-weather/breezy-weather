package wangdaye.com.geometricweather.main

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import wangdaye.com.geometricweather.common.basic.GeoViewModel
import wangdaye.com.geometricweather.common.basic.livedata.BusLiveData
import wangdaye.com.geometricweather.common.basic.livedata.EqualtableLiveData
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.main.utils.StatementManager
import wangdaye.com.geometricweather.settings.SettingsManager
import javax.inject.Inject


@HiltViewModel
class MainActivityViewModel @Inject constructor(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val repository: MainActivityRepository,
    val statementManager: StatementManager,
) : GeoViewModel(application),
    MainActivityRepository.WeatherRequestCallback {

    // live data.

    val currentLocation = EqualtableLiveData<DayNightLocation>()
    val validLocationList = MutableLiveData<SelectableLocationList>()
    val totalLocationList = MutableLiveData<SelectableLocationList>()

    val loading = EqualtableLiveData<Boolean>()
    val indicator = EqualtableLiveData<Indicator>()

    val permissionsRequest = MutableLiveData<PermissionsRequest?>()
    val mainMessage = BusLiveData<MainMessage?>(Handler(Looper.getMainLooper()))

    // inner data.

    private var initCompleted = false
    private var updating = false

    companion object {
        private const val KEY_FORMATTED_ID = "formatted_id"
    }

    // life cycle.

    override fun onCleared() {
        super.onCleared()
        repository.destroy()
    }

    @JvmOverloads
    fun init(formattedId: String? = null) {
        onCleared()

        var id = formattedId ?: savedStateHandle[KEY_FORMATTED_ID]

        // init live data.
        val totalList = repository.initLocations(
            context = getApplication(),
            formattedId = id ?: ""
        )
        val validList = Location.excludeInvalidResidentLocation(getApplication(), totalList)

        id = formattedId ?: validList[0].formattedId
        val current = validList.first { item -> item.formattedId == id }

        initCompleted = false

        currentLocation.setValue(DayNightLocation(location = current))
        validLocationList.value = SelectableLocationList(locationList = validList, selectedId = id)
        totalLocationList.value = SelectableLocationList(locationList = totalList, selectedId = id)

        loading.setValue(false)
        indicator.setValue(
            Indicator(
                total = validList.size,
                index = validList.indexOfFirst { it.formattedId == id }
            )
        )

        permissionsRequest.value = null
        mainMessage.setValue(null)

        // read weather caches.
        repository.getWeatherCacheForLocations(
            context = getApplication(),
            oldList = totalList,
            ignoredFormattedId = id,
        ) { newList, _ ->
            initCompleted = true
            newList?.let { updateInnerData(it) }
        }
    }

    // update inner data.

    private fun updateInnerData(location: Location) {
        val total = ArrayList(
            totalLocationList.value?.locationList ?: emptyList()
        )
        for (i in total.indices) {
            if (total[i].formattedId == location.formattedId) {
                total[i] = location
                break
            }
        }

        updateInnerData(total)
    }

    private fun updateInnerData(total: List<Location>) {
        // get valid locations and current index.
        val valid = Location.excludeInvalidResidentLocation(
            getApplication(),
            total,
        )

        var index = 0
        for (i in valid.indices) {
            if (valid[i].formattedId == currentLocation.value?.location?.formattedId) {
                index = i
                break
            }
        }

        indicator.setValue(Indicator(total = valid.size, index = index))

        // update current location.
        setCurrentLocation(valid[index])

        // check difference in valid locations.
        val diffInValidLocations = validLocationList.value?.locationList != valid
        if (
            diffInValidLocations
            || validLocationList.value?.selectedId != valid[index].formattedId
        ) {
            validLocationList.value = SelectableLocationList(
                locationList = valid,
                selectedId = valid[index].formattedId,
            )
        }

        // update total locations.
        totalLocationList.value = SelectableLocationList(
            locationList = total,
            selectedId = valid[index].formattedId,
        )
    }

    private fun setCurrentLocation(location: Location) {
        currentLocation.setValue(DayNightLocation(location = location))
        savedStateHandle[KEY_FORMATTED_ID] = location.formattedId

        checkToUpdateCurrentLocation()
    }

    private fun onUpdateResult(
        location: Location,
        locationResult: Boolean,
        weatherUpdateResult: Boolean,
        apiLimitReached: Boolean,
    ) {
        if (apiLimitReached) {
            mainMessage.setValue(MainMessage.API_LIMIT_REACHED)
        } else if (!weatherUpdateResult) {
            mainMessage.setValue(MainMessage.WEATHER_REQ_FAILED)
        } else if (!locationResult) {
            mainMessage.setValue(MainMessage.LOCATION_FAILED)
        }

        updateInnerData(location)

        loading.setValue(false)
        updating = false
    }

    private fun checkToUpdateCurrentLocation() {
        // is not loading
        if (!updating) {
            // if already valid, just return.
            if (currentLocationIsValid()) {
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
                loading.setValue(true)
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
                .intervalInHour
        ) ?: false

    // update.

    fun updateWithUpdatingChecking(
        triggeredByUser: Boolean,
        checkPermissions: Boolean,
    ) {
        if (updating) {
            return
        }

        loading.setValue(true)

        // don't need to request any permission -> request data directly.
        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M
            || currentLocation.value?.location?.isCurrentPosition == false
            || !checkPermissions
        ) {
            updating = true
            repository.getWeather(
                getApplication(),
                currentLocation.value!!.location,
                currentLocation.value!!.location.isCurrentPosition,
                this
            )
            return
        }

        // check permissions.
        val permissionList = getDeniedPermissionList()
        if (permissionList.isEmpty()) {
            // already got all permissions -> request data directly.
            updating = true
            repository.getWeather(
                getApplication(),
                currentLocation.value!!.location,
                true,
                this
            )
            return
        }

        // request permissions.
        updating = false
        permissionsRequest.value = PermissionsRequest(
            permissionList,
            currentLocation.value!!.location,
            triggeredByUser
        )
    }

    private fun getDeniedPermissionList(): List<String> {
        val permissionList = repository
            .getLocatePermissionList(getApplication())
            .toMutableList()

        for (i in permissionList.indices.reversed()) {
            if (
                ActivityCompat.checkSelfPermission(
                    getApplication(),
                    permissionList[i]
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                permissionList.removeAt(i)
            }
        }

        return permissionList
    }

    fun cancelRequest() {
        updating = false
        loading.setValue(false)
        repository.cancelWeatherRequest()
    }

    fun checkToUpdate() {
        checkToUpdateCurrentLocation()
    }

    fun updateLocationFromBackground(location: Location) {
        if (!initCompleted) {
            return
        }

        if (currentLocation.value?.location?.formattedId == location.formattedId) {
            cancelRequest()
        }
        updateInnerData(location)
    }

    // set location.

    fun setLocation(index: Int) {
        validLocationList.value?.locationList?.let {
            setLocation(it[index].formattedId)
        }
    }

    fun setLocation(formattedId: String) {
        cancelRequest()

        validLocationList.value?.locationList?.let {
            for (i in it.indices) {
                if (it[i].formattedId != formattedId) {
                    continue
                }

                setCurrentLocation(it[i])

                indicator.setValue(Indicator(total = it.size, index = i))

                totalLocationList.value = SelectableLocationList(
                    locationList = totalLocationList.value?.locationList ?: emptyList(),
                    selectedId = formattedId,
                )
                validLocationList.value = SelectableLocationList(
                    locationList = validLocationList.value?.locationList ?: emptyList(),
                    selectedId = formattedId,
                )
                break
            }
        }
    }

    // return true if current location changed.
    fun offsetLocation(offset: Int): Boolean {
        cancelRequest()

        val oldFormattedId = currentLocation.value?.location?.formattedId ?: ""

        // ensure current index.
        var index = 0
        validLocationList.value?.locationList?.let {
            for (i in it.indices) {
                if (it[i].formattedId == currentLocation.value?.location?.formattedId) {
                    index = i
                    break
                }
            }
        }

        // update index.
        index = (
                index + offset + (validLocationList.value?.locationList?.size ?: 0)
        ) % (
                validLocationList.value?.locationList?.size ?: 1
        )

        // update location.
        setCurrentLocation(validLocationList.value!!.locationList[index])

        indicator.setValue(
            Indicator(total = validLocationList.value!!.locationList.size, index = index)
        )

        totalLocationList.value = SelectableLocationList(
            locationList = totalLocationList.value?.locationList ?: emptyList(),
            selectedId = currentLocation.value?.location?.formattedId ?: "",
        )
        validLocationList.value = SelectableLocationList(
            locationList = validLocationList.value?.locationList ?: emptyList(),
            selectedId = currentLocation.value?.location?.formattedId ?: "",
        )

        return currentLocation.value?.location?.formattedId != oldFormattedId
    }

    // list.

    // return false if failed.
    fun addLocation(
        location: Location,
        index: Int? = null,
    ): Boolean {
        // do not add an existed location.
        if (totalLocationList.value!!.locationList.firstOrNull {
                it.formattedId == location.formattedId
        } != null) {
            return false
        }

        val total = ArrayList(totalLocationList.value?.locationList ?: emptyList())
        total.add(index ?: total.size, location)

        updateInnerData(total)
        repository.writeLocationList(context = getApplication(), locationList = total)

        return true
    }

    fun moveLocation(from: Int, to: Int) {
        if (from == to) {
            return
        }

        val total = ArrayList(totalLocationList.value?.locationList ?: emptyList())
        total.add(to, total.removeAt(from))

        updateInnerData(total)

        repository.writeLocationList(
            context = getApplication(),
            locationList = totalLocationList.value?.locationList ?: emptyList()
        )
    }

    fun updateLocation(location: Location) {
        updateInnerData(location)
        repository.writeLocationList(
            context = getApplication(),
            locationList = totalLocationList.value?.locationList ?: emptyList(),
        )
    }

    fun deleteLocation(position: Int): Location {
        val total = ArrayList(totalLocationList.value?.locationList ?: emptyList())
        val location = total.removeAt(position)

        updateInnerData(total)
        repository.deleteLocation(context = getApplication(), location = location)

        return location
    }

    // MARK: - getter.

    fun getValidLocation(offset: Int): Location {
        // ensure current index.
        var index = 0
        validLocationList.value?.locationList?.let {
            for (i in it.indices) {
                if (it[i].formattedId == currentLocation.value?.location?.formattedId) {
                    index = i
                    break
                }
            }
        }

        // update index.
        index = (
                index + offset + (validLocationList.value?.locationList?.size ?: 0)
        ) % (
                validLocationList.value?.locationList?.size ?: 1
        )

        return validLocationList.value!!.locationList[index]
    }

    // impl.

    override fun onCompleted(
        location: Location,
        locationFailed: Boolean?,
        weatherRequestFailed: Boolean,
        apiLimitReached: Boolean,
    ) {
        onUpdateResult(
            location = location,
            locationResult = locationFailed != true,
            weatherUpdateResult = !weatherRequestFailed,
            apiLimitReached = apiLimitReached
        )
    }
}