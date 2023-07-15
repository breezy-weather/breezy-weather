package org.breezyweather.weather

import android.content.Context
import androidx.annotation.WorkerThread
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.Weather
import org.breezyweather.main.utils.RequestErrorType

/**
 * Weather service.
 */
abstract class WeatherService {
    class WeatherResultWrapper(val result: Weather?)

    interface RequestLocationSearchCallback {
        fun requestLocationSearchSuccess(query: String, locationList: List<Location>)
        fun requestLocationSearchFailed(query: String, requestErrorType: RequestErrorType)
    }

    abstract fun isConfigured(context: Context): Boolean
    abstract fun requestWeather(context: Context, location: Location): Observable<WeatherResultWrapper>

    @WorkerThread
    abstract fun requestLocationSearch(context: Context, query: String): List<Location>
    abstract fun requestReverseGeocodingLocation(context: Context, location: Location): Observable<List<Location>>

    abstract fun cancel()
}
