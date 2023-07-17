package org.breezyweather.sources.accu

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import org.breezyweather.sources.accu.json.*

/**
 * Accu api.
 */
interface AccuEnterpriseApi : AccuDeveloperApi {

    @GET("forecasts/v1/minute/1minute")
    fun getMinutely(
        @Query("apikey") apikey: String,
        @Query("q") q: String,
        @Query("language") language: String,
        @Query("details") details: Boolean
    ): Observable<AccuMinutelyResult>

    @GET("alerts/v1/geoposition")
    fun getAlertsByPosition(
        @Query("apikey") apikey: String,
        @Query("q") q: String,
        @Query("language") language: String,
        @Query("details") details: Boolean
    ): Observable<List<AccuAlertResult>>

    @GET("airquality/v2/forecasts/hourly/96hour/{city_key}")
    fun getAirQuality(
        @Path("city_key") city_key: String,
        @Query("apikey") apikey: String,
        @Query("pollutants") pollutants: Boolean,
        @Query("language") language: String
    ): Observable<AccuAirQualityResult>

    // https://apidev.accuweather.com/developers/climoAPIguide
    /*@GET("climo/v1/summary/{year}/{month}/{city_key}")
    fun getClimo(
        @Path("year") year: Int,
        @Path("month") month: Int,
        @Path("city_key") city_key: String,
        @Query("apikey") apikey: String,
        @Query("language") language: String,
        @Query("details") details: Boolean
    ): Observable<List<AccuClimoResult>>*/
}