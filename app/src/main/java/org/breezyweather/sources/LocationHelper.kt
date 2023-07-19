package org.breezyweather.sources

import android.Manifest
import android.content.Context
import android.os.Build
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.rx3.awaitSingle
import org.breezyweather.common.basic.models.Location
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
        val currentLocation = requestCurrentLocation(context, location, background).awaitSingle()
        val source = location.weatherSource
        val weatherService = sourceManager.getReverseGeocodingSourceOrDefault(source)
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
        val locationService = sourceManager.getLocationSourceOrDefault(provider)
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

    fun getPermissions(context: Context): List<String> {
        // if IP:    none.
        // else:
        //      R:   foreground location. (set background location enabled manually)
        //      Q:   foreground location + background location.
        //      K-P: foreground location.
        val provider = SettingsManager.getInstance(context).locationProvider
        val service = sourceManager.getLocationSourceOrDefault(provider)
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