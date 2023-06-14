package org.breezyweather.location.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.WorkerThread

// static.

private const val TIMEOUT_MILLIS = (10 * 1000).toLong()

private fun isLocationEnabled(
    manager: LocationManager
) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    manager.isLocationEnabled
} else {
    manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            || manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

private fun getBestProvider(locationManager: LocationManager): String {
    var provider = locationManager.getBestProvider(
        Criteria().apply {
            isBearingRequired = false
            isAltitudeRequired = false
            isSpeedRequired = false
            accuracy = Criteria.ACCURACY_FINE
            horizontalAccuracy = Criteria.ACCURACY_HIGH
            powerRequirement = Criteria.POWER_HIGH
        },
        true
    ) ?: ""

    if (provider.isEmpty()) {
        provider = locationManager
            .getProviders(true)
            .getOrNull(0) ?: provider
    }

    return provider
}

@SuppressLint("MissingPermission")
private fun getLastKnownLocation(
    locationManager: LocationManager
) = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

// interface.

@SuppressLint("MissingPermission")
open class AndroidLocationService : LocationService(), LocationListener {

    private val timer = Handler(Looper.getMainLooper())

    private var locationManager: LocationManager? = null

    private var currentProvider = ""
    private var locationCallback: LocationCallback? = null
    private var lastKnownLocation: Location? = null
    private var gmsLastKnownLocation: Location? = null

    override fun requestLocation(context: Context, callback: LocationCallback) {
        cancel()

        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if (locationManager == null
            || !hasPermissions(context)
            || !isLocationEnabled(locationManager!!)
            || getBestProvider(locationManager!!).also { currentProvider = it }.isEmpty()) {
            callback.onCompleted(null)
            return
        }

        locationCallback = callback
        lastKnownLocation = getLastKnownLocation(locationManager!!)

        locationManager!!.requestLocationUpdates(
            currentProvider,
            0L,
            0F,
            this,
            Looper.getMainLooper()
        )
        timer.postDelayed({
            cancel()
            handleLocation(gmsLastKnownLocation ?: lastKnownLocation)
        }, TIMEOUT_MILLIS)
    }

    override fun cancel() {
        locationManager?.removeUpdates(this)
        timer.removeCallbacksAndMessages(null)
    }

    override val permissions: Array<String>
        get() = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    private fun handleLocation(location: Location?) {
        locationCallback?.onCompleted(
            location?.let { buildResult(it) }
        )
    }

    @WorkerThread
    private fun buildResult(location: Location): Result {
        return Result(
            location.latitude.toFloat(),
            location.longitude.toFloat()
        )
    }

    // location listener.

    override fun onLocationChanged(location: Location) {
        cancel()
        handleLocation(location)
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        // do nothing.
    }

    override fun onProviderEnabled(provider: String) {
        // do nothing.
    }

    override fun onProviderDisabled(provider: String) {
        // do nothing.
    }
}