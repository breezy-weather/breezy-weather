package wangdaye.com.geometricweather.weather.apis

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import wangdaye.com.geometricweather.weather.json.accu.*

/**
 * Accu api.
 */
interface AccuWeatherApi {
    @GET("locations/v1/cities/translate.json")
    suspend fun callWeatherLocation(
            @Query("alias") alias: String,
            @Query("apikey") apikey: String,
            @Query("q") q: String,
            @Query("language") language: String
    ): List<AccuLocationResult>

    @GET("locations/v1/cities/geoposition/search.json")
    suspend fun callWeatherLocationByGeoPosition(
            @Query("alias") alias: String,
            @Query("apikey") apikey: String,
            @Query("q") q: String,
            @Query("language") language: String
    ): AccuLocationResult

    @GET("currentconditions/v1/{city_key}.json")
    suspend fun callCurrent(
            @Path("city_key") city_key: String,
            @Query("apikey") apikey: String,
            @Query("language") language: String,
            @Query("details") details: Boolean
    ): List<AccuCurrentResult>

    @GET("forecasts/v1/daily/15day/{city_key}.json")
    suspend fun callDaily(
            @Path("city_key") city_key: String,
            @Query("apikey") apikey: String,
            @Query("language") language: String,
            @Query("metric") metric: Boolean,
            @Query("details") details: Boolean
    ): AccuDailyResult

    @GET("forecasts/v1/hourly/24hour/{city_key}.json")
    suspend fun callHourly(
            @Path("city_key") city_key: String,
            @Query("apikey") apikey: String,
            @Query("language") language: String,
            @Query("metric") metric: Boolean
    ): List<AccuHourlyResult>

    @GET("forecasts/v1/minute/1minute.json")
    suspend fun callMinutely(
            @Query("apikey") apikey: String,
            @Query("language") language: String,
            @Query("details") details: Boolean,
            @Query("q") q: String
    ): AccuMinuteResult

    @GET("airquality/v1/observations/{city_key}.json")
    suspend fun callAirQuality(
            @Path("city_key") city_key: String,
            @Query("apikey") apikey: String
    ): AccuAqiResult

    @GET("alerts/v1/{city_key}.json")
    suspend fun callAlert(
            @Path("city_key") city_key: String,
            @Query("apikey") apikey: String,
            @Query("language") language: String,
            @Query("details") details: Boolean
    ): List<AccuAlertResult>
}