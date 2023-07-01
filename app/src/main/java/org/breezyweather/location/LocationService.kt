package org.breezyweather.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

abstract class LocationService {

    // location.

    data class Result(
        val latitude: Float,
        val longitude: Float
    )
    interface LocationCallback {
        fun onCompleted(result: Result?)
    }

    abstract fun requestLocation(context: Context, callback: LocationCallback)
    abstract fun cancel()

    // permission.

    abstract val permissions: Array<String>
    open fun hasPermissions(context: Context): Boolean {
        val permissions = permissions
        for (p in permissions) {
            if (p == Manifest.permission.ACCESS_COARSE_LOCATION
                || p == Manifest.permission.ACCESS_FINE_LOCATION) {
                continue
            }
            if (ActivityCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        val coarseLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val fineLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return coarseLocation || fineLocation
    }
}