package org.breezyweather.weather.mf

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.weather.mf.json.*
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * API Météo France
 */
interface MfWeatherApi {

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