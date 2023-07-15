package org.breezyweather.weather.openweather

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.weather.openweather.json.OpenWeatherAirPollutionResult
import org.breezyweather.weather.openweather.json.OpenWeatherOneCallResult
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * OpenWeather API.
 */
interface OpenWeatherApi {

    // Contains current weather, minute forecast for 1 hour, hourly forecast for 48 hours, daily forecast for 7 days (8 for 3.0) and government weather alerts
    @GET("data/{version}/onecall")
    fun getOneCall(
        @Path("version") version: String,
        @Query("appid") apikey: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String,
        @Query("lang") lang: String
    ): Observable<OpenWeatherOneCallResult>

    @GET("data/2.5/air_pollution/forecast")
    fun getAirPollution(
        @Query("appid") apikey: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Observable<OpenWeatherAirPollutionResult>
}
