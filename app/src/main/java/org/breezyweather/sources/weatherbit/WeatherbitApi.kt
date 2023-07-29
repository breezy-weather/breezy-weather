package org.breezyweather.sources.weatherbit

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.weatherbit.json.WeatherbitAirQuality
import org.breezyweather.sources.weatherbit.json.WeatherbitCurrentResponse
import org.breezyweather.sources.weatherbit.json.WeatherbitDaily
import org.breezyweather.sources.weatherbit.json.WeatherbitHourly
import org.breezyweather.sources.weatherbit.json.WeatherbitMinutely
import org.breezyweather.sources.weatherbit.json.WeatherbitResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherbitApi {
    @GET("current")
    fun getCurrentWeather(
        @Query("key") apikey: String,
        @Query("lat") lat: Float,
        @Query("lon") lon: Float,
        @Query("units") units: String,
        @Query("lang") lang: String,
        @Query("include") include: String,
    ): Observable<WeatherbitCurrentResponse>

    @GET("forecast/minutely")
    fun getMinutelyForecast(
        @Query("key") apikey: String,
        @Query("lat") lat: Float,
        @Query("lon") lon: Float,
        @Query("units") units: String,
    ): Observable<WeatherbitResponse<WeatherbitMinutely>>

    @GET("forecast/hourly")
    fun getHourlyForecast(
        @Query("key") apikey: String,
        @Query("lat") lat: Float,
        @Query("lon") lon: Float,
        @Query("units") units: String,
        @Query("lang") lang: String,
        @Query("hours") hours: Int,
    ): Observable<WeatherbitResponse<WeatherbitHourly>>

    @GET("forecast/daily")
    fun getDailyForecast(
        @Query("key") apikey: String,
        @Query("lat") lat: Float,
        @Query("lon") lon: Float,
        @Query("units") units: String,
        @Query("lang") lang: String,
        @Query("days") days: Int,
    ): Observable<WeatherbitResponse<WeatherbitDaily>>

    @GET("current/airquality")
    fun getCurrentAirQuality(
        @Query("key") apikey: String,
        @Query("lat") lat: Float,
        @Query("lon") lon: Float
    ): Observable<WeatherbitResponse<WeatherbitAirQuality>>

    @GET("forecast/airquality")
    fun getHourlyAQForecast(
        @Query("key") apikey: String,
        @Query("lat") lat: Float,
        @Query("lon") lon: Float,
        @Query("hours") hours: Int,
    ): Observable<WeatherbitResponse<WeatherbitAirQuality>>
}