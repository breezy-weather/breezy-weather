package org.breezyweather.sources.pirateweather

import retrofit2.http.GET
import retrofit2.http.Query
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.pirateweather.json.PirateWeatherForecastResult
import retrofit2.http.Path

/**
 * See https://docs.pirateweather.net/en/latest/Specification/
 */
interface PirateWeatherApi {
    @GET("forecast/{apikey}/{lat},{lon}")
    fun getForecast(
        @Path("apikey") apikey: String,
        @Path("lat") lat: Float,
        @Path("lon") lon: Float,
        @Query("units") units: String,
        @Query("lang") lang: String
    ): Observable<PirateWeatherForecastResult>
}