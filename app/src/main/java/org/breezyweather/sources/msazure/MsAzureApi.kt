package org.breezyweather.sources.msazure

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.msazure.json.airquality.MsAzureAirQualityForecastResponse
import org.breezyweather.sources.msazure.json.alerts.MsAzureWeatherAlertsResponse
import org.breezyweather.sources.msazure.json.current.MsAzureCurrentConditionsResponse
import org.breezyweather.sources.msazure.json.daily.MsAzureDailyForecastResponse
import org.breezyweather.sources.msazure.json.geocoding.MsAzureGeocodingResponse
import org.breezyweather.sources.msazure.json.hourly.MsAzureHourlyForecastResponse
import org.breezyweather.sources.msazure.json.minutely.MsAzureMinutelyForecastResponse
import org.breezyweather.sources.msazure.json.timezone.MsAzureTzResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface MsAzureApi {
    @GET("weather/currentConditions/json?api-version=1.1")
    fun getCurrentConditions(
        @Header("subscription-key") apikey: String,
        @Query("query") coordinates: String,
        @Query("language") lang: String
    ): Observable<MsAzureCurrentConditionsResponse>

    @GET("weather/forecast/daily/json?api-version=1.1")
    fun getDailyForecast(
        @Header("subscription-key") apikey: String,
        @Query("query") coordinates: String,
        @Query("duration") days: Int,
        @Query("language") lang: String
    ): Observable<MsAzureDailyForecastResponse>

    @GET("weather/forecast/hourly/json?api-version=1.1")
    fun getHourlyForecast(
        @Header("subscription-key") apikey: String,
        @Query("query") coordinates: String,
        @Query("duration") hours: Int,
        @Query("language") lang: String
    ): Observable<MsAzureHourlyForecastResponse>

    @GET("weather/forecast/minute/json?api-version=1.1")
    fun getMinutelyForecast(
        @Header("subscription-key") apikey: String,
        @Query("query") coordinates: String,
        @Query("interval") interval: Int,
        @Query("language") lang: String
    ): Observable<MsAzureMinutelyForecastResponse>

    @GET("weather/airQuality/current/json?api-version=1.1")
    fun getCurrentAirQuality(
        @Header("subscription-key") apikey: String,
        @Query("query") coordinates: String,
        @Query("language") lang: String
    ): Observable<MsAzureAirQualityForecastResponse>

    @GET("weather/airQuality/forecasts/hourly/json?api-version=1.1")
    fun getHourlyAirQuality(
        @Header("subscription-key") apikey: String,
        @Query("query") coordinates: String,
        @Query("duration") hours: Int,
        @Query("language") lang: String
    ): Observable<MsAzureAirQualityForecastResponse>

    @GET("weather/severe/alerts/json?api-version=1.1")
    fun getWeatherAlerts(
        @Header("subscription-key") apikey: String,
        @Query("query") coordinates: String,
        @Query("language") lang: String
    ): Observable<MsAzureWeatherAlertsResponse>

    @GET("reverseGeocode?api-version=2022-12-01-preview")
    fun reverseGeocode(
        @Header("subscription-key") apikey: String,
        @Header("Accept-Language") lang: String,
        @Query("coordinates") coordinates: String
    ): Observable<MsAzureGeocodingResponse>

    @GET("geocode?api-version=2022-12-01-preview")
    fun geocode(
        @Header("subscription-key") apikey: String,
        @Header("Accept-Language") lang: String,
        @Query("query") query: String,
        @Query("top") max: Int
    ): Observable<MsAzureGeocodingResponse>

    @GET("timezone/byCoordinates/json?api-version=1.0")
    fun getTimezone(
        @Header("subscription-key") apikey: String,
        @Query("query") coordinates: String
    ): Observable<MsAzureTzResponse>
}