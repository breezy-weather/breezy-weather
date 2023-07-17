package org.breezyweather.sources.accu

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import org.breezyweather.sources.accu.json.*

/**
 * Accu api.
 */
interface AccuDeveloperApi {
    @GET("locations/v1/translate")
    fun getWeatherLocation(
        @Query("apikey") apikey: String,
        @Query("q") q: String,
        @Query("language") language: String,
        @Query("details") details: Boolean,
        @Query("alias") alias: String
    ): Observable<List<AccuLocationResult>>

    @GET("locations/v1/cities/geoposition/search")
    fun getWeatherLocationByGeoPosition(
        @Query("apikey") apikey: String,
        @Query("language") language: String,
        @Query("details") details: Boolean,
        @Query("q") q: String
    ): Observable<AccuLocationResult>

    @GET("currentconditions/v1/{city_key}")
    fun getCurrent(
        @Path("city_key") city_key: String,
        @Query("apikey") apikey: String,
        @Query("language") language: String,
        @Query("details") details: Boolean
    ): Observable<List<AccuCurrentResult>>

    @GET("forecasts/v1/daily/{days}day/{city_key}")
    fun getDaily(
        @Path("days") days: String,
        @Path("city_key") city_key: String,
        @Query("apikey") apikey: String,
        @Query("language") language: String,
        @Query("details") details: Boolean,
        @Query("metric") metric: Boolean
    ): Observable<AccuForecastDailyResult>

    @GET("forecasts/v1/hourly/{hours}hour/{city_key}")
    fun getHourly(
        @Path("hours") hours: String,
        @Path("city_key") city_key: String,
        @Query("apikey") apikey: String,
        @Query("language") language: String,
        @Query("details") details: Boolean,
        @Query("metric") metric: Boolean
    ): Observable<List<AccuForecastHourlyResult>>

    @GET("alerts/v1/{city_key}")
    fun getAlertsByCityKey(
        @Query("apikey") apikey: String,
        @Path("city_key") city_key: String,
        @Query("language") language: String,
        @Query("details") details: Boolean
    ): Observable<List<AccuAlertResult>>
}