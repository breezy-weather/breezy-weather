/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources.android

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.delay
import kotlinx.coroutines.rx3.rxObservable
import org.breezyweather.common.exceptions.LocationException
import org.breezyweather.common.source.LocationPositionWrapper
import org.breezyweather.common.source.LocationSource
import org.breezyweather.common.utils.helpers.LogHelper
import javax.inject.Inject

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
    manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
        manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

private fun getBestProvider(locationManager: LocationManager): String {
    return locationManager.getProviders(true).getOrNull(0) ?: ""
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
open class AndroidLocationSource @Inject constructor() : LocationSource, LocationListener {

    override val id = "native"
    override val name = "Android"

    private var locationManager: LocationManager? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null

    private var currentProvider = ""

    private var gpsLocation: Location? = null
    private var gmsLocation: Location? = null

    override fun requestLocation(context: Context): Observable<LocationPositionWrapper> {
        return rxObservable {
            gpsLocation = null
            gmsLocation = null
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

            // request constant gps updates
            locationManager!!.requestLocationUpdates(
                currentProvider,
                0L,
                0F,
                this@AndroidLocationSource,
                Looper.getMainLooper()
            )

            // if GMS is enabled, try and get one location update from gms
            if (isGMSEnabled(context)) {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

                fusedLocationClient?.let { client ->
                    val locationRequest = LocationRequest.Builder(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        1
                    ).setMaxUpdates(1).build()

                    client.requestLocationUpdates(
                        locationRequest,
                        gmsLocationCallback,
                        Looper.getMainLooper()
                    )

                    client.lastLocation.addOnSuccessListener {
                        gmsLocation = it
                    }
                }
            }

            // TODO: Dirty, should be improved
            // wait X seconds for callbacks to set locations
            for (i in 1..TIMEOUT_MILLIS / 1000) {
                delay(1000)

                gmsLocation?.let {
                    clearLocationUpdates()
                    send(LocationPositionWrapper(it.latitude, it.longitude))
                }

                gpsLocation?.let {
                    clearLocationUpdates()
                    send(LocationPositionWrapper(it.latitude, it.longitude))
                }
            }

            // if we are unlucky enough to get a fix, get the last known location
            getLastKnownLocation(locationManager!!)?.let {
                send(LocationPositionWrapper(it.latitude, it.longitude))
            } ?: throw LocationException()
        }.doOnDispose {
            clearLocationUpdates()
        }
    }

    private fun clearLocationUpdates() {
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
        clearLocationUpdates()
        gpsLocation = location
        LogHelper.log(msg = "Got GPS location")
    }

    override fun onProviderEnabled(provider: String) {
        // do nothing.
    }

    override fun onProviderDisabled(provider: String) {
        // do nothing.
    }

    // location callback.
    private val gmsLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (locationResult.locations.isNotEmpty()) {
                clearLocationUpdates()
                gmsLocation = locationResult.locations[0]
                LogHelper.log(msg = "Got GMS location")
            }
        }
    }
}
