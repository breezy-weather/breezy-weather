package org.breezyweather.weather.apis

import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import org.breezyweather.weather.json.mf.*

/**
 * API Météo France
 */
interface MfWeatherApi {
    @GET("places")
    fun callWeatherLocation(
        @Header("User-Agent") userAgent: String,
        @Query("q") q: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("token") token: String
    ): Call<List<MfLocationResult>>

    @GET("places")
    fun getWeatherLocation(
        @Header("User-Agent") userAgent: String,
        @Query("q") q: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("token") token: String
    ): Observable<List<MfLocationResult>>

    @GET("v2/forecast")
    fun getForecast(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("formatDate") formatDate: String,
        @Query("token") token: String
    ): Observable<MfForecastResult>

    @GET("v2/observation")
    fun getCurrent(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("lang") lang: String,
        @Query("formatDate") formatDate: String,
        @Query("token") token: String
    ): Observable<MfCurrentResult>

    @GET("v3/nowcast/rain")
    fun getRain(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("lang") lang: String,
        @Query("formatDate") formatDate: String,
        @Query("token") token: String
    ): Observable<MfRainResult>

    @GET("ephemeris")
    fun getEphemeris(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("lang") lang: String,
        @Query("formatDate") formatDate: String,
        @Query("token") token: String
    ): Observable<MfEphemerisResult>

    @GET("v3/warning/full")
    fun getWarnings(
        @Header("User-Agent") userAgent: String,
        @Query(encoded = true, value = "domain") domain: String,
        @Query("formatDate") formatDate: String,
        @Query("token") token: String
    ): Observable<MfWarningsResult>
}