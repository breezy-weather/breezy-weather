package org.breezyweather.weather.metno

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.weather.metno.json.MetNoAirQualityResult
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import org.breezyweather.weather.metno.json.MetNoForecastResult
import org.breezyweather.weather.metno.json.MetNoMoonResult
import org.breezyweather.weather.metno.json.MetNoNowcastResult
import org.breezyweather.weather.metno.json.MetNoSunResult

/**
 * MET Norway Weather API.
 */
interface MetNoApi {
    @GET("locationforecast/2.0/complete.json")
    fun getForecast(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Observable<MetNoForecastResult>

    @GET("sunrise/3.0/sun")
    fun getSun(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("date") date: String
    ): Observable<MetNoSunResult>

    @GET("sunrise/3.0/moon")
    fun getMoon(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("date") date: String
    ): Observable<MetNoMoonResult>

    // Only available in Nordic area
    @GET("nowcast/2.0/complete.json")
    fun getNowcast(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Observable<MetNoNowcastResult>

    @GET("airqualityforecast/0.1/")
    fun getAirQuality(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Observable<MetNoAirQualityResult>

    /*@GET("metalerts/1.1/")
    fun getAlerts(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Observable<List<MetNoAlertResult>>*/
}