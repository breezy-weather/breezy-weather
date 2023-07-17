package org.breezyweather.common.source

import android.Manifest
import android.content.Context
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.extensions.hasPermission


interface LocationSource : Source {

    fun requestLocation(context: Context): Observable<LocationPositionWrapper>

    // permission.
    val permissions: Array<String>
    fun hasPermissions(context: Context): Boolean {
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