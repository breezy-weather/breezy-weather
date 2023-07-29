package org.breezyweather.sources.here

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.here.json.HereWeatherForecastResult
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * OpenWeather API.
 */
interface HereWeatherApi {
    @GET("v3/report")
    fun getForecast(
        @Query("apiKey") apikey: String,
        @Query("products", encoded = true) products: String,
        @Query("location", encoded = true) location: String,
        @Query("units") units: String,
        @Query("lang") lang: String,
        @Query("oneObservation") oneObservation: Boolean,
    ): Observable<HereWeatherForecastResult>
}