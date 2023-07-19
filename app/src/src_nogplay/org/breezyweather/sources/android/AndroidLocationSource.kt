package org.breezyweather.sources.android

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.delay
import kotlinx.coroutines.rx3.rxObservable
import org.breezyweather.common.exceptions.LocationException
import org.breezyweather.common.source.LocationPositionWrapper
import org.breezyweather.common.utils.helpers.LogHelper
import org.breezyweather.common.source.LocationSource
import javax.inject.Inject

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
open class AndroidLocationSource @Inject constructor() : LocationSource, LocationListener {

    override val id = "native"
    override val name = "Android"

    private var locationManager: LocationManager? = null

    private var currentProvider = ""

    override fun requestLocation(context: Context): Observable<LocationPositionWrapper> {
        return rxObservable {
            cancel()

            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
            if (locationManager == null
                || !hasPermissions(context)
                || !isLocationEnabled(locationManager!!)
                || getBestProvider(locationManager!!).also { currentProvider = it }.isEmpty()
            ) {
                LogHelper.log(msg = "Location manager not ready, no permissions, no location enabled or no provider available")
                throw LocationException()
            }

            getLastKnownLocation(locationManager!!)

            locationManager!!.requestLocationUpdates(
                currentProvider,
                0L,
                0F,
                this@AndroidLocationSource,
                Looper.getMainLooper()
            )

            delay(TIMEOUT_MILLIS)
            cancel()
            getLastKnownLocation(locationManager!!)?.let {
                send(LocationPositionWrapper(it.latitude.toFloat(), it.longitude.toFloat()))
            } ?: run {
                // Actually it’s a timeout, but it is more reasonable to say it failed to find location
                throw LocationException()
            }
        }.doOnDispose {
            cancel()
        }
    }

    fun cancel() {
        locationManager?.removeUpdates(this)
    }

    override val permissions: Array<String>
        get() = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )


    // location listener.
    override fun onLocationChanged(location: Location) {
        // do nothing.
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