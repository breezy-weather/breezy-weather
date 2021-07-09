package wangdaye.com.geometricweather.location.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils
import androidx.annotation.WorkerThread
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellableContinuation
import wangdaye.com.geometricweather.common.utils.LanguageUtils
import wangdaye.com.geometricweather.common.utils.helpers.AsyncHelper
import wangdaye.com.geometricweather.common.utils.resumeSafely
import wangdaye.com.geometricweather.common.utils.suspendCoroutineWithTimeout
import java.io.IOException
import javax.inject.Inject

/**
 * Android Location service.
 */
@SuppressLint("MissingPermission")
open class AndroidLocationService @Inject constructor(
        @ApplicationContext private val context: Context) : LocationService() {

    companion object {

        private const val TIMEOUT_MILLIS = (10 * 1000).toLong()

        private fun gmsEnabled(context: Context) = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

        private fun locationEnabled(context: Context, manager: LocationManager): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (!manager.isLocationEnabled) {
                    return false
                }
            } else {
                var locationMode = -1
                try {
                    locationMode = Settings.Secure.getInt(
                            context.contentResolver, Settings.Secure.LOCATION_MODE)
                } catch (e: SettingNotFoundException) {
                    e.printStackTrace()
                }
                if (locationMode == Settings.Secure.LOCATION_MODE_OFF) {
                    return false
                }
            }
            return manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    || manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
    }

    private var manager: LocationManager? = null
    private var gmsCancellationSource: CancellationTokenSource? = null
    private var networkListener: LocationListener? = null
    private var gpsListener: LocationListener? = null
    private var geocoderController: AsyncHelper.Controller? = null

    private val handler = Handler(Looper.getMainLooper())
    private val continuationSet = HashSet<CancellableContinuation<Result?>>()

    private abstract class LocationListener : android.location.LocationListener {

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

    override suspend fun getLocation(context: Context) = suspendCoroutineWithTimeout<Result?>(
            TIMEOUT_MILLIS
    ) {
        runOnMainThread {
            continuationSet.add(it)

            if (manager != null) {
                // client is working. just register the continuation.
                return@runOnMainThread
            }

            val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            this.manager = manager

            val gmsClient = if (gmsEnabled(context)) {
                LocationServices.getFusedLocationProviderClient(context)
            } else {
                null
            }

            if (manager == null
                    || !locationEnabled(context, manager)
                    || !hasPermissions(context)) {
                publishResult(null)
                return@runOnMainThread
            }

            networkListener = object : LocationListener() {
                override fun onLocationChanged(location: Location) {
                    stopLocationUpdates()
                    handleLocation(location)
                }
            }
            gpsListener = object : LocationListener() {
                override fun onLocationChanged(location: Location) {
                    stopLocationUpdates()
                    handleLocation(location)
                }
            }

            it.invokeOnCancellation { _ ->
                stopLocationUpdates(it)
                runOnMainThread {
                    geocoderController?.let {
                        it.cancel()
                        geocoderController = null
                    }
                }
            }

            networkListener?.let {
                if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            100, 0f, it, Looper.getMainLooper())
                }
            }
            gpsListener?.let {
                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            100, 0f, it, Looper.getMainLooper())
                }
            }

            val source = CancellationTokenSource()
            gmsCancellationSource = source

            gmsClient?.getCurrentLocation(
                    LocationRequest.PRIORITY_HIGH_ACCURACY, source.token
            )?.addOnSuccessListener {
                stopLocationUpdates()
                handleLocation(it)
            }
        }
    }

    private fun stopLocationUpdates(continuation: CancellableContinuation<Result?>? = null) {
        runOnMainThread {

            continuation?.let {
                continuationSet.remove(it)
                if (continuationSet.isNotEmpty()) {
                    // there are still some location progress is working. don't stop services.
                    it.resumeSafely(null)
                    return@runOnMainThread
                }
            }

            // no location progress anymore. stop services at first, then resume the coroutine.

            networkListener?.let {
                manager?.removeUpdates(it)
            }
            gpsListener?.let {
                manager?.removeUpdates(it)
            }
            gmsCancellationSource?.let {
                it.cancel()
                gmsCancellationSource = null
            }
            manager = null

            continuation?.resumeSafely(null)
        }
    }

    private fun handleLocation(location: Location?) {
        if (location == null) {
            publishResult(null)
            return
        }

        geocoderController = AsyncHelper.runOnIO {
            publishResult(buildResult(location))
        }
    }

    @WorkerThread
    private fun buildResult(location: Location): Result {
        val result = Result(location.latitude.toFloat(), location.longitude.toFloat())
        result.hasGeocodeInformation = false
        if (!Geocoder.isPresent()) {
            return result
        }

        var addressList: List<Address>? = null
        try {
            addressList = Geocoder(context, LanguageUtils.getCurrentLocale(context))
                    .getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                    )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (addressList == null || addressList.isEmpty()) {
            return result
        }

        result.setGeocodeInformation(
                addressList[0].countryName,
                addressList[0].adminArea,
                if (TextUtils.isEmpty(addressList[0].locality)) {
                    addressList[0].subAdminArea
                } else {
                    addressList[0].locality
                },
                addressList[0].subLocality
        )

        val countryCode = addressList[0].countryCode
        if (TextUtils.isEmpty(countryCode)) {
            if (TextUtils.isEmpty(result.country)) {
                result.inChina = false
            } else {
                result.inChina = result.country == "中国"
                        || result.country == "香港"
                        || result.country == "澳门"
                        || result.country == "台湾"
                        || result.country == "China"
            }
        } else {
            result.inChina = countryCode == "CN"
                    || countryCode == "cn"
                    || countryCode == "HK"
                    || countryCode == "hk"
                    || countryCode == "TW"
                    || countryCode == "tw"
        }

        return result
    }

    private fun publishResult(result: Result?) {
        runOnMainThread {
            for (c in continuationSet) {
                c.resumeSafely(result)
            }
            continuationSet.clear()
        }
    }

    private fun runOnMainThread(r: Runnable) {
        if (Looper.getMainLooper().thread === Thread.currentThread()) {
            r.run()
        } else {
            handler.post(r)
        }
    }

    override fun getPermissions(): Array<String> {
        return arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}