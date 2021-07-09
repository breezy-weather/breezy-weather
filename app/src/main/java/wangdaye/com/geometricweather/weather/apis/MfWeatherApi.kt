package wangdaye.com.geometricweather.weather.apis

import retrofit2.http.GET
import retrofit2.http.Query
import wangdaye.com.geometricweather.weather.json.mf.*

/**
 * API Météo France
 */
interface MfWeatherApi {

    @GET("places")
    suspend fun callWeatherLocation(
            @Query("q") q: String,
            @Query("lat") lat: Double,
            @Query("lon") lon: Double,
            @Query("token") token: String
    ): List<MfLocationResult>

    @GET("forecast")
    suspend fun getForecast(
            @Query("lat") lat: Double,
            @Query("lon") lon: Double,
            @Query("lang") lang: String,
            @Query("token") token: String
    ): MfForecastResult

    @GET("v2/forecast")
    suspend fun getForecastV2(
            @Query("lat") lat: Double,
            @Query("lon") lon: Double,
            @Query("lang") lang: String,
            @Query("token") token: String
    ): MfForecastV2Result

    @GET("forecast")
    suspend fun getForecastInstants(
            @Query("lat") lat: Double,
            @Query("lon") lon: Double,
            @Query("lang") lang: String,
            @Query("instants") instants: String,
            @Query("token") token: String
    ): MfForecastResult

    @GET("forecast")
    suspend fun getForecastInseepp(
            @Query("id") id: Int,
            @Query("lang") lang: String,
            @Query("token") token: String
    ): MfForecastResult

    @GET("observation/gridded")
    suspend fun getCurrent(
            @Query("lat") lat: Double,
            @Query("lon") lon: Double,
            @Query("lang") lang: String,
            @Query("token") token: String
    ): MfCurrentResult

    @GET("rain")
    suspend fun getRain(
            @Query("lat") lat: Double,
            @Query("lon") lon: Double,
            @Query("lang") lang: String,
            @Query("token") token: String
    ): MfRainResult

    @GET("ephemeris")
    suspend fun getEphemeris(
            @Query("lat") lat: Double,
            @Query("lon") lon: Double,
            @Query("lang") lang: String,
            @Query("token") token: String
    ): MfEphemerisResult

    @GET("warning/full")
    suspend fun getWarnings(
            @Query("domain") domain: String,
            @Query("formatDate") formatDate: String?,
            @Query("token") token: String
    ): MfWarningsResult
}