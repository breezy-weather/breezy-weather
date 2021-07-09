package wangdaye.com.geometricweather.remoteviews.config

import android.content.Context
import kotlinx.coroutines.*
import wangdaye.com.geometricweather.GeometricWeather
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.weather.WeatherHelper

/**
 * Abstract update helper.
 */
class AbstractUpdateHelper @JvmOverloads constructor(
        private val weatherHelper: WeatherHelper,
        private val responder: Responder? = null,
        private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
        private val applicationScope: CoroutineScope = GeometricWeather.instance.applicationScope
) {

    private var job: Job? = null

    interface Responder {
        fun responseRequest(location: Location)
    }

    // control.

    fun update(context: Context, location: Location) {
        cancel()

        val loc = if (location.isCurrentPosition && !location.isUsable) {
            Location.buildDefaultLocation()
        } else {
            location
        }

        job = applicationScope.launch(Dispatchers.Main) {
            loc.weather = withContext(ioDispatcher) {
                weatherHelper.getWeather(context, loc).result ?: location.weather
            }
            responder?.responseRequest(loc)
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
    }
}