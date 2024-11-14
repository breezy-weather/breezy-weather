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
import org.breezyweather.common.exceptions.LocationException
import org.breezyweather.common.source.LocationPositionWrapper
import org.breezyweather.common.source.LocationSource
import org.breezyweather.common.utils.helpers.LogHelper
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@SuppressLint("MissingPermission")
class AndroidLocationService @Inject constructor() : LocationSource, LocationListenerCompat {

    override val id = "native"
    override val name = "Android"

    private lateinit var locationManager: LocationManager
    private var androidLocation: Location? = null

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
            throw LocationException()
        }

        return rxObservable {
            androidLocation = null
            clearLocationUpdates()

            LocationManagerCompat.requestLocationUpdates(
                locationManager,
                bestProvider,
                LocationRequestCompat.Builder(1000).apply {
                    setQuality(LocationRequestCompat.QUALITY_BALANCED_POWER_ACCURACY)
                }.build(),
                this@AndroidLocationService,
                Looper.getMainLooper()
            )

            // TODO: Dirty, should be improved
            // wait X seconds for callbacks to set locations
            for (i in 1..TIMEOUT_MILLIS / 1000) {
                delay(1000)

                androidLocation?.let {
                    clearLocationUpdates()
                    send(LocationPositionWrapper(it.latitude, it.longitude))
                }
            }

            clearLocationUpdates()
            getLastKnownLocation(locationManager)?.let {
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
        locationManager.removeUpdates(this)
    }

    // location listener.
    override fun onLocationChanged(location: Location) {
        clearLocationUpdates()
        androidLocation = location
        LogHelper.log(msg = "Got GPS location")
    }

    override fun onProviderEnabled(provider: String) {
        // do nothing.
    }

    override fun onProviderDisabled(provider: String) {
        // do nothing.
    }

    override val permissions: Array<String>
        get() = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    companion object {
        private val TIMEOUT_MILLIS = 10.seconds.inWholeMilliseconds

        @SuppressLint("MissingPermission")
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
