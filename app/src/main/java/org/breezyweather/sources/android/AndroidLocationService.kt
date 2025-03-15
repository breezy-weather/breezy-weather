package org.breezyweather.sources.android

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.delay
import kotlinx.coroutines.rx3.rxObservable
import org.breezyweather.common.exceptions.LocationAccessOffException
import org.breezyweather.common.exceptions.LocationException
import org.breezyweather.common.source.LocationPositionWrapper
import org.breezyweather.common.source.LocationSource
import org.breezyweather.common.utils.helpers.LogHelper
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@SuppressLint("MissingPermission")
class AndroidLocationService @Inject constructor() : LocationSource {

    override val id = "native"
    override val name = "Android"

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListenerCompat

    private val bestProvider: String
        get() = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                locationManager.allProviders.contains(LocationManager.FUSED_PROVIDER) -> LocationManager.FUSED_PROVIDER
            locationManager.allProviders.contains(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            locationManager.allProviders.contains(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            else -> LocationManager.PASSIVE_PROVIDER
        }

    override fun requestLocation(context: Context): Observable<LocationPositionWrapper> {
        if (!hasPermissions(context)) {
            LogHelper.log(msg = "Location permissions missing")
            throw LocationException()
        }

        if (!this::locationManager.isInitialized) {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }

        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
            LogHelper.log(msg = "Location service not enabled")
            throw LocationAccessOffException()
        }

        return rxObservable {
            locationListener = LocationListenerCompat {
                clearLocationUpdates(locationListener)
                val result = trySend(LocationPositionWrapper(longitude = it.longitude, latitude = it.latitude))
                if (!result.isSuccess) {
                    throw LocationException()
                }
            }

            LocationManagerCompat.requestLocationUpdates(
                locationManager,
                bestProvider,
                LocationRequestCompat.Builder(1000).apply {
                    setQuality(LocationRequestCompat.QUALITY_BALANCED_POWER_ACCURACY)
                }.build(),
                locationListener,
                Looper.getMainLooper()
            )
            delay(TIMEOUT_MILLIS)

            // Fall back to the last known location if it failed to find the location in the given time frame
            clearLocationUpdates(locationListener)
            getLastKnownLocation(locationManager)?.let {
                send(LocationPositionWrapper(it.latitude, it.longitude))
            } ?: run {
                // Actually it’s a timeout, but it is more reasonable to say it failed to find location
                throw LocationException()
            }
        }.doOnDispose {
            if (this::locationListener.isInitialized) {
                clearLocationUpdates(locationListener)
            }
        }
    }

    private fun clearLocationUpdates(listener: LocationListenerCompat) {
        locationManager.removeUpdates(listener)
    }

    override val permissions: Array<String>
        get() = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    companion object {
        private val TIMEOUT_MILLIS = 15.seconds.inWholeMilliseconds

        private fun getLastKnownLocation(
            locationManager: LocationManager,
        ): Location? {
            val lastKnownFused = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
            } else {
                null
            }
            return lastKnownFused
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
        }
    }
}
