package wangdaye.com.geometricweather.main

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import wangdaye.com.geometricweather.GeometricWeather
import wangdaye.com.geometricweather.common.basic.GeoViewModel
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.main.models.Indicator
import wangdaye.com.geometricweather.main.models.LocationResource
import wangdaye.com.geometricweather.main.models.PermissionsRequest
import wangdaye.com.geometricweather.main.models.SelectableLocationListResource
import wangdaye.com.geometricweather.main.models.SelectableLocationListResource.ItemMoved
import wangdaye.com.geometricweather.main.utils.MainModuleUtils
import wangdaye.com.geometricweather.main.utils.MainThemeManager
import wangdaye.com.geometricweather.main.utils.StatementManager
import java.util.*
import kotlin.collections.ArrayList

class MainActivityViewModel constructor(
        application: Application,
        private val savedStateHandle: SavedStateHandle,
        private val repository: MainActivityRepository,
        val statementManager: StatementManager,
        val themeManager: MainThemeManager,
        private val applicationScope: CoroutineScope
) : GeoViewModel(application) {

    companion object {
        private const val KEY_FORMATTED_ID = "formatted_id"

        private fun indexLocation(locationList: List<Location>, formattedId: String?): Int {
            if (TextUtils.isEmpty(formattedId)) {
                return 0
            }
            for (i in locationList.indices) {
                if (locationList[i].equals(formattedId)) {
                    return i
                }
            }
            return 0
        }
    }

    val currentLocation = MutableLiveData<LocationResource?>()
    val indicator = MutableLiveData<Indicator>()
    val permissionsRequest = MutableLiveData<PermissionsRequest>()
    val listResource = MutableLiveData<SelectableLocationListResource>()

    // inner data.
    private var formattedId: String? = savedStateHandle.get(KEY_FORMATTED_ID) // current formatted id.
        set(value) {
            field = value
            savedStateHandle.set(KEY_FORMATTED_ID, formattedId)
        }
    private var totalList: List<Location>? = null // all locations.
    private var validList: List<Location>? = null // location list optimized for resident city.

    // coroutines.
    private var weatherRequest: Job? = null

    init {
        currentLocation.value = null
        indicator.value = Indicator(1, 0)
        permissionsRequest.value = PermissionsRequest(
                ArrayList(), null, false)
        listResource.value = SelectableLocationListResource(
                ArrayList(), null, null)
    }

    @ViewModelInject
    constructor(
            application: Application,
            @Assisted savedStateHandle: SavedStateHandle,
            repository: MainActivityRepository,
            statementManager: StatementManager,
            themeManager: MainThemeManager
    ): this(
            application,
            savedStateHandle,
            repository,
            statementManager,
            themeManager,
            GeometricWeather.instance.applicationScope
    )

    override fun onCleared() {
        super.onCleared()
        repository.destroy()
    }

    @JvmOverloads
    fun init(newFormattedId: String? = formattedId) {
        formattedId = newFormattedId

        val oldList: List<Location> = if (totalList == null) {
            ArrayList()
        } else {
            Collections.unmodifiableList(totalList)
        }

        viewModelScope.launch {

            fun response(locationList: List<Location>, done: Boolean) {
                val totalList = ArrayList(locationList)
                val validList = Location.excludeInvalidResidentLocation(getApplication(), totalList)
                val validIndex = indexLocation(validList, formattedId)
                setInnerData(totalList, validList, validIndex)

                val current = validList[validIndex]
                val indicator = Indicator(validList.size, validIndex)
                val defaultLocation = validIndex == 0
                val event = if (done) {
                    LocationResource.Event.UPDATE
                } else {
                    LocationResource.Event.INITIALIZE
                }
                setLocationResourceWithVerification(current, defaultLocation, event,
                        indicator, null, SelectableLocationListResource.DataSetChanged())
            }

            response(repository.getLocationList(getApplication(), oldList), false)
            response(repository.getWeatherCaches(getApplication(), getTotalLocationList()!!), true)
        }
    }

    fun checkWhetherToChangeTheme() {
        val location = getCurrentLocationValue() ?: return
        val lightTheme = themeManager.isLightTheme

        themeManager.update(getApplication(), location)
        if (themeManager.isLightTheme == lightTheme) {
            return
        }

        currentLocation.value?.let {
            currentLocation.value = LocationResource(
                    location, it.status, it.defaultLocation, it.locateFailed, it.event)
        }
    }

    fun updateLocationFromBackground(location: Location) {
        if (totalList == null || validList == null) {
            return
        }

        val totalList  = ArrayList(totalList)
        for (i in totalList.indices) {
            if (totalList[i].equals(location)) {
                totalList[i] = location
                break
            }
        }
        val validList = Location.excludeInvalidResidentLocation(getApplication(), totalList)
        val validIndex = indexLocation(validList, formattedId)
        setInnerData(totalList, validList, validIndex)

        val current = validList[validIndex]
        val indicator = Indicator(validList.size, validIndex)
        val defaultLocation = validIndex == 0
        val event = if (location.equals(current)) {
            LocationResource.Event.BACKGROUND_UPDATE_CURRENT
        } else {
            LocationResource.Event.BACKGROUND_UPDATE_OTHERS
        }
        setLocationResourceWithVerification(current, defaultLocation, event,
                indicator, null, SelectableLocationListResource.DataSetChanged())
    }

    fun setLocation(formattedId: String) {
        if (totalList == null || validList == null) {
            return
        }

        val totalList = ArrayList(totalList)
        val validList = ArrayList(validList)
        val validIndex = indexLocation(validList, formattedId)
        setInnerData(totalList, validList, validIndex)

        val current = validList[validIndex]
        val indicator = Indicator(validList.size, validIndex)
        val defaultLocation = validIndex == 0
        setLocationResourceWithVerification(current, defaultLocation, LocationResource.Event.UPDATE,
                indicator, null, SelectableLocationListResource.DataSetChanged())
    }

    fun setLocation(offset: Int) {
        if (totalList == null || validList == null) {
            return
        }

        var validIndex = indexLocation(validList!!, formattedId)
        validIndex += offset + validList!!.size
        validIndex %= validList!!.size

        val totalList: List<Location> = ArrayList(totalList)
        val validList: List<Location> = ArrayList(validList)
        setInnerData(totalList, validList, validIndex)

        val current = validList[validIndex]
        val indicator = Indicator(validList.size, validIndex)
        val defaultLocation = validIndex == 0
        setLocationResourceWithVerification(current, defaultLocation, LocationResource.Event.UPDATE,
                indicator, null, SelectableLocationListResource.DataSetChanged())
    }

    fun updateWeather(triggeredByUser: Boolean, checkPermissions: Boolean) {

        fun callback(location: Location,
                     locateFailed: Boolean,
                     succeed: Boolean,
                     done: Boolean) {
            if (totalList == null || validList == null) {
                return
            }

            val totalList = ArrayList(totalList)
            for (i in totalList.indices) {
                if (totalList[i].equals(location)) {
                    totalList[i] = location
                    break
                }
            }
            val validList = Location.excludeInvalidResidentLocation(getApplication(), totalList)
            val validIndex = indexLocation(validList, getCurrentFormattedId())
            val defaultLocation = location.equals(validList[0])
            setInnerData(totalList, validList, validIndex)

            val resource = if (!done) {
                LocationResource.loading(
                        location, defaultLocation, locateFailed, LocationResource.Event.UPDATE)
            } else if (succeed) {
                LocationResource.success(
                        location, defaultLocation, LocationResource.Event.UPDATE)
            } else {
                LocationResource.error(
                        location, defaultLocation, locateFailed, LocationResource.Event.UPDATE)
            }
            val indicatorValue = Indicator(validList.size, validIndex)

            themeManager.update(getApplication(), location)

            currentLocation.value = resource
            indicator.value = indicatorValue
            listResource.value = SelectableLocationListResource(
                    totalList,
                    formattedId,
                    null,
                    SelectableLocationListResource.DataSetChanged()
            )
        }

        currentLocation.value?.let {
            weatherRequest?.cancel()

            weatherRequest = viewModelScope.launch {
                themeManager.update(getApplication(), it.data)
                currentLocation.value = LocationResource.loading(
                        it.data,
                        it.defaultLocation,
                        LocationResource.Event.UPDATE
                )

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                        || !it.data.isCurrentPosition
                        || !checkPermissions
                        || getDeniedPermissionList().isEmpty()) {
                    // don't need to request any permission ,or already got all permissions.
                    // request data directly.
                    if (it.data.isCurrentPosition) {
                        val response = repository.getLocation(getApplication(), it.data)
                        callback(
                                response.result ?: it.data,
                                locateFailed = response.isFailed(),
                                succeed = response.isSucceed(),
                                done = false
                        )
                    }

                    val response = repository.getWeather(getApplication(), it.data)
                    it.data.weather = response.result
                    callback(it.data, locateFailed = false, succeed = response.isSucceed(), done = true)
                    return@launch
                }

                // request permissions.
                permissionsRequest.value = PermissionsRequest(
                        getDeniedPermissionList(), it.data, triggeredByUser)
            }
        }
    }

    fun requestPermissionsFailed(location: Location) {
        if (totalList == null || validList == null) {
            return
        }

        themeManager.update(getApplication(), location)
        currentLocation.value = LocationResource.error(
                location,
                location.equals(validList!![0]),
                LocationResource.Event.UPDATE
        )
    }

    fun addLocation(location: Location) {
        totalList?.let {
            addLocation(location, it.size)
        }
    }

    fun addLocation(location: Location, position: Int) {
        totalList?.let {
            val totalList = ArrayList(it)
            totalList.add(position, location)

            val validList = Location.excludeInvalidResidentLocation(getApplication(), totalList)
            val validIndex = indexLocation(validList, formattedId)
            setInnerData(totalList, validList, validIndex)

            val current = validList[validIndex]
            val indicator = Indicator(validList.size, validIndex)
            val defaultLocation = validIndex == 0
            setLocationResourceWithVerification(current, defaultLocation, LocationResource.Event.UPDATE,
                    indicator, null, SelectableLocationListResource.DataSetChanged())

            applicationScope.launch {
                if (position == totalList.size - 1) {
                    repository.writeLocation(getApplication(), location)
                } else {
                    repository.writeLocationList(getApplication(),
                            Collections.unmodifiableList(totalList), position)
                }
            }
        }
    }

    fun moveLocation(from: Int, to: Int) {
        totalList?.let {
            val totalList = ArrayList(it)
            Collections.swap(totalList, from, to)

            val validList = Location.excludeInvalidResidentLocation(getApplication(), totalList)
            val validIndex = indexLocation(validList, formattedId)
            setInnerData(totalList, validList, validIndex)

            listResource.value = SelectableLocationListResource(
                    totalList, formattedId, null, ItemMoved(from, to))
        }
    }

    fun moveLocationFinish() {
        totalList?.let {
            val totalList = ArrayList(it)
            val validList = Location.excludeInvalidResidentLocation(getApplication(), totalList)
            val validIndex = indexLocation(validList, formattedId)
            setInnerData(totalList, validList, validIndex)

            val current = validList[validIndex]
            val indicator = Indicator(validList.size, validIndex)
            val defaultLocation = validIndex == 0
            setLocationResourceWithVerification(current, defaultLocation, LocationResource.Event.UPDATE,
                    indicator, null, SelectableLocationListResource.DataSetChanged())

            applicationScope.launch {
                repository.writeLocationList(getApplication(), totalList)
            }
        }
    }

    fun forceUpdateLocation(location: Location) {
        totalList?.let {
            val totalList = ArrayList(it)
            for (i in totalList.indices) {
                if (totalList[i].equals(location)) {
                    totalList[i] = location
                    break
                }
            }
            val validList = Location.excludeInvalidResidentLocation(getApplication(), totalList)
            val validIndex = indexLocation(validList, formattedId)
            setInnerData(totalList, validList, validIndex)

            val current = validList[validIndex]
            val indicator = Indicator(validList.size, validIndex)
            val defaultLocation = validIndex == 0
            setLocationResourceWithVerification(current, defaultLocation, LocationResource.Event.UPDATE,
                    indicator, location.formattedId, SelectableLocationListResource.DataSetChanged())

            applicationScope.launch {
                repository.writeLocation(getApplication(), location)
            }
        }
    }

    fun forceUpdateLocation(location: Location, position: Int) {
        totalList?.let {
            val totalList = ArrayList(it)
            totalList[position] = location

            val validList = Location.excludeInvalidResidentLocation(getApplication(), totalList)
            val validIndex = indexLocation(validList, formattedId)
            setInnerData(totalList, validList, validIndex)

            val current = validList[validIndex]
            val indicator = Indicator(validList.size, validIndex)
            val defaultLocation = validIndex == 0
            setLocationResourceWithVerification(current, defaultLocation, LocationResource.Event.UPDATE,
                    indicator, location.formattedId, SelectableLocationListResource.DataSetChanged())

            applicationScope.launch {
                repository.writeLocation(getApplication(), location)
            }
        }
    }

    fun deleteLocation(position: Int): Location? {
        totalList?.let {
            val totalList = ArrayList(it)

            val location = totalList.removeAt(position)
            if (location.formattedId == formattedId) {
                formattedId = totalList[0].formattedId
            }

            val validList = Location.excludeInvalidResidentLocation(getApplication(), totalList)
            val validIndex = indexLocation(validList, formattedId)
            setInnerData(totalList, validList, validIndex)

            val current = validList[validIndex]
            val indicator = Indicator(validList.size, validIndex)
            val defaultLocation = validIndex == 0
            setLocationResourceWithVerification(current, defaultLocation, LocationResource.Event.UPDATE,
                    indicator, location.formattedId, SelectableLocationListResource.DataSetChanged())

            applicationScope.launch {
                repository.deleteLocation(getApplication(), location)
            }
            return location
        }
        return null
    }

    private fun setInnerData(totalList: List<Location>, validList: List<Location>,
                             validIndex: Int) {
        this.totalList = totalList
        this.validList = validList

        formattedId = validList[validIndex].formattedId
    }

    private fun setLocationResourceWithVerification(location: Location, defaultLocation: Boolean,
                                                    event: LocationResource.Event,
                                                    indicatorValue: Indicator,
                                                    forceUpdateId: String?,
                                                    source: SelectableLocationListResource.Source) {
        themeManager.update(getApplication(), location)

        when (event) {
            LocationResource.Event.INITIALIZE -> currentLocation.value =
                    LocationResource.loading(location, defaultLocation, event)

            LocationResource.Event.UPDATE -> if (MainModuleUtils.needUpdate(getApplication(), location)) {
                currentLocation.value = LocationResource.loading(location, defaultLocation, event)
                updateWeather(triggeredByUser = false, checkPermissions = true)
            } else {
                weatherRequest?.cancel()
                weatherRequest = null

                currentLocation.value = LocationResource.success(location, defaultLocation, event)
            }

            LocationResource.Event.BACKGROUND_UPDATE_CURRENT -> {
                weatherRequest?.cancel()
                weatherRequest = null

                currentLocation.value = LocationResource.success(location, defaultLocation, event)
            }

            LocationResource.Event.BACKGROUND_UPDATE_OTHERS -> {
                val old = currentLocation.value ?: return
                currentLocation.value = LocationResource(
                        location, old.status, defaultLocation, old.locateFailed, event)
            }
        }
        indicator.value = indicatorValue
        listResource.value = SelectableLocationListResource(
                ArrayList(totalList), location.formattedId, forceUpdateId, source)
    }

    private fun getDeniedPermissionList(): List<String> {
        val permissionList = ArrayList(repository.getLocatePermissionList(getApplication()))
        for (i in permissionList.indices.reversed()) {
            if (ActivityCompat.checkSelfPermission(getApplication(),
                            permissionList[i]) == PackageManager.PERMISSION_GRANTED) {
                permissionList.removeAt(i)
            }
        }
        return permissionList
    }

    fun getLocationFromList(offset: Int): Location? {
        validList?.let {
            val validIndex = indexLocation(it, formattedId)
            return it[(validIndex + offset + it.size) % it.size]
        }
        return null
    }

    private fun getCurrentLocationValue(): Location? {
        return currentLocation.value?.data
    }

    fun getCurrentFormattedId(): String? {
        val location = getCurrentLocationValue()
        return when {
            location != null -> location.formattedId
            formattedId != null -> formattedId
            validList != null -> validList!![0].formattedId
            else -> null
        }
    }

    fun getTotalLocationList(): List<Location>? = if (totalList != null) {
        Collections.unmodifiableList(totalList)
    } else {
        ArrayList()
    }

    fun getValidLocationList(): List<Location>? = if (validList != null) {
        Collections.unmodifiableList(validList)
    } else {
        null
    }

    fun getPermissionsRequestValue(): PermissionsRequest?  = permissionsRequest.value
}