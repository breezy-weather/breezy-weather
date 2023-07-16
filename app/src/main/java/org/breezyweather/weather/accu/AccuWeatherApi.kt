package org.breezyweather.weather.accu

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import org.breezyweather.weather.accu.json.*

/**
 * Accu api.
 */
interface AccuWeatherApi {
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

    @GET("forecasts/v1/minute/1minute")
    fun getMinutely(
        @Query("apikey") apikey: String,
        @Query("q") q: String,
        @Query("language") language: String,
        @Query("details") details: Boolean
    ): Observable<AccuMinutelyResult>

    @GET("alerts/v1/geoposition")
    fun getAlert(
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