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
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.delay
import kotlinx.coroutines.rx3.rxObservable
import org.breezyweather.common.exceptions.LocationException
import org.breezyweather.common.source.LocationPositionWrapper
import org.breezyweather.common.source.LocationSource
import org.breezyweather.common.utils.helpers.LogHelper

// static.

private const val TIMEOUT_MILLIS = (10 * 1000).toLong()

private fun isGMSEnabled(
    context: Context
) = try {
    GoogleApiAvailability
        .getInstance()
        .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
} catch (e: Error) {
    e.printStackTrace()
    false
}

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
private fun getLastKnownLocation(locationManager: LocationManager): Location? {
    val fusedLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
    } else {
        null
    }
    return fusedLocation
        ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
}

// interface.

@SuppressLint("MissingPermission")
open class AndroidLocationSource : LocationSource, LocationListener {

    override val id = "native"
    override val name = "Android"

    private var locationManager: LocationManager? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null

    private var currentProvider = ""
    private var lastKnownLocation: Location? = null
    private var gmsLastKnownLocation: Location? = null

    override fun requestLocation(context: Context): Observable<LocationPositionWrapper> {
        return rxObservable {
            cancel()

            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
            fusedLocationClient = if (isGMSEnabled(context)) {
                LocationServices.getFusedLocationProviderClient(context)
            } else null
            if (locationManager == null
                || !hasPermissions(context)
                || !isLocationEnabled(locationManager!!)
                || getBestProvider(locationManager!!).also { currentProvider = it }
                    .isEmpty()
            ) {
                LogHelper.log(msg = "Location manager not ready, no permissions, no location enabled or no provider available")
                throw LocationException()
            }

            lastKnownLocation = getLastKnownLocation(locationManager!!)

            locationManager!!.requestLocationUpdates(
                currentProvider,
                0L,
                0F,
                this@AndroidLocationSource,
                Looper.getMainLooper()
            )
            fusedLocationClient?.let { client ->
                client.requestLocationUpdates(
                    LocationRequest
                        .create()
                        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                        .setNumUpdates(1),
                    gmsLocationCallback,
                    Looper.getMainLooper()
                )
                client.lastLocation.addOnSuccessListener {
                    gmsLastKnownLocation = it
                }
            }

            delay(TIMEOUT_MILLIS)

            gmsLastKnownLocation?.let {
                send(LocationPositionWrapper(it.latitude.toFloat(), it.longitude.toFloat()))
            } ?: run {
                getLastKnownLocation(locationManager!!)?.let {
                    send(LocationPositionWrapper(it.latitude.toFloat(), it.longitude.toFloat()))
                } ?: run {
                    throw LocationException()
                }
            }
        }.doOnDispose {
            cancel()
        }
    }

    fun cancel() {
        locationManager?.removeUpdates(this)
        fusedLocationClient?.removeLocationUpdates(gmsLocationCallback)
    }

    override val permissions: Array<String>
        get() = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    // location listener.
    override fun onLocationChanged(location: Location) {
        cancel()
        // do nothing.
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        // do nothing.
    }

    override fun onProviderEnabled(provider: String) {
        // do nothing.
    }

    override fun onProviderDisabled(provider: String) {
        // do nothing.
    }

    // location callback.
    private val gmsLocationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (locationResult.locations.isNotEmpty()) {
                cancel()
                // do nothing.
            }
        }
    }
}