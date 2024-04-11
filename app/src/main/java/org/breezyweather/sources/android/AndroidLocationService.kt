package org.breezyweather.sources.android

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
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
import org.breezyweather.common.source.LocationSource
import org.breezyweather.common.utils.helpers.LogHelper
import javax.inject.Inject

@SuppressLint("MissingPermission")
open class AndroidLocationService @Inject constructor() : LocationSource, LocationListener {

    override val id = "native"
    override val name = "Android"

    private var locationManager: LocationManager? = null

    private var currentProvider = ""

    private var gpsLocation: Location? = null

    override fun requestLocation(context: Context): Observable<LocationPositionWrapper> {
        return rxObservable {
            gpsLocation = null
            clearLocationUpdates()

            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
            currentProvider = locationManager?.let { getBestProvider(it) } ?: currentProvider
            if (locationManager == null ||
                !hasPermissions(context) ||
                !isLocationEnabled(locationManager!!) ||
                currentProvider.isEmpty()
            ) {
                LogHelper.log(msg = "Location manager not ready, no permissions, no location enabled or no provider available")
                throw LocationException()
            }

            locationManager!!.requestLocationUpdates(
                currentProvider,
                0L,
                0F,
                this@AndroidLocationService,
                Looper.getMainLooper()
            )

            // TODO: Dirty, should be improved
            // wait X seconds for callbacks to set locations
            for (i in 1..TIMEOUT_MILLIS / 1000) {
                delay(1000)

                gpsLocation?.let {
                    clearLocationUpdates()
                    send(LocationPositionWrapper(it.latitude, it.longitude))
                }
            }

            clearLocationUpdates()
            getLastKnownLocation(locationManager!!)?.let {
                send(LocationPositionWrapper(it.latitude, it.longitude))
            } ?: run {
                // Actually it’s a timeout, but it is more reasonable to say it failed to find location
                throw LocationException()
            }
        }.doOnDispose {
            clearLocationUpdates()
        }
    }

    private fun clearLocationUpdates() {
        locationManager?.removeUpdates(this)
    }

    override val permissions: Array<String>
        get() = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    // location listener.
    override fun onLocationChanged(location: Location) {
        clearLocationUpdates()
        gpsLocation = location
        LogHelper.log(msg = "Got GPS location")
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

    companion object {
        private const val TIMEOUT_MILLIS = (10 * 1000).toLong()

        private fun isLocationEnabled(
            manager: LocationManager
        ) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            manager.isLocationEnabled
        } else {
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                    manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

        private fun getBestProvider(locationManager: LocationManager): String {
            return locationManager
                .getProviders(true)
                .getOrNull(0) ?: ""
        }

        @SuppressLint("MissingPermission")
        private fun getLastKnownLocation(
            locationManager: LocationManager
        ) = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
    }
}