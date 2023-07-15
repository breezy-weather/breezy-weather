package org.breezyweather.location

import android.Manifest
import android.content.Context
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.extensions.hasPermission

abstract class LocationService {

    // location.

    data class Result(
        val latitude: Float,
        val longitude: Float
    )

    abstract fun requestLocation(context: Context): Observable<Result>
    abstract fun cancel()

    // permission.

    abstract val permissions: Array<String>
    open fun hasPermissions(context: Context): Boolean {
        val permissions = permissions
        for (p in permissions) {
            if (p == Manifest.permission.ACCESS_COARSE_LOCATION || p == Manifest.permission.ACCESS_FINE_LOCATION) {
                continue
            }
            if (!context.hasPermission(p)) {
                return false
            }
        }

        val coarseLocation = context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        val fineLocation = context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        return coarseLocation || fineLocation
    }
}