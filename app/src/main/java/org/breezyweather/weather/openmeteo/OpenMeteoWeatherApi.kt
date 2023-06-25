package org.breezyweather.weather.openmeteo

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Query
import org.breezyweather.weather.openmeteo.json.OpenMeteoWeatherResult

/**
 * Open-Meteo API
 */
interface OpenMeteoWeatherApi {
    @GET("v1/forecast?timezone=auto&timeformat=unixtime")
    fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String,
        @Query("hourly") hourly: String,
        @Query("forecast_days") forecastDays: Int,
        @Query("past_days") pastDays: Int,
        @Query("current_weather") currentWeather: Boolean
    ): Observable<OpenMeteoWeatherResult>
}