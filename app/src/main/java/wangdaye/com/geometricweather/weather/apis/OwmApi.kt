package wangdaye.com.geometricweather.weather.apis

import retrofit2.http.GET
import retrofit2.http.Query
import wangdaye.com.geometricweather.weather.json.owm.OwmAirPollutionResult
import wangdaye.com.geometricweather.weather.json.owm.OwmLocationResult
import wangdaye.com.geometricweather.weather.json.owm.OwmOneCallHistoryResult
import wangdaye.com.geometricweather.weather.json.owm.OwmOneCallResult

/**
 * OpenWeather API.
 */
interface OwmApi {

    @GET("geo/1.0/direct")
    suspend fun callWeatherLocation(
            @Query("appid") apikey: String,
            @Query("q") q: String
    ): List<OwmLocationResult>

    @GET("geo/1.0/direct")
    suspend fun getWeatherLocation(
            @Query("appid") apikey: String,
            @Query("q") q: String
    ): List<OwmLocationResult>

    @GET("geo/1.0/reverse")
    suspend fun getWeatherLocationByGeoPosition(
            @Query("appid") apikey: String,
            @Query("lat") lat: Double,
            @Query("lon") lon: Double
    ): List<OwmLocationResult>

    // Contains current weather, minute forecast for 1 hour, hourly forecast for 48 hours,
    // daily forecast for 7 days and government weather alerts
    @GET("data/2.5/onecall")
    suspend fun getOneCall(
            @Query("appid") apikey: String,
            @Query("lat") lat: Double,
            @Query("lon") lon: Double,
            @Query("units") units: String,
            @Query("lang") lang: String
    ): OwmOneCallResult

    @GET("data/2.5/onecall/timemachine")
    suspend fun getOneCallHistory(
            @Query("appid") apikey: String,
            @Query("lat") lat: Double,
            @Query("lon") lon: Double,
            @Query("dt") dt: Long,
            @Query("units") units: String,
            @Query("lang") lang: String
    ): OwmOneCallHistoryResult

    @GET("data/2.5/air_pollution")
    suspend fun getAirPollutionCurrent(
            @Query("appid") apikey: String?,
            @Query("lat") lat: Double,
            @Query("lon") lon: Double
    ): OwmAirPollutionResult

    @GET("data/2.5/air_pollution/forecast")
    suspend fun getAirPollutionForecast(
            @Query("appid") apikey: String,
            @Query("lat") lat: Double,
            @Query("lon") lon: Double
    ): OwmAirPollutionResult
}