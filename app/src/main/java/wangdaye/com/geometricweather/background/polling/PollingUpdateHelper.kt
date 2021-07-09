package wangdaye.com.geometricweather.background.polling

import android.content.Context
import kotlinx.coroutines.*
import wangdaye.com.geometricweather.GeometricWeather
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.Response
import wangdaye.com.geometricweather.common.basic.models.weather.Weather
import wangdaye.com.geometricweather.common.utils.helpers.BusHelper
import wangdaye.com.geometricweather.db.DatabaseHelper
import wangdaye.com.geometricweather.location.LocationHelper
import wangdaye.com.geometricweather.weather.WeatherHelper

/**
 * Polling update helper.
 */
class PollingUpdateHelper @JvmOverloads constructor(
        private val context: Context,
        private val locationHelper: LocationHelper,
        private val weatherHelper: WeatherHelper,
        private val responder: PollingResponder? = null,
        private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
        private val applicationScope: CoroutineScope = GeometricWeather.instance.applicationScope
) {

    private var job: Job? = null

    interface PollingResponder {
        fun responseSingleRequest(location: Location, old: Weather?,
                                  succeed: Boolean, index: Int, total: Int)
        fun responsePolling(locationList: List<Location>)
    }

    // control.

    fun pollingUpdate() {
        cancel()

        job = applicationScope.launch {

            // get location list with weather cache.
            val locationList = withContext(ioDispatcher) {
                val list = DatabaseHelper.getInstance(context).readLocationList()
                for (l in list) {
                    l.weather = DatabaseHelper.getInstance(context).readWeather(l)
                }
                return@withContext list
            }

            // generate deferred list.
            val deferredList = locationList.map {
                async {
                    val index = locationList.indexOfFirst { item -> item.equals(it) }
                    val oldWeather = it.weather

                    val response = withContext(ioDispatcher) {
                        update(it)
                    }

                    if (response.isSucceed() && response.result != null) {
                        locationList[index] = response.result

                        val newWeather = response.result.weather
                        if (newWeather != null && (oldWeather == null
                                        || newWeather.base.timeStamp != oldWeather.base.timeStamp)) {
                            BusHelper.postLocationChanged(response.result)
                        }
                    }
                    response.result?.let {
                        responder?.responseSingleRequest(
                                it, oldWeather, response.isSucceed(), index, locationList.size)
                    }
                }
            }

            deferredList.awaitAll()
            responder?.responsePolling(locationList)
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
    }

    private suspend fun update(location: Location): Response<Location> {
        // check cache.
        val weatherCache = location.weather
        if (weatherCache != null && weatherCache.isValid(0.25f)) {
            return Response.success(location)
        }

        // locate.
        var loc = location
        if (loc.isCurrentPosition) {
            val response = locationHelper.getLocation(context, loc, true)
            response.result?.let {
                loc = it
            }
        }

        // request weather.
        val response = weatherHelper.getWeather(context, loc)
        loc.weather = response.result
        return Response(loc, response.status)
    }
}