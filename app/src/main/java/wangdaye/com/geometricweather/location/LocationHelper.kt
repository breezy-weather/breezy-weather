package wangdaye.com.geometricweather.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.coroutineScope
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.Response
import wangdaye.com.geometricweather.common.basic.models.options.provider.LocationProvider
import wangdaye.com.geometricweather.common.utils.NetworkUtils
import wangdaye.com.geometricweather.db.DatabaseHelper
import wangdaye.com.geometricweather.location.services.AMapLocationService
import wangdaye.com.geometricweather.location.services.AndroidLocationService
import wangdaye.com.geometricweather.location.services.BaiduLocationService
import wangdaye.com.geometricweather.location.services.LocationService
import wangdaye.com.geometricweather.location.services.ip.BaiduIPLocationService
import wangdaye.com.geometricweather.settings.SettingsManager.Companion.getInstance
import wangdaye.com.geometricweather.weather.WeatherServiceSet
import java.util.*
import javax.inject.Inject

/**
 * Location helper.
 */
class LocationHelper @Inject constructor(androidLocationService: AndroidLocationService,
                                         baiduLocationService: BaiduLocationService,
                                         baiduIPService: BaiduIPLocationService,
                                         aMapLocationService: AMapLocationService,
                                         private val weatherServiceSet: WeatherServiceSet) {

    private val locationServices = arrayOf(
            androidLocationService,
            baiduLocationService,
            baiduIPService,
            aMapLocationService
    )

    private fun getLocationService(provider: LocationProvider): LocationService {
        return when (provider) {
            LocationProvider.BAIDU -> locationServices[1]
            LocationProvider.BAIDU_IP -> locationServices[2]
            LocationProvider.AMAP -> locationServices[3]
            else -> locationServices[0]
        }
    }

    suspend fun getLocation(context: Context,
                            location: Location,
                            background: Boolean): Response<Location?> = coroutineScope {
        val cache = DatabaseHelper.getInstance(context).readLocation(location)
        cache?.weather = location.weather

        try {
            val provider = getInstance(context).getLocationProvider()
            val service = getLocationService(provider)

            if (service.getPermissions().isNotEmpty()) {
                // if needs any location permission.
                if (!NetworkUtils.isAvailable(context)
                        || ActivityCompat.checkSelfPermission(context,
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(context,
                                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return@coroutineScope Response.failure(cache)
                }

                if (background
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        && ActivityCompat.checkSelfPermission(context,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return@coroutineScope Response.failure(cache)
                }
            }

            // 1. get location by location service.
            // 2. get available location by weather service.
            val result = service.getLocation(context)
            result?.let {
                val availableLoc = getAvailableWeatherLocation(
                        context,
                        Location(
                                location, it.latitude, it.longitude, TimeZone.getDefault(),
                                it.country, it.province, it.city, it.district, it.inChina
                        )
                )
                return@coroutineScope Response(availableLoc ?: cache, if (availableLoc != null) {
                    Response.Status.SUCCEED
                } else {
                    Response.Status.FAILED
                })
            }
            return@coroutineScope Response.failure(cache)
        } catch (e: Exception) {
            e.printStackTrace()
            return@coroutineScope Response.failure(cache)
        }
    }

    private suspend fun getAvailableWeatherLocation(context: Context,
                                                    location: Location): Location? = coroutineScope {
        try {
            val source = getInstance(context).getWeatherSource()
            val target = Location(location, source)
            val service = weatherServiceSet[source]

            val locationList = service.getLocation(context, target)

            if (locationList.isNotEmpty()) {
                val src = locationList[0]
                val result = Location(src, true, src.isResidentPosition)
                DatabaseHelper.getInstance(context).writeLocation(result)
                return@coroutineScope result
            }

            return@coroutineScope null
        } catch (e: Exception) {
            e.printStackTrace()
            return@coroutineScope null
        }
    }

    fun getPermissions(context: Context?): Array<String> {
        // if IP:    none.
        // else:
        //      R:   foreground location. (set background location enabled manually)
        //      Q:   foreground location + background location.
        //      K-P: foreground location.
        val provider = getInstance(context!!).getLocationProvider()
        val service = getLocationService(provider)
        val permissions: Array<String> = service.getPermissions()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || permissions.isEmpty()) {
            // device has no background location permission or locate by IP.
            return permissions
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return Array(permissions.size + 1) {
                if (it < permissions.size) {
                    permissions[it]
                } else {
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                }
            }
        }
        return permissions
    }
}