package wangdaye.com.geometricweather.weather.apis

import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import wangdaye.com.geometricweather.weather.json.openweather.OpenWeatherAirPollutionResult
import wangdaye.com.geometricweather.weather.json.openweather.OpenWeatherLocationResult
import wangdaye.com.geometricweather.weather.json.openweather.OpenWeatherOneCallResult

/**
 * OpenWeather API.
 */
interface OpenWeatherApi {
    @GET("geo/1.0/direct")
    fun callWeatherLocation(
        @Query("appid") apikey: String,
        @Query("q") q: String
    ): Call<List<OpenWeatherLocationResult>>

    @GET("geo/1.0/direct")
    fun getWeatherLocation(
        @Query("appid") apikey: String,
        @Query("q") q: String
    ): Observable<List<OpenWeatherLocationResult>>

    @GET("geo/1.0/reverse")
    fun getWeatherLocationByGeoPosition(
        @Query("appid") apikey: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Observable<List<OpenWeatherLocationResult>>

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
