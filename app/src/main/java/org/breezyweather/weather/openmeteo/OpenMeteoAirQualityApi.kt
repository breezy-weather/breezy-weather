package org.breezyweather.weather.openmeteo

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Query
import org.breezyweather.weather.openmeteo.json.OpenMeteoAirQualityResult

/**
 * Open-Meteo API
 */
interface OpenMeteoAirQualityApi {
    @GET("v1/air-quality?timezone=auto&timeformat=unixtime")
    fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String
    ): Observable<OpenMeteoAirQualityResult>
}