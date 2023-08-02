package org.breezyweather.sources.msazure

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.msazure.json.airquality.MsAzureAirQualityForecastResponse
import org.breezyweather.sources.msazure.json.alerts.MsAzureWeatherAlertsResponse
import org.breezyweather.sources.msazure.json.current.MsAzureCurrentConditionsResponse
import org.breezyweather.sources.msazure.json.daily.MsAzureDailyForecastResponse
import org.breezyweather.sources.msazure.json.hourly.MsAzureHourlyForecastResponse
import org.breezyweather.sources.msazure.json.minutely.MsAzureMinutelyForecastResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface MsAzureWeatherApi {
    @GET("currentConditions/json?api-version=1.1")
    fun getCurrentConditions(
        @Header("subscription-key") apikey: String,
        @Query("query") coordinates: String,
        @Query("language") lang: String
    ): Observable<MsAzureCurrentConditionsResponse>

    @GET("forecast/daily/json?api-version=1.1")
    fun getDailyForecast(
        @Header("subscription-key") apikey: String,
        @Query("query") coordinates: String,
        @Query("duration") days: Int,
        @Query("language") lang: String
    ): Observable<MsAzureDailyForecastResponse>

    @GET("forecast/hourly/json?api-version=1.1")
    fun getHourlyForecast(
        @Header("subscription-key") apikey: String,
        @Query("query") coordinates: String,
        @Query("duration") hours: Int,
        @Query("language") lang: String
    ): Observable<MsAzureHourlyForecastResponse>

    @GET("forecast/minute/json?api-version=1.1")
    fun getMinutelyForecast(
        @Header("subscription-key") apikey: String,
        @Query("query") coordinates: String,
        @Query("interval") interval: Int,
        @Query("language") lang: String
    ): Observable<MsAzureMinutelyForecastResponse>

    @GET("airQuality/current/json?api-version=1.1")
    fun getCurrentAirQuality(
        @Header("subscription-key") apikey: String,
        @Query("query") coordinates: String,
        @Query("language") lang: String
    ): Observable<MsAzureAirQualityForecastResponse>

    @GET("airQuality/forecasts/hourly/json?api-version=1.1")
    fun getHourlyAirQuality(
        @Header("subscription-key") apikey: String,
        @Query("query") coordinates: String,
        @Query("duration") hours: Int,
        @Query("language") lang: String
    ): Observable<MsAzureAirQualityForecastResponse>

    @GET("severe/alerts/json?api-version=1.1")
    fun getWeatherAlerts(
        @Header("subscription-key") apikey: String,
        @Query("query") coordinates: String,
        @Query("language") lang: String
    ): Observable<MsAzureWeatherAlertsResponse>
}