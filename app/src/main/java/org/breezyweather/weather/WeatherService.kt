package org.breezyweather.weather

import android.content.Context
import androidx.annotation.WorkerThread
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.Weather
import org.breezyweather.main.utils.RequestErrorType

/**
 * Weather service.
 */
abstract class WeatherService {
    class WeatherResultWrapper(val result: Weather?)
    interface RequestWeatherCallback {
        fun requestWeatherSuccess(requestLocation: Location)
        fun requestWeatherFailed(requestLocation: Location, requestErrorType: RequestErrorType)
    }

    interface RequestLocationCallback {
        fun requestLocationSuccess(query: String, locationList: List<Location>)
        fun requestLocationFailed(query: String, requestErrorType: RequestErrorType)
    }

    abstract fun isConfigured(context: Context): Boolean
    abstract fun requestWeather(context: Context, location: Location, callback: RequestWeatherCallback)

    @WorkerThread
    abstract fun requestLocationSearch(context: Context, query: String): List<Location>
    abstract fun requestReverseLocationSearch(context: Context, location: Location, callback: RequestLocationCallback)

    abstract fun cancel()
}
