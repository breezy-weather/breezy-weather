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
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import org.breezyweather.common.exceptions.LocationException
import org.breezyweather.common.source.LocationPositionWrapper
import org.breezyweather.common.source.LocationSource
import org.breezyweather.common.utils.helpers.LogHelper
import javax.inject.Inject

@SuppressLint("MissingPermission")
class GoogleFusedLocationService @Inject constructor() : LocationSource {

    override val id = "googlefused"
    override val name = "Google Fused"

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun requestLocation(context: Context): Observable<LocationPositionWrapper> {
        if (!hasPermissions(context)) {
            LogHelper.log(msg = "Location permissions missing")
            throw LocationException()
        }

        if (!isGMSEnabled(context)) {
            LogHelper.log(msg = "Google Play Services not available")
            throw LocationException()
        }

        if (!this::fusedLocationClient.isInitialized) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }

        return Observable.create { emitter ->
            fusedLocationClient.locationAvailability.addOnSuccessListener { locationAvailability ->
                // best guess
                if (locationAvailability.isLocationAvailable) {
                    requestLastLocation(emitter, fusedLocationClient)
                } else {
                    requestCurrentLocation(emitter, fusedLocationClient)
                }
            }
        }
    }

    private fun requestLastLocation(emitter: ObservableEmitter<LocationPositionWrapper>, client: FusedLocationProviderClient) {
        // will be called regardless of 'location' value
        client.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                LogHelper.log(msg = "GMS FUSED: got cached location")
                emitter.onNext(LocationPositionWrapper(location.latitude, location.longitude))
                emitter.onComplete()
            } else {
                // fall back and request current location
                requestCurrentLocation(emitter, client)
            }
        }
    }

    private fun requestCurrentLocation(emitter: ObservableEmitter<LocationPositionWrapper>, client: FusedLocationProviderClient) {
        LogHelper.log(msg = "GMS FUSED: no cached location, requesting current location")

        val currentLocationRequest = CurrentLocationRequest.Builder()
            .setDurationMillis(TIMEOUT_MILLIS)
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .setMaxUpdateAgeMillis(VALID_FOR_MILLIS)
            .build()

        client.getCurrentLocation(currentLocationRequest, null).addOnSuccessListener { location ->
            if (location == null) {
                LogHelper.log(msg = "GMS FUSED: current location request failed")
                emitter.onError(LocationException())
                return@addOnSuccessListener
            }

            LogHelper.log(msg = "GMS FUSED: got current location")
            emitter.onNext(LocationPositionWrapper(location.latitude, location.longitude))
            emitter.onComplete()
        }.addOnCanceledListener {
            // yet to encounter this case
            LogHelper.log(msg = "GMS FUSED: current location request was cancelled")
            emitter.onError(LocationException())
        }.addOnFailureListener {
            // called if request timed out
            LogHelper.log(msg = "GMS FUSED: current location request failed")
            emitter.onError(LocationException())
        }
    }

    override val permissions: Array<String>
        get() = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    companion object {
        private const val TIMEOUT_MILLIS = (10 * 1000).toLong() // 10 seconds
        private const val VALID_FOR_MILLIS = (1000 * 60 * 10).toLong() // 10 minutes

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
    }
}
