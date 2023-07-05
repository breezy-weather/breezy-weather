package org.breezyweather.location

import android.Manifest
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.provider.LocationProvider
import org.breezyweather.common.extensions.hasPermission
import org.breezyweather.common.extensions.isOnline
import org.breezyweather.db.repositories.LocationEntityRepository
import org.breezyweather.location.baiduip.BaiduIPLocationService
import org.breezyweather.location.services.AndroidLocationService
import org.breezyweather.main.utils.RequestErrorType
import org.breezyweather.settings.SettingsManager
import org.breezyweather.weather.WeatherService
import org.breezyweather.weather.WeatherServiceSet
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * Location helper.
 */
class LocationHelper @Inject constructor(
    @ApplicationContext context: Context,
    baiduIPService: BaiduIPLocationService,
    private val mWeatherServiceSet: WeatherServiceSet
) {
    private val mLocationServices: Array<LocationService> = arrayOf(
        AndroidLocationService(),
        baiduIPService
    )

    interface OnRequestLocationListener {
        fun requestLocationSuccess(requestLocation: Location)
        fun requestLocationFailed(requestLocation: Location, requestErrorType: RequestErrorType)
    }

    private fun getLocationService(provider: LocationProvider): LocationService {
        return when (provider) {
            LocationProvider.BAIDU_IP -> mLocationServices[1]
            else -> mLocationServices[0]
        }
    }

    fun requestLocation(
        context: Context, location: Location, background: Boolean,
        l: OnRequestLocationListener
    ) {
        val usableCheckListener: OnRequestLocationListener = object : OnRequestLocationListener {
            override fun requestLocationSuccess(requestLocation: Location) {
                l.requestLocationSuccess(requestLocation)
            }

            override fun requestLocationFailed(requestLocation: Location, requestErrorType: RequestErrorType) {
                l.requestLocationFailed(requestLocation, requestErrorType)
            }
        }
        val provider = SettingsManager.getInstance(context).locationProvider
        val service = getLocationService(provider)
        if (service.permissions.isNotEmpty()) {
            if (!context.isOnline()) {
                usableCheckListener.requestLocationFailed(location, RequestErrorType.NETWORK_UNAVAILABLE)
                return
            }
            // if needs any location permission.
            if (!context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                && !context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            ) {
                usableCheckListener.requestLocationFailed(location, RequestErrorType.ACCESS_LOCATION_PERMISSION_MISSING)
                return
            }
            if (background) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    && !context.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                ) {
                    usableCheckListener.requestLocationFailed(
                        location,
                        RequestErrorType.ACCESS_BACKGROUND_LOCATION_PERMISSION_MISSING
                    )
                    return
                }
            }
        }

        // 1. get location by location service.
        // 2. get available location by weather service.
        service.requestLocation(context) { result: LocationService.Result? ->
            if (result == null) {
                usableCheckListener.requestLocationFailed(location, RequestErrorType.LOCATION_FAILED)
                return@requestLocation
            }
            requestAvailableWeatherLocation(
                context,
                location.copy(
                    latitude = result.latitude,
                    longitude = result.longitude,
                    timeZone = TimeZone.getDefault()
                ),
                usableCheckListener
            )
        }
    }

    private fun requestAvailableWeatherLocation(
        context: Context,
        location: Location,
        l: OnRequestLocationListener
    ) {
        val source = SettingsManager.getInstance(context).weatherSource
        val service = mWeatherServiceSet[source]
        service.requestReverseLocationSearch(context, location, object : WeatherService.RequestLocationCallback {
            override fun requestLocationSuccess(query: String, locationList: List<Location>) {
                if (locationList.isNotEmpty()) {
                    val src = locationList[0]
                    val result = src.copy(isCurrentPosition = true)
                    LocationEntityRepository.writeLocation(result)
                    l.requestLocationSuccess(result)
                } else {
                    requestLocationFailed(query, RequestErrorType.LOCATION_FAILED)
                }
            }

            override fun requestLocationFailed(query: String, requestErrorType: RequestErrorType) {
                l.requestLocationFailed(location, requestErrorType)
            }
        })
    }

    fun cancel() {
        for (s in mLocationServices) {
            s.cancel()
        }
        for (s in mWeatherServiceSet.all) {
            s.cancel()
        }
    }

    fun getPermissions(context: Context): Array<String> {
        // if IP:    none.
        // else:
        //      R:   foreground location. (set background location enabled manually)
        //      Q:   foreground location + background location.
        //      K-P: foreground location.
        val provider = SettingsManager.getInstance(context).locationProvider
        val service = getLocationService(provider)
        val permissions: Array<String> = service.permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || permissions.isEmpty()) {
            // device has no background location permission or locate by IP.
            return permissions
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            val qPermissions = ArrayList<String>(permissions.size + 1)
            System.arraycopy(permissions, 0, qPermissions, 0, permissions.size)
            qPermissions[qPermissions.size - 1] = Manifest.permission.ACCESS_BACKGROUND_LOCATION
            return qPermissions.toTypedArray()
        }
        return permissions
    }
}