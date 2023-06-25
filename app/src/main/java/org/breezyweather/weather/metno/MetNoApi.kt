package org.breezyweather.weather.metno

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import org.breezyweather.weather.metno.json.MetNoForecastResult
import org.breezyweather.weather.metno.json.MetNoEphemerisResult

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

    @GET("sunrise/2.0/.json")
    fun getEphemeris(
        @Header("User-Agent") userAgent: String,
        @Query("date") date: String,
        @Query("days") days: Int,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("offset") offset: String
    ): Observable<MetNoEphemerisResult>

    // Only available in Nordic area
    /*@GET("nowcast/2.0/complete.json")
    fun getMinutely(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Observable<MetNoLocationForecastResult>*/

    /*@GET("airqualityforecast/0.1/")
    fun getAirQuality(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Observable<MetNoAqiResult>*/

    /*@GET("metalerts/1.1/")
    fun getAlerts(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Observable<List<MetNoAlertResult>>*/
}