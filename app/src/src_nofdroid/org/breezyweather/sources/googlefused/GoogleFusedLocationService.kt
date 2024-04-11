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

package org.breezyweather.sources.googlefused

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
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

@SuppressLint("MissingPermission")
class GoogleFusedLocationService @Inject constructor() : LocationSource {

    override val id = "googlefused"
    override val name = "Google Fused"

    private var fusedLocationClient: FusedLocationProviderClient? = null

    private var gmsLocation: Location? = null

    override fun requestLocation(context: Context): Observable<LocationPositionWrapper> {
        return rxObservable {
            gmsLocation = null
            clearLocationUpdates()

            if (!hasPermissions(context)) {
                LogHelper.log(msg = "Location permissions missing")
                throw LocationException()
            }
            if (!isGMSEnabled(context)) {
                LogHelper.log(msg = "Google Play Services not available")
                throw LocationException()
            }

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            if (fusedLocationClient == null) {
                LogHelper.log(msg = "Failed to initialize Fused Location provider")
                throw LocationException()
            }

            fusedLocationClient!!.let { client ->
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

            // TODO: Dirty, should be improved
            // wait X seconds for callbacks to set locations
            for (i in 1..TIMEOUT_MILLIS / 1000) {
                delay(1000)

                gmsLocation?.let {
                    clearLocationUpdates()
                    send(LocationPositionWrapper(it.latitude, it.longitude))
                }
            }

            // if we are unlucky enough to get a fix, get the last known location
            getLastKnownLocation(fusedLocationClient!!)?.let {
                send(LocationPositionWrapper(it.latitude, it.longitude))
            } ?: throw LocationException()
        }.doOnDispose {
            clearLocationUpdates()
        }
    }

    private fun clearLocationUpdates() {
        fusedLocationClient?.removeLocationUpdates(gmsLocationCallback)
    }

    override val permissions: Array<String>
        get() = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

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

    companion object {
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

        @SuppressLint("MissingPermission")
        private fun getLastKnownLocation(fusedLocationProviderClient: FusedLocationProviderClient): Location? {
            return fusedLocationProviderClient.lastLocation.result
        }
    }
}
