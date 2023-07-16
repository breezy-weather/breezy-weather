package org.breezyweather.location

import android.Manifest
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.rx3.awaitSingle
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.provider.LocationProvider
import org.breezyweather.common.exceptions.MissingPermissionLocationBackgroundException
import org.breezyweather.common.exceptions.MissingPermissionLocationException
import org.breezyweather.common.exceptions.NoNetworkException
import org.breezyweather.common.exceptions.ReverseGeocodingException
import org.breezyweather.common.extensions.hasPermission
import org.breezyweather.common.extensions.isOnline
import org.breezyweather.db.repositories.LocationEntityRepository
import org.breezyweather.location.baiduip.BaiduIPLocationService
import org.breezyweather.location.services.AndroidLocationService
import org.breezyweather.settings.SettingsManager
import org.breezyweather.weather.WeatherServiceSet
import java.util.TimeZone
import javax.inject.Inject

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

    private fun getLocationService(provider: LocationProvider): LocationService {
        return when (provider) {
            LocationProvider.BAIDU_IP -> mLocationServices[1]
            else -> mLocationServices[0]
        }
    }

    suspend fun getCurrentLocationWithReverseGeocoding(
        context: Context, location: Location, background: Boolean
    ): Location {
        val currentLocation = requestCurrentLocation(context, location, background).awaitSingle()
        val source = SettingsManager.getInstance(context).weatherSource
        val weatherService = mWeatherServiceSet[source]
        return weatherService.requestReverseGeocodingLocation(context, currentLocation).map { locationList ->
            if (locationList.isNotEmpty()) {
                val src = locationList[0]
                val locationWithGeocodeInfo = src.copy(isCurrentPosition = true)
                LocationEntityRepository.writeLocation(locationWithGeocodeInfo)
                locationWithGeocodeInfo
            } else {
                throw ReverseGeocodingException()
            }
        }.awaitSingle()
    }

    fun requestCurrentLocation(
        context: Context, location: Location, background: Boolean
    ): Observable<Location> {
        val provider = SettingsManager.getInstance(context).locationProvider
        val locationService = getLocationService(provider)
        if (locationService.permissions.isNotEmpty()) {
            if (!context.isOnline()) {
                return Observable.error(NoNetworkException())
            }
            // if needs any location permission.
            if (!context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                && !context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            ) {
                return Observable.error(MissingPermissionLocationException())
            }
            if (background) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    && !context.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                ) {
                    return Observable.error(MissingPermissionLocationBackgroundException())
                }
            }
        }

        return locationService
            .requestLocation(context)
            .map { result ->
                location.copy(
                    latitude = result.latitude,
                    longitude = result.longitude,
                    timeZone = TimeZone.getDefault()
                )
            }
    }

    fun cancel() {
        for (s in mLocationServices) {
            s.cancel()
        }
        for (s in mWeatherServiceSet.all) {
            s.cancel()
        }
    }

    fun getPermissions(context: Context): List<String> {
        // if IP:    none.
        // else:
        //      R:   foreground location. (set background location enabled manually)
        //      Q:   foreground location + background location.
        //      K-P: foreground location.
        val provider = SettingsManager.getInstance(context).locationProvider
        val service = getLocationService(provider)
        val permissions: MutableList<String> = service.permissions.toMutableList()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || permissions.isEmpty()) {
            // device has no background location permission or locate by IP.
            return permissions
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        return permissions
    }
}