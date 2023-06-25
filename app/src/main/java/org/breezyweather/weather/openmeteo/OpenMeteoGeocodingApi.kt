package org.breezyweather.weather.openmeteo

import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import org.breezyweather.weather.openmeteo.json.OpenMeteoLocationResults

/**
 * Open-Meteo API
 */
interface OpenMeteoGeocodingApi {
    @GET("v1/search?format=json")
    fun callWeatherLocation(
        @Query("name") name: String,
        @Query("count") count: Int,
        @Query("language") language: String
    ): Call<OpenMeteoLocationResults>

    @GET("v1/search?format=json")
    fun getWeatherLocation(
        @Query("name") name: String,
        @Query("count") count: Int,
        @Query("language") language: String
    ): Observable<OpenMeteoLocationResults>
}