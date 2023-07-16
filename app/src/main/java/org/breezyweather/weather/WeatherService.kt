package org.breezyweather.weather

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.Weather

/**
 * Weather service.
 */
abstract class WeatherService {
    class WeatherResultWrapper(val result: Weather?)

    abstract fun isConfigured(context: Context): Boolean
    abstract fun requestWeather(context: Context, location: Location): Observable<WeatherResultWrapper>
    abstract fun requestLocationSearch(context: Context, query: String): Observable<List<Location>>
    abstract fun requestReverseGeocodingLocation(context: Context, location: Location): Observable<List<Location>>

    abstract fun cancel()
}
