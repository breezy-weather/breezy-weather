package org.breezyweather.sources

import android.Manifest
import android.content.Context
import android.os.Build
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.rx3.awaitFirstOrElse
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.exceptions.LocationException
import org.breezyweather.common.exceptions.MissingPermissionLocationBackgroundException
import org.breezyweather.common.exceptions.MissingPermissionLocationException
import org.breezyweather.common.exceptions.NoNetworkException
import org.breezyweather.common.exceptions.ReverseGeocodingException
import org.breezyweather.common.extensions.hasPermission
import org.breezyweather.common.extensions.isOnline
import org.breezyweather.db.repositories.LocationEntityRepository
import org.breezyweather.settings.SettingsManager
import java.util.TimeZone
import javax.inject.Inject

/**
 * Location helper.
 */
class LocationHelper @Inject constructor(
    private val sourceManager: SourceManager
) {
    suspend fun getCurrentLocationWithReverseGeocoding(
        context: Context, location: Location, background: Boolean
    ): Location {
        val currentLocation = requestCurrentLocation(context, location, background).awaitFirstOrElse {
            throw LocationException()
        }
        val source = location.weatherSource
        val weatherService = sourceManager.getReverseGeocodingSource(source)
        return if (weatherService != null) {
            weatherService.requestReverseGeocodingLocation(context, currentLocation).map { locationList ->
                if (locationList.isNotEmpty()) {
                    val src = locationList[0]
                    val locationWithGeocodeInfo = src.copy(isCurrentPosition = true)
                    LocationEntityRepository.writeLocation(locationWithGeocodeInfo)
                    locationWithGeocodeInfo
                } else {
                    throw ReverseGeocodingException()
                }
            }.awaitFirstOrElse {
                throw ReverseGeocodingException()
            }
        } else {
            // Returned as-is if no reverse geocoding source
            // but write in case the location service has provided us information
            LocationEntityRepository.writeLocation(currentLocation)
            currentLocation
        }
    }

    fun requestCurrentLocation(
        context: Context, location: Location, background: Boolean
    ): Observable<Location> {
        val locationSource = SettingsManager.getInstance(context).locationSource
        val locationService = sourceManager.getLocationSourceOrDefault(locationSource)
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
                    /*
                     * Don’t keep old data as the user can have changed position
                     * It avoids keeping old data from a reverse geocoding-compatible weather source
                     * onto a weather source without reverse geocoding
                     */
                    timeZone = result.timeZone ?: TimeZone.getDefault(),
                    country = result.country ?: "",
                    countryCode = result.countryCode ?: "",
                    province = result.province ?: "",
                    provinceCode = result.provinceCode ?: "",
                    city = result.city ?: "",
                    district = result.district ?: ""
                )
            }
    }

    fun getPermissions(context: Context): List<String> {
        // if IP:    none.
        // else:
        //      R:   foreground location. (set background location enabled manually)
        //      Q:   foreground location + background location.
        //      K-P: foreground location.
        val locationSource = SettingsManager.getInstance(context).locationSource
        val service = sourceManager.getLocationSourceOrDefault(locationSource)
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