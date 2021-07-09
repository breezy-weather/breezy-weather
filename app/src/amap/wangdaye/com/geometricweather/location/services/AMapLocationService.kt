package wangdaye.com.geometricweather.location.services

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationManagerCompat
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.CoordinateConverter
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import wangdaye.com.geometricweather.GeometricWeather
import wangdaye.com.geometricweather.common.utils.helpers.BuglyHelper
import wangdaye.com.geometricweather.common.utils.resumeSafely
import wangdaye.com.geometricweather.location.utils.LocationException
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashSet

/**
 * A map location service.
 */
class AMapLocationService @Inject constructor() : LocationService() {

    private var client: AMapLocationClient? = null
    private val handler = Handler(Looper.getMainLooper())
    private val continuationSet = HashSet<CancellableContinuation<Result?>>()

    override suspend fun getLocation(context: Context) = suspendCancellableCoroutine<Result?> {
        runOnMainThread {
            continuationSet.add(it)

            if (client != null) {
                // client is working. just register the continuation.
                return@runOnMainThread
            }

            val client = AMapLocationClient(context.applicationContext)
            this.client = client

            val option = AMapLocationClientOption()
            option.locationMode = AMapLocationClientOption.AMapLocationMode.Battery_Saving
            option.isOnceLocation = true
            option.isOnceLocationLatest = true
            option.isNeedAddress = true
            option.isMockEnable = false
            option.isLocationCacheEnable = false

            client.setLocationOption(option)
            client.setLocationListener { aMapLocation ->

                cancel(null)

                if (aMapLocation.errorCode == 0) {
                    val result = Result(
                            aMapLocation.latitude.toFloat(),
                            aMapLocation.longitude.toFloat()
                    )
                    result.setGeocodeInformation(
                            aMapLocation.country,
                            aMapLocation.province,
                            aMapLocation.city,
                            aMapLocation.district
                    )
                    result.inChina = CoordinateConverter.isAMapDataAvailable(
                            aMapLocation.latitude,
                            aMapLocation.longitude
                    )
                    publishResult(result)
                } else {
                    BuglyHelper.report(
                            LocationException(
                                    aMapLocation.errorCode,
                                    aMapLocation.errorInfo
                            )
                    )
                    publishResult(null)
                }
            }

            it.invokeOnCancellation { _ ->
                cancel(it)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManagerCompat.from(context).createNotificationChannel(
                        getLocationNotificationChannel(context))
                client.enableBackgroundLocation(
                        GeometricWeather.NOTIFICATION_ID_LOCATION,
                        getLocationNotification(context))
            }
            client.startLocation()
        }
    }

    private fun cancel(continuation: CancellableContinuation<Result?>?) {
        runOnMainThread {

            continuation?.let {
                continuationSet.remove(it)
                if (continuationSet.isNotEmpty()) {
                    it.resumeSafely(null)
                    return@runOnMainThread
                }
            }

            client?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.disableBackgroundLocation(true)
                }
                it.stopLocation()
                it.onDestroy()
            }
            client = null

            continuation?.resumeSafely(null)
        }
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
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}