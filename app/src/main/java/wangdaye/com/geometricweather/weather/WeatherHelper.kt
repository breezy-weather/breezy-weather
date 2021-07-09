package wangdaye.com.geometricweather.weather

import android.content.Context
import kotlinx.coroutines.*
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.Response
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource
import wangdaye.com.geometricweather.common.basic.models.weather.Weather
import wangdaye.com.geometricweather.db.DatabaseHelper
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * Weather helper.
 */
class WeatherHelper @Inject constructor(private val serviceSet: WeatherServiceSet) {

    suspend fun getWeather(context: Context, location: Location): Response<Weather?> = coroutineScope {
        try {
            if (!location.isUsable) {
                return@coroutineScope Response.failure(null)
            }

            val service = serviceSet[location.weatherSource]

            service.getWeather(context, location)?.let {
                DatabaseHelper.getInstance(context).writeWeather(location, it)
                if (it.yesterday == null) {
                    it.yesterday = DatabaseHelper.getInstance(context).readHistory(location, it)
                }
                return@coroutineScope Response.success(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@coroutineScope Response.failure(
                DatabaseHelper.getInstance(context).readWeather(location))
    }

    suspend fun getLocation(
            context: Context,
            query: String,
            enabledSources: List<WeatherSource>
    ): Response<List<Location>> = coroutineScope {
        try {
            if (enabledSources.isEmpty()) {
                return@coroutineScope Response.failure(ArrayList<Location>())
            }

            // generate weather services.
            val services = Array(enabledSources.size) { serviceSet[enabledSources[it]] }

            // generate deferred list.
            val deferredList = services.map {
                async {
                    it.getLocation(context, query)
                }
            }

            val locationList = ArrayList<Location>()
            for (list in deferredList.awaitAll()) {
                locationList.addAll(list)
            }

            return@coroutineScope Response(locationList, if (locationList.isNotEmpty()) {
                Response.Status.SUCCEED
            } else {
                Response.Status.FAILED
            })
        } catch (e: Exception) {
            e.printStackTrace()
            return@coroutineScope Response.failure(ArrayList())
        }
    }
}